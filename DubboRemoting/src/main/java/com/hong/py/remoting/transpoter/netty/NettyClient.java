package com.hong.py.remoting.transpoter.netty;

import com.hong.py.commonUtils.URL;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;

import com.hong.py.remoting.ChannelHandler;
import com.hong.py.remoting.transport.AbstractClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.TimeUnit;

/**
 * 文件描述
 *
 **/
public class NettyClient extends AbstractClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private volatile Channel channel;

    private Bootstrap  bootstrap;

    private static EventLoopGroup eventGroup = new NioEventLoopGroup();

    public NettyClient(URL url, ChannelHandler channelHandler) {
        super(url, channelHandler);
    }

    @Override
    protected void doOpen() throws Throwable {

        bootstrap = new Bootstrap();
        bootstrap.group(eventGroup)
                  .channel(NioSocketChannel.class);
        bootstrap.handler(new NettyClientInitializer(getUrl(),this));
    }

    @Override
    protected void doConnect() throws Throwable {
        //getConnectAddress 获取连接地址
        ChannelFuture future = bootstrap.connect(getConnectAddress());
        try {
            boolean b = future.awaitUninterruptibly(getConnectTimeout(), TimeUnit.MILLISECONDS);
            if (b && future.isSuccess()) {
                Channel newChannel = future.channel();
                  //移除老的channel
                Channel oldChannel = NettyClient.this.channel;
                if (oldChannel != null) {
                    try {
                        oldChannel.close();
                    } finally {
                        NettyChannel.removeChannelIfDisconnected(oldChannel);
                    }
                }
                NettyClient.this.channel = newChannel;
            }
        } finally {

        }
    }

    @Override
    protected void doDisConnect() throws Throwable {
        try {
            NettyChannel.removeChannelIfDisconnected(channel);
        }catch (Throwable t) {
            logger.warn(t.getMessage());
        }
    }

    @Override
    protected void doClose() throws Throwable {

    }

    @Override
    protected com.hong.py.remoting.Channel getChannel() {
        Channel c=channel;
        if (c == null || !c.isActive()) {
            return null;
        }

        return NettyChannel.getOrAddChannel(c,getUrl(),this);
    }
}
