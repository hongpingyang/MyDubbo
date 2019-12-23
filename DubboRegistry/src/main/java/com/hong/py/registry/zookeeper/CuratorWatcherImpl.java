package com.hong.py.registry.zookeeper;

import com.hong.py.commonUtils.StringUtils;
import com.hong.py.registry.ChildrenListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 监听节点变化
 **/
public class CuratorWatcherImpl implements CuratorWatcher {

    private final CuratorFramework client;

    private ChildrenListener childrenListener;

    public CuratorWatcherImpl(CuratorFramework client, ChildrenListener childrenListener) {
        this.client = client;
        this.childrenListener = childrenListener;
    }

    public void unWatch() {
        this.childrenListener = null;
    }

    @Override
    public void process(WatchedEvent watchedEvent) throws Exception {
        if (childrenListener != null) {
            String path = watchedEvent.getPath() == null ? "" : watchedEvent.getPath();
            List<String> children = new ArrayList<>();
            if(StringUtils.isNotEmpty(path)) {
                children=client.getChildren().usingWatcher(this).forPath(path);
            }
            childrenListener.childChanged(path,children);
        }

    }
}
