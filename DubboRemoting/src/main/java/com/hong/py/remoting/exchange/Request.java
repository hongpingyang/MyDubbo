package com.hong.py.remoting.exchange;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 请求
 **/
public class Request {

    private static final AtomicLong INVOKE_ID = new AtomicLong(0);

    private final long mId;

    private boolean mTwoWay=true;

    private Object data;

    private boolean broken;

    public Request() {
        mId = newId();
    }
    public Request(long id) {
        mId = id;
    }
    private static long newId() {
        return INVOKE_ID.getAndIncrement();
    }

    public long getmId() {
        return mId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean ismTwoWay() {
        return mTwoWay;
    }

    public void setmTwoWay(boolean mTwoWay) {
        this.mTwoWay = mTwoWay;
    }


    public void setBroken(boolean b) {
        broken=b;
    }

    public boolean isBroken() {
        return broken;
    }
}
