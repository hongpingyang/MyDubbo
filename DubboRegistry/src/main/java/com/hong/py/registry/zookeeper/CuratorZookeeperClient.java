package com.hong.py.registry.zookeeper;

import com.hong.py.commonUtils.URL;

import java.util.List;

public class CuratorZookeeperClient implements ZookeeperClient {


    public CuratorZookeeperClient(URL url) {

    }

    @Override
    public void create(String path, boolean ephemeral) {

    }

    @Override
    public void delete(String path) {

    }

    @Override
    public List<String> getChildren(String path) {
        return null;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public URL getUrl() {
        return null;
    }
}
