package com.hong.py.registry.zookeeper;

import com.hong.py.commonUtils.URL;
import com.hong.py.registry.support.ZookeeperTransporter;

public class CuratorZookeeperTransporter implements ZookeeperTransporter {

    @Override
    public ZookeeperClient connect(URL url) {
        return new CuratorZookeeperClient(url);
    }
}
