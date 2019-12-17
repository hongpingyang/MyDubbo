package com.hong.py.remoting.exchange.support.header;

import com.hong.py.commonUtils.URL;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.remoting.ChannelHandler;
import com.hong.py.remoting.Client;
import com.hong.py.remoting.RemotingException;
import com.hong.py.remoting.exchange.ExchangeChannel;
import com.hong.py.remoting.exchange.ExchangeClient;
import com.hong.py.remoting.exchange.ExchangeHandler;
import com.hong.py.remoting.exchange.ResponseFuture;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 *  invoker 里调用的Client
 *  HeaderExchangeClient里调用HeaderExchangeChannel来处理消息发送
 **/
public class HeaderExchangeClient implements ExchangeClient {

    private static final Logger logger = LoggerFactory.getLogger(HeaderExchangeClient.class);

    private final Client client;
    private final ExchangeChannel channel;

    public HeaderExchangeClient(Client client) {
        this.client = client;
        this.channel = new HeaderExchangeChannel(client);
    }

    @Override
    public void reconnect() throws RemotingException {
        client.reconnect();
    }

    @Override
    public ResponseFuture request(Object request) throws RemotingException {
        return channel.request(request);
    }

    @Override
    public ResponseFuture request(Object request, int timeout) throws RemotingException {
        return channel.request(request,timeout);
    }

    @Override
    public void send(Object message) throws RemotingException {
        channel.send(message);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        channel.send(message,sent);
    }

    @Override
    public ExchangeHandler getExchangeHandler() {
        return channel.getExchangeHandler();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return channel.getRemoteAddress();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return channel.getLocalAddress();
    }

    @Override
    public boolean isConnected() {
        return channel.isConnected();
    }

    @Override
    public boolean hasAttribute(String key) {
        return channel.hasAttribute(key);
    }

    @Override
    public Object getAttribute(String key) {
        return channel.getAttribute(key);
    }

    @Override
    public void setAttribute(String key, Object value) {

    }

    @Override
    public void removeAttribute(String key) {

    }

    @Override
    public URL getUrl() {
        return channel.getUrl();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channel.getChannelHandler();
    }


    @Override
    public void close() {

    }

    @Override
    public void close(int timeout) {

    }

    @Override
    public void startClose() {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void reset(URL url) {

    }
}
