package com.hong.py.registry.support;

import com.hong.py.annotation.Adaptive;
import com.hong.py.annotation.SPI;
import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.registry.zookeeper.ZookeeperClient;

@SPI("curator")
public interface ZookeeperTransporter {

    @Adaptive({Constants.CLIENT_KEY, Constants.TRANSPORTER_KEY})
    ZookeeperClient connect(URL url);
}
