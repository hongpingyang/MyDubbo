package com.hong.py.rpc.dubbo;

import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.rpc.Exporter;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.Protocol;
import com.hong.py.rpc.RpcException;
import com.hong.py.rpc.support.ProtocolUtils;

/**
 * Dubbo
 */
public class DubboProtocol implements Protocol {

    private static int DUBBO_DEFAULT_PORT=20880;



    @Override
    public int getDefaultPort() {
        return DUBBO_DEFAULT_PORT;
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {

        URL url=invoker.getUrl();
        String serverKey = serviceKey(url);

        return null;
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        return null;
    }

    private static String serviceKey(URL url) {
        int port = url.getParameter(Constants.BIND_PORT_KEY, url.getPort());
        return serviceKey(port, url.getPath(), url.getParameter(Constants.VERSION_KEY),
                url.getParameter(Constants.GROUP_KEY));
    }

    private static String serviceKey(int port, String path, String version, String group) {
        return ProtocolUtils.serviceKey(port, path, version, group);
    }

    @Override
    public void destroy() {

    }
}
