package com.hong.py.registry;

import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.registry.listener.OverrideNotifyListener;
import com.hong.py.rpc.Exporter;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.Protocol;
import com.hong.py.rpc.RpcException;
import com.hong.py.rpc.proxy.InvokerWrapper;

import java.util.HashMap;
import java.util.Map;

import static com.hong.py.commonUtils.Constants.CATEGORY_KEY;
import static com.hong.py.commonUtils.Constants.CHECK_KEY;
import static com.hong.py.commonUtils.Constants.CONSUMERS_CATEGORY;

public class RegistryProtocol implements Protocol {

    //为  Protocol&Adaptive
    private Protocol protocol;
    private final Map<String, Exporter<?>> exporterMap = new HashMap<>();
    private final Map<URL, NotifyListener> notifyListenerMap = new HashMap<>();
    //为  RegistryFactory&Adaptive
    private RegistryFactory registryFactory;

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public RegistryFactory getRegistryFactory() {
        return registryFactory;
    }

    public void setRegistryFactory(RegistryFactory registryFactory) {
        this.registryFactory = registryFactory;
    }

    private Registry registry;

    @Override
    public int getDefaultPort() {
        return 9090;
    }


    //远程服务的暴露总体步骤：
    //
    //将ref封装为invoker
    //将invoker转换为exporter
    //启动netty
    //注册服务到zookeeper
    //订阅与通知
    //返回新的exporter实例
    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {

        final Exporter<T> exporter = doExport(invoker);

        URL registryUrl = getRegistryUrl(invoker);

        //获取的是ZookeeperRegistry
        Registry registry=getRegistry(invoker);

        final URL reigisteredProviderURL = getProviderUrl(invoker);

        //注册
        registry.register(reigisteredProviderURL);

        //讲协议改为provider
        //添加参数：category=configurators和check=false；
        final URL overrideSubscribeUrl = getSubscribedOverrideUrl(reigisteredProviderURL);

        final OverrideNotifyListener overrideSubscribeListener = new OverrideNotifyListener(overrideSubscribeUrl, invoker);

        notifyListenerMap.put(overrideSubscribeUrl, overrideSubscribeListener);

        //发布
        registry.subscribe(overrideSubscribeUrl,overrideSubscribeListener);

        return new DestoryableExporter(exporter,invoker,overrideSubscribeUrl,registryUrl,registry,notifyListenerMap);
    }

    //会到dubboProtocol发布
    private <T> Exporter<T> doExport(final Invoker<T> invoker) {
        String key = getCacheKey(invoker);
        Exporter<T> exporter = (Exporter<T>)exporterMap.get(key);
        if (exporter == null) {
            InvokerWrapper<T> invokerWrapper = new InvokerWrapper<>(invoker, getProviderUrl(invoker));
            exporter = protocol.export(invokerWrapper);
            exporterMap.put(key, exporter);
        }
        return exporter;
    }

    private Registry getRegistry(final Invoker<?> originInvoker) {
        URL registryUrl = getRegistryUrl(originInvoker);
        return registryFactory.getRegistry(registryUrl);
    }

    private String getCacheKey(final Invoker<?> invoker) {
        URL providerUrl = getProviderUrl(invoker);
        String key = providerUrl.toFullString();
        return key;
    }

    private URL getProviderUrl(final Invoker<?> invoker) {
        String export = invoker.getUrl().getParameterAndDecoded(Constants.EXPORT_KEY);
        if (export == null || export.length() == 0) {
            throw new IllegalArgumentException("The registry export url is null! registry: " + invoker.getUrl());
        }
        URL providerUrl = URL.valueOf(export);
        return providerUrl;
    }

    private URL getRegistryUrl(Invoker<?> originInvoker) {
        URL registryUrl = originInvoker.getUrl();
        if (Constants.REGISTRY_PROTOCOL.equals(registryUrl.getProtocol())) {
            String protocol = registryUrl.getParameter(Constants.REGISTRY_KEY, Constants.DEFAULT_DIRECTORY);
            registryUrl = registryUrl.setProtocol(protocol).removeParameter(Constants.REGISTRY_KEY);
        }
        return registryUrl;
    }

    private URL getSubscribedOverrideUrl(URL registedProviderUrl) {
        return registedProviderUrl.setProtocol(Constants.PROVIDER_PROTOCOL)
                .addParameters(CATEGORY_KEY, Constants.CONFIGURATORS_CATEGORY,
                        CHECK_KEY, String.valueOf(false));
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        //url 的protocol由registry立马换成zookeeper
        // zookeeper://10.20.29.203:2181/com.alibaba.dubbo.registry.RegistryService?application=demo-consumer&dubbo=2.0.2&pid=20052&qos.port=33333&refer=application%3Ddemo-consumer%26check%3Dfalse%26dubbo%3D2.0.2%26interface%3Dcom.alibaba.dubbo.demo.DemoService2%26methods%3DsayHello%26pid%3D20052%26qos.port%3D33333%26register.ip%3D192.168.158.78%26side%3Dconsumer%26timestamp%3D1577761153089&timestamp=1577761425152
        url = url.setProtocol(url.getParameter(Constants.REGISTRY_KEY, Constants.DEFAULT_REGISTRY)).removeParameter(Constants.REGISTRY_KEY);
        //获取注册中心
        Registry registry = registryFactory.getRegistry(url);

        return doRefer(cluster, registry, type, url);
    }

    private <T> Invoker<T> doRefer(Cluster cluster, Registry registry, Class<T> type, URL url) {

        RegistryDirectory<T> directory = new RegistryDirectory<T>(type, url);
        directory.setRegistry(registry);
        directory.setProtocol(protocol);

        Map<String, String> parameters = new HashMap<String, String>(directory.getUrl().getParameters());
        //consumer://192.168.158.78/com.alibaba.dubbo.demo.DemoService2?application=demo-consumer&check=false&dubbo=2.0.2&interface=com.alibaba.dubbo.demo.DemoService2&methods=sayHello&pid=20052&qos.port=33333&side=consumer&timestamp=1577761153089
        URL subscribeUrl = new URL(Constants.CONSUMER_PROTOCOL, parameters.remove(Constants.REGISTER_IP_KEY), 0, type.getName(), parameters);

        if (!Constants.ANY_VALUE.equals(url.getServiceInterface())
                && url.getParameter(Constants.REGISTER_KEY, true)) {
            URL registeredConsumerUrl = getRegisteredConsumerUrl(subscribeUrl, url);
            registry.register(registeredConsumerUrl);
            directory.setRegisteredConsumerUrl(registeredConsumerUrl);
        }

        //发布 暂时只订阅Provider
        directory.subscribe(subscribeUrl.addParameter(CATEGORY_KEY,
                Constants.PROVIDERS_CATEGORY
                        /*+ "," + Constants.CONFIGURATORS_CATEGORY
                        + "," + Constants.ROUTERS_CATEGORY*/));

        Invoker invoker = cluster.join(directory);

        return invoker;
    }

    public URL getRegisteredConsumerUrl(final URL consumerUrl, URL registryUrl) {
        return consumerUrl.addParameters(CATEGORY_KEY, CONSUMERS_CATEGORY,
                CHECK_KEY, String.valueOf(false));
    }


    @Override
    public void destroy() {

    }

}
