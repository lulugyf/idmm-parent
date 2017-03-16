package com.sitech.crmpd.idmm2.client;

import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.PullCode;
import com.sitech.crmpd.idmm2.client.api.ResultCode;
import com.sitech.crmpd.idmm2.client.pool.PooledMessageContextFactory;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月3日 下午5:31:52
 */
public class Consumer {

	/**
	 *
	 * @param args
	 *            入参
	 */
	public static void main(String[] args) {
		final String topic = "TUrStatusToOboss";
		final long processingTime = 60;
		final KeyedObjectPool<String, MessageContext> pool = new GenericKeyedObjectPool<String, MessageContext>(
				new PooledMessageContextFactory("172.21.3.101:22181", 60000));
		try (MessageContext context = pool.borrowObject("Sub108")) {
			String lastMessageId = null;
			PullCode code = null;
			String description = "消费成功";
			while (true) {
				final Message message = context.fetch(topic, processingTime, lastMessageId, code,
						description, false);
				final ResultCode resultCode = ResultCode.valueOf(message
						.getProperty(PropertyOption.RESULT_CODE));
				if (resultCode == ResultCode.NO_MORE_MESSAGE) {
					try {
						TimeUnit.SECONDS.sleep(3);
					} catch (final InterruptedException e) {}
					lastMessageId = null;
					code = null;
					description = null;
					continue;
				}
				System.out.println(message);
				System.out.println(message.getContentAsString());
				System.out.println(message.getContentAsUtf8String());
				lastMessageId = message.getId();
				code = PullCode.COMMIT;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
