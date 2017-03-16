package com.sitech.crmpd.idmm2.ble;

import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sitech.crmpd.idmm2.client.BasicMessageContext;
import com.sitech.crmpd.idmm2.client.api.FrameMessage;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageType;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.PullCode;
import com.sitech.crmpd.idmm2.client.api.ResultCode;


/*
 * 
 * 
insert into topic_subscribe_rel_8(client_id, dest_topic_id, client_pswd, max_request, min_timeout, max_timeout, use_status, login_no, opr_time, note)
    values('30000001', '20000001', '1', 10, 30, 600, '1', 'a', now(), 'aa');
insert into topic_subscribe_rel_8 values('30000002', '20000001', '1', 10, 30, 600, '1', 'a', now(), 'aa');

-- ble 分担的目标主题关系
insert into ble_dest_topic_rel_8(dest_topic_id, BLE_id, use_status, login_no,opr_time, note ) 
           values('20000001', '10000001', '1', 'aa', 1111, '111');
insert into ble_dest_topic_rel_8 values('20000002', '10000001', '1', 'aa', 1111, '111');

INSERT INTO topic_subscribe_rel_8 VALUES('30000005', '20000003', '1', 10, 30, 600, '1', 'a', NOW(), 'aa');
insert into ble_dest_topic_rel_8 values('20000003', '10000002', '1', 'aa', 1111, '111');
 */
public class BLEClient {
	private static final Logger log = LoggerFactory.getLogger(BLEClient.class);
//	protected static String ble_ip = "127.0.0.1";
//	protected static int ble_port = 5678;
//	
//	private static String clientid = "30000005";
//	private static String target_topic = "20000003";
//	private static String bleid = "10000002";
	
	protected static String ble_ip = "10.162.200.221";
	protected static int ble_port = 8765;
	
	private static String clientid = "Sub_test1";
	private static String target_topic = "TopictestDest1";
	private static String bleid = "10000002";
	
	
	private BasicMessageContext connect() throws Exception{
		BasicMessageContext c = new BasicMessageContext(ble_ip, ble_port, 5000);
		Message msg = Message.create();
		msg.setProperty(PropertyOption.CLIENT_ID, "brokerid");
		Message msgr = c.trade(bleid, MessageType.QUERY, msg);
		log.info("Query return {}",
				ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE)));
		return c;
	}

	public void produce_force(int count) throws Exception {
		String threadname = Thread.currentThread().getName();
		BasicMessageContext c = connect();
		
		long msgid_seed = System.currentTimeMillis();
		int  id11 = 0;//(int)(Math.random() * 1000000.0);
		int priority = 100;

		Message msg = Message.create();
		String msgid = threadname + "." + msgid_seed + "." + (++id11);//String.format("%d.%d", msgid_seed, ++id11);
		msg.setProperty(PropertyOption.CLIENT_ID, clientid);
		msg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
		msg.setProperty(PropertyOption.MESSAGE_ID, msgid);
		msg.setProperty(PropertyOption.PRIORITY, priority);
		Message msgr = c.trade(clientid, MessageType.SEND_COMMIT, msg);
		log.info("send-commit msg {} return {}",
				msgid, ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE)));
		

		for(int i=1; i<count; i++){
			msg.setProperty(PropertyOption.PRIORITY,   i%priority);
			msgid = threadname + "." + msgid_seed + "." + (++id11); //String.format("%d.%d", msgid_seed, ++id11);
			msg.setProperty(PropertyOption.MESSAGE_ID, msgid);
			msg.setProperty(PropertyOption.GROUP, String.valueOf(i));
			msgr = c.trade(clientid, MessageType.SEND_COMMIT, msg);
//			log.info("send-commit msg {} return {}",
//					msgid, ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE)));
			System.out.println(msgid + "  " + System.currentTimeMillis()/1000);

		}
		c.close();
	}
	
	
	public void consume_force() throws Exception {
		BasicMessageContext c = connect();
		
		Message pmsg = Message.create();
		Message cmsg = Message.create();
		
		log.info("=====clientid: {}", clientid);
		pmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
		Message msgr = c.trade(clientid, MessageType.PULL, pmsg);
		String msgid = null;
		
		ResultCode rcode = ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE) );
		
		int counter = 0;
		
		log.info("pull msg {} return {}  {}", msgid, rcode, rcode == ResultCode.OK);
		//  测试消费的正常流程， 连接后发pull， 返回消息后commit，commit成功后再发pull，返回no-more-message 则终止
		while(true){
			if(rcode != ResultCode.OK){
				System.out.printf("====NO_MORE_MESSAGE :%s\n", rcode);
				Thread.sleep(3000L);
			}else if(rcode == ResultCode.OK){
				msgid = null;
				if(msgr.existProperty(PropertyOption.MESSAGE_ID))
					msgid = msgr.getStringProperty(PropertyOption.MESSAGE_ID);
				if(msgid != null){
					counter ++;
					cmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
					cmsg.setProperty(PropertyOption.PULL_CODE, PullCode.COMMIT_AND_NEXT); //PullCode.COMMIT);
					cmsg.setProperty(PropertyOption.MESSAGE_ID, msgid);
					msgr = c.trade(clientid, MessageType.PULL, cmsg);
//					log.info("pull commit msg {} return {}",
//							msgid, ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE)));
					System.out.println(msgid + "  " + System.currentTimeMillis()/1000);
				}else {
					log.info("===not found Message-id");
				}
			}
		}
//		log.info("consume message count: {}", counter);
//		c.close();
	}
	
	
	//////////////////////////////////////////////
	// function test
	public void produce(BasicMessageContext c) throws Exception{

		
		long msgid_seed = System.currentTimeMillis();
		int  id11 = 0;//(int)(Math.random() * 1000000.0);
		int priority = 1000;

		Message msg = Message.create();
		String msgid = String.format("%d.%d", msgid_seed, ++id11);
		msg.setProperty(PropertyOption.CLIENT_ID, clientid);
		msg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
		msg.setProperty(PropertyOption.MESSAGE_ID, msgid);
		msg.setProperty(PropertyOption.GROUP, "1398001");
		msg.setProperty(PropertyOption.PRIORITY, priority+100);
		Message msgr = c.trade(clientid, MessageType.SEND_COMMIT, msg);
		log.info("send-commit msg {} return {}",
		msgid, ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE)));

	}
	
	public void consume(BasicMessageContext c) throws Exception {
		// PULL
		Message pmsg = Message.create();
		log.info("=====clientid: {}", clientid);
		pmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
		Message msgr = c.trade(clientid, MessageType.PULL, pmsg);
		String msgid = null;

		ResultCode rcode = ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE) );
		
		if(rcode != ResultCode.OK){
			System.out.printf("====NO_MORE_MESSAGE :%s\n", rcode);
		}else if(rcode == ResultCode.OK){
			msgid = null;
			if(msgr.existProperty(PropertyOption.MESSAGE_ID))
				msgid = msgr.getStringProperty(PropertyOption.MESSAGE_ID);
			if(msgid != null){
				Message cmsg = Message.create();
				//rmsg.setProperty(PropertyOption.CLIENT_ID, clientid);   c.trade 函数中会把 第一个参数设置为 CLIENT_ID
				cmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
				cmsg.setProperty(PropertyOption.PULL_CODE, PullCode.COMMIT); //PullCode.COMMIT  PullCode.COMMIT_AND_NEXT
				cmsg.setProperty(PropertyOption.MESSAGE_ID, msgid);
				msgr = c.trade(clientid, MessageType.PULL, cmsg);
				log.info("pull commit msg {} return {}",
						msgid, ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE)));
			}else {
				log.info("===not found Message-id");
			}
		}

		log.info("pull msg {} return {}  {}", msgid, rcode, rcode == ResultCode.OK);
	}
	
	public void rollback(BasicMessageContext c) throws Exception {
		// PULL
		Message pmsg = Message.create();
		pmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
		Message msgr = c.trade(clientid, MessageType.PULL, pmsg);
		String msgid = null;

		ResultCode rcode = ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE) );

		if(rcode != ResultCode.OK){
			log.info("====NO_MORE_MESSAGE :{}", rcode);
			return;
		}
		
		msgid = null;
		if(msgr.existProperty(PropertyOption.MESSAGE_ID))
			msgid = msgr.getStringProperty(PropertyOption.MESSAGE_ID);
		if(msgid != null){
			log.info("pull msg {} return {}  {}", msgid, rcode, rcode == ResultCode.OK);
			Message cmsg = Message.create();
			//rmsg.setProperty(PropertyOption.CLIENT_ID, clientid);   c.trade 函数中会把 第一个参数设置为 CLIENT_ID
			cmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
			cmsg.setProperty(PropertyOption.PULL_CODE, PullCode.ROLLBACK); //PullCode.COMMIT  PullCode.COMMIT_AND_NEXT
			cmsg.setProperty(PropertyOption.MESSAGE_ID, msgid);
			msgr = c.trade(clientid, MessageType.PULL, cmsg);
			log.info("pull rollback msg {} return {}",
					msgid, ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE)));
		}else {
			log.info("===not found Message-id");
		}
	}
	
	public void commitnext(BasicMessageContext c) throws Exception {
		// PULL
		Message pmsg = Message.create();
		pmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
		Message msgr = c.trade(clientid, MessageType.PULL, pmsg);
		String msgid = null;

		ResultCode rcode = ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE) );

		if(rcode != ResultCode.OK){
			log.info("====NO_MORE_MESSAGE :{}", rcode);
			return;
		}
		
		msgid = null;
		if(msgr.existProperty(PropertyOption.MESSAGE_ID))
			msgid = msgr.getStringProperty(PropertyOption.MESSAGE_ID);
		if(msgid != null){
			log.info("pull msg {} return {}  {}", msgid, rcode, rcode == ResultCode.OK);
			Message cmsg = Message.create();
			//rmsg.setProperty(PropertyOption.CLIENT_ID, clientid);   c.trade 函数中会把 第一个参数设置为 CLIENT_ID
			cmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
			cmsg.setProperty(PropertyOption.PULL_CODE, PullCode.COMMIT_AND_NEXT); //PullCode.COMMIT  PullCode.COMMIT_AND_NEXT
			cmsg.setProperty(PropertyOption.MESSAGE_ID, msgid);
			msgr = c.trade(clientid, MessageType.PULL, cmsg);
			String new_msgid = null;
			if(msgr.existProperty(PropertyOption.MESSAGE_ID))
				new_msgid = msgr.getStringProperty(PropertyOption.MESSAGE_ID);
			log.info("pull commitandnext msg {} return {} newmsgid: {}",
					msgid, ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE)), new_msgid);
		}else {
			log.info("===not found Message-id");
		}
	}

	public void rollbacknext(BasicMessageContext c) throws Exception {
		// PULL
		Message pmsg = Message.create();
		pmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
		Message msgr = c.trade(clientid, MessageType.PULL, pmsg);
		String msgid = null;

		ResultCode rcode = ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE) );

		if(rcode != ResultCode.OK){
			log.info("====NO_MORE_MESSAGE :{}", rcode);
			return;
		}
		
		msgid = null;
		if(msgr.existProperty(PropertyOption.MESSAGE_ID))
			msgid = msgr.getStringProperty(PropertyOption.MESSAGE_ID);
		if(msgid != null){
			log.info("pull msg {} return {}  {}", msgid, rcode, rcode == ResultCode.OK);
			Message cmsg = Message.create();
			//rmsg.setProperty(PropertyOption.CLIENT_ID, clientid);   c.trade 函数中会把 第一个参数设置为 CLIENT_ID
			cmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
			cmsg.setProperty(PropertyOption.PULL_CODE, PullCode.ROLLBACK_AND_NEXT); //PullCode.COMMIT  PullCode.COMMIT_AND_NEXT
			cmsg.setProperty(PropertyOption.MESSAGE_ID, msgid);
			cmsg.setProperty(PropertyOption.CODE_DESCRIPTION, "I don't want it again!!!");
			msgr = c.trade(clientid, MessageType.PULL, cmsg);
			String new_msgid = null;
			if(msgr.existProperty(PropertyOption.MESSAGE_ID))
				new_msgid = msgr.getStringProperty(PropertyOption.MESSAGE_ID);
			log.info("pull ROLLBACK_AND_NEXT msg {} return {} newmsgid: {}",
					msgid, ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE)), new_msgid);
		}else {
			log.info("===not found Message-id");
		}
	}

	public void rollbackretry(BasicMessageContext c) throws Exception {
		// PULL
		Message pmsg = Message.create();
		pmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
		Message msgr = c.trade(clientid, MessageType.PULL, pmsg);
		String msgid = null;

		ResultCode rcode = ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE) );

		if(rcode != ResultCode.OK){
			log.info("====NO_MORE_MESSAGE :{}", rcode);
			return;
		}
		
		msgid = null;
		if(msgr.existProperty(PropertyOption.MESSAGE_ID))
			msgid = msgr.getStringProperty(PropertyOption.MESSAGE_ID);
		if(msgid != null){
			log.info("pull msg {} return {}  {}", msgid, rcode, rcode == ResultCode.OK);
			Message cmsg = Message.create();
			//rmsg.setProperty(PropertyOption.CLIENT_ID, clientid);   c.trade 函数中会把 第一个参数设置为 CLIENT_ID
			cmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
			cmsg.setProperty(PropertyOption.PULL_CODE, PullCode.ROLLBACK_BUT_RETRY);
			cmsg.setProperty(PropertyOption.MESSAGE_ID, msgid);
			cmsg.setProperty(PropertyOption.RETRY_AFTER, 60L);
			msgr = c.trade(clientid, MessageType.PULL, cmsg);
			String new_msgid = null;
			if(msgr.existProperty(PropertyOption.MESSAGE_ID))
				new_msgid = msgr.getStringProperty(PropertyOption.MESSAGE_ID);
			log.info("pull ROLLBACK_BUT_RETRY msg {} return {} newmsgid: {}",
					msgid, ResultCode.valueOf( msgr.getProperty(PropertyOption.RESULT_CODE)), new_msgid);
		}else {
			log.info("===not found Message-id");
		}
	}

	public static void main(String[] args) throws Exception{
		BLEClient b = new BLEClient();
		BasicMessageContext c = b.connect();
		try{
//			b.produce(c);
//			b.rollback(c);
//			b.commitnext(c);
//			b.rollbacknext(c);
//			b.rollbackretry(c);
//			b.produce_force();
			b.consume_force();
//			b.consume(c);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		c.close();
	}
	
	private static void usage(){
		System.out.println("<p|c> <thread-count> [produce-size]");
	}
	public static void main2(String[] args) throws Exception{
		if(args.length < 2){
			usage();
			return;
		}
		ResourceBundle c = ResourceBundle.getBundle("ble_test");
		ble_ip = c.getString("ble_ip");
		ble_port = Integer.parseInt(c.getString("ble_port"));
		target_topic = c.getString("dest_topic");
		clientid = c.getString("dest_client");
		final BLEClient b = new BLEClient();
		int threadcount = Integer.parseInt(args[1]);
		if("p".equals(args[0])){
			final int psize = Integer.parseInt(args[2]);
			for(int i=0; i<threadcount; i++){
				new  Thread(){
					public void run(){
						try {
							b.produce_force(psize);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}.start();
			}
		}else if("c".equals(args[0])){
			for(int i=0; i<threadcount; i++){
				new  Thread(){
					public void run(){
						try {
							b.consume_force();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}.start();
			}
		}
	}
	
}
