package com.hong.py.remoting.transport;

import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.remoting.Channel;
import com.hong.py.remoting.ChannelHandler;
import com.hong.py.remoting.Endpoint;
import com.hong.py.remoting.RemotingException;

import java.net.InetSocketAddress;

/**
 * 文件描述
 * channelHandler为com.hong.py.remoting.exchange.support.header.HeaderExchangeHandler
 **/
public abstract class AbstractPeer implements Endpoint,ChannelHandler {

    private final  ChannelHandler channelHandler;

    private final  URL url;

    private volatile boolean closed=false;
    private volatile boolean closeing=false;

    public AbstractPeer(URL url,ChannelHandler channelHandler) {

        if (url == null) {
            throw new IllegalArgumentException("url==null");
        }
        if (channelHandler == null) {
            throw new IllegalArgumentException("channelHandler==null");
        }
        this.url=url;
        this.channelHandler=channelHandler;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }


    @Override
    public void send(Object message) throws RemotingException {
       send(message,url.getParameter(Constants.SENT_KEY,false));
    }

    @Override
    public void close() {
        closed=true;
    }

    @Override
    public void close(int timeout) {
        close();
    }

    public boolean isCloseing() {
        return closeing;
    }

    @Override
    public void startClose() {
        if(isClosed())
            return;
       closeing=true;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void connected(Channel channel) throws RemotingException {
        channelHandler.connected(channel);
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
        channelHandler.disconnected(channel);
    }

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {
        channelHandler.sent(channel,message);
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        channelHandler.received(channel,message);
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        channelHandler.caught(channel,exception);
    }
}
