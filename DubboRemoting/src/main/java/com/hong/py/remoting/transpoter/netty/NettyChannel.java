package com.hong.py.remoting.transpoter.netty;

import com.hong.py.commonUtils.URL;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.remoting.ChannelHandler;
import com.hong.py.remoting.RemotingException;
import com.hong.py.remoting.transport.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 文件描述
 * NettyChannel是io.netty.channel的管理类
 * send是最终的发送消息
 **/
public class NettyChannel extends AbstractChannel {

    private static final Logger logger = LoggerFactory.getLogger(NettyChannel.class);

    //NettyChannel集合
    private static final ConcurrentMap<Channel, NettyChannel> channelMap = new ConcurrentHashMap<Channel, NettyChannel>();

    private final Channel channel;

    //绑定HeadExchangeChannel
    private final Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    public NettyChannel(Channel channel,URL url, ChannelHandler channelHandler) {
        super(url, channelHandler);
        this.channel=channel;
    }

    public static NettyChannel  getOrAddChannel(Channel channel, URL url, ChannelHandler handler) {

         if(channel==null) return null;
         NettyChannel ret=channelMap.get(channel);

         if (ret == null) {
            NettyChannel nettyChannel = new NettyChannel(channel,url,handler);
            if (channel.isActive()) {
                ret = channelMap.putIfAbsent(channel, nettyChannel);
            }
            if (ret == null) {
                ret = nettyChannel;
            }
         }
        return ret;
    }


    public static void removeChannelIfDisconnected(Channel channel) {
        if (channel != null && !channel.isActive()) {
            channelMap.remove(channel);
        }
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress)channel.remoteAddress();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) channel.localAddress();
    }

    @Override
    public boolean isConnected() {
        return channel.isActive();
    }

    @Override
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        attributes.putIfAbsent(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        attributes.remove(key);
    }


    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        ChannelFuture future = channel.writeAndFlush(message);

    }

    @Override
    public void close() {
        super.close();
        removeChannelIfDisconnected(channel);
        attributes.clear();
        channel.close();
    }
}
