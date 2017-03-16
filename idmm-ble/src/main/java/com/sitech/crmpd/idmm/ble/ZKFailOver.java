package com.sitech.crmpd.idmm.ble;

import com.sitech.crmpd.idmm.ble.utils.Util;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Created by guanyf on 2016/11/11.
 */
@Configuration
public class ZKFailOver {
    private static final Logger log = LoggerFactory.getLogger(ZKFailOver.class);

    @Value("${zookeeper.addr}")
    protected String zk_addr;
    @Value("${zookeeper.connectTimeout}")
    protected int zk_connectTimeout;
    @Value("${zookeeper.sessionTimeout}")
    protected int zk_sessionTimeout;

    @Value("${ble.id:}")
    private String bleid;      // BLE编码

    @Value("${ble.id.seq:}")
    private String ble_id_seq; // BLE节点编号的序列，进程在这些id中竞争

    @Value("${netty.listen.port}")
    private int listen_port;

    @Value("${zookeeper.ble.path}")
    protected String zk_ble_path;

    @Value("${ble.id.tag.local:}")
    private String tag_local;
    @Value("${ble.id.seq.remote:}")
    private String seq_remote;
    @Value("${ble.id.tag.remote:}")
    private String tag_remote;
    @Value("${zookeeper.ble.path.temp:}")
    private String zk_path_temp;

    @Value("${jmx.jolokiaPort}")
    protected String jmx_jolokiaPort;

    private static final RetryPolicy RETRYPOLICY = new RetryOneTime(10);
    private boolean getMaster = false;
    private String local_addr;


    protected CuratorFramework connectZK() {
        local_addr = Util.getlocalip() + ":" + listen_port;
        if("".equals(ble_id_seq) ){
            if("".equals(bleid)){
                log.error("config error, one of ble.id and ble.id.seq must be set");
                System.exit(3);
            }
            ble_id_seq = bleid;
        }
        CuratorFramework zkClient = CuratorFrameworkFactory.builder().connectString(zk_addr)
                .retryPolicy(RETRYPOLICY).connectionTimeoutMs(zk_connectTimeout)
                .sessionTimeoutMs(zk_sessionTimeout).build();

        zkClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if (ConnectionState.CONNECTED == newState) {

                } else if (	ConnectionState.LOST == newState) {
                    // 连接挂起或者丢失的情况下， 都直接关闭应用
                    log.error("zookeeper connection lost, app exit...");
                    System.exit(1);
                } else if (ConnectionState.SUSPENDED == newState){
                    log.warn("zookeeper connection suspended, waiting...");
                }
            }
        });
        zkClient.start();
        try {
            zkClient.blockUntilConnected();
            log.info("zookeeper connected!");
        } catch (final InterruptedException e) {
            log.error("wait zookeeper connect failed, exit", e);
            System.exit(2);
        }

        //注册备份节点的路径
        String tmp_path = null;
        if(!"".equals(zk_path_temp) && !"".equals(tag_local)){
            tmp_path = zk_path_temp+"/"+tag_local + "_" + local_addr;
            try {
                zkClient.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(tmp_path, "".getBytes());
                log.info("backup node register succ {}", tmp_path);
            } catch (Exception e) {
                log.error("create backup node path[{}] failed", tmp_path, e);
            }
        }

        //尝试接管一个ble节点id
        while(!getMaster){
            tryMakeMaster(zkClient);
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(tmp_path != null){
            // 接管成功后删除备份路径
            try {
                zkClient.delete().forPath(tmp_path);
                log.info("remove backup node path {} created succ", tmp_path);
            } catch (Exception e) {
                log.error("delete backup node path[{}] failed", tmp_path, e);
            }
        }

        return zkClient;
    }

    private String final_ble_id;
    protected String getBleid() {return final_ble_id; }
    private void tryMakeMaster(CuratorFramework zkClient){

        final byte[] data = String.format("%s jolokia-port:%s", local_addr, jmx_jolokiaPort).getBytes();

        for (String bid: ble_id_seq.split(",")) {
            bleid = bid.trim();
            log.info("trying id: {}...", bleid);
            String path = zk_ble_path + "/id." + bleid;
            try {
                zkClient.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(path, data);
                log.info("create node zk path [{}] data[{}] success, continue",
                        path, local_addr);
                getMaster = true;
                final_ble_id = bleid;
                return;
            } catch (final KeeperException.NodeExistsException e) {
                log.warn("node {} already exists, try next", bleid);
            } catch (final Exception e) {
                log.error("create zkpath failed", e);
                break;
            }
        }

        // 开始检查跨机房节点
        if("".equals(tag_remote) || "".equals(seq_remote) || "".equals(tag_local))
            return;
        log.info("checking remote backup node for {}", tag_remote==null);
        // 检查是否有对端机房的备份节点在线， 有的话就直接退出了
        try {
            for(String k: zkClient.getChildren().forPath(zk_path_temp)){
                log.info("----path: {}", k);
                if(k.startsWith(tag_remote+"_")) {
                    log.info("there are some remote backup node exists: {}, stop checking", k);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("begin checking remote ble...");
        for (String bid: seq_remote.split(",")) {
            bleid = bid.trim();
            log.info("trying id: {}...", bleid);
            String path = zk_ble_path + "/id." + bleid;
            try {
                zkClient.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(path, data);
                log.info("create node zk path [{}] data[{}] success, continue",
                        path, local_addr);
                getMaster = true;
                final_ble_id = bleid;
                return;
            } catch (final KeeperException.NodeExistsException e) {
                log.warn("node {} already exists, try next", bleid);
            } catch (final Exception e) {
                log.error("create zkpath failed", e);
                break;
            }
        }

    }

}
