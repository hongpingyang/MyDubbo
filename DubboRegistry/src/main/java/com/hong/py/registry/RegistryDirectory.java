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
    private volatile Set<URL> cachedInvokerUrls; // The initial value is null and the midway may be assigned to null, please use the local variable reference


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
     * @param addParameter
     */
    public void subscribe(URL addParameter) {
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
        //Map<String, List<Invoker<T>>> newMethodInvokerMap = toMethodInvokers(newUrlInvokerMap); // Change method name to map Invoker Map

        if (newUrlInvokerMap == null || newUrlInvokerMap.size() == 0) {
            logger.error(new IllegalStateException("urls to invokers error .invokerUrls.size :" + invokerUrls.size() + ", invoker.size :0. urls :" + invokerUrls.toString()));
            return;
        }
        this.urlInvokerMap = newUrlInvokerMap;

        try {
            destroyUnusedInvokers(oldUrlInvokerMap, newUrlInvokerMap); // Close the unused Invoker
        } catch (Exception e) {
            logger.warn("destroyUnusedInvokers error. ", e);
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

    /*private Map<String, List<Invoker<T>>> toMethodInvokers(Map<String, Invoker<T>> newUrlInvokerMap) {

    }*/

    private void destroyUnusedInvokers(Map<String, Invoker<T>> oldUrlInvokerMap, Map<String, Invoker<T>> newUrlInvokerMap) {

    }

    @Override
    public Class<T> getInterface() {
        return serviceType;
    }

    @Override
    public List<Invoker<T>> list(Invocation invocation) throws RpcException {
        return null;
    }
}
