package com.sitech.crmpd.idmm.ble.store;

import javax.annotation.Resource;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StoreFactoryJDBCImpl extends StoreFactory {
	
	@Resource
	private IndexRepositoryImpl indexRepo;

	@Override
	public Store getStore(String dest_topic_id, String dest_client_id) {
		return new StoreJDBCImpl(dest_topic_id, dest_client_id, indexRepo);
	}

	@Override
	public void startSlaveNode(CuratorFramework zk) {

	}

	@Override
	public void startMasterNode(CuratorFramework zk) {
//		indexRepo.createTables();
	}

}
