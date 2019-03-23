package com.demo;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ConnectMultiWatcher implements Watcher {

    protected static final int SESSION_TTMEOUT = 5000;

    protected ZooKeeper zk;
    private CountDownLatch connectedSignal = new CountDownLatch(1);

    public void connect(String hosts) throws IOException, InterruptedException {
        zk = new ZooKeeper(hosts, SESSION_TTMEOUT, this);
        // 异步任务，当connectedSignal为0时运行
        connectedSignal.await();
    }

    /**
     * 获取事件之后的回调函数
     */
    public void process(WatchedEvent watchedEvent) {
        try {
            System.out.println( watchedEvent.getPath() + "\t\t" + watchedEvent.getType());
            zk.getChildren("/", true);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close() throws InterruptedException {
        zk.close();
    }

}
