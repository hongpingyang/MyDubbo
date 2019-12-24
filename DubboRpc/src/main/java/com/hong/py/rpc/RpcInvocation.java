package com.hong.py.rpc;

import com.hong.py.commonUtils.Constants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 通信传输的
 **/
public class RpcInvocation implements Invocation, Serializable {

    private static final long serialVersionUID = 883200900771950456L;

    private String methodName;

    private Class<?>[] ParameterTypes;

    private Object[] Arguments;

    private Map<String, String> Attachments;

    private transient Invoker<?> Invoker;

    public RpcInvocation(Invocation invocation, Invoker<?> invoker) {
        this(invocation.getMethodName(), invocation.getParameterTypes(),
                invocation.getArguments(), invocation.getAttachments(), invoker);
        if (invoker != null) {
            setAttachment(Constants.PATH_KEY, invoker.getUrl().getPath());
        }
    }


    public RpcInvocation(String methodName, Class<?>[] parameterTypes, Object[] arguments, Map<String, String> attachments, Invoker<?> invoker) {
        this.methodName = methodName;
        this.ParameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes;
        this.Arguments = arguments == null ? new Object[0] : arguments;
        this.Attachments = attachments == null ? new HashMap<String, String>() : attachments;
        this.Invoker = invoker;
    }


    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return ParameterTypes;
    }

    @Override
    public Object[] getArguments() {
        return Arguments;
    }

    @Override
    public Map<String, String> getAttachments() {
        return Attachments;
    }

    @Override
    public String getAttachment(String key) {
        if(Attachments==null) return null;
        return Attachments.get(key);
    }

    @Override
    public String getAttachment(String key, String defaultValue) {
        if(Attachments==null) return null;
        return Attachments.getOrDefault(key, defaultValue);
    }

    public void setAttachment(String key, String value) {
        if (Attachments == null) {
            Attachments = new HashMap<String, String>();
        }
        Attachments.put(key, value);
    }

    @Override
    public Invoker<?> getInvoker() {
        return Invoker;
    }
}
