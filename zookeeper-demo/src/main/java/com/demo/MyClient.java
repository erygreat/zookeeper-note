package com.demo;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MyClient implements Watcher {
    /**
     * 连接超时时间，当Zookeeper在该时间长度后没有连接该客户端，会认为该客户端已经死掉了。
     */
    protected static final int SESSION_TTMEOUT = 5000;

    protected ZooKeeper zk;
    private CountDownLatch connectedSignal = new CountDownLatch(1);
    private String group;
    private String groupPath;
    private String hosts;
    private volatile List<String> activeServers;

    public MyClient(String group, String hosts) throws Exception {
        this.group = group;
        this.hosts = hosts;
        this.groupPath = "/" + this.group;
        getServers();
    }

    public void connect() throws IOException, InterruptedException {
        System.out.println("正在连接Zookeeper");
        zk = new ZooKeeper(this.hosts, SESSION_TTMEOUT, this);
        // 异步任务，当connectedSignal为0时运行
        connectedSignal.await();
        System.out.println("Zookeeper已连接");
    }

    /**
     * 获取事件之后的回调函数。
     */
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            connectedSignal.countDown();
        }
        if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
            try {
                getServers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void close() throws InterruptedException {
        zk.close();
    }

    public void getServers() throws Exception{
        if (zk == null) {
            connect();
        }
        List<String> children = zk.getChildren(groupPath, true);
        List<String> ips = new ArrayList<String>();
        Stat stat = zk.exists(groupPath, false);
        for (String child: children) {
            byte[] ip = zk.getData(groupPath + "/" +child, false, stat);
            ips.add(new String(ip, "UTF-8"));
        }
        System.out.println("服务器节点已更新，更新结果为：" + ips.toString());
        // 注意：由于多线程同步的原因，所以activeServers必须要同时修改所有数据，而不能循环添加。因为有可能上个监听处理还未处理完成，下个监听处理就启动了。
        activeServers = ips;
    }

    public List<String> getActiveServers(){
        return this.activeServers;
    }

    public static void main(String[] args) throws Exception {
        MyClient myClient = new MyClient("myServer","tiny01:2181,tiny02:2181,tiny03:2181" );
        // 服务器的业务逻辑，理论上服务器是一直运行的. 其中会不断获取服务器节点
        for (int i = 0; i < 10; i++) {
            List<String> activeServers = myClient.getActiveServers();
            System.out.println("获取服务器节点：" + activeServers.toString() + "当前时间为：" + (new Date()).toString());
            Thread.sleep(2000);
        }
    }

}
