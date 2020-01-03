package com.hong.py.cluster.support;

import com.hong.py.cluster.Directory;
import com.hong.py.cluster.LoadBalance;
import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.extension.ExtensionLoader;
import com.hong.py.rpc.*;
import com.hong.py.rpc.support.RpcUtils;

import java.util.List;
import java.util.Map;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: MyDubbo
 * @Package: com.hong.py.cluster.support
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2020/1/2 15:07
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2020/1/2 15:07
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2020 hongpy Technologies Inc. All Rights Reserved
 **/
public abstract class AbstractClusterInvoker<T> implements Invoker<T> {

    private final Directory<T> directory;

    public AbstractClusterInvoker(Directory<T> directory) {
        this.directory = directory;
    }

    @Override
    public Class<T> getInterface() {
        return directory.getInterface();
    }

    @Override
    public URL getUrl() {
        return directory.getUrl();
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {

        LoadBalance loadBalance=null;

        Map<String, String> attachments = RpcContext.getContext().getAttachments();
        if (attachments != null&&attachments.size()>0) {
            ((RpcInvocation) invocation).addAttachments(attachments);
        }

        List<Invoker<T>> invokers = list(invocation);
        if (invokers != null && !invokers.isEmpty()) {
            loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(invokers.get(0).getUrl()
                    .getMethodParameter(RpcUtils.getMethodName(invocation), Constants.LOADBALANCE_KEY, Constants.DEFAULT_LOADBALANCE));

        }
        return doInvoke(invocation, invokers, loadBalance);
    }

    protected abstract Result doInvoke(Invocation invocation, List<Invoker<T>> invokers,
                                   LoadBalance loadBalance) throws RpcException;


    private List<Invoker<T>> list(Invocation invocation) {
        List<Invoker<T>> list = directory.list(invocation);
        return list;
    }

    @Override
    public void destroy() {

    }
}
