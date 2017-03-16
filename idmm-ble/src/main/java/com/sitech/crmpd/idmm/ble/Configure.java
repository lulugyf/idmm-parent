package com.sitech.crmpd.idmm.ble;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.sitech.crmpd.idmm.ble.store.PrioQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.sitech.crmpd.idmm.ble.utils.MapFormat;

@Configuration
public class Configure {
	private static final Logger log = LoggerFactory.getLogger(Configure.class);
	
	protected Map<String, Map<String, PrioQueue>> queues;  //保存目标主题 消费者 对应的消息队列
	protected Map<String, String> topic2ble;
	protected Map<String, List<BLEConfig.ConsumeOrder> >  orders;
	protected Map<String, BLEConfig.ConsumeNotice> notices;

	@Resource
	private BLEConfig cfg;

	@Autowired
	private JdbcTemplate jdbcConfig; //配置库
	@Resource
	private Map<String, String> cfg_sqls;
	
	protected void load(String version, final String bleid){
		Map<String, String>versionMap = new HashMap<String, String>();
		versionMap.put("version", version);
		
		topic2ble = new HashMap<String, String>();
		queues = new HashMap<String, Map<String, PrioQueue>> ();
		orders = new HashMap<String, List<BLEConfig.ConsumeOrder> >();
		notices = new HashMap<String, BLEConfig.ConsumeNotice>();

		
		//载入配置中的消费顺序数据
		// select src_topic_id, attribute_key, attribute_value, dest_topic_id,	consumer_client_id, consume_seq
		// from consume_order_info_{version}
		jdbcConfig.query(MapFormat.format(cfg_sqls.get("sqlCfgConsumeOrder"),  versionMap),
				new RowCallbackHandler()
		{
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				BLEConfig.ConsumeOrder c = new BLEConfig.ConsumeOrder();
				c.src_topic_id = rs.getString(1).trim();
				c.attribute_key = rs.getString(2).trim();
				c.attribute_value = rs.getString(3).trim();
				String key = String.format("%s.%s.%s", c.src_topic_id, c.attribute_key, c.attribute_value);
				c.dest_topic_id = rs.getString(4).trim();
				c.consumer_client_id = rs.getString(5).trim();
				c.consume_seq = rs.getInt(6);
				
				List<BLEConfig.ConsumeOrder> l = orders.get(key);
				if(l == null){
					l = new ArrayList<BLEConfig.ConsumeOrder>();
					orders.put(key, l);
				}
				l.add(c);
			}
		});
		log.info("load consumeOrder count: {}", orders.size());
		
		// 加载消费通知配置表
		jdbcConfig.query(MapFormat.format(cfg_sqls.get("sqlCfgQueryNotice"),  versionMap),
				new RowCallbackHandler()
		{
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				//select producer_client_id, src_topic_id, dest_topic_id,
				// consumer_client_id, notice_topic_id, notice_client_id
				BLEConfig.ConsumeNotice c = new BLEConfig.ConsumeNotice();
				c.notice_topic_id = rs.getString(5).trim();
				c.notice_client_id = rs.getString(6).trim();
				String key =  
						rs.getString(1) + '.' +
						rs.getString(2) + '.' +
						rs.getString(3) + '.' +
						rs.getString(4);
				notices.put(key, c);
			}
		});
		log.info("load notice_config count: {}", notices.size());
		
		// DONE 需要为消费通知配置表中属于本ble的目标主题建立队列
//		for(ConsumeNotice c: notices.values()){
//			String ble = topic2ble.get(c.notice_topic_id);
//			if(ble == null){
//				log.error("config error: target-topic({}) of consume notice(clientid{}) not owned by a ble",
//						c.notice_topic_id, c.notice_client_id);
//				continue;
//			}
//			if(ble.equals(bleid)){
//				Map<String, PrioQueue> l = queues.get(c.notice_topic_id);
//				if(l == null){
//					l = new HashMap<String, PrioQueue>();
//					queues.put(c.notice_topic_id, l);
//				}
//				PrioQueue q = new PrioQueue(c.notice_client_id, c.notice_topic_id, 1000);
//				q.setTimeouts(60,  3600);
//				l.put(c.notice_client_id, q);
//			}
//		}
		
		// 载入目标主题  消费者， 并建立对应消息队列
		// select a.ble_id, b.client_id, a.dest_topic_id, b.max_request, b.min_timeout, b.max_timeout
		jdbcConfig.query(MapFormat.format(cfg_sqls.get("sqlCfgQueryTopics"), versionMap), new RowCallbackHandler(){
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				String ble_id = rs.getString(1);
				String cli_id = rs.getString(2);
				String topic_id = rs.getString(3);
				int max_request = rs.getInt(4);
				int min_timeout = rs.getInt(5);
				int max_timeout = rs.getInt(6);
				int consume_speed_limit = rs.getInt(7);
				int limit_count_max = rs.getInt(8);
				int limit_count_warn = rs.getInt(9);
				topic2ble.put(topic_id, ble_id);
				if(ble_id.equals(bleid)){
					Map<String, PrioQueue> l = queues.get(topic_id);
					if(l == null){
						l = new HashMap<String, PrioQueue>();
						queues.put(topic_id, l);
					}
					PrioQueue q = new PrioQueue(cli_id, topic_id, max_request);
					q.setConfig(cfg);
					q.setTimeouts(min_timeout, max_timeout);
					q.setConsumeSpeedLimit(consume_speed_limit);
					q.setMessageCountLimit(limit_count_max, limit_count_warn);
//					q.setStore(storeFactory.getStore(q.getTopic(), q.getClient()));
//					q.setMaxRetry(index_maxretry);
//					q.setStoreOperThreads(thds);
//					q.start();
					l.put(cli_id, q);
				}
			}
		});
	}

}
