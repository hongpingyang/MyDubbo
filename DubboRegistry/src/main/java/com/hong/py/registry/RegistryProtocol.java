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
import java.util.PrimitiveIterator;

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
                .addParameters(Constants.CATEGORY_KEY, Constants.CONFIGURATORS_CATEGORY,
                        Constants.CHECK_KEY, String.valueOf(false));
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {

        return null;
    }

    @Override
    public void destroy() {

    }

}
