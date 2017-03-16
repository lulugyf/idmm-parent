package com.sitech.crmpd.idmm.ble.store;

import java.util.Random;

import com.sitech.crmpd.idmm.ble.BLEConfig;
import com.sitech.crmpd.idmm.ble.MsgIndex;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestQueue {
	
	
	/** 
	 * 消费
	 * @param q
	 * @param tname
	 * @param COUNT
	 * @throws Exception
	 */
	static void test_consume(PrioQueue q, String tname, int COUNT ) throws Exception{
	
		long t1 = System.currentTimeMillis();
		int cc = 0;
		while(true){
			MsgIndex mi = q.get("bk", 60000);

			if(mi == null){
//				System.out.println("get failed!");
				continue;
			}
//			System.out.printf("====get: %s\n", mi.getMsgid());
			String msgid = mi.getMsgid();
			if(q.ack(msgid) == null){
				System.out.printf("failed of ack: %s, qsize: %d\n", msgid, q.size());
			}
			cc ++;
			if(cc >= COUNT)
				break;
		}
		
		long t2 = System.currentTimeMillis();
		System.out.printf("count:%d spd of consume:%f  time:%d  %s\n", cc, cc*1000.0/(t2-t1), t2-t1, tname);
	
	}


	static void test_produce(PrioQueue q, String tname , int COUNT, String[] gp) throws Exception{
		

//		int[] pp = new int[]{1, 2, 3, 4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
		int[] pp = new int[]{1}; //优先级表
		

		
		Random rand = new Random();
		rand.setSeed(System.currentTimeMillis());
		long S = System.currentTimeMillis();
		
		long t1 = System.currentTimeMillis();
		int cp = 0;
		for(int i=0; i<COUNT; i++){
				MsgIndex x = new MsgIndex();
				String msgid = S + "." + tname + "." + i;
				x.setMsgid(msgid);
				x.setPriority(pp[rand.nextInt(pp.length)]);
				x.setGroupid(gp[rand.nextInt(gp.length)]);
				x.setData(x.getGroupid() + "-" + x.getPriority());
				boolean ret = q.add(x);
				if(ret){
					cp ++;
//					System.out.println("add:"+msgid);
				}else
					System.out.println("add failed");
		}
		
		long t2 = System.currentTimeMillis();

		System.out.printf("count:%d spd of produce:%f  time:%d   %s\n",  cp, cp*1000.0/(t2-t1), t2-t1, tname);
	}
	
	static void test_normal(PrioQueue q){
			long S = System.currentTimeMillis();
			String tname = Thread.currentThread().getName();
			MsgIndex x = new MsgIndex();
			String msgid = S + "." + 1 + "." + tname;
			x.setMsgid(msgid);
			x.setPriority(100);
			x.setGroupid("groupno");
			x.setData(x.getGroupid() + "-" + x.getPriority());
			boolean ret = q.add(x);
			System.out.println("produce return: "+ret + " msgid:"+msgid);

			MsgIndex mi = q.get("bk", 60000);
			if(mi == null){
				System.out.println("get failed!");
				return;
			}
			msgid = mi.getMsgid();
			System.out.println("fetch return: "+msgid);
			
			q.rollback(msgid, 0L);
			if(q.ack(msgid) == null){
				System.out.printf("failed of ack: %s, qsize: %d\n", msgid, q.size());
			}else{
				System.out.println("ack success:"+q.sending());
			}
	}

    final static String topic_id = "topic";
    final static String cli_id   = "cli";
    private PrioQueue q;
    private StoreOperThreads thds;
    private Store store;

    @Before
    public void init() {
        q = new PrioQueue(cli_id,  topic_id,  20);
        thds = new StoreOperThreads();

        thds.start();
        q.ready();

        q.setStoreOperThreads(thds);
        q.setConfig(new BLEConfig());
        store = new StoreTestImpl();
        q.setStore(store);
        q.start();
        System.out.println("initialize done!!!!!!!!");
    }

    private String produce_one() {
        long S = System.currentTimeMillis();
        String tname = Thread.currentThread().getName();
        MsgIndex x = new MsgIndex();
        String msgid = S + "." + 1 + "." + tname;
        x.setMsgid(msgid);
        x.setPriority(100);
        x.setGroupid("groupno");
        x.setData(x.getGroupid() + "-" + x.getPriority());
        boolean ret = q.add(x);
        return msgid;
    }

    /**
     * 测试单个消息的生产和消费， 正常流程
     */
    @Test
    public void test_produce_and_consume(){
        String msgid = produce_one();
        Assert.assertEquals(q.size(), 1);

        MsgIndex mi = q.get("bk", 30000);
        Assert.assertNotEquals(mi, null);

        Assert.assertEquals(msgid, mi.getMsgid());

        MsgIndex mi1 = q.ack(msgid);
        System.out.println("mi is null:" + mi1 == null);

        Assert.assertEquals(q.size(), 0);
    }

    @Test
    public void test_toomany_retry(){
        String msgid = produce_one();
        Assert.assertEquals(q.size(), 1);

        MsgIndex mi = null;
        int i = 0;

        int last_retry = 0;
        while(true){
            mi = q.get("brokerid", 30000);
            if(mi == null)
                break;
            i++;
            last_retry = mi.getRetry();
            q.rollback(mi.getMsgid(), 0);
        }
        Assert.assertEquals(i, last_retry);
        Assert.assertEquals(q.size(), 0);
    }

	
	public static void main(String[] args) throws Exception{

		final PrioQueue q = new PrioQueue(cli_id,  topic_id,  20);
		StoreOperThreads thds = new StoreOperThreads();
		thds.start();
		q.ready();

		q.setStoreOperThreads(thds);
		q.start();

		
		int tcount = 40;
		final  int COUNT = 50000;
		//压力测试
		int GroupCOUNT = 1;
		
		// 分组表
		final String[] gp = new String[GroupCOUNT];
		int seed = 10000;
		for(int i=0; i<GroupCOUNT; i++){
			gp[i] = String.valueOf(seed+i);
		}
		
		for(int i=0; i<60; i++)
			test_produce(q, String.valueOf(i), COUNT, gp);
		for(int i=0; i<60; i++)
			test_consume(q, String.valueOf(i), COUNT);
		if(true) return;
		
		Thread[] th = new Thread[tcount];
		for(int i=0; i<tcount; i++){
			final int x = i;
			th[i] = new Thread(){
				public void run() {
					String tname = currentThread().getName();
					try {
						if(x %2 == 0)
							test_consume(q, tname, COUNT);
						else
							test_produce(q, tname, COUNT, gp);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			th[i].start();
		}

//		while(true){
//			System.out.printf("==size:%d  q: %d  sent: %d  max: %d\n", 
//					q.size(), q.queue.size(), q.nsent, q.nmax);
//			Thread.sleep(5*1000);
//		}
	}
	
	private static int findTableId(String msgid){
		int p1 = msgid.lastIndexOf("::");
		if(p1 <= 0) return -1;
		int p2 = msgid.lastIndexOf("::", p1-1);
		if(p2 <=0) return -1;
		return Integer.parseInt(msgid.substring(p2+2, p1));
	}
	public static void main2(String[] args){
		String msgid = "123::2277::33";
		System.out.println("===:"+findTableId(msgid));
	}

}
