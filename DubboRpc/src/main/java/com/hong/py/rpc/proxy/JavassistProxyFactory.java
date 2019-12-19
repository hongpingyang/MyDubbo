package com.hong.py.rpc.proxy;

import com.hong.py.common.bytecode.Wrapper;
import com.hong.py.commonUtils.URL;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.RpcException;

/**
 * javassist
 *
 **/
public class JavassistProxyFactory extends AbstractProxyFactory {

    @Override
    public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) throws RpcException {
        final Wrapper wrapper = Wrapper.getWrapper(type);
        return new ProxyInvoker<T>(wrapper,proxy, type, url);
    }
}
