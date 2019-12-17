package com.hong.py.remoting.exchange.support.header;

import com.hong.py.commonUtils.URL;
import com.hong.py.remoting.Channel;
import com.hong.py.remoting.ChannelHandler;
import com.hong.py.remoting.RemotingException;
import com.hong.py.remoting.Server;
import com.hong.py.remoting.exchange.ExchangeChannel;
import com.hong.py.remoting.exchange.ExchangeServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;

/**
 * invoker里调用的server
 * HeaderExchangeServer 内部调用 HeaderExchangerChannel
 * 内部server 为 nettyServer
 */
public class HeaderExchangeServer implements ExchangeServer {

    private final Server server;

    public HeaderExchangeServer(Server server) {
        if (server == null) {
            throw new IllegalArgumentException("server == null");
        }
        this.server = server;
    }

    public Server getServer() {
        return this.server;
    }

    @Override
    public Collection<ExchangeChannel> getExchangeChannels() {
        Collection<ExchangeChannel> exchangeChannels = new ArrayList<>();
        Collection<Channel> channels = server.getChannels();
        if (channels != null && !channels.isEmpty()) {
            for (Channel channel : channels) {
                exchangeChannels.add(new HeaderExchangeChannel(channel));
            }
        }
        return exchangeChannels;
    }

    @Override
    public ExchangeChannel getExchangeChannel(InetSocketAddress remoteAddress) {
        Channel channel = server.getChannel(remoteAddress);
        return new HeaderExchangeChannel(channel);
    }

    @Override
    public boolean isBound() {
        return server.isBound();
    }

    @Override
    public Collection<Channel> getChannels() {
        return (Collection)getExchangeChannels();
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        return getExchangeChannel(remoteAddress);
    }

    @Override
    public URL getUrl() {
        return server.getUrl();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return server.getChannelHandler();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return server.getLocalAddress();
    }

    @Override
    public void send(Object message) throws RemotingException {
        server.send(message);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
       server.send(message,sent);
    }

    @Override
    public void reset(URL url) {

    }

    @Override
    public void close() {

    }

    @Override
    public void close(int timeout) {

    }

    @Override
    public void startClose() {
       server.startClose();
    }

    @Override
    public boolean isClosed() {
        return server.isClosed();
    }
}
