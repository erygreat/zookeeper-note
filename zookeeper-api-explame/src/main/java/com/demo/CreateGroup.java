package com.demo;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

public class CreateGroup extends ConnectWatcher {

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

    public static void main(String[] args) throws Exception {
        String path = "zoo5";
        CreateGroup createGroup = new CreateGroup();
        createGroup.connect("tiny01:2181,tiny02:2181,tiny03:2181");
        if (!createGroup.exists(path)) {
            createGroup.create(path);
        }
        createGroup.close();
    }
}
