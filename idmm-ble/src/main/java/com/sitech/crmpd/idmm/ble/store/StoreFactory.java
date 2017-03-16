package com.sitech.crmpd.idmm.ble.store;

import org.apache.curator.framework.CuratorFramework;

public abstract class StoreFactory {

	/**
	 * 为一个队列获取一个存储实例
	 * @param dest_topic_id
	 * @param dest_client_id
	 * @return
	 */
	public abstract Store getStore(String dest_topic_id, String dest_client_id);
	
	/**
	 * 启动索引数据存储的备份节点, 备份节点连接到master节点后， 接收索引数据的双（多）写
	 * 启动的时机是在ble启动， 但是创建zookeeper上的节点不能成功的时候
	 * slave启动后， 从zk上获得master节点的监听端口， 找不到的时候反复找
	 */
	public abstract void startSlaveNode(CuratorFramework zk);
	
	/**
	 * 启动索引数据存储的主用节点，
	 * 启动前需要先关闭slaveNode的运行
	 * master启动后需要建立一个serversocket 端口， 然后把端口发布到zk上
	 */
	public abstract void startMasterNode(CuratorFramework zk);
}
