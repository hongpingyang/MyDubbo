package com.hong.py.rpc.proxy;

import com.hong.py.common.bytecode.Proxy;
import com.hong.py.common.bytecode.Wrapper;
import com.hong.py.commonUtils.URL;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.RpcException;

/**
 * javassist
 **/
public class JavassistProxyFactory extends AbstractProxyFactory {

    @Override
    public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) throws RpcException {
        final Wrapper wrapper = Wrapper.getWrapper(type);
        return new WrapperInvoker<T>(wrapper,proxy, type, url);
    }

    @Override
    public <T> T createProxy(Invoker<T> invoker, Class<?>[] types) {
        InvokerInvocationHandler handler = new InvokerInvocationHandler(invoker);
        return (T)Proxy.getProxy(types).newInstance(handler);
    }
}
