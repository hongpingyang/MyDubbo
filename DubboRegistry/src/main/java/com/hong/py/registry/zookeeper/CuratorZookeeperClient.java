package com.hong.py.registry.zookeeper;

import com.hong.py.commonUtils.Constants;
import com.hong.py.commonUtils.URL;
import com.hong.py.registry.ChildrenListener;
import com.hong.py.registry.StateListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class CuratorZookeeperClient implements ZookeeperClient {

    private final  URL url;
    private final CuratorFramework client;
    private Map<String, ConcurrentHashMap<ChildrenListener,CuratorWatcher>> listeners=new ConcurrentHashMap<>();
    private final Set<StateListener> stateListeners = new CopyOnWriteArraySet<StateListener>();

    public CuratorZookeeperClient(URL url) {
        this.url=url;

        try {

            client=curatorClient();
            //监听StateListener
            client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                @Override
                public void stateChanged(CuratorFramework client, ConnectionState state) {
                    if (state == ConnectionState.LOST) {
                        CuratorZookeeperClient.this.stateChanged(StateListener.DISCONNECTED);
                    } else if (state == ConnectionState.CONNECTED) {
                        CuratorZookeeperClient.this.stateChanged(StateListener.CONNECTED);
                    } else if (state == ConnectionState.RECONNECTED) {
                        CuratorZookeeperClient.this.stateChanged(StateListener.RECONNECTED);
                    }
                }
            });

            client.start();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }



    public CuratorFramework curatorClient() {

        //之前设置为5000ms报超时，会进行重试，也能连接上。
        // org.apache.curator.curatorconnectionlossexception:
        //是因为zookeeper的链接注册过程没完成然后就去获取zk客户端的链接状态了，
        // 只需将注册zookeeper的超时时间加大就好了。
        int timeout = url.getParameter(Constants.TIMEOUT_KEY, 30000);

        CuratorFrameworkFactory.Builder builder=CuratorFrameworkFactory.builder()
                .connectString(url.getAddress())
                .connectionTimeoutMs(timeout)
                .retryPolicy(new RetryNTimes(1,1000));
                //.namespace("hongpy");

        return builder.build();
    }

    //在每次新建一个节点时，一定要判断该节点（路径）是否存在
    @Override
    public void create(String path, boolean ephemeral) {
        if (!ephemeral) {
            if (checkExists(path)) {
                return;
            }
        }
        int i = path.lastIndexOf('/');
        if (i > 0) {
            create(path.substring(0, i), false);
        }
        if (ephemeral) {
            createEphemeral(path);
        }
        else {
            createPersistent(path);
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
    public void addStateListener(StateListener listener) {
        stateListeners.add(listener);
    }

    @Override
    public void removeStateListener(StateListener listener) {
        stateListeners.remove(listener);
    }

    public Set<StateListener> getSessionListeners() {
        return stateListeners;
    }

    private void stateChanged(int state) {
        for (StateListener sessionListener : getSessionListeners()) {
            sessionListener.stateChanged(state);
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
