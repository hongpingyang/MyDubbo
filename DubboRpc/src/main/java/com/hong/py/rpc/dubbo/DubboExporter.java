package com.hong.py.rpc.dubbo;

import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.rpc.Exporter;
import com.hong.py.rpc.Invoker;

/**
 *  就是把invoke和key包一下
 **/
public class DubboExporter<T> implements Exporter<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final Invoker<T> invoker;
    private final String key;
    private volatile boolean unexported=false;

    public DubboExporter(Invoker<T> invoker, String key) {
        this.key = key;
        this.invoker = invoker;
    }


    @Override
    public Invoker<T> getInvoker() {
        return invoker;
    }

    @Override
    public void unexport() {

    }
}
