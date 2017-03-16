package com.sitech.crmpd.idmm.ble.store;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.sitech.crmpd.idmm.ble.BLEConfig;
import com.sitech.crmpd.idmm.ble.utils.ConsumeSpeedLimit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.sitech.crmpd.idmm.ble.MsgIndex;


/**
 * 
 * 排序规则说明：
 * 1. 消息必须要有优先级, 基本原则是：先进先出； 优先级大的先出； 同一groupid下的消息按最大优先级使用同一优先级
 * 2. 可选的groupid， 如果有groupid， 则按同一groupid下最大优先级处理， 并按进入队列的时间顺序先进先出，同一groupid下的消息，只能有一个在途
 * 3. 队列划分依据为 clientid + topicid, 一个队列只给一个clientid消费
 * 4. clientid对应配置上最大并行数量nmax， 当未确认消息的数量超过nmax时， 则不再提供新的消息
 * 5. 消费确认后才移除消息， 超时未确认则恢复为未发送状态， 重发次数+1
 */
public class PrioQueue {
	
	private static final Logger log = LoggerFactory.getLogger(PrioQueue.class);

    private MemQueue mem;
	
	protected String dst_cli_id;
	protected String dst_topic_id;

	private long totalCount = 0;
	private ConsumeSpeedLimit csl = new ConsumeSpeedLimit(-1);
	private int limit_count_max = -1;
	private int limit_count_warn = -1;

    /**
     *
     * @param dst_cli_id
     * @param dst_topic_id
     * @param nConcurrents  最大并发数
     */
    public PrioQueue(String dst_cli_id, String dst_topic_id, int nConcurrents){
        this.dst_cli_id = dst_cli_id;
        this.dst_topic_id = dst_topic_id;
        mem = new MemQueue(dst_cli_id, dst_topic_id, nConcurrents);

    }

	public long getTotalCount() { return totalCount; }
    public void ready() { mem.finishLoading(null); }
	/**
	 * 启动时的异步恢复内存数据过程说明：
	 * 1. 启动是 loading_flag 为true
	 * 2.1 为每个queue启动一个线程从存储中恢复数据到内存
	 * 2.2 get操作直接返回无消息
	 * 2.3 add操作检查loading_flag == true 则在 synchronized(temp_list){...} 中把收到的消息保存到temp_list 中并保存都存储中
	 * 3 恢复线程处理完存储中的数据后， 获取到 temp_list 的锁， 并把其中数据全部保存到优先队列中
	 * 4 恢复线程修改 loading_flag = false
	 */

	public void setMaxRetry(int r) { mem.setMaxRetry(r); }
	

    public int size() { return mem.size(); }
    public int sending() {
        return mem.sending();
    }

	private BLEConfig cfg;
	public void setConfig(BLEConfig cfg){
		this.cfg = cfg;
	}

	private Store store = null;

	public String getTopic() {
		return dst_topic_id;
	}
	public String getClient() {
		return dst_cli_id;
	}

	private String errmsg = null;
	public String err(){
		return errmsg;
	}

	public void setLockTimeout(int t){
		mem.setLockTimeout(t);
	}

	public int errCount(){
		return mem.errCount();
	}

	
	/**
	 * 从当前BLE中移除本队列， 配置更新时触发
	 */
	public void stop(){
		if(store != null)
			store.removeQueue();
		running = false;
	}

	/////////////////////////////////////////////////////////
	/// 下面这段代码以消息队列来与优先级队列做交互， 接收请求端单线程处理， 避免另外加锁
	/////////////////////////////////////////////////////////
	// netty 线程的请求处理路线
	// netty线程 -> PrioQueue.req -> queue -> PrioQueue.processReq_stage0
	//                                -> dbQueues(n) -> PrioQueue.dbThreads(n) -> answers -> netty thread

	private BlockingQueue<OPItem> queue;
	private ArrayBlockingQueue<OPItem> answers; //请求对象的存储队列， 空闲的请求对象放在这里
	/**
	 * 获取一个发送请求对象
	 * @return
	 */
	private OPItem take(){
		long create_time = System.currentTimeMillis();
		try {
			OPItem op = answers.poll(cfg.netty_wait_item_sec, TimeUnit.SECONDS);

            if(op == null)
                return null;
            // some initializes
			op.create_time = create_time;
            op.stage = 0;
            op.ans.tp = OPAns.Type.NONE;
            op.q.clear(); // because of following q.poll with timeout, the answer queue maybe full fill

            return op;
		} catch (InterruptedException e) {
			log.error("need more request objects", e);
		}catch(Exception e){
			log.error("take a request Object failed", e);
		}
		return null;
	}
	public void addMoreObj(int size) {
		for(int i=0; i<size; i++){
			try {
				answers.put(new OPItem(i));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 将请求对象返还到对象池中
	 * @param t
	 */
	private void put(OPItem t){
		try {
			answers.offer(t, cfg.req_put_timeout_sec, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("return answer queue failed", e);
		}
	}

	private boolean running = true;
	private void req_deal() {
		Thread.currentThread().setName("MemDealer-"+dst_cli_id +"-"+dst_topic_id);
		OPItem item = null;
		while(running){
			try {
				item = queue.poll(cfg.req_poll_timeout_sec, TimeUnit.SECONDS);
				if(item == null) continue;

                if(item.stage == 0){
					item.ans.op = null;
					processReq_stage0(item);
				}else if(item.stage == 1) {
					processReq_stage1(item);
				}
				item.store = store;
			} catch (InterruptedException e) {
				log.error("queue.take interrupted topic:{} client:{}", dst_topic_id,  dst_cli_id);
			}catch(Throwable e){
				log.error("queue process failed", e);
			}finally{
			}
		}
		queue.clear();
		log.warn("Queue T:{} C:{} stoped", dst_topic_id, dst_cli_id);

	}

	// 从netty请求过来的线程调用
	private JournalOP req(OPItem item){
		try {
            try {
                queue.offer(item, cfg.req_put_timeout_sec, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                JournalOP op = item.q.poll(cfg.netty_timeout_sec, TimeUnit.SECONDS);
                if(op == null)
                    log.warn("wait mem oper timeout===");
                return op;
            } catch (InterruptedException e) {
                log.error("wait for answer timeout(default 20s):" + item.req.toString() + " " + this.dst_cli_id + " " + this.dst_topic_id, e);
            }
        }catch(Throwable e){
            log.error("req failed", e);
		}finally{
			this.put(item);
			if(log.isDebugEnabled()){
				log.debug("=== {} req time {} ms", this.dst_cli_id + " " + this.dst_topic_id, System.currentTimeMillis()-item.create_time);
			}
		}
		return null;
	}

	////////////////////////////////////////////////// public functions
	//////////////////////////////////////////////////
	public boolean add(MsgIndex mi){
		OPItem item = this.take();
		item.req.tp = OPReq.Type.ADD;
		item.req.mi = mi;
		JournalOP op = req(item);
		return (op != null  && op.op != JournalOP.OP.NONE);
	}
	private int no_more_msg_counter = 0;
	public MsgIndex get(String brokerid, long process_time){
		OPItem item = this.take();
		item.req.tp = OPReq.Type.GET;
		item.req.tm = process_time;
		item.req.brokerid = brokerid;
		JournalOP op = req(item);
		if(op != null && op.op != JournalOP.OP.NONE)
			return op.mi;
		no_more_msg_counter ++;
		return null;
	}
	public MsgIndex ack(String msgid){
		OPItem item = this.take();
		item.req.tp = OPReq.Type.ACK;
		item.req.msgid = msgid;
		JournalOP op = req(item);
		if(op != null && op.op != JournalOP.OP.NONE)
			return op.mi;
		return null;
	}

	public boolean delete(String msgid){
		OPItem item = this.take();
		item.req.tp = OPReq.Type.DELETE;
		item.req.msgid = msgid;
		JournalOP op = req(item);
		return op != null && op.op != JournalOP.OP.NONE;
	}

	public boolean rollback(String msgid, long tm){
		OPItem item = this.take();
		item.req.tp = OPReq.Type.ROLLBACK;
		item.req.msgid = msgid;
		item.req.tm = tm;
		JournalOP op = req(item);
		return op != null && op.op != JournalOP.OP.NONE;
	}

	public boolean delay(String msgid, long tm){
		OPItem item = this.take();
		item.req.tp = OPReq.Type.DELAY;
		item.req.msgid = msgid;
		item.req.tm = tm;
		JournalOP op = req(item);
		return op != null && op.op != JournalOP.OP.NONE;
	}

	public MsgIndex fail(String msgid, String code, String desc){
		OPItem item = this.take();
		item.req.tp = OPReq.Type.FAIL;
		item.req.msgid = msgid;
		item.req.desc  = desc;
		JournalOP op = req(item);
		if(op == null || op.op == JournalOP.OP.NONE)
			return null;
		return op.mi;
	}



	public int setConsumeSpeedLimit(int limit) {
		return csl.setLimit(limit);
	}
	public int getConsumeSpeedLimit() {
		return csl.getLimit();
	}

	//////////////////////////////////////// public functions end.
	////////////////////////////////////////


	public void setMessageCountLimit(int max, int warn) {
		this.limit_count_max = max;
		this.limit_count_warn = warn;
	}

	/**
add(MsgIndex mi)
get(broker_id, process_time)
ack(msgid);
delete(messageid)
rollback(messageid, 0L)
delay(msgid, delay_time)
fail(msgid, "00", commit_desc)
	 * @param item an object that contains request data and answer queue
     *
     *   第一阶段的处理， 对于不同的请求，会有不同的过程：
     *        GET: 只有第一阶段，不操作数据库， 直接获得数据后向消费者应答
     *        ADD: 检查messageid是否存在， 存在的话， 就不进行db操作
     *        <Others>: 第一阶段检查消息是否存在， 不存在直接返回
	 */
	private void processReq_stage0(OPItem item){
		OPAns an = item.ans;
		OPReq req = item.req;
		switch(req.tp){
		case ADD:
            if(!mem.exists(req.mi.getMsgid())) {
				if(limit_count_max > 0 && mem.size() >= limit_count_max){
					log.error("{} {} message store exceed limit {}", dst_topic_id, dst_cli_id, limit_count_max);
					break;
				}else if(limit_count_warn > 0 && mem.size() >= limit_count_warn){
					log.warn("{} {} message store exceed warn-limit {}", dst_topic_id, dst_cli_id, limit_count_warn);
				}
                JournalOP op = JournalOP.add(req.mi);
                op.msgid = req.mi.getMsgid();
                item.stage = 1; // can be continue
                mem.setTblID(op, req.mi);
                an.op = op;
            }
			break;
		case GET:
//			an.op = mem.get(req.brokerid, req.tm); // 对于 GET 操作， 并没有更新到数据库中， 因此直接应答
			if(csl.check()) {
				an.op = mem.get(req.brokerid, req.tm);
				if (an.op != null) {
					item.stage = 1;
					mem.setTblID(an.op, an.op.mi);
					csl.add();
				}
			}else{
				log.warn("consume {}-{} speed exceed of {}", dst_topic_id, dst_cli_id, csl.getLimit());
			}
			break;
        case ACK:
            if(mem.exists(req.msgid)) {
                item.stage = 1;
                JournalOP op = JournalOP.commit(item.req.msgid);
                op.mi =mem.mi(item.req.msgid); //需要把消息体返回给调用者
                mem.setTblID(op, op.mi);
                an.op = op;
//                System.out.println("3===== ack stage0");
            }
            break;
		case ROLLBACK:
            if(mem.exists(req.msgid)) {
                JournalOP op = JournalOP.rollback(req.msgid);
		        op.maxwait = req.tm;
		        mem.setTblID(op, mem.mi(item.req.msgid));
                item.stage = 1;
                an.op = op;
            }
            break;
        case DELETE:
            if(mem.exists(req.msgid)) {
                JournalOP op = JournalOP.delete(req.msgid);
                mem.setTblID(op, mem.mi(item.req.msgid));
                an.op = op;
                item.stage = 1;
            }
            break;
        case FAIL:
            if(mem.exists(req.msgid)) {
                item.stage = 1;
                MsgIndex mi = mem.mi(item.req.msgid);
                JournalOP op = JournalOP.fail(mi, "00", req.desc);
                mem.setTblID(op, mi);
                an.op = op;
            }
            break;
        case DELAY:
			if(mem.exists(req.msgid)) {
                long delay = System.currentTimeMillis()+req.tm*1000;
                JournalOP op = JournalOP.delay(req.msgid, "11", delay);
                op.maxwait = req.tm;
                mem.setTblID(op, mem.mi(item.req.msgid));
                item.stage = 1;
                an.op = op;
            }
			break;
		}
        item.store = store;
        item.queue = queue;
        an.tp = (an.op != null)? OPAns.Type.OK:OPAns.Type.FAIL;
//        log.info("4=====stage:{}, op is null {}", item.stage, an.op == null);
        try{
            if (item.stage == 0) {
                try {
                    JournalOP op = item.ans.op;
                    if(op == null) op = JournalOP.none();
                    item.q.offer(op, cfg.req_put_timeout_sec, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if(item.stage == 1){
                // 否则送到db操作的通道中， 根据 messageid 计算 hash 后取模
                thds.send2Store(item, cfg.req_put_timeout_sec, TimeUnit.SECONDS);
            }
        }catch(Throwable ex){
            log.error("finally failed", ex);
        }

	}
    // 第二阶段的消息处理（数据库操作完成后）
    private void processReq_stage1(OPItem item){
		if(item.ans.tp == OPAns.Type.FAIL){ //如果數據庫操作失敗，則不處理內存的操作
			try{
				log.warn("dboper failed, skip memoper:");
				item.q.offer(JournalOP.none(), cfg.req_put_timeout_sec, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				log.error("return false to netty failed", e);
			}
			return;
		}
        OPAns an = item.ans;
        OPReq req = item.req;
        boolean ret = false;
        switch(req.tp){
            case ADD:
                mem.add(req.mi);
                break;
//            case GET:
//                an.op = qq_get(req.brokerid, req.tm);
//                break;
            case ROLLBACK:
                mem.rollback(req.msgid, req.tm);
                break;
            case DELETE:
                mem.delete(req.msgid);
                break;
            case ACK:
//                System.out.println("=======hello:"+ (an != null) + "  ==" + req.msgid);
                ret = mem.ack(req.msgid);
//                System.out.println("!!!!!======:"+ret);
                break;
            case FAIL:
                mem.fail(req.msgid, "00", req.desc);
                break;
            case DELAY:
                mem.delay(req.msgid, req.tm);
                break;
        }
        //an.tp = (an.op != null) ? OPAns.Type.OK : OPAns.Type.FAIL;

        // 向netty线程回送应答
        try {
            if(an.op == null) {
//				log.info("====stage_1, op is null");
				item.q.offer(JournalOP.none(), cfg.req_put_timeout_sec, TimeUnit.SECONDS);
			}else {
//				log.info("====stage_1, op is not null");
				item.q.offer(an.op, cfg.req_put_timeout_sec, TimeUnit.SECONDS);
			}
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



	public void setStore(Store s) {
        this.store = s;
		mem.setStore(s);
	}
	
	private StoreOperThreads thds;

	public void setStoreOperThreads(StoreOperThreads thds) {
		this.thds = thds;
	}

	public void setTimeouts(int min_timeout, int max_timeout) {
        mem.setTimeouts(min_timeout, max_timeout);
	}
	public int   getMinProcessTime() { return mem.getMinProcessTime(); }
	public int   getMaxProcessTime() { return mem.getMaxProcessTime(); }

	
	private boolean failure = false;
	

	public void loadFailure() {
		failure = true;
	}


	public boolean reload() {
		if(!failure)
			return false;
		if(store != null){
			failure = false;
			try {
				mem.loading_flag = true;
				store.restore(mem);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public String status() {
		if(failure)
			return "load-failed";
		if(mem.loading_flag)
			return "loading";
		return "ready";
	}
	

	public String lockdetail() {

		JSONObject jo = mem.lockdetail();
        jo.put("no_more_msg_counter", no_more_msg_counter);
        jo.put("blocking_request", queue.size());
        if(thds != null)
            thds.blockingDetail(jo);
        return jo.toJSONString();
	}

	public void setMaxSending(int nmax) {
		mem.setMaxSending(nmax);
	}
	public int   getMaxSending(){ return mem.getMaxSending(); }

    public void start() {
        queue = new ArrayBlockingQueue<OPItem>(cfg.req_queue_capacity);

        // 建立应答消息通道
        answers = new ArrayBlockingQueue<OPItem>(cfg.req_queue_capacity);
        for(int i=0; i<cfg.req_queue_capacity; i++){
            try {
                answers.put(new OPItem(i));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 启动队列线程
        Thread th = new Thread(){
            public void run(){
                try{
                    req_deal();
                }catch(Throwable ex){
                    log.error("req_deal exit", ex);
                }
            }
        };
        th.setDaemon(true);
        th.start();
    }

}
