package com.hong.py.registry.zookeeper;

import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.registry.ChildrenListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CuratorZookeeperClient implements ZookeeperClient {

    private final  URL url;
    private final CuratorFramework client;
    private Map<String, ConcurrentHashMap<ChildrenListener,CuratorWatcher>> listeners=new ConcurrentHashMap<>();

    public CuratorZookeeperClient(URL url) {
        this.url=url;

        try {

            client=curatorClient();
            client.start();

        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public CuratorFramework curatorClient() {

        int timeout = url.getParameter(Constants.TIMEOUT_KEY, 5000);

        CuratorFrameworkFactory.Builder builder=CuratorFrameworkFactory.builder()
                .connectString(url.getAddress())
                .connectionTimeoutMs(timeout)
                .retryPolicy(new RetryNTimes(1,1000))
                .namespace("hongpy");

        return builder.build();
    }

    @Override
    public void create(String path, boolean ephemeral) {
        if (ephemeral) {
            createEphemeral(path);
        }
        else {
            if (!checkExists(path)) {
                createPersistent(path);
            }
        }
    }

    //检测某个节点是否存在
    public boolean checkExists(String path) {
        try {
            if (client.checkExists().forPath(path) != null) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    //创建永久节点
    public void createPersistent(String path) {
        try {
            client.create().forPath(path);
        } catch (KeeperException.NodeExistsException e) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    //创建临时节点
    public void createEphemeral(String path) {
        try {
            client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (KeeperException.NodeExistsException e) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            client.delete().forPath(path);
        } catch (KeeperException.NoNodeException e) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> addChildListener(String path, ChildrenListener listener) {
        ConcurrentHashMap<ChildrenListener, CuratorWatcher> concurrentHashMap = listeners.get(path);
        if (concurrentHashMap == null) {
            concurrentHashMap = new ConcurrentHashMap<>();
            concurrentHashMap.put(listener, createCuratorWatcher(listener));
        }

        CuratorWatcher watcher = concurrentHashMap.get(listener);
        if (watcher == null) {
            watcher=createCuratorWatcher(listener);
            concurrentHashMap.putIfAbsent(listener,watcher);
        }
        try {
          return  client.getChildren().usingWatcher(watcher).forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private CuratorWatcher createCuratorWatcher(ChildrenListener listener) {
        return new CuratorWatcherImpl(client,listener);
    }


    @Override
    public void removeChildListener(String path, ChildrenListener listener) {
        ConcurrentHashMap<ChildrenListener, CuratorWatcher> concurrentHashMap = listeners.get(path);
        if (concurrentHashMap != null) {
            CuratorWatcher watcher = concurrentHashMap.get(listener);
            if (watcher != null) {
                ((CuratorWatcherImpl)watcher).unWatch();
            }
        }
    }

    @Override
    public List<String> getChildren(String path) {
        try {
            return client.getChildren().forPath(path);
        }  catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected() {
        return client.getZookeeperClient().isConnected();
    }

    @Override
    public void close() {
        client.close();
    }

    @Override
    public URL getUrl() {
        return url;
    }
}
