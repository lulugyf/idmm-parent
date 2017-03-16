package com.sitech.crmpd.idmm.ble;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import com.sitech.crmpd.idmm.ble.store.PrioQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.alibaba.fastjson.JSONObject;
import com.sitech.crmpd.idmm2.client.BasicMessageContext;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageType;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.PullCode;
import com.sitech.crmpd.idmm2.client.api.ResultCode;
import com.sitech.crmpd.idmm2.client.exception.OperationException;

@Configuration
public class BLEEntry {
	private static final Logger log = LoggerFactory.getLogger(BLEEntry.class);

	@Resource
	private BLEConfig blecfg;
	
	@Resource
	private org.apache.commons.pool2.KeyedObjectPool<Key, BasicMessageContext> messageContextPool;
	
	@Value("${ble.default.priority}")
	private int default_priority;
	
	@Value("${index.default.lock_time_in_miniseconds}")
	private int msglocktime;
	
	@Value("${ble.id}")
	private String bleid;

	/*内存优先队列的容器， 按 target-topic clientid   组织*/
	private Map<String, Map<String, PrioQueue>> queues = new HashMap<String, Map<String, PrioQueue>>();
	
	public Map<String, Map<String, PrioQueue>> getQ(){
		return queues;
	}
	
	private AtomicInteger clientCount = new AtomicInteger(0);
	public void addClient(int i){
		clientCount.addAndGet(i);
	}
	public int getClient() { return clientCount.get(); }
	
	public boolean initialize(){
		try {
			queues = blecfg.queues;
			if(queues == null){
				log.error("load queues failed for null");
				return false;
			}
			
			for(String dst_topic_id: queues.keySet()){
				Map<String, PrioQueue> t = queues.get(dst_topic_id);
				for(String dst_cli_id: t.keySet()){
					log.debug("loading queue topic:{} client:{}",dst_topic_id, dst_cli_id);
					PrioQueue q = t.get(dst_cli_id);
					q.setLockTimeout(msglocktime); //设定默认锁定时间
					
					//indexRepository.load(q, q.dst_cli_id, q.dst_topic_id);
					//log.info("---load index data from store {} {} count: {}", q.dst_cli_id, q.dst_topic_id, q.size());
				}
				
				// finished 需要在线程中启动恢复内存， 并设置加载标志， 加载期间只处理SEND, 不处理其它消息
				
				//TODO 配置修改后在线更新，  文件配置暂时不支持在线生效  2015-5-7
				// 对于新增 topic-client,  在queues中新增对应的队列
				// 对于减少 topic-client,  则在queues中删除对应的数据
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void addQueue(PrioQueue q){
		q.setLockTimeout(msglocktime);
		Map<String, PrioQueue> t = queues.get(q.getTopic());
		if(t == null){
			t = new HashMap<String, PrioQueue>();
			queues.put(q.getTopic(), t);
		}
		t.put(q.getClient(), q);
		/*try {
			indexRepository.load(q, q.getClient(), q.getTopic()); //加载离线存储
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}
	
	public void removeQueue(PrioQueue q){
		Map<String, PrioQueue> t = queues.get(q.getTopic());
		if(t == null)
			return;
		t.remove(q.getClient());
		if(t.size() == 0)
			queues.remove(q.getTopic());
	}
	
	private PrioQueue findQueue(String dst_cli_id, String dst_topic_id, Message msgr){
		Map<String, PrioQueue> cq = queues.get(dst_topic_id);
		if(cq == null){
			if(msgr != null){
				msgr.setProperty(PropertyOption.RESULT_CODE, ResultCode.BAD_REQUEST);
				msgr.setProperty(PropertyOption.CODE_DESCRIPTION, 
						String.format("Target-Topic[%s] not found!", dst_topic_id));
			}
			return null;
		}
		PrioQueue q = cq.get(dst_cli_id);
		if(q == null){
			if(msgr != null){
				msgr.setProperty(PropertyOption.RESULT_CODE, ResultCode.BAD_REQUEST);
				msgr.setProperty(PropertyOption.CODE_DESCRIPTION, 
						"to topic["+dst_topic_id+"], Client-id["+dst_cli_id+"] not found!");
			}
		}
		if(log.isDebugEnabled()){
			if(!dst_cli_id.equals(q.getClient()) || !dst_topic_id.equals(q.getTopic()))
				log.debug("----findQueue return wrong result, expect "+dst_cli_id+"."+dst_topic_id+" but "+q.getClient()+"."+q.getTopic());
		}
		return q;
	}
	
	/**
	 * 从messageid 中拆出索引分表数字（分表由broker生成messageid时确定）
	 * 便于维护时查找数据
	 * @param msgid
	 * @return
	 */
	private int findTableId(String msgid){
		int p1 = msgid.lastIndexOf("::");
		if(p1 <= 0) return -1;
		int p2 = msgid.lastIndexOf("::", p1-1);
		if(p2 <=0) return -1;
		try{
			return Integer.parseInt(msgid.substring(p2+2, p1));
		}catch(Exception ex){
		}
		return -1;
	}

	public Message
	messageSend(Message msg, String broker_id) {

		String msgid = null;
		String[] msgids = null;
		if(msg.existProperty(PropertyOption.MESSAGE_ID))
			msgid = msg.getStringProperty(PropertyOption.MESSAGE_ID);
		else if(msg.existProperty(PropertyOption.BATCH_MESSAGE_ID)){
			msgids = msg.getArray(PropertyOption.BATCH_MESSAGE_ID, new String[0]);
		}
		String dst_topic_id = msg.getStringProperty(PropertyOption.TARGET_TOPIC);
		String groupid = null;
		if(msg.existProperty(PropertyOption.GROUP))
				groupid = msg.getStringProperty(PropertyOption.GROUP);
		int priority = default_priority;
		if(msg.existProperty(PropertyOption.PRIORITY))
			priority = msg.getIntegerProperty(PropertyOption.PRIORITY);
		String cli_id = msg.getStringProperty(PropertyOption.CLIENT_ID);
		
		
		String src_topic_id = null;
		if(msg.existProperty(PropertyOption.TOPIC))
			src_topic_id = msg.getStringProperty(PropertyOption.TOPIC);
		
		long effective_time = 0L;
		if(msg.existProperty(PropertyOption.EFFECTIVE_TIME)){
			effective_time = msg.getLongProperty(PropertyOption.EFFECTIVE_TIME);
			// 改为 - 数
			effective_time = 0 - effective_time;
		}
		long expire_time = 0L;
		if(msg.existProperty(PropertyOption.EXPIRE_TIME)){
			expire_time = msg.getLongProperty(PropertyOption.EXPIRE_TIME);
		}
		
		Message msgr = Message.create();
		
		if(msg.existProperty(PropertyOption.CUSTOM_SERIAL)
				&& "__notice__".equals(msg.getStringProperty(PropertyOption.CUSTOM_SERIAL) ) ) {
				//msg.getContent() != null && msg.getContent().length > 0){
			// 从其它BLE直接发过来的消费通知消息， 标识： CUSTOM_SERIAL=__notice__， 直接保存, 且对方发送的 client_id 就是目标消费者，不需要根据配置来分解
			if(saveNotice(msgid, msg.getContentAsString(), dst_topic_id, cli_id, msgr))
				msgr.setProperty(PropertyOption.RESULT_CODE, ResultCode.OK);
			return msgr;
		}

		if(msgid == null && msgids == null){
			msgr.setProperty(PropertyOption.RESULT_CODE, ResultCode.REQUIRED_PARAMETER_MISSING);
			msgr.setProperty(PropertyOption.CODE_DESCRIPTION, "MESSAGE_ID or BATCH_MESSAGE_ID not found!");
			return msgr;			
		}
		
		// 分解到订阅者
		Map<String, PrioQueue> cq = queues.get(dst_topic_id);
		if(cq == null){
			msgr.setProperty(PropertyOption.RESULT_CODE, ResultCode.BAD_REQUEST);
			msgr.setProperty(PropertyOption.CODE_DESCRIPTION, 
					String.format("Target-Topic[%s] not found!", dst_topic_id));
			return msgr;
		}
		boolean ret = false;
		Object data = msg.getContent();
		for(String dst_cli_id: cq.keySet()){
			PrioQueue q = cq.get(dst_cli_id);
			
			if(msgid != null){
				MsgIndex mi = new MsgIndex();
				mi.setMsgid(msgid);
				mi.setGroupid(groupid);
				mi.setPriority(priority);
				mi.setProduceClient(cli_id);
				mi.setSrcTopic(src_topic_id);
				mi.setTblid(findTableId(msgid));
				mi.setGetTime(effective_time); //设置生效时间
				mi.setExpireTime(expire_time);
				if(data != null)
					mi.setData(data); //保存消息体
				ret = saveIndex(msg, q, mi, dst_cli_id, dst_topic_id, broker_id);
				log.info("add single msg: {} {}=={} return {}", new Object[]{msgid, dst_cli_id, dst_topic_id, ret});
			}else{
				for(String mid: msgids){ //批量提交
					MsgIndex mi = new MsgIndex();
					mi.setMsgid(mid);
					mi.setGroupid(groupid);
					mi.setPriority(priority);
					mi.setProduceClient(cli_id);
					mi.setSrcTopic(src_topic_id);
					mi.setTblid(findTableId(mid));
                    mi.setGetTime(effective_time); //设置生效时间
                    mi.setExpireTime(expire_time);
					if(data != null)
						mi.setData(data); //保存消息体
					ret = saveIndex(msg, q, mi, dst_cli_id, dst_topic_id, broker_id);
					log.info("add batch msg, id: {} {}=={} return {}", new Object[]{mid, dst_cli_id, dst_topic_id, ret});
					if(!ret)
						break;
				}
			}
		}
		if(ret)
			msgr.setProperty(PropertyOption.RESULT_CODE, ResultCode.OK);
		else
			msgr.setProperty(PropertyOption.RESULT_CODE, ResultCode.INTERNAL_DATA_ACCESS_EXCEPTION);
		return msgr;
	}
	
	private boolean saveIndex(Message msg, PrioQueue q, MsgIndex mi,
							  String dst_cli_id, String dst_topic_id, String broker_id)
	{
		mi.setCreateTime(System.currentTimeMillis());
		// DONE 检查是否有顺序消费要求， 是的话, 找到下一目标主题， 并判断当前是否第一个（非第一个的话，要求锁定， 并等待前面的消费完成后解锁）
		blecfg.checkOrders(msg, mi, dst_topic_id, dst_cli_id);
		
		return q.add(mi);//保存到内存
	}
	
	private boolean saveNotice(String msgid, String body, String dst_topic_id, String dst_cli_id, Message msgr) {
		PrioQueue q = findQueue(dst_cli_id, dst_topic_id, msgr);
		if(q == null)
			return false;
		MsgIndex mi = new MsgIndex();
		mi.setCommitDesc(body);
		mi.setMsgid(msgid);
        log.info("===saveNotice: {} {}", body, body.length());
		return q.add(mi);
	}
	
	private void commitIndex(Message msg, PrioQueue q, String dst_cli_id, String dst_topic_id){
		String msgid = msg.getStringProperty(PropertyOption.MESSAGE_ID);
		MsgIndex mi = q.ack(msgid);

		if(mi == null){
			log.error("commit index failed, msgid={} not found", msgid);
			return;
		}
		
		//Done 201505121623 检查是否顺序消费， 判断是否需要解锁下一个消息
		if(mi.getNextTopic() != null && mi.getNextClient() != null){
			String nextTopic = mi.getNextTopic();
			String nextClient = mi.getNextClient();
			
			String ble = blecfg.topic2ble.get(nextTopic);
			if(ble != null){
				if(bleid.equals(ble)){
					log.info("unlock ordered msg {} {} {} return {}",
							msgid, nextTopic, nextClient, unlockIndex(msgid, nextTopic, nextClient) );
				}else{
					// TODO 向其它BLE发送消息最好放到单独的线程里
					Key k = blecfg.findBLE(nextTopic);
					if(k != null){
						BasicMessageContext c;
						try {
							c = messageContextPool.borrowObject(k);
							Message msgunlock = Message.create();
							msgunlock.setProperty(PropertyOption.MESSAGE_ID, msgid);
							msgunlock.setProperty(PropertyOption.TARGET_TOPIC, nextTopic);
							msgunlock.setProperty(PropertyOption.CLIENT_ID, nextClient);
							Message msgr = c.trade(bleid, MessageType.UNLOCK, msgunlock);
							log.info("unlock ordered msg {} {} {} return {}",
									msgid, nextTopic, nextClient,
									ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE)));
						} catch (OperationException | IOException e) {
							e.printStackTrace();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}

		noticeCheck(msg, mi, dst_cli_id, dst_topic_id);

	}

	private void noticeCheck(Message msg, MsgIndex mi, String dst_cli_id, String dst_topic_id) {
		// 检查是否需要消费通知（向生产者发送消费结果）
		BLEConfig.ConsumeNotice n = blecfg.checkNotice(mi, dst_topic_id, dst_cli_id);
		if(n == null)
			return;
		String msgid = mi.getMsgid();
		String ble = blecfg.topic2ble.get(n.notice_topic_id);
		log.info("need consume notice for t:{} c:{} id:{}", dst_topic_id, dst_cli_id, mi.getMsgid());
		if(ble == null) {
			log.error("ble id not found for notice {} {}", dst_topic_id, dst_cli_id);
			return;
		}
		// 把通知的内容放到json格式的body中
		JSONObject jo = new JSONObject();
		jo.put(PropertyOption.MESSAGE_ID.toString(), msgid);
		jo.put(PropertyOption.CLIENT_ID.toString(), mi.getProduceClient());
		jo.put(PropertyOption.TOPIC.toString(), mi.getSrcTopic());
		jo.put(PropertyOption.TARGET_TOPIC.toString(), dst_topic_id);
		jo.put("Consume-client", dst_cli_id);
		if(msg.existProperty(PropertyOption.COMMIT_TIME))
			jo.put(PropertyOption.COMMIT_TIME.toString(), msg.getLongProperty(PropertyOption.COMMIT_TIME));
		if(msg.existProperty(PropertyOption.RESULT_CODE))
			jo.put("Commit-code", msg.getProperty(PropertyOption.PULL_CODE).toString());
		if(msg.existProperty(PropertyOption.CODE_DESCRIPTION))
			jo.put("Description", msg.getStringProperty(PropertyOption.CODE_DESCRIPTION));
		String body = jo.toJSONString();
		if(bleid.equals(ble)){
			//DONE 要比较通知用的目标主题是否在本BLE上
			saveNotice(msgid, body, n.notice_topic_id, n.notice_client_id, null);
		}else{
			BasicMessageContext c;
			Key k = blecfg.findBLE(n.notice_topic_id);
			try {
				c = messageContextPool.borrowObject(k);

				Message msgnotify = Message.create(body);
				msgnotify.setProperty(PropertyOption.MESSAGE_ID, msgid);
				msgnotify.setProperty(PropertyOption.TARGET_TOPIC, n.notice_topic_id);
				msgnotify.setProperty(PropertyOption.CLIENT_ID, n.notice_client_id);
				msgnotify.setProperty(PropertyOption.CUSTOM_SERIAL, "__notify__");

				Message msgr = c.trade(bleid, MessageType.SEND_COMMIT, msgnotify);
				log.info("send a notice msg {} {} {} return {}",
						msgid, n.notice_topic_id, n.notice_client_id,
						ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE)));
			} catch (OperationException | IOException e) {
				e.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}
	
	private boolean unlockIndex(String msgid, String dst_topic_id, String client_id){
		//TODO 还要修改表的数据
		PrioQueue q = findQueue(client_id, dst_topic_id, null);
		if(q == null)
			return false;
		q.rollback(msgid, 0);
		return false;
	}


	public Message messagePull(Message msg, String broker_id) {
		String dst_cli_id = msg.getStringProperty(PropertyOption.CLIENT_ID);
		String dst_topic_id = msg.getStringProperty(PropertyOption.TARGET_TOPIC);
		
		Message msgr = Message.create();
		PrioQueue q = findQueue(dst_cli_id, dst_topic_id, msgr);
		if(q == null)
			return msgr;
		boolean getnext = true;
		
		MsgIndex mi = null;
		
		// 检查是否带有messageid 和 result_code, 如果有的话， 则表明带有消费确认
		if(msg.existProperty(PropertyOption.MESSAGE_ID) 
				&& msg.existProperty(PropertyOption.PULL_CODE))
		{
			String msgid = msg.getStringProperty(PropertyOption.MESSAGE_ID);
			PullCode pc = PullCode.valueOf((String)msg.getProperty(PropertyOption.PULL_CODE));
//			log.info("pull-code:{}", pc);
			switch(pc){
			case COMMIT:
				getnext = false;
				commitIndex(msg, q, dst_cli_id, dst_topic_id);
				log.info("COMMIT msg: {} {} {}", msgid, q.getClient(), q.getTopic());
				break;
			case COMMIT_AND_NEXT:
				commitIndex(msg, q, dst_cli_id, dst_topic_id);
				log.info("COMMIT_AND_NEXT msg: {} {} {}", msgid, q.getClient(), q.getTopic());
				break;
			case ROLLBACK:
				getnext = false;
				q.rollback(msgid, 0L);
				log.info("ROLLBACK msg: {} {} {}", msgid, q.getClient(), q.getTopic());
				break;
			case ROLLBACK_AND_NEXT:
				String commit_desc = "commit-desc";
				if(msg.existProperty(PropertyOption.CODE_DESCRIPTION))
					commit_desc = msg.getStringProperty(PropertyOption.CODE_DESCRIPTION);
				mi = q.fail(msgid, "00", commit_desc); //q.rollback(msgid);
				noticeCheck(msg, mi, dst_cli_id, dst_topic_id); //检查并生成消费通知
				log.info("ROLLBACK_AND_NEXT msg: {} {} {}", msgid, q.getClient(), q.getTopic());
				break;
			case ROLLBACK_BUT_RETRY:
				getnext = false;
				//延迟后重试, 延迟时间单位秒
				if(!msg.existProperty(PropertyOption.RETRY_AFTER)){
					msgr.setProperty(PropertyOption.RESULT_CODE, ResultCode.BAD_REQUEST);
					msgr.setProperty(PropertyOption.CODE_DESCRIPTION, "need option [RETRY_AFTER]");
					return msgr;
				}
				q.delay(msgid, msg.getLongPropertyValue(PropertyOption.RETRY_AFTER));
				log.info("ROLLBACK_BUT_RETRY msg: {} {} {}", msgid, q.getClient(), q.getTopic());
				break;
			}
		}
		
		if(getnext){
			long process_time =  -1L;
			if(msg.existProperty(PropertyOption.PROCESSING_TIME)){
				process_time = msg.getLongPropertyValue(PropertyOption.PROCESSING_TIME);
				process_time *= 1000;
			}

			mi = q.get(broker_id, process_time);

			if(mi == null){
				msgr.setProperty(PropertyOption.RESULT_CODE, ResultCode.NO_MORE_MESSAGE);
				if(log.isDebugEnabled())
					log.debug("====clientid:{} target-topic:{} qsize:{} reason: {}", dst_cli_id, dst_topic_id, q.size(), q.err());
			}else{
				log.info("Next msg: {} {} {}", mi.getMsgid(), q.getClient(), q.getTopic());
				if(mi.getCommitDesc() != null) {
                    msgr = Message.create(mi.getCommitDesc()); // 认为是消费通知，把通知的内容放到body里
//                    msgr.setProperty(PropertyOption.CODE_DESCRIPTION, mi.getCommitDesc());
                }else if(mi.getData() != null){
					msgr = Message.create( (byte[] )mi.getData()); //如果索引中包含了消息体， 则送回
				}
				msgr.setProperty(PropertyOption.RESULT_CODE, ResultCode.OK);
				msgr.setProperty(PropertyOption.MESSAGE_ID, mi.getMsgid());
				msgr.setProperty(PropertyOption.CONSUMER_RETRY, mi.getRetry()-1);
			}
		}else{
			msgr.setProperty(PropertyOption.RESULT_CODE, ResultCode.OK);
		}
		return msgr;
	}


	public Message messageDelete(Message msg, String broker_id) {
		String dst_topic_id = msg.getStringProperty(PropertyOption.TARGET_TOPIC); //broker添加的目标主题
		String messageid = msg.getStringProperty(PropertyOption.MESSAGE_ID);
	
		Message msgr = Message.create();
		
		Map<String, PrioQueue> cq = queues.get(dst_topic_id);
		if(cq == null){
			msgr.setProperty(PropertyOption.RESULT_CODE, ResultCode.BAD_REQUEST);
			msgr.setProperty(PropertyOption.CODE_DESCRIPTION, "Target-Topic not found!");
			return msgr;
		}
		for(String dst_cli_id: cq.keySet()){
			PrioQueue q = cq.get(dst_cli_id);
			boolean ret = q.delete(messageid);
			if(log.isInfoEnabled())
				log.info("delete message: cli_id: {} topic: {} msgid:{}, return: {}", new Object[]{q.getClient(), q.getTopic(), messageid, ret});
		}
		msgr.setProperty(PropertyOption.RESULT_CODE, ResultCode.OK);
		return msgr;
	}
	
	
	public Message messageUnlock(Message msg, String broker_id) {
		String dst_topic_id = msg.getStringProperty(PropertyOption.TARGET_TOPIC); //broker添加的目标主题
		String messageid = msg.getStringProperty(PropertyOption.MESSAGE_ID);
		String client_id = msg.getStringProperty(PropertyOption.CLIENT_ID);
	
		Message msgr = Message.create();
		PrioQueue q = findQueue(client_id, dst_topic_id, msgr);
		if(q == null)
			return msgr;
		
		boolean ret = q.rollback(messageid, 0L);
		log.info("unlock message {} return {}", messageid, ret);
		if(ret)
			msgr.setProperty(PropertyOption.RESULT_CODE, ResultCode.OK);
		else{
			msgr.setProperty(PropertyOption.RESULT_CODE, ResultCode.INTERNAL_DATA_ACCESS_EXCEPTION);
			msgr.setProperty(PropertyOption.CODE_DESCRIPTION, "unlock message failed");
		}
		return msgr;
	}
}
