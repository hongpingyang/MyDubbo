package com.hong.py.registry;

import com.hong.py.commonUtils.URL;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.proxy.InvokerWrapper;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: MyDubbo
 * @Package: com.hong.py.registry
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2019/12/31 15:19
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2019/12/31 15:19
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2019 hongpy Technologies Inc. All Rights Reserved
 **/
public class InvokerDelegate<T> extends InvokerWrapper<T> {

    private final URL providerUrl;

    public InvokerDelegate(Invoker<T> invoker, URL url,URL providerUrl) {
        super(invoker, url);
        this.providerUrl=providerUrl;
    }

    public URL getProviderUrl() {
        return providerUrl;
    }
}
