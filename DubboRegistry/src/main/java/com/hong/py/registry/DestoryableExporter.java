package com.hong.py.registry;

import com.hong.py.commonUtils.ConfigUtils;
import com.hong.py.commonUtils.URL;
import com.hong.py.logger.Logger;
import com.hong.py.logger.LoggerFactory;
import com.hong.py.rpc.Exporter;
import com.hong.py.rpc.Invoker;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DestoryableExporter<T> implements Exporter<T> {
    private final static Logger logger = LoggerFactory.getLogger(RegistryProtocol.class);
    public static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Exporter<T> exporter;
    private Invoker<T> originInvoker;
    private URL subscribeUrl;
    private URL registerUrl;
    private Registry registry;
    private Map<URL, NotifyListener> notifyListenerMap;

    public DestoryableExporter(Exporter<T> exporter, Invoker<T> originInvoker, URL subscribeUrl, URL registerUrl,Registry registry,Map<URL, NotifyListener> notifyListenerMap) {
      this.exporter=exporter;
      this.originInvoker=originInvoker;
      this.subscribeUrl=subscribeUrl;
      this.registerUrl=registerUrl;
      this.registry=registry;
      this.notifyListenerMap=notifyListenerMap;
    }

    @Override
    public Invoker<T> getInvoker() {
        return exporter.getInvoker();
    }

    @Override
    public void unexport() {

        try {
            registry.unregister(registerUrl);
            NotifyListener notifyListener = notifyListenerMap.remove(subscribeUrl);
            registry.unsubscribe(subscribeUrl,notifyListener);
        } catch (Throwable e) {
            logger.warn(e.getMessage(),e);
        }

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    int timeout = ConfigUtils.getServerShutdownTimeout();
                    if (timeout > 0) {
                        logger.info("Waiting " + timeout + "ms for registry to notify all consumers before unexport. Usually, this is called when you use dubbo API");
                        Thread.sleep(timeout);
                    }
                    exporter.unexport();
                } catch (Throwable t) {
                    logger.warn(t.getMessage(), t);
                }
            }
        });
    }
}
