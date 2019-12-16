package com.hong.py.remoting.transpoter.netty;

import com.hong.py.commonUtils.URL;
import com.hong.py.remoting.ChannelHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

/**
 * 文件描述
 *
 **/
public class NettyInitializer extends ChannelInitializer<Channel> {

    private NettyCodecAdapter adapter;

    public NettyInitializer(NettyCodecAdapter adapter) {
        this.adapter=adapter;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline()
                .addLast("decoder", adapter.getDecoder())
                .addLast("encoder", adapter.getEncoder())
                .addLast("handler", new NettyHandler());

    }
}
