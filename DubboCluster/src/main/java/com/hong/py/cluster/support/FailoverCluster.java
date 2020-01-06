package com.hong.py.cluster.support;

import com.hong.py.cluster.Cluster;
import com.hong.py.cluster.Directory;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.RpcException;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: MyDubbo
 * @Package: com.hong.py.cluster.support
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2020/1/6 17:26
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2020/1/6 17:26
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2020 hongpy Technologies Inc. All Rights Reserved
 **/
public class FailoverCluster implements Cluster {

    @Override
    public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
        return new FailoverClusterInvoker<T>(directory);
    }
}
