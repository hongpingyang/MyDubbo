package com.hong.py.rpc.proxy;

import com.hong.py.rpc.Invoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *
 **/
public class InvokerInvocationHandler implements InvocationHandler {

    private final Invoker<?> invoker;

    public InvokerInvocationHandler(Invoker<?> handler) {
        this.invoker = handler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return invoker.invoke(null);
    }

}
