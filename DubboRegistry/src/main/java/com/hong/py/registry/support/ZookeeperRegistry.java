package com.hong.py.registry.support;

import com.hong.py.commonUtils.ConcurrentHashSet;
import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.commonUtils.UrlUtils;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.registry.ChildrenListener;
import com.hong.py.registry.NotifyListener;
import com.hong.py.registry.Registry;
import com.hong.py.registry.StateListener;
import com.hong.py.registry.zookeeper.ZookeeperClient;
import com.hong.py.rpc.RpcException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ZookeeperRegistry  implements Registry {

    protected final Logger logger = LoggerFactory.getLogger(ZookeeperRegistry.class);
    private final static String DEFAULT_ROOT = "dubbo";

    private final Set<URL> registered = new ConcurrentHashSet<>();

    //会进行重试
    private final Set<URL> failedRegistered = new ConcurrentHashSet<URL>();
    private final Set<URL> failedUnregistered = new ConcurrentHashSet<URL>();

    private final Map<URL, Set<NotifyListener>> subscribed = new ConcurrentHashMap<>();
    //会进行重试
    private final ConcurrentMap<URL, Set<NotifyListener>> failedSubscribed = new ConcurrentHashMap<URL, Set<NotifyListener>>();
    private final ConcurrentMap<URL, Set<NotifyListener>> failedUnsubscribed = new ConcurrentHashMap<URL, Set<NotifyListener>>();

    private final ConcurrentMap<URL, ConcurrentMap<NotifyListener, ChildrenListener>> zkListeners = new ConcurrentHashMap<URL, ConcurrentMap<NotifyListener, ChildrenListener>>();
    private final Set<String> anyServices = new ConcurrentHashSet<String>();

    private final ConcurrentMap<URL, Map<NotifyListener, List<URL>>> failedNotified = new ConcurrentHashMap<URL, Map<NotifyListener, List<URL>>>();
    private final ConcurrentMap<URL, Map<String, List<URL>>> notified = new ConcurrentHashMap<URL, Map<String, List<URL>>>();

    private final String root;

    private final ZookeeperClient client;

    public ZookeeperRegistry(URL url,ZookeeperTransporter zookeeperTransporter) {
        String group = url.getParameter(Constants.GROUP_KEY, DEFAULT_ROOT);

        if (!group.startsWith(Constants.PATH_SEPARATOR)) {
            group = Constants.PATH_SEPARATOR + group;
        }
        this.root = group; // 必须都是/开头
        this.client = zookeeperTransporter.connect(url);

        // zk状态改变监听，RECONNECTED 会尝试恢复
        this.client.addStateListener(new StateListener() {
            @Override
            public void stateChanged(int state) {
                if (state == RECONNECTED) {
                    try {
                        recover();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
    }

    private void recover() {

        Set<URL> registered = this.registered;
        if (registered != null&&!registered.isEmpty()) {
            for (URL url : registered) {
                failedRegistered.add(url);
            }
        }

        Map<URL, Set<NotifyListener>> subscribed = this.subscribed;
        if (subscribed != null && !subscribed.isEmpty()) {
            for (Map.Entry<URL,Set<NotifyListener>> entry: subscribed.entrySet()) {
                for (NotifyListener listener : entry.getValue()) {
                    addFailedSubscribe(entry.getKey(), listener);
                }
            }
        }
    }

    @Override
    public void register(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("registered url==null");
        }

        registered.add(url);
        failedRegistered.remove(url);
        failedUnregistered.remove(url);

        try {
            doRegister(url);
        } catch (Exception e) {
            logger.error("Failed to register " + url + ", waiting for retry, cause: " + e.getMessage(), e);
            failedRegistered.add(url);
        }
    }

    //在zk上创建一个路径
    private void doRegister(URL url) {
        //dynamic是临时节点，否则是持久节点
        try {
            client.create(toUrlPath(url), url.getParameter(Constants.DYNAMIC_KEY, true));
        } catch (Throwable e) {
            throw new RpcException("Failed to register " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    private String toRootDir() {
        if (root.equals(Constants.PATH_SEPARATOR)) {
            return root;
        }
        return root + Constants.PATH_SEPARATOR;
    }

    private String toRootPath() {
        return root;
    }

    //类似： /dubbo/com.hong.py.demo.DemoService
    private String toServicePath(URL url) {
        String name = url.getServiceInterface();
        if (Constants.ANY_VALUE.equals(name)) {
            return toRootPath();
        }
        return toRootDir() + URL.encode(name);
    }

    private String toCategoryPath(URL url) {
        return toServicePath(url) + Constants.PATH_SEPARATOR + url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
    }

    private String toUrlPath(URL url) {
        return toCategoryPath(url) + Constants.PATH_SEPARATOR + URL.encode(url.toFullString());
    }

    @Override
    public void unregister(URL url) {

    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        if (url == null) {
            throw new IllegalArgumentException("subscribe url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("subscribe listener == null");
        }

        dealSubscribeMap(url, listener);

        try {
            doSubsribe(url, listener);
        } catch (Throwable e) {

            logger.error("Failed to subscribe " + url + ", waiting for retry, cause: " + e.getMessage(), e);

            addFailedSubscribe(url, listener);
        }
    }


    //provider://192.168.158.78:20880/com.alibaba.dubbo.demo.DemoService?anyhost=true&application=demo-provider&bean.name=com.alibaba.dubbo.demo.DemoService&category=configurators&check=false&dubbo=2.0.2&generic=false&interface=com.alibaba.dubbo.demo.DemoService&methods=sayHello&pid=15868&side=provider&timestamp=1577089692522
    //consumer://192.168.158.78/com.alibaba.dubbo.demo.DemoService2?application=demo-consumer&category=providers,configurators,routers&check=false&dubbo=2.0.2&interface=com.alibaba.dubbo.demo.DemoService2&methods=sayHello&pid=20052&qos.port=33333&side=consumer&timestamp=1577761153089
    private void doSubsribe(URL url, NotifyListener listener) {

        try {

            if (Constants.ANY_VALUE.equals(url.getServiceInterface())) {
                ConcurrentMap<NotifyListener, ChildrenListener> listeners = zkListeners.get(url);
                if (listeners == null) {
                    listeners = new ConcurrentHashMap<NotifyListener, ChildrenListener>();
                    zkListeners.putIfAbsent(url, listeners);
                }

                ChildrenListener zklistener = listeners.get(listener);
                if (zklistener == null) {
                    listeners.putIfAbsent(listener, new ChildrenListener() {
                        //如果子节点有变化则会直接通知，遍历所有子节点
                        @Override
                        public void childChanged(String path, List<String> children) {
                            for (String child : children) {
                                child = URL.decode(child);
                                if (!anyServices.contains(child)) { //如果字节还没有被订阅
                                    anyServices.add(child);
                                    //订阅子节点
                                    subscribe(url.setPath(child).addParameters(Constants.INTERFACE_KEY, child,
                                            Constants.CHECK_KEY, String.valueOf(false)), listener);
                                }
                            }
                        }
                    });
                    zklistener = listeners.get(listener);
                }

                client.create(root, false);

                List<String> services = client.addChildListener(root, zklistener);
                if (services != null && !services.isEmpty()) {
                    for (String service : services) {
                        service = URL.decode(service);
                        anyServices.add(service);
                        subscribe(url.setPath(service).addParameters(Constants.INTERFACE_KEY, service,
                                Constants.CHECK_KEY, String.valueOf(false)), listener);
                    }
                }
            } else {

                List<URL> urls = new ArrayList<URL>();
                //providers,configurators,routers 消费者会订阅这3种
                for (String path : toCategoriesPath(url)) {

                    ConcurrentMap<NotifyListener, ChildrenListener> listeners = zkListeners.get(url);
                    if (listeners == null) {
                        zkListeners.putIfAbsent(url, new ConcurrentHashMap<NotifyListener, ChildrenListener>());
                        listeners = zkListeners.get(url);
                    }
                    ChildrenListener zkListener = listeners.get(listener);
                    if (zkListener == null) {
                        listeners.putIfAbsent(listener, new ChildrenListener() {
                            //监听子节点的变化
                            @Override
                            public void childChanged(String parentPath, List<String> currentChilds) {
                                ZookeeperRegistry.this.notify(url, listener, toUrlsWithEmpty(url, parentPath, currentChilds));
                            }
                        });
                        zkListener = listeners.get(listener);
                    }
                    // /dubbo/com.hong.py.demo.DemoService/providers
                    client.create(path, false);

                    //会构造curatorClient和curatorWatcher监听path下子节点的变化 dubbo%3A%2F%2F192.168.158.78%3A20880%2Fcom.hong.py.demo.DemoHelloServiceforHpy%3Fapplication%3Dservice-provide%26bean.name%3Dcom.hong.py.demo.DemoHelloServiceforHpy%26bind.ip%3D192.168.158.78%26bind.port%3D20880%26interface%3Dcom.hong.py.demo.DemoHelloServiceforHpy%26methods%3DsayHello%26pid%3D1748%26side%3Dprovider%26timestamp%3D1578387568560
                    List<String> children = client.addChildListener(path, zkListener);

                    if (children != null) {
                        urls.addAll(toUrlsWithEmpty(url, path, children));
                    }
                }

                //回调NotifyListener,更新本地缓存信息
                //第一次监听的时候children为空的话 会设置url的protocol为Empty，达到区别和清空Invokers的作用。
                //如果children不为空，则会到directory会生成invoker
                notify(url, listener, urls);
            }
        } catch (Throwable e) {
            throw new RpcException("Failed to subscribe " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
        }


    }

    private void addFailedSubscribe(URL url, NotifyListener listener) {
        Set<NotifyListener> listeners = failedSubscribed.get(url);
        if (listeners == null) {
            failedSubscribed.putIfAbsent(url, new ConcurrentHashSet<NotifyListener>());
            listeners = failedSubscribed.get(url);
        }
        listeners.add(listener);
    }

    private void dealSubscribeMap(URL url, NotifyListener listener) {
        Set<NotifyListener> notifyListeners = subscribed.get(url);
        if (notifyListeners == null) {
            notifyListeners = new ConcurrentHashSet<>();
            subscribed.put(url, notifyListeners);
        }
        notifyListeners.add(listener);

        Set<NotifyListener> listeners = failedSubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
        listeners = failedUnsubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }


    private String[] toCategoriesPath(URL url) {
        String[] categories;
        if (Constants.ANY_VALUE.equals(url.getParameter(Constants.CATEGORY_KEY))) {
            categories = new String[]{Constants.PROVIDERS_CATEGORY, Constants.CONSUMERS_CATEGORY,
                    Constants.ROUTERS_CATEGORY, Constants.CONFIGURATORS_CATEGORY};
        } else {
            categories = url.getParameter(Constants.CATEGORY_KEY, new String[]{Constants.DEFAULT_CATEGORY});
        }
        String[] paths = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            paths[i] = toServicePath(url) + Constants.PATH_SEPARATOR + categories[i];
        }
        return paths;
    }

    private List<URL> toUrlsWithEmpty(URL consumer, String path, List<String> providers) {
        List<URL> urls = toUrlsWithoutEmpty(consumer, providers);
        if (urls == null || urls.isEmpty()) {
            int i = path.lastIndexOf('/');
            String category = i < 0 ? path : path.substring(i + 1);
            URL empty = consumer.setProtocol(Constants.EMPTY_PROTOCOL).addParameter(Constants.CATEGORY_KEY, category);
            urls.add(empty);
        }
        return urls;
    }

    private List<URL> toUrlsWithoutEmpty(URL consumer, List<String> providers) {
        List<URL> urls = new ArrayList<URL>();
        if (providers != null && !providers.isEmpty()) {
            for (String provider : providers) {
                provider = URL.decode(provider);
                if (provider.contains("://")) {
                    URL url = URL.valueOf(provider);
                    if (UrlUtils.isMatch(consumer, url)) {
                        urls.add(url);
                    }
                }
            }
        }
        return urls;
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {

    }

    protected void notify(URL url, NotifyListener listener, List<URL> urls) {
        if (url == null) {
            throw new IllegalArgumentException("url==null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener==null");
        }
        try {

            doNotify(url, listener, urls);
        } catch (Exception e) {
            Map<NotifyListener, List<URL>> notifyListenerListMap = failedNotified.get(url);
            if (notifyListenerListMap == null) {
                failedNotified.putIfAbsent(url, new ConcurrentHashMap<>());
                notifyListenerListMap = failedNotified.get(url);
            }
            notifyListenerListMap.put(listener, urls);
            logger.error("Failed to notify for subscribe " + url + ", waiting for retry, cause: " + e.getMessage(), e);
        }

    }

    protected void doNotify(URL url, NotifyListener listener, List<URL> urls) {

        Map<String, List<URL>> result = new HashMap<String, List<URL>>();

        for (URL u : urls) {
            if (UrlUtils.isMatch(url, u)) {
                //类别
                String category = u.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
                List<URL> categoryList = result.get(category);
                if (categoryList == null) {
                    categoryList = new ArrayList<URL>();
                    result.put(category, categoryList);
                }
                categoryList.add(u);
            }
        }
        if (result.size() == 0) {
            return;
        }
        //记录已经通知的
        Map<String, List<URL>> categoryNotified = notified.get(url);
        if (categoryNotified == null) {
            notified.putIfAbsent(url, new ConcurrentHashMap<String, List<URL>>());
            categoryNotified = notified.get(url);
        }

        for (Map.Entry<String, List<URL>> entry : result.entrySet()) {
            String category = entry.getKey();
            List<URL> categoryList = entry.getValue();
            categoryNotified.put(category, categoryList);
            saveProperties(url);
            //通知
            listener.notify(categoryList);
        }
    }


    private void saveProperties(URL url) {

    }

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void destroy() {

    }

    @Override
    public List<URL> lookup(URL url) {
        return null;
    }
}
