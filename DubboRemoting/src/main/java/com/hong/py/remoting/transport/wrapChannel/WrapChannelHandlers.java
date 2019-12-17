package com.hong.py.remoting.transport.wrapChannel;

import com.hong.py.commonUtils.URL;
import com.hong.py.remoting.ChannelHandler;

/**
 * 包装Channelhandler
 */
public class WrapChannelHandlers {

    private static WrapChannelHandlers Instance = new WrapChannelHandlers();

    protected WrapChannelHandlers() {

    }

    public static WrapChannelHandlers getInstance() {
        return Instance;
    }

    public static ChannelHandler wrap(ChannelHandler channelHandler, URL url) {
        return WrapChannelHandlers.getInstance().wrapInternal(channelHandler,url);
    }

    private ChannelHandler  wrapInternal(ChannelHandler channelHandler, URL url) {
        //todo
        return null;
    }

}
