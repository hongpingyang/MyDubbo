package com.hong.py.registry;

import com.hong.py.commonUtils.URL;
import com.hong.py.rpc.Exporter;
import com.hong.py.rpc.Invoker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DestoryableExporter<T> implements Exporter<T> {

    public static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Exporter<T> exporter;
    private Invoker<T> originInvoker;
    private URL subscribeUrl;
    private URL registerUrl;
    private Registry registry;

    public DestoryableExporter(Exporter<T> exporter, Invoker<T> originInvoker, URL subscribeUrl, URL registerUrl,Registry registry) {
      this.exporter=exporter;
      this.originInvoker=originInvoker;
      this.subscribeUrl=subscribeUrl;
      this.registerUrl=registerUrl;
      this.registry=registry;
    }

    @Override
    public Invoker<T> getInvoker() {
        return exporter.getInvoker();
    }

    @Override
    public void unexport() {

        try {
            registry.unregister(registerUrl);
        } catch (Throwable e) {

        }


    }
}
