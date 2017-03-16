package com.sitech.crmpd.idmm.ble;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import com.sitech.crmpd.idmm.ble.mon.HttpServer;
import com.sitech.crmpd.idmm.ble.store.PrioQueue;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.sitech.crmpd.idmm.ble.store.StoreFactory;
import com.sitech.crmpd.idmm.ble.store.StoreOperThreads;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;

@Configuration
public class BLEConfig {
	
	private static final Logger log = LoggerFactory.getLogger(BLEConfig.class);

	private String bleid;      // BLE编码

	@Value("${zookeeper.ble.path}")
	protected String zk_ble_path;
	
	@Value("${zookeeper.config.version.path}")
	protected String zk_version_path;
	
	@Value("${index.maxretry}")
	private int index_maxretry;

	// netty 线程等待应答的超时时间
	@Value("${netty.timeout_sec:20}")
	public int netty_timeout_sec = 20;

    // netty 线程获得请求对象（用以封装请求参数并发送给内存线程）的超时时间， 因为该对象也保存于一个queue中，如果空的话会导致获取超时
    @Value("${netty_wait_item_sec:5}")
    public int netty_wait_item_sec = 5;

    // 内存处理线程获取请求的超时时间， 这个时间并没有多大影响， 超时只是表明请求队列是空闲的
	@Value("${req_poll_timeout_sec:5}")
	public int req_poll_timeout_sec = 5;

	// 内存处理线程向数据操作队列发送请求以及回送应答的超时时间， 数据库库操作如果阻塞，将导致其队列积压而使这个超时产生
	@Value("${req_put_timeout_sec:5}")
	public int req_put_timeout_sec = 5;

    // 内存处理请求队列的大小， 请求队列如果积满， 则netty线程将会阻塞
    @Value("${req_queue_capacity:50}")
    public int req_queue_capacity = 50;

	@Value("${monitor_http_port:8000}")
	private int monitor_http_port;

    @Resource
	private StoreFactory storeFactory;
	
	@Resource
	private StoreOperThreads thds; //数据库操作线程池

	@Resource
	private BLEEntry entry;
	
	@Resource
	private Configure conf;

	@Resource
	private HttpServer monServ;

	@Resource
	private ZKFailOver zkfailover;

	private String cfgVersion;
	
	protected Map<String, Map<String, PrioQueue>> queues;  //保存目标主题 消费者 对应的消息队列
//	protected Map<String, Key> topics; //保存 目标主题 与ble的关系
	protected Map<String, String> topic2ble;
	
	private CuratorFramework zkClient;
	
	private String getVersion(){
		String tmp_v = null;

		try {
			byte[] data = zkClient.getData().forPath(zk_version_path);
			tmp_v = new String(data);
		} catch (Exception e) {
			log.error("get version from zk:"+zk_version_path+" failed", e);
		}
		log.info("==current config version:{}", tmp_v);
		
		return tmp_v;
	}
	

	private ConcurrentHashMap<String, Key> k = new ConcurrentHashMap<String, Key>();
	protected Key findBLE(String topic){
		String tbleid = topic2ble.get(topic);
		for(ChildData data: pathCache.getCurrentData()){
			String path = data.getPath();
			String bid = path.substring(path.lastIndexOf('.')+1);
			if(!tbleid.equals(bid))
				continue;
			String s = new String(data.getData());
			int pos = s.indexOf(':');
			int pos1 = s.indexOf(' ', pos);
			String host = s.substring(0, pos);
			int port = Integer.valueOf(s.substring(pos+1, pos1));
			String kk = host+":"+port;
			if(!k.containsKey(kk))
				k.put(kk, Key.create(bid, host, port));
			return k.get(kk);
		}
		return null;
	}

	private PrioQueue getq(Map<String, Map<String, PrioQueue>> c, String tid, String cid){
		Map<String, PrioQueue> cc = c.get(tid);
		if(cc == null) return null;
		return cc.get(cid);
	}
	
	public String getId(){
		return bleid;
	}

	// 获取目标主题与订阅关系的配置数据
	public void initialize() {
		zkClient = zkfailover.connectZK(); //等待竞争bleid
		bleid = zkfailover.getBleid();

		pathCache = new PathChildrenCache(zkClient, zk_ble_path, true);
		try {
			pathCache.start();
		} catch (Exception e1) {
			log.error("start PathCache for {} failed", zk_ble_path, e1);
		}

		versionCache = new NodeCache(zkClient, zk_version_path);
		versionCache.getListenable().addListener(new NodeCacheListener() {
			@Override
			public void nodeChanged() throws Exception {
				String new_version = new String(versionCache.getCurrentData().getData());
				log.warn("NodeCache [{}] changed, data is: {}", zk_version_path, new_version );
				checkVersion(new_version);
			}
		});
		try {
			versionCache.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 启动存储操作线程池
		thds.start();
		
		cfgVersion = getVersion();
		
		conf.load(cfgVersion, bleid);
		
		topic2ble = conf.topic2ble;
		queues = conf.queues;
		orders = conf.orders;
		notices = conf.notices;

		
		// 逐个启动队列线程
		for(Map<String, PrioQueue> qs: queues.values()){
			for(PrioQueue q: qs.values()){
				q.setStore(storeFactory.getStore(q.getTopic(), q.getClient()));
				q.setMaxRetry(index_maxretry);
				q.setStoreOperThreads(thds);
				q.start();
			}
		}
		
		int tcount = 0;
		for(Map<String, PrioQueue> c: queues.values())
			tcount += c.size();
		log.info("total topics count: {}, my topics count: {}", topic2ble.size(), tcount);

		//启动队列监控http server
		monServ.start(monitor_http_port);

	}
	
	/**
	 * 检查消息是否属于顺序消费， 并找出下一个目标主题和消费者，保存到 mi 中
	 * @param msg
	 * @param mi
	 * @param dst_cli_id
	 * @param dst_topic_id
	 * @return 返回true则属于顺序消费
	 */
	public boolean checkOrders(Message msg, MsgIndex mi, String dst_topic_id, String dst_cli_id){
		if(msg.existProperty(PropertyOption.CURRENT_PROPERTY_KEY)
				&& msg.existProperty(PropertyOption.CURRENT_PROPERTY_VALUE)
				&& msg.existProperty(PropertyOption.TOPIC)){
			String pkey = msg.getStringProperty(PropertyOption.CURRENT_PROPERTY_KEY);
			String pval = msg.getStringProperty(PropertyOption.CURRENT_PROPERTY_VALUE);
			String src_topic = msg.getStringProperty(PropertyOption.TOPIC);
			String key = String.format("%s.%s.%s", src_topic, pkey, pval);
			if(!orders.containsKey(key))
				return false;
			List<ConsumeOrder> l = orders.get(key);
			int i;
			for(i=0; i<l.size(); i++){
				if(l.get(i).equals(dst_topic_id, dst_cli_id))
					break;
			}
			if(i >= l.size())
				return false;
			if(i+1<l.size()){
				ConsumeOrder c = l.get(i+1);
				mi.setNextTopic(c.dest_topic_id);
				mi.setNextClient(c.consumer_client_id);
			}
			if(i >  0){
				// 非第一个消息，需要锁定, 锁定的标记为-1
				mi.setGetTime(-1);
			}
			return true;
		}
		return false;
	}

	private Map<String, List<ConsumeOrder> >  orders;


	static class ConsumeOrder {
		protected String src_topic_id, attribute_key, attribute_value, dest_topic_id, consumer_client_id;
		protected int consume_seq;

		public boolean equals(String dst_topic_id, String dst_cli_id){
			return dst_cli_id.equals(consumer_client_id) && dst_topic_id.equals(dest_topic_id);
		}
	}
	
	/**
	 * 检查是否存在消费通知的配置
	 * @param mi
	 * @param dst_topic_id
	 * @param dst_cli_id
	 * @return
	 */
	public ConsumeNotice checkNotice(MsgIndex mi, String dst_topic_id, String dst_cli_id){
		String key = mi.getProduceClient() + '.' + mi.getSrcTopic() + '.'+ dst_topic_id +'.' + dst_cli_id;
		log.info("checkNotice, key:[{}]  found:{} pub_client:{}", key, notices.containsKey(key), mi.getProduceClient());
		for(String k: notices.keySet())
			log.info("--[{}]--", k);
		return notices.get(key);
//		return null; //不再檢查消費通知相關過程， 改为由broker处理消费通知， 在 pull-commit 的时候指定 REPLY_TO
	}
	
	/**
	 * 保存消费通知配置数据， key为 String.format("%s.%s.%s.%s", producer_client_id, src_topic_id, dest_topic_id, consumer_client_id)
	 */
	private Map<String, ConsumeNotice> notices;
	static class ConsumeNotice {
		protected String notice_topic_id;
		protected String notice_client_id;
	}

	private PathChildrenCache pathCache;
	private NodeCache versionCache;

	private void versionChanged(List<PrioQueue> qadd, List<PrioQueue> qdel, String version) {
		Map<String, Map<String, PrioQueue>> newq, oldq;  //保存目标主题 消费者 对应的消息队列
		oldq = queues;
		
		// Done: 配置版本变化， 暂时只检查ble与所属目标主题和订阅者的变化
		conf.load(version, bleid);
		
		notices = conf.notices;
		orders  = conf.orders;
		topic2ble = conf.topic2ble;
		
		newq   = conf.queues;
		
		// check add
		for(String tid: newq.keySet()){
			Map<String, PrioQueue> t = newq.get(tid);
			for(String cid: t.keySet()){
				PrioQueue q = getq(oldq, tid, cid);
				if(q == null){
					PrioQueue q1 = t.get(cid);
					qadd.add(q1);
				}else{
					// 更新几个参数值
					PrioQueue q1 = t.get(cid);
                    log.warn("Queue {} {} params changed, old:(max-sending:{} min-locktime:{} max-locktime:{} consumespeed:{})\n" +
                            "new(max-sending:{} min-locktime:{} max-locktime:{} consumespeed:{})",
                            new Object[]{q1.getMaxSending(), q1.getMinProcessTime(), q1.getMaxProcessTime(), q1.getConsumeSpeedLimit(),
                                    q.getMaxSending(), q.getMinProcessTime(), q.getMaxProcessTime(), q.getConsumeSpeedLimit()});
					q.setMaxSending(q1.getMaxSending());
					q.setTimeouts(q1.getMinProcessTime(), q1.getMaxProcessTime());
					q.setConsumeSpeedLimit(q1.getConsumeSpeedLimit());
				}
			}
		}
		
		// check remove
		for(String tid: oldq.keySet()){
			Map<String, PrioQueue> t = oldq.get(tid);
			for(String cid: t.keySet()){
				PrioQueue q = getq(newq, tid, cid);
				if(q == null)
					qdel.add(t.get(cid));
			}
		}
	}

	/**
	 * 配置版本变化， 检查增删的队列， 并实施
	 */
	private void checkVersion(String new_version) {
		if(new_version.equals(cfgVersion)){
			return;
		}
		if(queues == null)
			return;
		
		final List<PrioQueue> qadd = new LinkedList<PrioQueue>(), qdel = new LinkedList<PrioQueue>();

		versionChanged(qadd, qdel, new_version);
		for(final PrioQueue q: qadd){//新增的队列
			q.setStore(storeFactory.getStore(q.getTopic(), q.getClient()));
			q.setMaxRetry(index_maxretry);
			q.setStoreOperThreads(thds);
			q.start();
			entry.addQueue(q);
			
			log.warn("==new Queue topic:{} client:{}", q.getTopic(), q.getClient());
		}

		for(final PrioQueue q: qdel){
			q.stop();
			entry.removeQueue(q);
			log.warn("==remove Queue topic:{} client:{}", q.getTopic(), q.getClient());
		}
		
		cfgVersion = new_version;
	}

}
