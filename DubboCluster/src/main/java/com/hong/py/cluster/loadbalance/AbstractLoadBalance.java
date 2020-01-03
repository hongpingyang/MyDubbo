package com.hong.py.cluster.loadbalance;

import com.hong.py.cluster.LoadBalance;
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
 * @Package: com.hong.py.cluster.loadbalance
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2020/1/3 11:03
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2020/1/3 11:03
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2020 hongpy Technologies Inc. All Rights Reserved
 **/
public abstract class AbstractLoadBalance implements LoadBalance {


    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        return null;
    }
}
