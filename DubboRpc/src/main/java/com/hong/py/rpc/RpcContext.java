package com.hong.py.rpc;

import java.util.concurrent.Future;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: MyDubbo
 * @Package: com.hong.py.rpc
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2020/1/2 9:52
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2020/1/2 9:52
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2020 hongpy Technologies Inc. All Rights Reserved
 **/
public class RpcContext {

    private static ThreadLocal<RpcContext> LOCAL = new ThreadLocal<>();

    private Future<?> future;

    public static RpcContext getContext() {
        return LOCAL.get();
    }

    public void setFuture(Future<?> future) {
        this.future=future;
    }

}
