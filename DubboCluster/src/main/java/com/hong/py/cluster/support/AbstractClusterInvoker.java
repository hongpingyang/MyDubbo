package com.hong.py.cluster.support;

import com.hong.py.cluster.Directory;
import com.hong.py.commonUtils.URL;
import com.hong.py.rpc.Invocation;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.Result;
import com.hong.py.rpc.RpcException;

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
public  class AbstractClusterInvoker<T> implements Invoker<T> {

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
        return null;
    }



    @Override
    public void destroy() {

    }
}
