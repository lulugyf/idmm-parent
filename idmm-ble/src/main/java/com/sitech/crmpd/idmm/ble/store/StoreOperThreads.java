package com.sitech.crmpd.idmm.ble.store;

/**
 2016-4-25 与zhanghr  zhanglei 讨论的情况总结如下：
        数据库的不可靠， 不应该由应用程序来承担。 只是调整了数据库操作与内存操作的顺序， 并把部分参数改为可配置（参考BLEConfig.java代码中)
 */

import java.lang.reflect.Array;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

@Component
public class StoreOperThreads{
	@Value("${index.dboper.paras:20}")
	private int index_dboper_paras = 20; //数据库操作并发线程数， 队列初始化的时候启动
	
	@Value("${index.netty.paras:30}")
	private int index_netty_paras = 30; //每队列的netty并发线程数（需建立对应的blockingqueue)
	
	@Value("${jdbc.logwarn.time_in_ms}")
	private int store_oper_warn = 0;

    @Value("${jdbc.capacity_of_dbqueue:100}")
    private int capacity_of_dbqueue=100;

    @Value("${req_put_timeout_sec:5}")
    public int req_put_timeout_sec=5;

	private static final Logger log = LoggerFactory.getLogger(StoreOperThreads.class);
	
	private boolean started = false;
	
	private BlockingQueue<OPItem>[] dbQueues; //操作数据库的线程交互队列

	static class DBThread extends Thread{
		private BlockingQueue<OPItem> q;
		private boolean running = true;
		private int store_oper_warn;
        private int req_put_timeout_sec;
		DBThread(int index, BlockingQueue<OPItem> q, int store_oper_warn, int req_put_timeout_sec){
			this.setName("DBThread-"+index);
			this.q = q;
			this.store_oper_warn = store_oper_warn;
            this.req_put_timeout_sec = req_put_timeout_sec;
		}
	
		public void run() {
			long tm_begin=0, tm_end=0;
			OPItem it = null;
			boolean oper_flag;
			while(running){
				it = null;
				oper_flag = false;
				try {
					it = q.take();
					if(it.store != null && it.ans.tp == OPAns.Type.OK){
						if(store_oper_warn > 0)
							tm_begin = System.currentTimeMillis();

						oper_flag = it.store.put(it.ans.op); //操作成功， 才保存数据
//						log.info("store.put, op is null {}, return {}", it.ans.op == null, oper_flag);

						if(store_oper_warn > 0){
							tm_end = System.currentTimeMillis();
							if(tm_end - tm_begin >= store_oper_warn)
								log.warn("store oper {} timeout warning, {} ms", it.ans.op.op, tm_end-tm_begin);
						}
					}
				} catch (InterruptedException e) {
					log.error("take item failed");
					break;
				}catch(Throwable e){
					log.error("===db oper failed", e);
				}finally{
					if(it != null){
						try {
							it.ans.tp = oper_flag ? OPAns.Type.OK : OPAns.Type.FAIL; // 爲應答標記是否成功
							it.queue.offer(it, req_put_timeout_sec, TimeUnit.SECONDS); //结果回送mem-thread
//                            log.info("sent back to mem, op is null {}", it.ans.op == null);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
		}
	}
	
	public void send2Store(OPItem item, int timeout, TimeUnit unit){
		OPAns an = item.ans;
		int pos = 0;
		try {
			pos = Math.abs(an.op.msgid.hashCode()) % dbQueues.length;
			BlockingQueue<OPItem> dbq = dbQueues[pos];
			dbq.offer(item, timeout, unit);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}catch(IndexOutOfBoundsException e){
			log.error("send2Store:: index out of range, size:{} index:{}", dbQueues.length, pos, e);
		}catch(Throwable ex){
			ex.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void start() {
		if(started){
			log.warn("dulplicated start DBOperThreads==========");
			return;
		}
		started = true;
			
		// 建立数据库操作通道
		dbQueues = (BlockingQueue<OPItem>[]) Array.newInstance( BlockingQueue.class, index_dboper_paras);
		for(int i=0; i<dbQueues.length; i++)
			dbQueues[i] = new ArrayBlockingQueue<OPItem>(capacity_of_dbqueue);

		// 建立数据库操作线程池, 每个通道建立一个线程
		for(int i=0; i<dbQueues.length; i++){
			Thread th = new DBThread(i, dbQueues[i], store_oper_warn, req_put_timeout_sec);
			th.setDaemon(true);
			th.start();
		}
	}
	

	
	public void blockingDetail(JSONObject jo){
		JSONObject jo1 = new JSONObject();

		int blocking_db_oper = 0;
		for(BlockingQueue<OPItem> b: dbQueues){
			blocking_db_oper += b.size();
		}
		jo1.put("blocking_db_oper", blocking_db_oper);
		jo.put("global", jo1);
	}
}
