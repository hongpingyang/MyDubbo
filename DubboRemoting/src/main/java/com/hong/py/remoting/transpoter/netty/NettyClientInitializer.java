package com.hong.py.remoting.transpoter.netty;

import com.hong.py.commonUtils.URL;
import com.hong.py.remoting.ChannelHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 *
 **/
public class NettyClientInitializer extends ChannelInitializer<Channel> {

    //private NettyCodecAdapter adapter;
    /* public NettyClientInitializer(NettyCodecAdapter adapter) {
        this.adapter=adapter;
    }
    */
    private final URL url;

    private final ChannelHandler client;

    public NettyClientInitializer(URL url, ChannelHandler client) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (client == null) {
            throw new IllegalArgumentException("client == null");
        }
        this.url = url;
        this.client = client;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        ByteBuf delimiter = Unpooled.copiedBuffer("$_$".getBytes());
        channel.pipeline()
                //.addLast("decoder", adapter.getDecoder())
                //.addLast("encoder", adapter.getEncoder())
                .addLast("delimiterBasedFrameDecoder", new DelimiterBasedFrameDecoder(4096, delimiter))
                .addLast("byteArrayDecoder", new ByteArrayDecoder())
                .addLast("byteArrayEncoder", new ByteArrayEncoder())
                .addLast("handler", new NettyClientHandler(url,client));

    }
}
