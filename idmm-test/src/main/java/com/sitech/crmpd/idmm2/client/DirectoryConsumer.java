package com.sitech.crmpd.idmm2.client;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

import com.google.common.io.Files;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.PullCode;
import com.sitech.crmpd.idmm2.client.pool.PooledMessageContextFactory;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月3日 下午5:31:52
 */
public class DirectoryConsumer {

	public static void main(String[] args) {
		final String storeDir = "";
		final String topic = "TUrStatusToOboss";
		final long processingTime = 60;
		final KeyedObjectPool<String, MessageContext> pool = new GenericKeyedObjectPool<String, MessageContext>(
				new PooledMessageContextFactory("172.21.3.101:22181", 60000));
		final PropertyOption<String> filename = PropertyOption.valueOf("filename");
		try (MessageContext context = pool.borrowObject("Sub108")) {
			final String lastMessageId = null;
			PullCode code = null;
			final String description = "消费成功";
			while (true) {
				final Message message = context.fetch(topic, processingTime, lastMessageId, code,
						description, true);
				if (message.existProperty(filename)) {
					final String name = message.getStringProperty(filename);
					Files.append(message.getContentAsUtf8String(), new File(storeDir, name),
							StandardCharsets.UTF_8);
				}
				code = PullCode.COMMIT;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
