package com.hong.py.registry.support;

import com.hong.py.annotation.SPI;
import com.hong.py.commonUtils.URL;
import com.hong.py.registry.zookeeper.ZookeeperClient;

@SPI("curator")
public interface ZookeeperTransporter {

    ZookeeperClient connect(URL url);
}
