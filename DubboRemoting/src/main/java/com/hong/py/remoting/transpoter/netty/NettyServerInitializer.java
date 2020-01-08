package com.hong.py.remoting.transpoter.netty;

import com.hong.py.commonUtils.URL;
import com.hong.py.remoting.ChannelHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

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
        ByteBuf delimiter = Unpooled.copiedBuffer("$_$".getBytes());
        nioSocketChannel.pipeline()
                 .addLast("delimiterBasedFrameDecoder", new DelimiterBasedFrameDecoder(4096, delimiter))
                 .addLast("byteArrayDecoder", new ByteArrayDecoder())
                 .addLast("byteArrayEncoder", new ByteArrayEncoder())
                 .addLast("handler", new NettyServerHandler(url, server));
    }
}
