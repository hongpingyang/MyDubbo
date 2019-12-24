package com.hong.py.rpc.dubbo;

import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.remoting.Channel;
import com.hong.py.remoting.RemotingException;
import com.hong.py.remoting.exchange.ExchangeChannel;
import com.hong.py.remoting.exchange.ExchangeHandlerAdapter;
import com.hong.py.rpc.Invocation;
import com.hong.py.rpc.Invoker;
import com.hong.py.rpc.RpcInvocation;

/**
 * 回复处理
 **/
public class DubboRequestHandler extends ExchangeHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(DubboRequestHandler.class);
    private DubboProtocol dubboProtocol;

    public DubboRequestHandler(DubboProtocol protocol) {
        dubboProtocol=protocol;
    }

    @Override
    public Object reply(ExchangeChannel channel, Object request) throws RemotingException {
        if (request instanceof Invocation) {
             Invocation invocation=(Invocation)request;
             Invoker<?> invoker = getInvoker(channel, invocation);
             //RpcContext.getContext().setRemoteAddress(channel.getRemoteAddress());
             return invoker.invoke(invocation);
        }
        throw new RemotingException(channel, "Unsupported request");
    }

    private Invoker<?> getInvoker(Channel channel,Invocation invocation) throws RemotingException {
        return dubboProtocol.getInvoker(channel,invocation);
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        if (message instanceof Invocation) {
            reply((ExchangeChannel)channel, message);
        }
    }

    //会调用到接口里的connected方法
    @Override
    public void connected(Channel channel) throws RemotingException {
        invoke(channel, Constants.ON_CONNECT_KEY);
    }

    //会调用接口里的disconnected方法
    @Override
    public void disconnected(Channel channel) throws RemotingException {
        invoke(channel, Constants.ON_DISCONNECT_KEY);
    }


    private void invoke(Channel channel, String methodKey) {
        Invocation invocation = createInvocation(channel.getUrl(), methodKey);
        if (invocation != null) {
            try {
                received(channel, invocation);
            } catch (Throwable t) {
                logger.warn("Failed to invoke event method " + invocation.getMethodName() + "(), cause: " + t.getMessage(), t);
            }
        }
    }

    private Invocation createInvocation(URL url, String methodKey) {
        String methodName = url.getParameter(methodKey);
        if (methodName == null || methodName.isEmpty()) {
            return null;
        }
        RpcInvocation rpcInvocation = new RpcInvocation(methodName, new Class<?>[0], new Object[0], null, null);
        rpcInvocation.setAttachment(Constants.PATH_KEY, url.getPath());
        rpcInvocation.setAttachment(Constants.INTERFACE_KEY, url.getParameter(Constants.INTERFACE_KEY));
        return rpcInvocation;
    }


}
