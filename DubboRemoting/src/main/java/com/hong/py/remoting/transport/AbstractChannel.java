package com.hong.py.remoting.transport;

import com.hong.py.commonUtils.URL;
import com.hong.py.remoting.Channel;
import com.hong.py.remoting.ChannelHandler;

/**
 *
 **/
public abstract class AbstractChannel extends AbstractPeer implements Channel {


    public AbstractChannel(URL url, ChannelHandler channelHandler) {
        super(url, channelHandler);
    }

}
