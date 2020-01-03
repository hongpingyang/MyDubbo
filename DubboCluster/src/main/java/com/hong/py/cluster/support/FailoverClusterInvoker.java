package com.hong.py.cluster.support;

import com.hong.py.cluster.Directory;
import com.hong.py.cluster.LoadBalance;
import com.hong.py.commonUtils.Constants;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.rpc.Invocation;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.Result;
import com.hong.py.rpc.RpcException;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: MyDubbo
 * @Package: com.hong.py.cluster.support
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2020/1/2 18:59
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2020/1/2 18:59
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2020 hongpy Technologies Inc. All Rights Reserved
 **/
public class FailoverClusterInvoker<T> extends AbstractClusterInvoker<T> {

    private static final Logger logger = LoggerFactory.getLogger(FailoverClusterInvoker.class);

    public FailoverClusterInvoker(Directory<T> directory) {
        super(directory);
    }

    @Override
    protected Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadBalance) throws RpcException {

        List<Invoker<T>> copyInvokers=invokers;
        int retryies = getUrl().getMethodParameter(invocation.getMethodName(),Constants.RETRIES_KEY, Constants.DEFAULT_RETRIES);
        retryies+=1; // 包含一次正常调用
        if (retryies <= 0) {
            retryies=1;
        }

        RpcException rpcException=null;
        List<Invoker<T>> invoked = new ArrayList<>(copyInvokers.size());

        for (int i = 0; i < retryies; i++) {
            Invoker<T> selectInvoker = select(loadBalance, invocation, invokers, invoked);
            invoked.add(selectInvoker);
            try {
                Result result = selectInvoker.invoke(invocation);
                if (rpcException != null) {
                  logger.warn(rpcException.getMessage());
                }
                return result;
            } catch (RpcException e) {
                rpcException=e;
            } catch (Throwable throwable) {
                rpcException = new RpcException(throwable);
            }
        }

        throw new RpcException("invoke failed");
    }
}
