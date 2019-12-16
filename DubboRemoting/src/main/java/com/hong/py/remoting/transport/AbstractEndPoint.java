package com.hong.py.remoting.transport;

import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.extension.ExtensionLoader;
import com.hong.py.remoting.ChannelHandler;
import com.hong.py.remoting.Client;
import com.hong.py.remoting.Codec;
import com.hong.py.remoting.Codec2;

/**
 * 文件描述
 *
 **/
public abstract class AbstractEndPoint extends AbstractPeer {

    private Codec2 codec;

    private int timeout;

    private int connectTimeout;

    public AbstractEndPoint(URL url, ChannelHandler channelHandler) {
        super(url, channelHandler);
        this.codec=getChannelCodec(url);
        this.timeout = url.getPositiveParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
        this.connectTimeout = url.getPositiveParameter(Constants.CONNECT_TIMEOUT_KEY, Constants.DEFAULT_CONNECT_TIMEOUT);
    }

    protected static Codec2 getChannelCodec(URL url) {
        String codecName = url.getParameter(Constants.CODEC_KEY, "telnet");
        return ExtensionLoader.getExtensionLoader(Codec2.class).getExtension(codecName);
    }

    protected Codec2 getCodec() {
        return codec;
    }

    protected int getTimeout() {
        return timeout;
    }

    protected int getConnectTimeout() {
        return connectTimeout;
    }








}
