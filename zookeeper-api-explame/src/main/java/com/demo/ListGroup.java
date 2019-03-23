package com.demo;

import org.apache.zookeeper.KeeperException;

import java.util.List;

public class ListGroup extends ConnectWatcher {

    public List<String> list(String groupName) throws KeeperException, InterruptedException {
        String path = "/" + groupName;
        try {
            return zk.getChildren(path, false);
        } catch (KeeperException.NoNodeException e) {
            throw new RuntimeException("组不存在!");
        }
    }

    public static void main(String[] args) throws Exception {
        String path = "zoo52";
        ListGroup group = new ListGroup();
        group.connect("tiny01:2181,tiny02:2181,tiny03:2181");
        List<String> members = group.list(path);
        for (String member: members) {
            System.out.println(member);
        }
        group.close();

    }
}
