package com.hong.py.remoting.exchange;

import com.hong.py.remoting.Channel;
import com.hong.py.remoting.RemotingException;

/**
 * 适配器类，所有子类为真正的消息处理类
 */
public abstract class ExchangeHandlerAdapter implements ExchangeHandler {

    @Override
    public Object reply(ExchangeChannel channel, Object request) throws RemotingException {
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
    public String telnet(Channel channel, String message) throws RemotingException {
        return null;
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {

    }

}
