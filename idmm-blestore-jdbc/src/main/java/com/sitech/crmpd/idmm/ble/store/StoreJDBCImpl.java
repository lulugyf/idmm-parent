package com.sitech.crmpd.idmm.ble.store;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sitech.crmpd.idmm.ble.MsgIndex;

public class StoreJDBCImpl implements Store {
	private static final Logger log = LoggerFactory.getLogger(StoreJDBCImpl.class);
	private String dst_topic_id;
	private String dst_cli_id;
	private IndexRepositoryImpl indexRepository;
	
	protected StoreJDBCImpl(String topic, String client, IndexRepositoryImpl ir){
		dst_topic_id = topic;
		dst_cli_id = client;
		indexRepository = ir;
	}

	@Override
	public boolean put(JournalOP op) {
//		if(true) 	return;
		if(op == null) return false;
		String sql = null;
		boolean ret = true;
		switch(op.op){
		case ADD:
			ret = indexRepository.addIndex(op.mi, dst_cli_id, dst_topic_id, op.broker_id);
			break;
		case COMMIT:
//			indexRepository.insertHis(op.msgid, op.tableid, op.create_time, dst_cli_id, dst_topic_id);
//			indexRepository.removeIndex(op.msgid, op.tableid, op.create_time, dst_cli_id, dst_topic_id);
			ret = indexRepository.updateCommitTime(dst_cli_id, dst_topic_id, op); // 移动到历史表改为只更新状态
			break;
		case GET:
//			indexRepository.updateIndex(op.msgid, op.tableid, op.create_time, dst_cli_id, dst_topic_id, op.broker_id, op.maxwait, op.retry);
			break;
		case ROLLBACK:
		case UNLOCK:
			ret = indexRepository.delayIndex(op.msgid, op.tableid, op.create_time, dst_cli_id, dst_topic_id, "00", 0);
			break;
		case DEL:
			ret = indexRepository.insertHis(op.msgid, op.tableid, op.create_time, dst_cli_id, dst_topic_id);
			ret = indexRepository.removeIndex(op.msgid, op.tableid, op.create_time, dst_cli_id, dst_topic_id);
			break;
		case FAIL:
			ret = indexRepository.insertErr(op.mi, dst_cli_id, dst_topic_id, op.rcode, op.desc);
			ret = indexRepository.removeIndex(op.msgid, op.tableid, op.create_time, dst_cli_id, dst_topic_id);
			break;
		case DELAY:
			ret = indexRepository.delayIndex(op.msgid, op.tableid, op.create_time, dst_cli_id, dst_topic_id, "00", op.maxwait);
			break;
		default:
			ret = false;
			break;
		}
		return ret;
	}

	@Override
	public void setTopic(String topic_id) {
		this.dst_topic_id = topic_id;
	}

	@Override
	public void setClient(String client_id) {
		this.dst_cli_id = client_id;
	}

	@Override
	public String getTopic() {
		return dst_topic_id;
	}

	@Override
	public String getClient() {
		return dst_cli_id;
	}

	@Override
	public void restore(LoadCallback q) throws IOException {
		final LoadCallback q1 = q;
		new Thread() {
			public void run(){
				indexRepository.beginLoad(dst_cli_id, dst_topic_id);
				
				log.info("====restoring queue for t:{} c:{}", dst_topic_id, dst_cli_id);
				long t1 = System.currentTimeMillis();
				List<MsgIndex> arr = null;
				try{
					arr = indexRepository.load(dst_cli_id, dst_topic_id);
				}catch(Throwable ex){
					log.error("jdbcstore load failed, topic:{} client:{}", dst_topic_id, dst_cli_id, ex);
				}
				q1.finishLoading(arr);
				long t2 = System.currentTimeMillis();
				log.info("===queue t:{} c:{} loaded: {} elapsed: {} ms", 
						new Object[]{dst_topic_id, dst_cli_id, arr != null?arr.size():-1, t2-t1});
				
				indexRepository.finishLoad(dst_cli_id, dst_topic_id);
			}
		}.start();		
	}

	@Override
	public void archive() throws IOException {
		//nothing todo
	}

	private long last_clear_time = 0L;
	private final long  clear_interval = 10*60000;
	@Override
	public void archive(final PrioQueue q) throws IOException {
//		if(q.size() == 0){
//			if(System.currentTimeMillis() - last_clear_time<clear_interval)
//				return;
//			// 清理下索引表的数据， 避免累积数据差异， 导致后续启动出现数据问题
//			new Thread(){
//				public void run() {
//					synchronized(q){ //会锁队列， 这个需要考虑一下， 如果按照commit不移动历史表而是update状态， 数据会比较多
//						try{
//							indexRepository.clearIndexTables(dst_cli_id, dst_topic_id);
//						}catch(Exception ex){
//							log.error("clearIndexTable {} {} failed", dst_cli_id, dst_topic_id, ex);
//						}
//					}
//				}
//			}.start();
//			last_clear_time = System.currentTimeMillis();
//		}
	}

	@Override
	public void close() throws IOException {
		//nothing todo
	}

	@Override
	public void removeQueue() {
		// nothing todo
		
	}

}
