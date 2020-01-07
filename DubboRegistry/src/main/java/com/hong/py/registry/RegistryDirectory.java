package com.hong.py.registry;

import com.hong.py.cluster.Directory;
import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.NetUtils;
import com.hong.py.commonUtils.StringUtils;
import com.hong.py.commonUtils.URL;
import com.hong.py.extension.ExtensionLoader;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.rpc.Invocation;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.Protocol;
import com.hong.py.rpc.RpcException;
import com.hong.py.rpc.support.RpcUtils;

import java.util.*;

public class RegistryDirectory<T> implements NotifyListener, Directory<T> {

    private static final Logger logger = LoggerFactory.getLogger(RegistryDirectory.class);

    private final URL url;

    private URL consumerUrl;
    private final Class<T> serviceType;
    private final String serviceKey;
    private final Map<String, String> queryMap;
    private Registry registry;
    private Protocol protocol;

    private volatile URL overrideDirectoryUrl;
    private volatile URL registeredConsumerUrl;

    private volatile Map<String, Invoker<T>> urlInvokerMap = new HashMap<>();
    private volatile Map<String,List<Invoker<T>>> methodInvokerMap;
    private volatile Set<URL> cachedInvokerUrls; // The initial value is null and the midway may be assigned to null, please use the local variable reference
    private volatile boolean forbidden=false;

    //directoryUrl  zookeeper://10.20.29.203:2181/com.alibaba.dubbo.registry.RegistryService?application=demo-consumer&check=false&dubbo=2.0.2&interface=com.alibaba.dubbo.demo.DemoService2&methods=sayHello&pid=20052&qos.port=33333&register.ip=192.168.158.78&side=consumer&timestamp=1577761153089
    //overrideDirectoryUrl zookeeper://10.20.29.203:2181/com.alibaba.dubbo.registry.RegistryService?application=demo-consumer&check=false&dubbo=2.0.2&interface=com.alibaba.dubbo.demo.DemoService2&methods=sayHello&pid=20052&qos.port=33333&register.ip=192.168.158.78&side=consumer&timestamp=1577761153089
    public RegistryDirectory(Class<T> type, URL url) {
        this.url=url;
        this.consumerUrl=url;
        this.serviceType=type;
        this.serviceKey = url.getServiceKey();
        this.queryMap = StringUtils.parseQueryString(url.getParameterAndDecoded(Constants.REFER_KEY));
    }

    public void setRegistry(Registry registry) {
        this.registry=registry;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol=protocol;
    }

    public URL getUrl() {
        return this.url;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void destroy() {

    }

    public URL getConsumerUrl() {
        return consumerUrl;
    }

    public void setConsumerUrl(URL consumerUrl) {
        this.consumerUrl = consumerUrl;
    }

    public void setRegisteredConsumerUrl(URL registeredConsumerUrl) {
        this.registeredConsumerUrl=registeredConsumerUrl;
    }

    /**
     * 订阅
     * @param url 要订阅的url
     */
    public void subscribe(URL url) {
        setConsumerUrl(url);
        registry.subscribe(url, this);
    }

    /**
     * 有变动通知
     * @param urls The list of registered information , is always not empty. The meaning is the same as the return value of
     */
    @Override
    public void notify(List<URL> urls) {
        //urls empty://192.168.158.78/com.alibaba.dubbo.demo.DemoService2?application=demo-consumer&category=configurators&check=false&dubbo=2.0.2&interface=com.alibaba.dubbo.demo.DemoService2&methods=sayHello&pid=20052&qos.port=33333&side=consumer&timestamp=1577761153089
        List<URL> invokerUrls = new ArrayList<URL>();
        List<URL> routerUrls = new ArrayList<URL>();
        List<URL> configuratorUrls = new ArrayList<URL>();
        for (URL url : urls) {
            String protocol = url.getProtocol();
            String category = url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
            if (Constants.ROUTERS_CATEGORY.equals(category)
                    || Constants.ROUTE_PROTOCOL.equals(protocol)) {
                routerUrls.add(url);
            } else if (Constants.CONFIGURATORS_CATEGORY.equals(category)
                    || Constants.OVERRIDE_PROTOCOL.equals(protocol)) {
                configuratorUrls.add(url);
            } else if (Constants.PROVIDERS_CATEGORY.equals(category)) {
                invokerUrls.add(url);
            } else {
                logger.warn("Unsupported category " + category + " in notified url: " + url + " from registry " + getUrl().getAddress() + " to consumer " + NetUtils.getLocalHost());
            }
        }

        refreshInvoker(invokerUrls);
    }

    private void refreshInvoker(List<URL> invokerUrls) {

        if (invokerUrls != null && invokerUrls.size() == 1 && invokerUrls.get(0) != null &&
                Constants.EMPTY_PROTOCOL.equals(invokerUrls.get(0).getProtocol())){
            this.forbidden = true;
            this.methodInvokerMap = null;
            destroyAllInvokers();

        } else {

            this.forbidden = false;
            //dubbo://192.168.158.78:20880/com.alibaba.dubbo.demo.DemoService2?anyhost=true&application=demo-provider&bean.name=com.alibaba.dubbo.demo.DemoService2&dubbo=2.0.2&generic=false&interface=com.alibaba.dubbo.demo.DemoService2&methods=sayHello&pid=27096&side=provider&timestamp=1577760953001
            Map<String, Invoker<T>> oldUrlInvokerMap = this.urlInvokerMap; // local reference
            if (invokerUrls.isEmpty() && this.cachedInvokerUrls != null) {
                invokerUrls.addAll(this.cachedInvokerUrls);
            } else {
                this.cachedInvokerUrls = new HashSet<URL>();
                this.cachedInvokerUrls.addAll(invokerUrls);//Cached invoker urls, convenient for comparison
            }
            if (invokerUrls.isEmpty()) {
                return;
            }

            Map<String, Invoker<T>> newUrlInvokerMap = toInvokers(invokerUrls);
            Map<String, List<Invoker<T>>> newMethodInvokerMap = toMethodInvokers(newUrlInvokerMap); // Change method name to map Invoker Map

            if (newUrlInvokerMap == null || newUrlInvokerMap.size() == 0) {
                logger.error(new IllegalStateException("urls to invokers error .invokerUrls.size :" + invokerUrls.size() + ", invoker.size :0. urls :" + invokerUrls.toString()));
                return;
            }
            this.methodInvokerMap = newMethodInvokerMap;
            this.urlInvokerMap = newUrlInvokerMap;

            try {
                destroyUnusedInvokers(oldUrlInvokerMap, newUrlInvokerMap); // Close the unused Invoker
            } catch (Exception e) {
                logger.warn("destroyUnusedInvokers error. ", e);
            }
        }

    }


    /**
     * 产生 invoker
     * @param urls
     * @return
     */
    private Map<String, Invoker<T>> toInvokers(List<URL> urls) {
        // dubbo://192.168.158.78:20880/com.alibaba.dubbo.demo.DemoService2?anyhost=true&application=demo-provider&bean.name=com.alibaba.dubbo.demo.DemoService2&dubbo=2.0.2&generic=false&interface=com.alibaba.dubbo.demo.DemoService2&methods=sayHello&pid=27096&side=provider&timestamp=1577760953001
        Map<String, Invoker<T>> newUrlInvokerMap = new HashMap<>();
        if (urls == null || urls.isEmpty()) {
            return newUrlInvokerMap;
        }

        Set<String> keys = new HashSet<String>();
        //如果reference端配置了protocol，则只选择匹配的protocol
        String queryProtocols = this.queryMap.get(Constants.PROTOCOL_KEY);
        for (URL providerUrl : urls) {
            if (queryProtocols != null && queryProtocols.length() > 0) {
                boolean accept=false;
                String[] acceptProtocols=queryProtocols.split(",");
                for (String acceptProtocol : acceptProtocols) {
                    if (providerUrl.getProtocol().equals(acceptProtocol)) {
                        accept = true;
                        break;
                    }
                }
                if (!accept) {
                    continue;
                }
            }

            //对于empty
            if (Constants.EMPTY_PROTOCOL.equals(providerUrl.getProtocol())) {
                continue;
            }

            //需要有扩展
            if (!ExtensionLoader.getExtensionLoader(Protocol.class).hasExtension(providerUrl.getProtocol())) {
                logger.error(new IllegalStateException("Unsupported protocol " + providerUrl.getProtocol() + " in notified url: " + providerUrl + " from registry " + getUrl().getAddress() + " to consumer " + NetUtils.getLocalHost()
                        + ", supported protocol: " + ExtensionLoader.getExtensionLoader(Protocol.class).getSupportedExtensions()));
                continue;
            }

            //合并consumer和provider生成新url
            //dubbo://192.168.158.78:20880/com.alibaba.dubbo.demo.DemoService2?anyhost=true&application=demo-consumer&bean.name=com.alibaba.dubbo.demo.DemoService2&check=false&dubbo=2.0.2&generic=false&interface=com.alibaba.dubbo.demo.DemoService2&methods=sayHello&pid=20052&qos.port=33333&register.ip=192.168.158.78&remote.timestamp=1577760953001&side=consumer&timestamp=1577761153089
            URL url = mergeUrl(providerUrl);

            String key = url.toFullString(); // dubbo://192.168.158.78:20880/com.alibaba.dubbo.demo.DemoService2?anyhost=true&application=demo-consumer&bean.name=com.alibaba.dubbo.demo.DemoService2&check=false&dubbo=2.0.2&generic=false&interface=com.alibaba.dubbo.demo.DemoService2&methods=sayHello&pid=20052&qos.port=33333&register.ip=192.168.158.78&remote.timestamp=1577760953001&side=consumer&timestamp=1577761153089
            if (keys.contains(key)) { //
                continue;
            }
            keys.add(key);

            Map<String, Invoker<T>> localUrlInvokerMap = this.urlInvokerMap;
            Invoker<T> invoker = localUrlInvokerMap == null ? null : localUrlInvokerMap.get(key);
            if (invoker == null) {
                //获取是 DubboInvoker
                invoker = new InvokerDelegate<T>(protocol.refer(serviceType, url), url, providerUrl);
            }
            if (invoker != null) { // Put new invoker in cache
                newUrlInvokerMap.put(key, invoker);
            }
        }

        keys.clear();
        return newUrlInvokerMap;
    }

    private URL mergeUrl(URL providerUrl) {

        providerUrl = providerUrl.addParameter(Constants.CHECK_KEY, String.valueOf(false)); // Do not check whether the connection is successful or not, always create Invoker!

        return providerUrl;
    }

    /**
     * 产生MethodInvoker
     * @param newUrlInvokerMap
     * @return
     */
    private Map<String, List<Invoker<T>>> toMethodInvokers(Map<String, Invoker<T>> newUrlInvokerMap) {
        Map<String, List<Invoker<T>>> methodInvokers = new HashMap<>();
        List<Invoker<T>> invokerList = new ArrayList<>();
        if (newUrlInvokerMap != null &&     newUrlInvokerMap.size()>0) {
            for (Invoker<T> invoker : newUrlInvokerMap.values()) {
                String parameter = invoker.getUrl().getParameter(Constants.METHODS_KEY);
                if (parameter != null && parameter.length() > 0) {
                    String[] methods = Constants.COMMA_SPLIT_PATTERN.split(parameter);
                    if (methods != null && methods.length > 0) {
                        for (String method : methods) {
                            if (method != null && method.length() > 0 &&
                                    !Constants.ANY_VALUE.equals(method)) {
                                List<Invoker<T>> methodinvokerList = methodInvokers.get(method);
                                if (methodinvokerList == null) {
                                    methodinvokerList = new ArrayList<>();
                                    methodInvokers.put(method, methodinvokerList);
                                }
                                methodinvokerList.add(invoker);
                            }
                        }
                    }
                }
                invokerList.add(invoker);
            }
        }

        return Collections.unmodifiableMap(methodInvokers);
    }

    private void destroyAllInvokers() {
        Map<String, Invoker<T>> localurlInvokerMap = this.urlInvokerMap;
        if (localurlInvokerMap != null) {
            for (Invoker<T> invoker : localurlInvokerMap.values()) {
                invoker.destroy();
            }
        }
        localurlInvokerMap.clear();
        this.methodInvokerMap=null;
    }

    private void destroyUnusedInvokers(Map<String, Invoker<T>> oldUrlInvokerMap, Map<String, Invoker<T>> newUrlInvokerMap) {

        if (newUrlInvokerMap == null || newUrlInvokerMap.size() == 0) {
            destroyAllInvokers();
            return;
        }
        List<String> deleted=null;
        if (oldUrlInvokerMap != null) {
            Collection<Invoker<T>> newvalues = newUrlInvokerMap.values();
            for (Map.Entry<String,Invoker<T>> entry : oldUrlInvokerMap.entrySet()) {
                if (!newvalues.contains(entry.getValue())) {
                    if (deleted == null) {
                        deleted = new ArrayList<>();
                    }
                    deleted.add(entry.getKey());
                }
            }
        }
        if (deleted != null) {
            for (String url : deleted) {
                Invoker<T> remove = this.urlInvokerMap.remove(url);
                if (remove != null) {
                    try {
                        remove.destroy();
                        if (logger.isDebugEnabled()) {
                           logger.debug("invoker ["+remove.getUrl()+"] destory succeed.");
                        }
                    } catch (Exception e) {
                        logger.warn("invoker [" + remove.getUrl() + "] destory failed." + e.getMessage(), e);
                    }
                }
            }
        }

    }

    @Override
    public Class<T> getInterface() {
        return serviceType;
    }

    /**
     *获取Invokerlist
     * @param invocation
     * @return
     * @throws RpcException
     */
    @Override
    public List<Invoker<T>> list(Invocation invocation) throws RpcException {
        if (forbidden) {
            throw new RpcException(" ");
        }
        List<Invoker<T>> invokerList=null;
        Map<String, List<Invoker<T>>> localmethodInvokerMap = this.methodInvokerMap;
        if (localmethodInvokerMap != null && localmethodInvokerMap.size() > 0) {
            String methodName = RpcUtils.getMethodName(invocation);

            invokerList = localmethodInvokerMap.get(methodName);

            if (invokerList == null) {
                invokerList = localmethodInvokerMap.get(Constants.ANY_VALUE);
            }
        }

        return invokerList==null?new ArrayList<Invoker<T>>(0):invokerList;
    }
}
