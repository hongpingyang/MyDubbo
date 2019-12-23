package com.hong.py.rpc.proxy;

import com.hong.py.commonUtils.URL;
import com.hong.py.rpc.Invocation;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.Result;
import com.hong.py.rpc.RpcException;

/**
 *
 **/
public class InvokerWrapper<T> implements Invoker<T> {

    private final Invoker<T> invoker;
    private final URL url;

    public InvokerWrapper(Invoker<T> invoker, URL url) {
        this.invoker = invoker;
        this.url = url;
    }

    @Override
    public Class<T> getInterface() {
        return invoker.getInterface();
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        return invoker.invoke(invocation);
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public boolean isAvailable() {
        return invoker.isAvailable();
    }

    @Override
    public void destroy() {

    }
}
