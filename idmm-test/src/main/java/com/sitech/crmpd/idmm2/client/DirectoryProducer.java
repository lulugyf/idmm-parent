package com.sitech.crmpd.idmm2.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.pool.PooledMessageContextFactory;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年5月7日 下午11:10:10
 */
public class DirectoryProducer {

	/** name="{@link com.sitech.crmpd.idmm2.client.DirectoryProducer}" */
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryProducer.class);

	/**
	 *
	 * @param args
	 *            入参
	 */
	public static void main(String[] args) {
		final KeyedObjectPool<String, MessageContext> pool = new GenericKeyedObjectPool<String, MessageContext>(
				new PooledMessageContextFactory("172.21.3.101:22181", 60000));
		final String clientID = "Pub1081";
		final File directory = new File("文件目录");
		final PropertyOption<String> filename = PropertyOption.valueOf("filename");
		if (directory.isDirectory()) {
			final File[] children = directory.listFiles();
			final int maxLen = 1024 * 1024;
			final byte[] buffer = new byte[maxLen];
			int len = -1;
			for (final File file : children) {
				try (final InputStream stream = new FileInputStream(file)) {
					while ((len = stream.read(buffer)) != -1) {
						final byte[] content = len == maxLen ? buffer : new byte[len];
						if (len < maxLen) {
							System.arraycopy(buffer, 0, content, 0, len);
						}
						final Message message = Message.create(content);
						message.setProperty(filename, file.getName());
						send(pool, clientID, message);
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void send(KeyedObjectPool<String, MessageContext> pool, String clientID,
			Message message) {
		MessageContext context = null;
		try {
			context = pool.borrowObject(clientID);
			final String id = context.send("TUrStatusToOboss", message);
			context.commit(id);
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			if (context != null) {
				try {
					pool.returnObject(clientID, context);
				} catch (final Exception e) {}
			}
		}
	}
}
