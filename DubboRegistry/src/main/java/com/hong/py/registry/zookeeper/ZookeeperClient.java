package com.hong.py.registry.zookeeper;

import com.hong.py.commonUtils.URL;
import com.hong.py.registry.ChildrenListener;
import com.hong.py.registry.StateListener;

import java.util.List;

public interface ZookeeperClient {

    //ephemeral是否是临时节点
    void create(String path, boolean ephemeral);

    void delete(String path);

    List<String> addChildListener(String path, ChildrenListener listener);

    void removeChildListener(String path, ChildrenListener listener);

    void addStateListener(StateListener listener);

    void removeStateListener(StateListener listener);

    List<String> getChildren(String path);

    boolean isConnected();

    void close();

    URL getUrl();
}
