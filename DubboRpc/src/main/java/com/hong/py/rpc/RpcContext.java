package com.hong.py.rpc;

import java.util.HashMap;
import java.util.Map;
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

    private final Map<String, String> attachments = new HashMap<String, String>();

    public static RpcContext getContext() {

        RpcContext rpcContext = LOCAL.get();
        if (rpcContext == null) {
            rpcContext = new RpcContext();
            LOCAL.set(rpcContext);
        }
        return rpcContext;
    }

    public void setFuture(Future<?> future) {
        this.future=future;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public RpcContext setAttachment(String key, String value) {
        if (value == null) {
            attachments.remove(key);
        } else {
            attachments.put(key,value);
        }
        return this;
    }

    public RpcContext setAttachments(Map<String, String> attachment) {
        this.attachments.clear();
        if (attachment != null && attachment.size() > 0) {
            this.attachments.putAll(attachment);
        }
        return this;
    }


}
