package com.hong.py.registry;

import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.rpc.Exporter;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.Protocol;
import com.hong.py.rpc.RpcException;

import java.util.HashMap;
import java.util.Map;

public class RegistryProtocol implements Protocol {

    //为  Protocol&Adaptive
    private Protocol protocol;
    private Map<String, Exporter<?>> exporterMap = new HashMap<>();
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

    @Override
    public int getDefaultPort() {
        return 9090;
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        final Exporter<T> exporter = doExport(invoker);

        URL registryUrl = getRegistryUrl(invoker);

        Registry registry=getRegistry(invoker);



        return null;
    }

    //会到dubboProtocol发布
    private <T> Exporter<T> doExport(final Invoker<T> invoker) {
        String key = getCacheKey(invoker);
        Exporter<T> exporter = (Exporter<T>)exporterMap.get(key);
        if (exporter == null) {
            exporter = protocol.export(invoker);
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

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        return null;
    }

    @Override
    public void destroy() {

    }
}
