package com.hong.py.remoting.transpoter.netty;

import com.hong.py.commonUtils.URL;
import com.hong.py.remoting.ChannelHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

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
        channel.pipeline()
                //.addLast("decoder", adapter.getDecoder())
                //.addLast("encoder", adapter.getEncoder())
                .addLast("handler", new NettyClientHandler(url,client));

    }
}
