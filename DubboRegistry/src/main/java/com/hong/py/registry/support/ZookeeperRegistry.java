package com.hong.py.registry.support;

import com.hong.py.commonUtils.ConcurrentHashSet;
import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.registry.NotifyListener;
import com.hong.py.registry.Registry;
import com.hong.py.registry.zookeeper.ZookeeperClient;
import com.hong.py.rpc.RpcException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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


    private final String root;

    private final ZookeeperClient client;

    public ZookeeperRegistry(URL url,ZookeeperTransporter zookeeperTransporter) {
        String group = url.getParameter(Constants.GROUP_KEY, DEFAULT_ROOT);

        if (!group.startsWith(Constants.PATH_SEPARATOR)) {
            group = Constants.PATH_SEPARATOR + group;
        }
        this.root = group; // 必须都是/开头
        this.client = zookeeperTransporter.connect(url);

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
        }

        addFailedSubscribe(url, listener);
    }


    private void doSubsribe(URL url, NotifyListener listener) {

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

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {

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
