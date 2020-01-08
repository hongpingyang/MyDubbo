package com.hong.py.remoting.transpoter.netty;

import com.hong.py.commonUtils.NetUtils;
import com.hong.py.commonUtils.URL;
import com.hong.py.remoting.Channel;
import com.hong.py.remoting.ChannelHandler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyServerHandler extends ChannelDuplexHandler {

    //所有连接
    //当硬件与服务器建立一条链接（channel），我们将活动链接存储到Map中，失效的链接则从map中移除。
    private final Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>(); // <ip:port, channel>

    private final URL url;

    private final ChannelHandler server;

    public NettyServerHandler(URL url, ChannelHandler server) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (server == null) {
            throw new IllegalArgumentException("server == null");
        }
        this.url = url;
        this.server = server;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, server);
        try {
            if (channel != null) {
                channels.put(NetUtils.toAddressString((InetSocketAddress) ctx.channel().remoteAddress()), channel);
            }
            server.connected(channel);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, server);
        try {
            if (channel != null) {
                channels.remove(NetUtils.toAddressString((InetSocketAddress) ctx.channel().remoteAddress()), channel);
            }
            server.disconnected(channel);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, server);
        try {
            server.received(channel, msg);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, server);
        try {
            server.sent(channel, msg);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.channelRead(ctx, cause);
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, server);
        try {
            server.caught(channel, cause);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }
}
