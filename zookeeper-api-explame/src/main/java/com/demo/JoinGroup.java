package com.demo;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

public class JoinGroup extends ConnectWatcher {

    public String create(String groupName) throws KeeperException, InterruptedException {
        String path = "/" + groupName;
        // 权限为所有人共享，创建模式为持久节点
        return zk.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    public boolean exists(String groupName) throws KeeperException, InterruptedException {
        String path = "/" + groupName;
        Stat stat =  zk.exists(path, this);
        return stat != null;
    }

    public boolean exists(String groupName, String memberName) throws KeeperException, InterruptedException {
        String path = "/" + groupName + "/" + memberName;
        Stat stat =  zk.exists(path, this);
        return stat == null;
    }

    public String join(String groupName, String memberName) throws KeeperException, InterruptedException {
        String path = "/" + groupName + "/" + memberName;
        // 权限为所有人共享，创建模式为短暂节点
        return zk.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }

    public static void main(String[] args) throws Exception {
        String path = "zoo52";
        String member = "test";
        JoinGroup group = new JoinGroup();
        group.connect("tiny01:2181,tiny02:2181,tiny03:2181");
        if(!group.exists(path)) {
            group.create(path);
        }
        if(group.exists(path, member)) {
            group.join(path, member);
            Thread.sleep(10000);
        }
        group.close();

    }
}
