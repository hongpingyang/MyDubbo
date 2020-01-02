package com.hong.py.rpc.dubbo;

import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.remoting.RemotingException;
import com.hong.py.remoting.exchange.ExchangeClient;
import com.hong.py.remoting.exchange.ResponseFuture;
import com.hong.py.rpc.*;
import com.hong.py.rpc.support.RpcUtils;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: MyDubbo
 * @Package: com.hong.py.rpc.dubbo
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2019/12/31 16:20
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2019/12/31 16:20
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2019 hongpy Technologies Inc. All Rights Reserved
 **/
public class DubboInvoker<T> implements Invoker<T> {

    private final ExchangeClient[] clients;

    private final Class<T> type;

    private final URL url;

    private final Set<Invoker<?>> invokers;

    private volatile boolean available=false;

    private AtomicInteger index = new AtomicInteger(0);

    public DubboInvoker(ExchangeClient[] clients, Class<T> type, URL url, Set<Invoker<?>> invokers) {
        this.clients = clients;
        this.type = type;
        this.url = url;
        this.invokers = invokers;
    }

    /**
     * 发起调用
     * @param invocation
     * @return
     * @throws RpcException
     */
    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        RpcInvocation inv = (RpcInvocation) invocation;
        String methodName = inv.getMethodName();//方法名

        inv.setAttachment(Constants.PATH_KEY, getUrl().getPath());
        inv.setAttachment(Constants.VERSION_KEY, "1.0");

        //方法有异步标识async=true
        if (getUrl().getMethodParameter(invocation.getMethodName(), Constants.ASYNC_KEY, false)) {
            inv.setAttachment(Constants.ASYNC_KEY, Boolean.TRUE.toString());
        }

        try {
            ExchangeClient currentClient;
            if (clients.length == 1) {
                currentClient = clients[0];
            } else {
                currentClient = clients[index.getAndIncrement() % clients.length];
            }
            //是否是异步调用
            boolean isAsync = RpcUtils.isAsync(getUrl(), invocation);
            //是否有回调 默认是true
            boolean isOneway = RpcUtils.isOneway(getUrl(), invocation);

            int timeout = getUrl().getMethodParameter(invocation.getMethodName(), Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);

            if (isOneway) { //没有回调
                boolean isSent = getUrl().getMethodParameter(methodName, Constants.SENT_KEY, false);
                currentClient.send(inv, isSent);
                RpcContext.getContext().setFuture(null);
                return new RpcResult();
            } else if (isAsync) {//有回调异步
                ResponseFuture future = currentClient.request(inv, timeout);
                RpcContext.getContext().setFuture(new FutureAdapter<T>(future));
                return new RpcResult();
            }
            else { //有回调且不是异步
                RpcContext.getContext().setFuture(null);
                return (Result) currentClient.request(inv,timeout).get();
            }
        } catch (RemotingException e) {
            throw new RpcException(RpcException.TIMEOUT_EXCEPTION, "Invoke remote method timeout. method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }


    @Override
    public Class<T> getInterface() {
        return type;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void destroy() {

    }
}
