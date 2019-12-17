package com.hong.py.remoting.transport;

import com.hong.py.remoting.ChannelHandler;

public interface ChannelHandlerDelegate  extends ChannelHandler {

    /**
     * 获取Handler
     * @return
     */
    ChannelHandler getChannelHandler();
}
