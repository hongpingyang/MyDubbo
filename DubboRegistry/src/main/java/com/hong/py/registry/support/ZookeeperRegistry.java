package com.hong.py.registry.support;

import com.hong.py.commonUtils.URL;
import com.hong.py.registry.NotifyListener;
import com.hong.py.registry.Registry;

import java.util.List;

public class ZookeeperRegistry  implements Registry {

    public ZookeeperRegistry(URL url,ZookeeperTransporter zookeeperTransporter) {

    }

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void register(URL url) {

    }

    @Override
    public void unregister(URL url) {

    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {

    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {

    }

    @Override
    public List<URL> lookup(URL url) {
        return null;
    }
}
