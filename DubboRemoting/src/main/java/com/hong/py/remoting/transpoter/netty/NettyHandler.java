package com.hong.py.remoting.transpoter.netty;

import com.hong.py.commonUtils.URL;
import com.hong.py.remoting.ChannelHandler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

/**
 * 文件描述
 **/
public class NettyHandler extends ChannelDuplexHandler {

    private final URL url;

    private final ChannelHandler client;

    public NettyHandler(URL url, ChannelHandler client) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (client == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.url = url;
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {


    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
