package com.hong.py.remoting.exchange.support.header;

import com.hong.py.commonUtils.StringUtils;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.remoting.Channel;
import com.hong.py.remoting.ChannelHandler;
import com.hong.py.remoting.RemotingException;
import com.hong.py.remoting.exchange.ExchangeChannel;
import com.hong.py.remoting.exchange.ExchangeHandler;
import com.hong.py.remoting.exchange.Request;
import com.hong.py.remoting.exchange.Response;
import com.hong.py.remoting.exchange.support.DefaultFuture;
import com.hong.py.remoting.transport.ChannelHandlerDelegate;

/**
 * 服务端和客户端的  接收到的消息处理类,会wrap真正的处理ExchangeHandler(com.hong.py.rpc.dubbo.DubboRequestHandler)
 * 都会调用HeaderExchangeChannel来处理
 * 有些在这里处理待完成
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
            this.exchangeHandler.connected(exchangeChannel);
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            this.exchangeHandler.disconnected(exchangeChannel);
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            this.exchangeHandler.sent(exchangeChannel, message);
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    /**
     * 处理请求
     * @param exchangeChannel
     * @param request
     * @return
     */
    private Response handleRequest(ExchangeChannel exchangeChannel, Request request) {
        Response response = new Response();
        // find handler by message class.
        Object msg = request.getData();//msg 实际是invocation
        try {
            // handle data.
            Object result = exchangeHandler.reply(exchangeChannel, msg);
            response.setStatus(Response.OK);
            response.setmResult(result); //result 实际是RpcResult
        } catch (Throwable e) {
            response.setStatus(Response.SERVICE_ERROR);
            response.setmErrorMsg(e.getMessage());
        }
        return response;
    }

    /**
     * 处理相应
     * @param channel
     * @param response
     */
    private static void handleResponse(Channel channel, Response response) {
        if (response != null) {
            DefaultFuture.received(channel,response);
        }
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            if (message instanceof Request) {
                Request request = (Request) message;
                if (request.ismTwoWay()) {
                    Response response = handleRequest(exchangeChannel, request);
                    channel.send(response);
                } else {
                    this.exchangeHandler.received(exchangeChannel, ((Request) message).getData());
                }
            } else if (message instanceof Response) {
                Response response = (Response) message;
                handleResponse(channel, response);
            } else {
                this.exchangeHandler.received(exchangeChannel, message);
            }
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            this.exchangeHandler.caught(exchangeChannel, exception);
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }


}
