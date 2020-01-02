package com.hong.py.rpc;

import java.io.Serializable;
import java.util.Map;

/**
 * 文件描述
 *
 * @ProductName: HONGPY
 * @ProjectName: MyDubbo
 * @Package: com.hong.py.rpc
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2020/1/2 11:22
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2020/1/2 11:22
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2020 hongpy Technologies Inc. All Rights Reserved
 **/
public class RpcResult implements Result, Serializable {

    private static final long serialVersionUID = 945052384033520729L;

    private Object result;

    private Throwable exception;

    public RpcResult() {

    }

    public RpcResult(Object result) {
        this.result = result;
    }

    public RpcResult(Throwable exception) {
        this.exception = exception;
    }


    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public Throwable getException() {
        return null;
    }

    @Override
    public boolean hasException() {
        return false;
    }

    @Override
    public Object recreate() throws Throwable {
        return null;
    }

    @Override
    public Object getResult() {
        return null;
    }

    @Override
    public Map<String, String> getAttachments() {
        return null;
    }

    @Override
    public String getAttachment(String key) {
        return null;
    }

    @Override
    public String getAttachment(String key, String defaultValue) {
        return null;
    }
}
