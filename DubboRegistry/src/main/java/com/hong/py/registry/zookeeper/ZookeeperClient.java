package com.hong.py.registry.zookeeper;

import com.hong.py.commonUtils.URL;

import java.util.List;

public interface ZookeeperClient {

    //ephemeral是否是临时节点
    void create(String path, boolean ephemeral);

    void delete(String path);

    List<String> getChildren(String path);

    boolean isConnected();

    void close();

    URL getUrl();
}
