package com.hong.py.remoting.transpoter.netty;

import com.hong.py.commonUtils.URL;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.remoting.ChannelHandler;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 文件描述
 *NettyChannel是channel的管理类
 **/
public class NettyChannel {

    private static final Logger logger = LoggerFactory.getLogger(NettyChannel.class);

    //NettyChannel集合
    private static final ConcurrentMap<Channel, NettyChannel> channelMap = new ConcurrentHashMap<Channel, NettyChannel>();

    private final Channel channel;

    private final Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    public NettyChannel(Channel channel) {
        this.channel = channel;
    }

    public static NettyChannel  getOrAddChannel(Channel channel, URL url, ChannelHandler handler) {
         if(channel==null) return null;
         NettyChannel ret=channelMap.get(channel);

        if (ret == null) {
            NettyChannel nettyChannel = new NettyChannel(channel);
            if (channel.isActive()) {
                ret = channelMap.putIfAbsent(channel, nettyChannel);
            }
            if (ret == null) {
                ret = nettyChannel;
            }
        }
        return ret;
    }










}
