package com.hong.py.cluster;

import com.hong.py.annotation.Adaptive;
import com.hong.py.annotation.SPI;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.RpcException;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: MyDubbo
 * @Package: com.hong.py.cluster
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2020/1/2 15:41
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2020/1/2 15:41
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2020 hongpy Technologies Inc. All Rights Reserved
 **/
@SPI("failOver")
public interface Cluster {

    @Adaptive
    <T> Invoker<T> join(Directory<T> directory) throws RpcException;
}
