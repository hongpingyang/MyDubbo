package com.hong.py.remoting.transpoter.netty;

import com.hong.py.commonUtils.URL;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.remoting.Channel;
import com.hong.py.remoting.ChannelHandler;
import com.hong.py.remoting.transport.AbstractClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

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





    }

    @Override
    protected void doClose() throws Throwable {

    }

    @Override
    protected void doConnect() throws Throwable {

    }

    @Override
    protected void doDisConnect() throws Throwable {

    }

    @Override
    protected Channel getChannel() {
        return null;
    }
}
