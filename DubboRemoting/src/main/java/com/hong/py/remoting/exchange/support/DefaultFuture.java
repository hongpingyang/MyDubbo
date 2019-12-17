package com.hong.py.remoting.exchange.support;

import com.hong.py.commonUtils.Constants;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.remoting.Channel;
import com.hong.py.remoting.RemotingException;
import com.hong.py.remoting.exchange.Request;
import com.hong.py.remoting.exchange.Response;
import com.hong.py.remoting.exchange.ResponseCallback;
import com.hong.py.remoting.exchange.ResponseFuture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 保留请求等待返回
 *
 * @ProductName: HONGPY
 * @ProjectName: MyDubbo
 * @Package: com.hong.py.remoting.exchange.support
 * @Description: note
 * @Author: hongpy21691
 * @CreateDate: 2019/12/17 15:57
 * @UpdateUser: hongpy21691
 * @UpdateDate: 2019/12/17 15:57
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2019 hongpy Technologies Inc. All Rights Reserved
 **/
public class DefaultFuture implements ResponseFuture {

    private static final Logger logger = LoggerFactory.getLogger(DefaultFuture.class);

    private static final Map<Long, Channel> CHANNELS = new ConcurrentHashMap<>();

    private static final Map<Long, DefaultFuture> FUTURES = new ConcurrentHashMap<>();

    private long id;
    private final  Channel channel;
    private final  Request request;
    private final int timeout;
    private final  Lock lock = new ReentrantLock();
    private final Condition done = lock.newCondition();
    private long start = System.currentTimeMillis();
    private volatile Response response;


    public DefaultFuture(Channel channel, Request request,int timeOut) {
        this.id=request.getmId();
        this.channel=channel;
        this.request=request;
        this.timeout=timeOut;

        CHANNELS.put(id, channel);
        FUTURES.put(id, this);
    }

    //是否存在DefaultFuture
    public static DefaultFuture getFuture(long id) {
        return FUTURES.get(id);
    }

    //channel是否还有Future
    public static boolean hasFuture(Channel channel) {
        return CHANNELS.containsValue(channel);
    }

    //收到了回复
    public static void received(Channel channel, Response response) {
        DefaultFuture future = FUTURES.remove(response.getmId());
        if (future != null) {
            future.doReceived(response);
        }
        CHANNELS.remove(response.getmId());
    }

    //处理接收
    private void doReceived(Response response) {
        lock.lock();
        try {
            this.response=response;
            if (done != null) {
                done.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Object get() throws RemotingException {
        return get(timeout);
    }

    //获取结果
    @Override
    public Object get(int timeoutInMillis) throws RemotingException {
        if (timeoutInMillis <= 0) {
            timeoutInMillis = Constants.DEFAULT_TIMEOUT;
        }

        if(!isDone())
        {
            long start=System.currentTimeMillis();
            lock.lock();
            try {
                while (!isDone()) {
                    done.await(timeoutInMillis, TimeUnit.MILLISECONDS);
                    if (isDone() || System.currentTimeMillis() - start > timeoutInMillis) {
                           break;
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
            if (!isDone()) {
                //报超时异常
                //throw new TimeoutException(sent > 0, channel, getTimeoutMessage(false));
            }
        }

        return returnFromresponse();
    }

    private Object returnFromresponse() throws RemotingException {
        Response res=response;
        if (res == null) {
            throw new IllegalStateException("response==null");
        }
        if (res.getStatus() == Response.OK) {
            return res.getmResult();
        }

        throw new RemotingException(channel, res.getmErrorMsg());
    }

    //出现异常取消
    public void cancle() {
        Response errorResult = new Response(id);
        errorResult.setmErrorMsg("request future has been canceled.");
        response = errorResult;
        FUTURES.remove(id);
        CHANNELS.remove(id);
    }

    @Override
    public void setCallback(ResponseCallback callback) {

    }

    @Override
    public boolean isDone() {
        return response!=null;
    }
}
