package com.hong.py.rpc.proxy;

import com.hong.py.common.bytecode.Wrapper;
import com.hong.py.commonUtils.URL;
import com.hong.py.rpc.Invocation;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.Result;
import com.hong.py.rpc.RpcException;

import java.util.InvalidPropertiesFormatException;

/**
 * 包装真正的Invoker
 * @param <T>
 */
public class WrapperProxy<T> implements Invoker<T> {

     private Wrapper wrapper;
     private T proxy;
     private Class<T> type;
     private URL url;

    public WrapperProxy(Wrapper wrapper, T proxy, Class<T> type, URL url) {
        if (wrapper == null) {
            throw new IllegalArgumentException("wrapper==null");
        }
        if (proxy == null) {
            throw new IllegalArgumentException("proxy==null");
        }
        if (type == null) {
            throw new IllegalArgumentException("interface==null");
        }
        if (!type.isInstance(proxy)) {
            throw new IllegalArgumentException(proxy.getClass().getName() + " is not implement from " + type);
        }
        this.wrapper=wrapper;
        this.proxy = proxy;
        this.type = type;
        this.url = url;
    }


    @Override
    public Class<T> getInterface() {
        return this.type;
    }

    //发起调用
    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        //todo
        try {
            invokeMethod(proxy, invocation.getMethodName(), invocation.getParameterTypes(), invocation.getArguments());


        } catch (Throwable throwable) {
            throw new RpcException("failed to invoke remote proxy method " + invocation.getMethodName());
        }
        return null;
    }


    public Object invokeMethod(T proxy,String methodName,Class<?>[] types,Object[] args) throws Throwable {
        return wrapper.invokeMethod(proxy, methodName, types, args);
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void destroy() {

    }
}
