package com.demo;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

public class DeleteGroup extends ConnectWatcher {

    public int getVersion(String groupName) throws KeeperException, InterruptedException {
        String path = "/" + groupName;
        Stat stat =  zk.exists(path, this);
        if(stat != null) {
            return stat.getVersion();
        }
        throw new KeeperException.NoNodeException("组不存在");
    }

    public void delete(String groupName,int version) throws KeeperException, InterruptedException {
        String path = "/" + groupName;
        zk.delete(path, version);
    }

    public static void main(String[] args) throws Exception {
        String path = "zoo52";
        DeleteGroup group = new DeleteGroup();
        group.connect("tiny01:2181,tiny02:2181,tiny03:2181");
        int version;
        try {
            version = group.getVersion(path);
        } catch(KeeperException.NoNodeException e) {
            group.close();
            return;
        }
        group.delete(path, version);
        group.close();

    }
}
