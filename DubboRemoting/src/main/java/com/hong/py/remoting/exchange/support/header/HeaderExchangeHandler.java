package com.hong.py.remoting.exchange.support.header;

import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.remoting.Channel;
import com.hong.py.remoting.ChannelHandler;
import com.hong.py.remoting.RemotingException;
import com.hong.py.remoting.exchange.ExchangeChannel;
import com.hong.py.remoting.exchange.ExchangeHandler;
import com.hong.py.remoting.transport.ChannelHandlerDelegate;

/**
 * 服务端和客户端的  消息处理类，会wrap真正的处理ExchangeHandler
 * 都会调用HeaderExchangeChannel来处理
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
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            this.exchangeHandler.connected(channel);
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            this.exchangeHandler.disconnected(channel);
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            this.exchangeHandler.sent(channel,message);
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {

            this.exchangeHandler.received(channel,message);
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            this.exchangeHandler.caught(channel,exception);
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }


}
