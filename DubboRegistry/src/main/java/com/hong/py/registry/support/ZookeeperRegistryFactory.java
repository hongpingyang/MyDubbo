package com.hong.py.registry.support;

import com.hong.py.commonUtils.URL;
import com.hong.py.registry.Registry;
import com.hong.py.registry.RegistryFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ZookeeperRegistryFactory implements RegistryFactory {

    private static final Map<String, Registry> registryMap = new ConcurrentHashMap<>();
    private static final ReentrantLock LOCK = new ReentrantLock();

    //会注入
    private ZookeeperTransporter zookeeperTransporter;

    public void setZookeeperTransporter(ZookeeperTransporter zookeeperTransporter) {
        this.zookeeperTransporter = zookeeperTransporter;
    }

    @Override
    public Registry getRegistry(URL url) {

        //是啥意思？
        String key = url.toServiceStringWithoutResolving();
        Registry registry;
        LOCK.lock();
        try {
            registry = registryMap.get(key);
            if (registry == null) {
                registry = createRegistry(url);
                registryMap.put(key, registry);
            }
        } finally {
            LOCK.unlock();
        }
        return registry;
    }

    private Registry createRegistry(URL url) {
        return new ZookeeperRegistry(url, zookeeperTransporter);
    }
}
