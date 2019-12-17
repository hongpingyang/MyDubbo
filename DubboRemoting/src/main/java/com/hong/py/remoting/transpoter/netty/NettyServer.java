package com.hong.py.remoting.transpoter.netty;

import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.NetUtils;
import com.hong.py.commonUtils.URL;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.remoting.Channel;
import com.hong.py.remoting.ChannelHandler;
import com.hong.py.remoting.Server;
import com.hong.py.remoting.transport.AbstractServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class NettyServer extends AbstractServer implements Server {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    //所有的连接
    private Map<String, Channel> channelMap;

    private ServerBootstrap serverBootstrap;

    private io.netty.channel.Channel channel;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyServer(URL url, ChannelHandler channelHandler) {
        super(url, channelHandler);
    }

    @Override
    protected void doOpen() throws Throwable {
        serverBootstrap = new ServerBootstrap();

        bossGroup=new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(getUrl().getPositiveParameter(Constants.IO_THREADS_KEY, Constants.DEFAULT_IO_THREADS));

        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new NettyServerInitializer(getUrl(), this));

        ChannelFuture channelFuture = serverBootstrap.bind(getBindAddress());
        channelFuture.syncUninterruptibly();
        channel = channelFuture.channel();
    }

    @Override
    protected void doClose() throws Throwable {
        if (channel != null) {
            channel.close();
        }
        Collection<Channel> channels = getChannels();
        for (Channel channel : channels) {
            channel.close();
        }
        if (serverBootstrap != null) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        if (channelMap != null) {
            channelMap.clear();
        }
    }

    @Override
    public boolean isBound() {
        return channel.isActive();
    }

    @Override
    public Collection<Channel> getChannels() {
        Collection<Channel> chs = new HashSet<Channel>();
        for (Channel channel : this.channelMap.values()) {
            if (channel.isConnected()) {
                chs.add(channel);
            } else {
                channelMap.remove(NetUtils.toAddressString(channel.getRemoteAddress()));
            }
        }
        return chs;
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        return this.channelMap.get(NetUtils.toAddressString(remoteAddress));
    }
}


