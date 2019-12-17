package com.hong.py.remoting.transpoter.netty;

import com.hong.py.commonUtils.URL;
import com.hong.py.remoting.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyServerInitializer extends ChannelInitializer<NioSocketChannel> {

    private final URL url;

    private final ChannelHandler server;

    public NettyServerInitializer(URL url, ChannelHandler server) {
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
    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
        nioSocketChannel.pipeline()
                .addLast("handler", new NettyServerHandler(url,server));
    }
}
