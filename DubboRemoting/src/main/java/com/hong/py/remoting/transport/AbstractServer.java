package com.hong.py.remoting.transport;

import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.remoting.Channel;
import com.hong.py.remoting.ChannelHandler;
import com.hong.py.remoting.RemotingException;
import com.hong.py.remoting.Server;

import java.net.InetSocketAddress;
import java.util.Collection;

public abstract  class AbstractServer extends AbstractPeer implements Server {

    private static final Logger logger = LoggerFactory.getLogger(AbstractServer.class);
    private InetSocketAddress localAddress;
    private InetSocketAddress bindAddress;

    public AbstractServer(URL url, ChannelHandler channelHandler) {
        super(url, channelHandler);
        localAddress = getUrl().toInetSocketAddress();
        String bindIp = getUrl().getParameter(Constants.BIND_IP_KEY, getUrl().getHost());
        int bindPort = getUrl().getParameter(Constants.BIND_PORT_KEY, getUrl().getPort());
        bindAddress = new InetSocketAddress(bindIp, bindPort);

        try {
            doOpen();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    protected abstract void doOpen() throws Throwable;

    protected abstract void doClose() throws Throwable;

    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    @Override
    public void reset(URL url) {

    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        Collection<Channel> channels = getChannels();
        for (Channel channel : channels) {
            if (channel.isConnected()) {
                channel.send(message, sent);
            }
        }
    }
}
