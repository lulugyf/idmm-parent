package com.sitech.crmpd.idmm.ble;

import java.util.Map;

import javax.annotation.Resource;

import com.sitech.crmpd.idmm.ble.store.PrioQueue;
import org.springframework.context.annotation.Configuration;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


/*

http://127.0.0.1:15678/jolokia/list

http://127.0.0.1:15678/jolokia/exec/com.sitech.crmpd.idmm2.ble:hashCode=1d4aa61,type=RunTime/info

http://127.0.0.1:15678/jolokia/exec/com.sitech.crmpd.idmm2.ble:hashCode=1d4aa61,type=RunTime/delete/$msgid/$consumer_id/$dst_topic_id

http://127.0.0.1:15678/jolokia/exec/com.sitech.crmpd.idmm2.ble:hashCode=1d4aa61,type=RunTime/reset/$msgid/$consumer_id/$dst_topic_id

http://127.0.0.1:15678/jolokia/exec/com.sitech.crmpd.idmm2.ble:hashCode=1d4aa61,type=RunTime/send/$msgid/$consumer_id/$dst_topic_id/$groupid/$priority


直接向ble做消息修改：
1，在途消息删除 delete
        messageid, consumer_client_id, target_topic_id
2，消息重发 send
       messageid, consumer_client_id, target_topic_id, [group_id], [priority]
3，消息重置 reset
      messageid, consumer_client_id, target_topic_id
      
4. 消息数量查询 info
 
[] 里面的参数为可选
 */

@Configuration
public class RunTime {
	@Resource
	private BLEEntry entry;
	
//	@Resource
//	private BLEHandler handler;
	
	public String info() {
		JSONArray ja = new JSONArray();
		Map<String, Map<String, PrioQueue>> qs = entry.getQ();
		for(Map<String, PrioQueue> t: qs.values()){
			for(PrioQueue q: t.values()){
				JSONObject j = new JSONObject();
				j.put("target_topic_id", q.getTopic());
				j.put("target_client_id", q.getClient());
				j.put("size", q.size());
				j.put("err", q.errCount());
				j.put("sending", q.sending());
				j.put("total", q.getTotalCount());
				j.put("status", q.status());
				ja.add(j);
			}
		}
		return ja.toJSONString();
	}
	
	public int getClientCount(){
		return entry.getClient();
	}
	
	private PrioQueue findQ(String topic, String consumer){
		Map<String, Map<String, PrioQueue>> qs = entry.getQ();
		Map<String, PrioQueue> cq = qs.get(topic);
		if(cq == null)
			return null;
		return cq.get(consumer);
	}
	
	public boolean delete(String msgid, String consumer_id, String dst_topic) {
		PrioQueue q = findQ(dst_topic, consumer_id);
		if(q == null)
			return false;
		return q.delete(msgid);
	}

	public boolean moreObj(String consumer_id, String dst_topic, int size ){
		PrioQueue q = findQ(dst_topic, consumer_id);
		if(q == null)
			return false;
		q.addMoreObj(size);
		return true;
	}
	
	public boolean send(String msgid, String consumer_id, String dst_topic, String groupid, int priority){
		PrioQueue q = findQ(dst_topic, consumer_id);
		if(q == null)
			return false;
		MsgIndex mi = new MsgIndex();
		mi.setMsgid(msgid);
		mi.setGroupid(groupid);
		mi.setPriority(priority);
		mi.setProduceClient("from-jmx");
		mi.setSrcTopic("unknown-src-topic");
		mi.setCreateTime(System.currentTimeMillis());
		q.add(mi);
		return true;
	}

	public boolean reset(String msgid, String consumer_id, String dst_topic) {
		PrioQueue q = findQ(dst_topic, consumer_id);
		if(q == null)
			return false;
		return q.rollback(msgid, 0L);
	}
	
	public boolean reload(String consumer_id, String dst_topic){
		PrioQueue q = findQ(dst_topic, consumer_id);
		if(q == null)
			return false;
		return q.reload();
	}
	
	public String lockdetail(String consumer_id, String dst_topic){
		PrioQueue q = findQ(dst_topic, consumer_id);
		if(q == null)
			return null;
		return q.lockdetail();
	}

}
