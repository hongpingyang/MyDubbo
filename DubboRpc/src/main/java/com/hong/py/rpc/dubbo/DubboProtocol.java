package com.hong.py.rpc.dubbo;

import com.hong.py.commonUtils.ConcurrentHashSet;
import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.remoting.Channel;
import com.hong.py.remoting.RemotingException;
import com.hong.py.remoting.exchange.ExchangeClient;
import com.hong.py.remoting.exchange.ExchangeManage;
import com.hong.py.remoting.exchange.ExchangeServer;
import com.hong.py.rpc.*;
import com.hong.py.rpc.support.ProtocolUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dubbo
 */
public class DubboProtocol implements Protocol {

    private static int DUBBO_DEFAULT_PORT=20880;
    private Map<String, ExchangeServer> exchangeServerMap = new ConcurrentHashMap<>();
    private DubboRequestHandler dubboRequestHandler=new DubboRequestHandler(this);
    private Map<String, Exporter<?>> exporterMap = new ConcurrentHashMap<>();
    protected final Set<Invoker<?>> invokers = new ConcurrentHashSet<Invoker<?>>();
    private Map<String, ExchangeClient> refrenceClientMap = new ConcurrentHashMap<>();
    private Map<String, Object> locks = new ConcurrentHashMap<>();
    @Override
    public int getDefaultPort() {
        return DUBBO_DEFAULT_PORT;
    }

    //获取Invoker
    public Invoker<?> getInvoker(Channel channel, Invocation inv) throws RemotingException {
        int port = channel.getLocalAddress().getPort();
        String path = inv.getAttachments().get(Constants.PATH_KEY);
        String serviceKey = serviceKey(port, path, inv.getAttachments().get(Constants.VERSION_KEY), inv.getAttachments().get(Constants.GROUP_KEY));
        DubboExporter<?> exporter = (DubboExporter<?>)exporterMap.get(serviceKey);
        if (exporter == null) {
            throw new RemotingException(channel,"not found exporter");
        }
        return exporter.getInvoker();
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        URL url=invoker.getUrl();
        String serverKey = serviceKey(url);
        DubboExporter<T> exporter = new DubboExporter<>(invoker, serverKey);
        this.exporterMap.put(serverKey, exporter);
        openServer(url);
        return exporter;
    }

    private void openServer(URL url) {
        String address = url.getAddress();
        ExchangeServer exchangeServer = exchangeServerMap.get(address);
        if (exchangeServer == null) {
            exchangeServer = createServer(url);
            exchangeServerMap.put(address, exchangeServer);
        }
    }
    //
    private ExchangeServer createServer(URL url) {

        ExchangeServer server;
        try {
            server = ExchangeManage.getExchangeServer(url, dubboRequestHandler);
        } catch (RemotingException e) {
            throw new RpcException("Fail to start server(url: " + url + ") " + e.getMessage(), e);
        }
        return server;
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        ExchangeClient[] clients=getClients(url);
        DubboInvoker<T> invoker = new DubboInvoker<>(clients, type, url,invokers);
        invokers.add(invoker);
        return invoker;
    }

    private ExchangeClient[] getClients(URL url) {

        boolean service_share_connect = false;
        int connections = url.getParameter(Constants.CONNECTIONS_KEY, 0);
        //   客户端连接限制，如果没有配置 那是共享一个连接，否则一个服务一个连接
        // if not configured, connection is shared, otherwise, one connection for one service
        if (connections == 0) {
            service_share_connect = true;
            connections = 1;
        }

        ExchangeClient[] clients = new ExchangeClient[connections];
        for (int i = 0; i < clients.length; i++) {
            if (service_share_connect) {
                clients[i] = getSharedClient(url);
            } else {
                clients[i] = initClient(url);
            }
        }
        return clients;
    }

    /**
     * 创建共享连接
     * @param url
     * @return
     */
    private ExchangeClient getSharedClient(URL url) {
        String key = url.getAddress();
        ExchangeClient exchangeClient = refrenceClientMap.get(key);
        if (exchangeClient != null) {
            if (!exchangeClient.isClosed()) {
                return exchangeClient;
            } else {
                refrenceClientMap.remove(key);
            }
        }

        locks.putIfAbsent(key, new Object());
        synchronized (locks.get(key)) {
            if (refrenceClientMap.containsKey(key)) {
                return refrenceClientMap.get(key);
            }

            ExchangeClient client = initClient(url);
            refrenceClientMap.put(key, client);
            locks.remove(key);
            return client;
        }
    }

    /**
     * 初始化ExchangeClient
     * @param url
     * @return
     */
    private ExchangeClient initClient(URL url) {
        ExchangeClient client;
        try {
            client = ExchangeManage.getExchangeClient(url, dubboRequestHandler);
        } catch (RemotingException e) {
            throw new RpcException("Fail to start client(url: " + url + ") " + e.getMessage(), e);
        }
        return client;
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
