package com.hong.py.registry.listener;

import com.hong.py.commonUtils.URL;
import com.hong.py.registry.NotifyListener;
import com.hong.py.rpc.Invoker;

import java.util.List;

public class OverrideNotifyListener implements NotifyListener {

    private final URL subscribeUrl;
    private final Invoker originInvoker;

    public OverrideNotifyListener(URL subscribeUrl, Invoker originalInvoker) {
        this.subscribeUrl = subscribeUrl;
        this.originInvoker = originalInvoker;
    }


    @Override
    public void notify(List<URL> urls) {



    }
}
