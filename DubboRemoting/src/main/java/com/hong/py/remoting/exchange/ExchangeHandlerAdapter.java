package com.hong.py.remoting.exchange;

import com.hong.py.remoting.RemotingException;

/**
 * 适配器类，所有子类为真正的消息处理类
 */
public abstract class ExchangeHandlerAdapter implements ExchangeHandler {

    @Override
    public Object reply(ExchangeChannel channel, Object request) throws RemotingException {
        return null;
    }
}
