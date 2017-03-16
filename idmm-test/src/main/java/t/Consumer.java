package t;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.sitech.crmpd.idmm2.client.MessageContext;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.PullCode;
import com.sitech.crmpd.idmm2.client.api.ResultCode;
import com.sitech.crmpd.idmm2.client.pool.PooledMessageContextFactory;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015�?4�?3�? 下午5:31:52
 */
public class Consumer {
	public static void test_temp(){
//		String s = "abc %d 123";
//		System.out.println(s.replace("%d", String.valueOf(5)));
		String s1 = "123,456";
		System.out.println(s1.split(",")[1]);
	}
	public static void main(String[] args) throws Exception{
//		test_temp();
//		test_err1();
//		test_nocommit(10, -1);
		consume_all(-1);
		
//		test_seq();

//		test_q();
	}

	public static void test_q() {
		ArrayBlockingQueue<Object> answers = new ArrayBlockingQueue<Object>(10);
		try {
			Object op = answers.poll(5, TimeUnit.SECONDS);
			if(op == null)
				System.out.println("NULL");
			else
				System.out.println("Success");
		} catch (InterruptedException e) {
			System.out.println("need more request objects:"+e);
		}catch(Exception e){
			System.out.println("take a request Object failed:"+e);
		}
	}
	
	private static String target_topic = "TRecOprCnttDest";
	private static String consumer_id = "Sub119Opr";
	
	private static String src_topic = "TRecOprCntt";
	private static String producer_id = "Pub101";
	private static String zkaddr = "127.0.0.1:2181";
	
	public static void test_seq() throws Exception{
		final KeyedObjectPool<String, MessageContext> pool = new GenericKeyedObjectPool<String, MessageContext>(
				new PooledMessageContextFactory(zkaddr, 60000));
		MessageContext context_producer = pool.borrowObject(producer_id);
		MessageContext context_consumer = pool.borrowObject(consumer_id);
		try{
			seq_produce(context_producer);
			
			Message msg1 = context_consumer.fetch(target_topic, 60, null, null, "");
			System.out.printf("1-- fetch return %s messageid:  %s\n", seq_result(msg1),  msg1.getId());
			
			Message msg2 = context_consumer.fetch(target_topic,  60,  msg1.getId(), PullCode.ROLLBACK, "rollback");
			System.out.printf("rollback return %s\n", seq_result(msg2));
			
			msg1 = context_consumer.fetch(target_topic, 60, null, null, "");
			System.out.printf("1-- fetch return %s messageid:  %s\n", 	seq_result(msg1),  msg1.getId());
			Message msg3 = context_consumer.fetch(target_topic,  60,  msg1.getId(), PullCode.ROLLBACK_AND_NEXT, "ROLLBACK_AND_NEXT");
			System.out.printf("rollback_and_next return %s, msgid: %s\n", seq_result(msg3), msg3.getId());
			
			msg1 = context_consumer.fetch(target_topic, 60, null, null, "");
			System.out.printf("1-- fetch return %s messageid:  %s\n", 	seq_result(msg1),  msg1.getId());
			Message msg4 = context_consumer.fetch(target_topic,  60,  msg1.getId(), PullCode.COMMIT, "commit");
			System.out.printf("commit return %s\n", seq_result(msg4));

			seq_produce(context_producer);
			
			msg1 = context_consumer.fetch(target_topic, 60, null, null, "");
			System.out.printf("1-- fetch return %s messageid:  %s\n", 	seq_result(msg1),  msg1.getId());
			Message msg5 = context_consumer.fetch(target_topic,  60,  msg1.getId(), PullCode.COMMIT_AND_NEXT, "COMMIT_AND_NEXT");
			System.out.printf("rollback_and_next return %s, msgid: %s\n", seq_result(msg5), msg5.getId());

//			msg1 = context_consumer.fetch(target_topic, 60, null, null, "");
//			System.out.printf("1-- fetch return %s messageid:  %s\n", 	seq_result(msg1),  msg1.getId());
//			Message msg6 = context_consumer.fetch(target_topic,  60,  msg1.getId(), PullCode.ROLLBACK_BUT_RETRY, "ROLLBACK_BUT_RETRY");
//			System.out.printf("rollback_and_next return %s, msgid: %s\n", seq_result(msg6), msg6.getId());

			seq_consumNoCommit(context_consumer);
			
			System.out.println("-------------- waiting 60s");
			Thread.sleep(60*1000L);
			
			seq_consumAll(context_consumer);
		}finally{
			if(context_producer != null)
				pool.returnObject(producer_id, context_producer);
			if(context_consumer != null)
				pool.returnObject(consumer_id, context_consumer);
			pool.close();
		}
	}
	private static ResultCode seq_result(Message msg){
		return ResultCode.valueOf(msg.getProperty(PropertyOption.RESULT_CODE));
	}
	
	private static void seq_consumAll(MessageContext context) throws Exception{
		PullCode code = null;
		String lastMsgId = null;
		String description = "success";
		int c = 0;
		while (true) {
			Message msg3 = context.fetch(target_topic, 60, lastMsgId, code,
					description, false);
			final ResultCode resultCode = seq_result(msg3);
			if (resultCode == ResultCode.NO_MORE_MESSAGE) {
				System.out.println("no more message break. count:"+c);
				break;
			}
			lastMsgId = msg3.getId();
			if(lastMsgId == null){System.out.println("messageid is null, break");
					break; }
			System.out.println("messageid: "+lastMsgId);
			c++;

			code = PullCode.COMMIT_AND_NEXT;
			
		}
		System.out.printf("consume total: %d\n", c);
	}

	private static void seq_consumNoCommit(MessageContext context) throws Exception{
		PullCode code = null;
		String lastMsgId = null;
		String description = "success";
		int c = 0;
		while (true) {
			Message msg3 = context.fetch(target_topic, 60, lastMsgId, code,
					description, false);
			final ResultCode resultCode = seq_result(msg3);
			if (resultCode == ResultCode.NO_MORE_MESSAGE) {
				System.out.println("no more message break. count:"+c);
				break;
			}
			if(msg3.getId() == null){System.out.println("messageid is null, break");
					break; }
			System.out.println("messageid: "+msg3.getId());
			c++;
		}
		System.out.printf("consume no commit total: %d\n", c);
	}
	
	private static void seq_produce(MessageContext context ) {
//		final KeyedObjectPool<String, MessageContext> pool = new GenericKeyedObjectPool<String, MessageContext>(
//				new PooledMessageContextFactory(zkaddr, 60000));
		long t1, t2;
		try {
			final PropertyOption<String> MESSAGE_PART = PropertyOption.valueOf("msg_part");
//			final MessageContext context = pool.borrowObject(producer_id);
			for (int i = 0; i < 20; i++) {
				final Message message = Message.create("I am here!好的:"+i);
				message.setProperty(PropertyOption.valueOf("msg_part"),  MESSAGE_PART);
				message.setProperty(PropertyOption.GROUP,  ""+i);
				
				t1 = System.currentTimeMillis();
				final String id = context.send(src_topic, message);
				context.commit(id);
				t2 = System.currentTimeMillis();
				System.out.println("id=" + id + " -- "+(t2-t1));
				//TimeUnit.SECONDS.sleep(1);
				//context.commit(id);
			}
			// context.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static String fetchOne(MessageContext context) throws Exception{
		String lastMessageId = null;
		PullCode code = null;
		String description = "消费成功";
		final Message message = context.fetch(target_topic, 60, lastMessageId, code,
				description, false);
		final ResultCode resultCode = ResultCode.valueOf(message
				.getProperty(PropertyOption.RESULT_CODE));
		if (resultCode == ResultCode.NO_MORE_MESSAGE) {
			System.out.println("no more message");
			return null;
		}
		if(resultCode != ResultCode.OK){
			System.out.println("other error while fetch! "+resultCode);
			return null;
		}
		int retry = -1;
		if(message.existProperty(PropertyOption.CONSUMER_RETRY)){
			retry = message.getIntegerProperty(PropertyOption.CONSUMER_RETRY);
			System.out.println("message retry: "+ retry);
		}
		
		return message.getId();
	}
	
	public static void test_nocommit(long timeout, long count)  throws Exception{
		final long processingTime = timeout;
		final KeyedObjectPool<String, MessageContext> pool = new GenericKeyedObjectPool<String, MessageContext>(
				new PooledMessageContextFactory(zkaddr, 120000));
		MessageContext context = pool.borrowObject(consumer_id) ;
			String lastMessageId = null;
			PullCode code = null;
			String description = "消费成功";
			for(int i=0; i<count || count < 0; i++){
				long t1 = System.currentTimeMillis();
				final Message message = context.fetch(target_topic, processingTime, lastMessageId, code,
						description, false);
				final ResultCode resultCode = ResultCode.valueOf(message
						.getProperty(PropertyOption.RESULT_CODE));
				if (resultCode == ResultCode.NO_MORE_MESSAGE) {
					System.out.println("no more message, sleep 5s");
					Thread.sleep(5000L);
					lastMessageId = null;
					code = null;
					continue;
				}
//				code = PullCode.ROLLBACK_AND_NEXT; //此操作丢弃当前消息， 并取下一�?
				long t2 = System.currentTimeMillis();
				int retry = -1;
				if(message.existProperty(PropertyOption.CONSUMER_RETRY))
				 retry = message.getIntegerProperty(PropertyOption.CONSUMER_RETRY);
				lastMessageId = message.getId();
				System.out.printf("messageid:  %s  elapse: %d  retry: %d\n", lastMessageId , (t2-t1), retry);
				lastMessageId = null;
			}
			pool.clear();

	}

	public static void test_err1()  throws Exception{
		final String topic = "TRecOprCnttDest";
		final long processingTime = 30;
		final KeyedObjectPool<String, MessageContext> pool = new GenericKeyedObjectPool<String, MessageContext>(
				new PooledMessageContextFactory("127.0.0.1:2181/idmm2_gyf/broker", 60000));
		MessageContext context = pool.borrowObject("Sub119Opr") ;
			String lastMessageId = null;
			PullCode code = null;
			String description = "消费成功";
			for(int i=0; i<2; i++){
				long t1 = System.currentTimeMillis();
				final Message message = context.fetch(topic, processingTime, lastMessageId, code,
						description, false);
				final ResultCode resultCode = ResultCode.valueOf(message
						.getProperty(PropertyOption.RESULT_CODE));
				if (resultCode == ResultCode.NO_MORE_MESSAGE) {
//					try {
////						TimeUnit.SECONDS.sleep(3);
//						System.out.println("sleep 3s");
//					} catch (final InterruptedException e) {}
					lastMessageId = null;
					code = null;
					description = null;
					continue;
				}
				lastMessageId = message.getId();
				code = PullCode.ROLLBACK_AND_NEXT; //此操作丢弃当前消息， 并取下一�?
				long t2 = System.currentTimeMillis();
				int retry = -1;
				if(message.existProperty(PropertyOption.CONSUMER_RETRY))
				 retry = message.getIntegerProperty(PropertyOption.CONSUMER_RETRY);
				System.out.printf("messageid:  %s  elapse: %d  retry: %d\n", lastMessageId , (t2-t1), retry);
			}
			pool.clear();

	}

	public static void test_err()  throws Exception{
		final String topic = "TRecOprCnttDest";
		final long processingTime = 20;
		final KeyedObjectPool<String, MessageContext> pool = new GenericKeyedObjectPool<String, MessageContext>(
				new PooledMessageContextFactory("127.0.0.1:2181/idmm2_gyf/broker", 60000));
		MessageContext context = pool.borrowObject("Sub119Opr") ;
			String lastMessageId = null;
			PullCode code = null;
			String description = "消费成功";
			while (true) {
				long t1 = System.currentTimeMillis();
				final Message message = context.fetch(topic, processingTime, lastMessageId, code,
						description, false);
				final ResultCode resultCode = ResultCode.valueOf(message
						.getProperty(PropertyOption.RESULT_CODE));
				if (resultCode == ResultCode.NO_MORE_MESSAGE) {
					try {
						TimeUnit.SECONDS.sleep(3);
						System.out.println("sleep 3s");
					} catch (final InterruptedException e) {}
					lastMessageId = null;
					code = null;
					description = null;
					continue;
				}
				lastMessageId = message.getId();
//				code = PullCode.COMMIT_AND_NEXT;  //确认当前消息，  并取下一条
				code = PullCode.ROLLBACK_AND_NEXT; //此操作丢弃当前消息， 并取下一条
				long t2 = System.currentTimeMillis();
				System.out.println("messageid: "+lastMessageId + "  " + (t2-t1));
				
			}

	}

	/**
	 *
	 * @param count
	 *            入参
	 */
	public static void consume_all(int count) {
		final String topic = target_topic;
		final long processingTime = 60;
		final KeyedObjectPool<String, MessageContext> pool = new GenericKeyedObjectPool<String, MessageContext>(
				new PooledMessageContextFactory(zkaddr, 60000));
		try (MessageContext context = pool.borrowObject(consumer_id)) {
			String lastMessageId = null;
			PullCode code = null;
			String description = "消费成功";
			int c = 0;
			while (true) {
				long t1 = System.currentTimeMillis();
				final Message message = context.fetch(topic, processingTime, lastMessageId, code,
						description, false);
				final ResultCode resultCode = ResultCode.valueOf(message
						.getProperty(PropertyOption.RESULT_CODE));
				if (resultCode == ResultCode.NO_MORE_MESSAGE) {
					try {
						TimeUnit.SECONDS.sleep(10);
						System.out.println("============= no more message, sleep 10s");
						lastMessageId = null;
						code = null;
						continue;
					} catch (final InterruptedException e) {}
//					lastMessageId = null;
//					code = null;
//					description = null;
//					continue;
//					System.out.println("no more message break. count:"+c);
//					break;
				}
				lastMessageId = message.getId();
				if(lastMessageId == null)
						break;
				long t2 = System.currentTimeMillis();
//				System.out.println(message);
//				System.out.println(message.getContentAsString());
				System.out.println("messageid: "+lastMessageId + "  " + (t2-t1));
				c++;
				if(count > 0 &&  c >= count){
					code = PullCode.COMMIT;
				}else{
					code = PullCode.COMMIT_AND_NEXT;
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}








}
