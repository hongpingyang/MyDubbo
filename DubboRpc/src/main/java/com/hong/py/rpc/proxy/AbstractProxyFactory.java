package com.hong.py.rpc.proxy;

import com.hong.py.commonUtils.URL;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.ProxyFactory;
import com.hong.py.rpc.RpcException;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: MyDubbo
 * @Package: com.hong.py.rpc.proxy
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2019/12/18 19:47
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2019/12/18 19:47
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2019 hongpy Technologies Inc. All Rights Reserved
 **/
public abstract class AbstractProxyFactory implements ProxyFactory {

        @Override
    public <T> T getProxy(Invoker<T> invoker) throws RpcException {
        return getProxy(invoker,false);
    }

    @Override
    public <T> T getProxy(Invoker<T> invoker, boolean generic) throws RpcException {
        return null;
    }

}
