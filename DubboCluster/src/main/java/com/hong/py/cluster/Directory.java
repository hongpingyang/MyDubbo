package com.hong.py.cluster;

import com.hong.py.commonUtils.URL;
import com.hong.py.rpc.Invocation;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.RpcException;

import java.util.List;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: MyDubbo
 * @Package: com.hong.py.cluster
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2020/1/2 15:09
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2020/1/2 15:09
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2020 hongpy Technologies Inc. All Rights Reserved
 **/
public interface Directory<T> {

    Class<T> getInterface();


    URL getUrl();


    boolean isAvailable();

    /**
     * 获取Invoker
     * @param invocation
     * @return
     * @throws RpcException
     */
    List<Invoker<T>> list(Invocation invocation) throws RpcException;
}
