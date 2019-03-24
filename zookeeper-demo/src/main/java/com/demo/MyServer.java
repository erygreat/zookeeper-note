package com.demo;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class MyServer implements Watcher {
    /**
     * 连接超时时间，当Zookeeper在该时间长度后没有连接该客户端，会认为该客户端已经死掉了。
     */
    protected static final int SESSION_TTMEOUT = 5000;

    protected ZooKeeper zk;
    private CountDownLatch connectedSignal = new CountDownLatch(1);
    private String group;
    private String member;
    private String groupPath;
    private String memberPath;
    private String hosts;

    public MyServer(String group, String member, String hosts) {
        this.group = group;
        this.hosts = hosts;
        this.groupPath = "/" + this.group;
        this.member = member;
        this.memberPath = this.groupPath + "/" + this.member;
    }

    public void connect() throws IOException, InterruptedException {
        System.out.println("正在连接Zookeeper");
        zk = new ZooKeeper(this.hosts, SESSION_TTMEOUT, this);
        // 异步任务，当connectedSignal为0时运行
        connectedSignal.await();
        System.out.println("Zookeeper已连接");
    }

    /**
     * 获取事件之后的回调函数
     */
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            connectedSignal.countDown();
        }
    }

    public void close() throws InterruptedException {
        zk.close();
    }

    public boolean existGroup() throws KeeperException, InterruptedException {
        Stat stat =  zk.exists(this.groupPath, false);
        return stat != null;
    }

    public String createGroup() throws KeeperException, InterruptedException {
        // 权限为所有人共享，创建模式为持久节点
        System.out.println("创建group组【" + this.group +"】");
        return zk.create(this.groupPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    public String registerMember() throws Exception {
        // 权限为所有人共享，创建模式为序列短暂节点
        String path = zk.create(this.memberPath, getIp().getBytes("UTF-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("注册服务完成，服务路径：【" + path +"】，IP地址为：【" + getIp() + "】" + "当前时间为：" + (new Date()).toString());
        return path;
    }

    public String getIp() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        return addr.getHostAddress().toString(); //获取本机ip
    }

    public String registerServer() throws Exception {
        connect();
        if (!existGroup()) {
            createGroup();
        }
        return registerMember();
    }

    public static void main(String[] args) throws Exception {
        MyServer myServer = new MyServer("myServer","demo","tiny01:2181,tiny02:2181,tiny03:2181" );
        myServer.registerServer();
        // 服务器的业务逻辑，理论上服务器是一直运行的。这里为了方便，只模拟60秒。
        Thread.sleep(1000 * 60);
    }

}