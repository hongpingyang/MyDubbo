package com.hong.py.remoting.transport;

import com.hong.py.common.dataStore.DataStore;
import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.NetUtils;
import com.hong.py.commonUtils.URL;
import com.hong.py.extension.ExtensionLoader;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;

import com.hong.py.remoting.Channel;
import com.hong.py.remoting.ChannelHandler;
import com.hong.py.remoting.Client;
import com.hong.py.remoting.RemotingException;

import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 文件描述
 *
 **/
public abstract class AbstractClient extends AbstractEndPoint implements Client {

    private static final Logger logger = LoggerFactory.getLogger(AbstractClient.class);
    private final Lock connectLock = new ReentrantLock();
    private volatile ExecutorService executor;

    public AbstractClient(URL url, ChannelHandler channelHandler) {
        super(url, channelHandler);

        try {
            doOpen();
        } catch (Throwable throwable) {
            close();
        }
        try {
            doConnect();
        } catch (Throwable throwable) {
            close();
        }

        executor = (ExecutorService) ExtensionLoader.getExtensionLoader(DataStore.class)
                .getDefaultExtension().get(Constants.CONSUMER_SIDE, Integer.toString(url.getPort()));
    }

    //创建线程池
    protected ExecutorService createExecutor() {
        return Executors.newCachedThreadPool();
    }

    /**
     * Open client.
     *
     * @throws Throwable
     */
    protected abstract void doOpen() throws Throwable;

    /**
     * Close client.
     *
     * @throws Throwable
     */
    protected abstract void doClose() throws Throwable;

    /**
     * Connect to server.
     *
     * @throws Throwable
     */
    protected abstract void doConnect() throws Throwable;

    /**
     * disConnect to server.
     *
     * @throws Throwable
     */
    protected abstract void doDisConnect() throws Throwable;

    /**
     * Get the connected channel.
     * 获取 NettyClient-> NettyChannel->io.netty.Channel
     *
     * @return channel
     */
    protected abstract Channel getChannel();


    @Override
    public void reconnect() throws RemotingException {
        disconnect();
        connect();
    }

    @Override
    public void reset(URL url) {

    }

    public InetSocketAddress getConnectAddress() {
        return new InetSocketAddress(NetUtils.filterLocalHost(getUrl().getHost()), getUrl().getPort());
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        Channel channel = getChannel();
        return channel.getRemoteAddress();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        Channel channel = getChannel();
        return channel.getLocalAddress();
    }

    @Override
    public boolean isConnected() {
        Channel channel = getChannel();
        if(channel==null) return false;
        return channel.isConnected();
    }

    @Override
    public boolean hasAttribute(String key) {
        Channel channel = getChannel();
        if(channel==null) return false;
        return channel.hasAttribute(key);
    }

    @Override
    public Object getAttribute(String key) {
        Channel channel = getChannel();
        if(channel==null) return null;
        return channel.getAttribute(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        Channel channel = getChannel();
        if(channel==null) return;
        channel.setAttribute(key,value);
    }

    @Override
    public void removeAttribute(String key) {
        Channel channel = getChannel();
        if(channel==null) return;
        channel.removeAttribute(key);
    }

    /**
     * 发送消息
     * @param message
     * @param sent    already sent to socket?
     * @throws RemotingException
     */
    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        if (!isConnected()) {
            connect();
        }

        Channel channel = getChannel();

        if (channel == null || !channel.isConnected()) {
            throw new RemotingException(this, "message can not send, because channel is closed . url:" + getUrl());
        }

        channel.send(message, sent);
    }


    protected void connect() throws RemotingException {
        connectLock.lock();
        if(isConnected()) return;

        try {
            doConnect();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        finally {
            connectLock.unlock();
        }
    }

    //关闭连接
    public void disconnect() {
        connectLock.lock();
        try {
            try {
                Channel channel = getChannel();
                if (channel != null) {
                    channel.close();
                }
            } catch (Throwable e) {
                logger.warn(e.getMessage(), e);
            }

            try {
                doDisConnect();
            } catch (Throwable e) {
                logger.warn(e.getMessage(), e);
            }
        } finally {
            connectLock.unlock();
        }
    }


    @Override
    public void close() {
        try {
            if (executor != null) {
                //停止线程
                //ExecutorUtil.shutdownNow(executor, 100);
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }

        try {
            super.close();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }

        try {
            disconnect();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            doClose();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }



}
