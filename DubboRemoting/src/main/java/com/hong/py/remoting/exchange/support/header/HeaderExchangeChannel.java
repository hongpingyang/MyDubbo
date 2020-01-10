package com.hong.py.remoting.exchange.support.header;

import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.remoting.Channel;
import com.hong.py.remoting.ChannelHandler;
import com.hong.py.remoting.RemotingException;
import com.hong.py.remoting.exchange.*;
import com.hong.py.remoting.exchange.support.DefaultFuture;

import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * 在HeaderExchangeClient构造里被构造
 * HeaderExchangeChannel会调用NettyChannel来进行真正的发送
 **/
public class HeaderExchangeChannel implements ExchangeChannel {

    private static final Logger logger = LoggerFactory.getLogger(HeaderExchangeChannel.class);

    private static final String CHANNEL_KEY = HeaderExchangeChannel.class.getName() + ".CHANNEL";

    private final Channel channel;

    public HeaderExchangeChannel(Channel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("channel == null");
        }
        this.channel=channel;
    }

    public static ExchangeChannel getOrAddChannel(Channel channel) {
        if(channel==null)
            return null;
        HeaderExchangeChannel headerExchangeChannel = (HeaderExchangeChannel) channel.getAttribute(CHANNEL_KEY);
        if (headerExchangeChannel == null) {
            headerExchangeChannel = new HeaderExchangeChannel(channel);
            channel.setAttribute(CHANNEL_KEY,headerExchangeChannel);
        }
       return headerExchangeChannel;
    }

    public static void removeChannelIfDisconnected(Channel channel) {
        if (channel != null && !channel.isConnected()) {
            channel.removeAttribute(CHANNEL_KEY);
        }
    }

    @Override
    public void send(Object message) throws RemotingException {
        send(message, getUrl().getParameter(Constants.SENT_KEY, false));
    }

    //send是直接发送，默认是oneway
    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        if (message instanceof Request||
                message instanceof Response||
                message instanceof String) {
            channel.send(message,sent);
        }else {
            Request request = new Request();
            //request.setVersion(Version.getProtocolVersion());
            request.setmTwoWay(false);
            request.setData(message);
            channel.send(request, sent);
        }
    }

    @Override
    public ResponseFuture request(Object request) throws RemotingException {
        return request(request, getUrl().getPositiveParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT));
    }

    //处理请求 寄存到DefaultFuture里，等待响应 默认是twoway
    @Override
    public ResponseFuture request(Object request, int timeout) throws RemotingException {
        Request req = new Request();
        req.setData(request); //request 实际是invocation
        req.setmTwoWay(true); //
        DefaultFuture future = new DefaultFuture(channel, req, timeout);
        try {
            channel.send(req);
        } catch (RemotingException ex) {
            future.cancle();
            throw ex;
        }
        return future;
    }

    @Override
    public ExchangeHandler getExchangeHandler() {

        return (ExchangeHandler) this.channel.getChannelHandler();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return this.channel.getChannelHandler();
    }

    @Override
    public URL getUrl() {
        return channel.getUrl();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return channel.getLocalAddress();
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
    public InetSocketAddress getRemoteAddress() {
        return channel.getRemoteAddress();
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
        channel.setAttribute(key,value);
    }

    @Override
    public void removeAttribute(String key) {
        channel.removeAttribute(key);
    }
}
