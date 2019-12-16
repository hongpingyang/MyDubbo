package com.hong.py.remoting.exchange.support.header;

import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.remoting.Channel;
import com.hong.py.remoting.ChannelHandler;
import com.hong.py.remoting.RemotingException;
import com.hong.py.remoting.exchange.ExchangeHandler;
import com.hong.py.remoting.transport.ChannelHandlerDelegate;

/**
 *
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
        return null;
    }



    @Override
    public void connected(Channel channel) throws RemotingException {

    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {

    }

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {

    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {

    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {

    }
}
