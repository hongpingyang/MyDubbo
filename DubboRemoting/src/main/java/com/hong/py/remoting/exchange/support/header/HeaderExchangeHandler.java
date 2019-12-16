package com.hong.py.remoting.exchange.support.header;

import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.remoting.Channel;
import com.hong.py.remoting.ChannelHandler;
import com.hong.py.remoting.RemotingException;
import com.hong.py.remoting.exchange.ExchangeHandler;
import com.hong.py.remoting.transport.ChannelHandlerDelegate;

/**
 * 消息处理类
 */
public class HeaderExchangeHandler implements ChannelHandlerDelegate {

    protected static final Logger logger = LoggerFactory.getLogger(HeaderExchangeHandler.class);

    private final ExchangeHandler exchangeHandler;

    public HeaderExchangeHandler(ExchangeHandler exchangeHandler) {
        if (exchangeHandler == null) {
            throw new IllegalArgumentException("exchaneHandler==null");
        }
        this.exchangeHandler = exchangeHandler;
    }


    @Override
    public ChannelHandler getChannelHandler() {
        return this.exchangeHandler;
    }


    @Override
    public void connected(Channel channel) throws RemotingException {
        this.exchangeHandler.connected(channel);
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
        this.exchangeHandler.disconnected(channel);
    }

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {
        this.exchangeHandler.sent(channel,message);
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        this.exchangeHandler.received(channel,message);
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        this.exchangeHandler.caught(channel,exception);
    }


}
