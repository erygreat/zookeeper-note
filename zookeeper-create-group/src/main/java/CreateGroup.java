import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class CreateGroup implements Watcher {

    private static final int SESSION_TTMEOUT = 5000;

    private ZooKeeper zk;
    private CountDownLatch connectedSignal = new CountDownLatch(1);

    public void connect(String hosts) throws IOException, InterruptedException {
        zk = new ZooKeeper(hosts, SESSION_TTMEOUT, this);
        // 异步任务，当connectedSignal为0时运行
        connectedSignal.await();
    }

    /**
     * 获取事件之后的回调函数
     *
     * @param watchedEvent
     */
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            connectedSignal.countDown();
        }
    }

    public String create(String groupName) throws KeeperException, InterruptedException {
        String path = "/" + groupName;
        // 权限为所有人共享，创建模式为持久节点
        return zk.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    public void close() throws InterruptedException {
        zk.close();
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
