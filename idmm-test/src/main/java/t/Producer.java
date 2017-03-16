package t;

import java.util.concurrent.TimeUnit;

import com.sitech.crmpd.idmm2.client.MessageContext;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;

import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.pool.PooledMessageContextFactory;

/**
 *
 */
public class Producer {

	/**
	 * 
	 * @param args
	 *           测试生产者
	 */
	public static void main(String[] args) {
		System.out.println("-----------------------begin....");
		final KeyedObjectPool<String, MessageContext> pool = new GenericKeyedObjectPool<String, MessageContext>(
				new PooledMessageContextFactory("127.0.0.1:2181/idmm2/broker", 60000));
		System.out.println("-----------------------start");
		long t1, t2;
		try {
			final PropertyOption<String> MESSAGE_PART = PropertyOption.valueOf("msg_part");
			final MessageContext context = pool.borrowObject("Pub101");
			for (int i = 0; i < 1; i++) {
				final Message message = Message.create("I am here! 序号:"+i);
				message.setProperty(PropertyOption.valueOf("msg_part"),  "12");
				message.setProperty(PropertyOption.GROUP,  "123");
				//message.setProperty(PropertyOption.EXPIRE_TIME, System.currentTimeMillis()+60*1000);
				//message.setProperty(PropertyOption.EFFECTIVE_TIME, System.currentTimeMillis()+60*1000);
//				message.setProperty(PropertyOption.REPLY_TO, "notice_1");
				
				t1 = System.currentTimeMillis();
				final String id = context.send("TRecOprCntt", message);
				context.commit(id);
				t2 = System.currentTimeMillis();
				System.out.println("id=" + id + " -- "+(t2-t1));
				//TimeUnit.SECONDS.sleep(1);
				//context.commit(id);
			}
			pool.returnObject("Pub101", context);
			// context.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		pool.close();
	}
}
