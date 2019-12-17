package com.hong.py.remoting.exchange;

/**
 * 响应
 *
 **/
public class Response {

    private long mId;

    /**
     * ok.
     */
    public static final byte OK = 20;

    /**
     * request format error.
     */
    public static final byte BAD_REQUEST = 40;

    private byte mStatus=OK;

    private Object mResult;

    private String mErrorMsg;


    public Response() {
    }

    public Response(long id) {
        mId = id;
    }

    public long getmId() {
        return mId;
    }

    public byte getStatus() {
        return mStatus;
    }

    public void setStatus(byte status) {
        mStatus = status;
    }

    public Object getmResult() {
        return mResult;
    }

    public void setmResult(Object mResult) {
        this.mResult = mResult;
    }

    public String getmErrorMsg() {
        return mErrorMsg;
    }

    public void setmErrorMsg(String mErrorMsg) {
        this.mErrorMsg = mErrorMsg;
    }
}
