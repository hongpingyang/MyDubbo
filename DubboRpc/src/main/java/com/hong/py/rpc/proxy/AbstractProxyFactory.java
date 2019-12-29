package com.hong.py.rpc.proxy;

import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.ReflectUtils;
import com.hong.py.commonUtils.URL;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.ProxyFactory;
import com.hong.py.rpc.RpcException;
import com.hong.py.rpc.support.EchoService;

/**
 * 文件描述
 *
 **/
public abstract class AbstractProxyFactory implements ProxyFactory {

        @Override
    public <T> T getProxy(Invoker<T> invoker) throws RpcException {
        return getProxy(invoker,false);
    }

    @Override
    public <T> T getProxy(Invoker<T> invoker, boolean generic) throws RpcException {
        Class<?>[] interfaces = null;
        String config = invoker.getUrl().getParameter("interfaces");
        if (config != null && config.length() > 0) {
            String[] types = Constants.COMMA_SPLIT_PATTERN.split(config);
            if (types != null && types.length > 0) {
                interfaces = new Class<?>[types.length + 2];
                interfaces[0] = invoker.getInterface();
                interfaces[1] = EchoService.class; //EchoService?
                for (int i = 0; i < types.length; i++) {
                    interfaces[i + 1] = ReflectUtils.forName(types[i]);
                }
            }
        }
        if (interfaces == null) {
            interfaces = new Class<?>[]{invoker.getInterface(), EchoService.class};
        }

        return createProxy(invoker,interfaces);
    }

    public abstract <T> T createProxy(Invoker<T> invoker,Class<?>[] types);
}
