package com.sitech.crmpd.idmm2.client;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.PullCode;
import com.sitech.crmpd.idmm2.client.pool.PooledMessageContextFactory;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年5月7日 下午11:10:10
 */
public class PooledProducer {

	/** name="{@link com.sitech.crmpd.idmm2.client.PooledProducer}" */
	private static final Logger LOGGER = LoggerFactory.getLogger(PooledProducer.class);
	private static final GenericKeyedObjectPoolConfig CONFIG = new GenericKeyedObjectPoolConfig();
	static {
		System.setProperty("idmm2.pool.testOnBorrow", "true");

		if (!Strings.isNullOrEmpty(System.getProperty("idmm2.pool.maxTotalPerKey"))) {
			CONFIG.setMaxTotalPerKey(Integer.getInteger("idmm2.pool.maxTotalPerKey"));
		}
		if (!Strings.isNullOrEmpty(System.getProperty("idmm2.pool.maxIdlePerKey"))) {
			CONFIG.setMaxIdlePerKey(Integer.getInteger("idmm2.pool.maxIdlePerKey"));
		} else {
			CONFIG.setMaxIdlePerKey(Integer.getInteger("idmm2.pool.maxTotalPerKey",
					CONFIG.getMaxTotalPerKey()));
		}
		if (!Strings.isNullOrEmpty(System.getProperty("idmm2.pool.testOnBorrow"))) {
			CONFIG.setTestOnBorrow(Boolean.parseBoolean(System
					.getProperty("idmm2.pool.testOnBorrow")));
		}
		if (!Strings.isNullOrEmpty(System.getProperty("idmm2.pool.testWhileIdle"))) {
			CONFIG.setTestWhileIdle(Boolean.parseBoolean(System
					.getProperty("idmm2.pool.testWhileIdle")));
		}
		if (!Strings.isNullOrEmpty(System.getProperty("idmm2.pool.jmxEnabled"))) {
			CONFIG.setJmxEnabled(Boolean.getBoolean("idmm2.pool.jmxEnabled"));
		}
		if (!Strings.isNullOrEmpty(System.getProperty("idmm2.pool.timeBetweenEvictionRunsMillis"))) {
			// 检测线程运行时间
			CONFIG.setTimeBetweenEvictionRunsMillis(Long
					.getLong("idmm2.pool.timeBetweenEvictionRunsMillis"));
		}
		if (!Strings.isNullOrEmpty(System.getProperty("idmm2.pool.minEvictableIdleTimeMillis"))) {
			// 对象过期时间
			CONFIG.setMinEvictableIdleTimeMillis(Long
					.getLong("idmm2.pool.minEvictableIdleTimeMillis"));
		}
		if (!Strings.isNullOrEmpty(System.getProperty("idmm2.pool.blockWhenExhausted"))) {
			CONFIG.setBlockWhenExhausted(Boolean.parseBoolean(System
					.getProperty("idmm2.pool.blockWhenExhausted")));
		}
	}

	/**
	 *
	 * @param args
	 *            入参
	 */
	public static void main(String[] args) {
		final Configuration configuration = new Configuration();
		final JCommander jcmdr = new JCommander(configuration);
		try {
			jcmdr.parse(args);
		} catch (final ParameterException e) {
			System.err.println(e.getMessage());
			jcmdr.usage();
			System.exit(1);
		}
		System.out.println(JSON.toJSON(configuration));
		final KeyedObjectPool<String, MessageContext> pool = new GenericKeyedObjectPool<String, MessageContext>(
				new PooledMessageContextFactory(configuration.getZookeeper(), 60000), CONFIG);

		final String clientId = configuration.getClientId();

		if (Strings.isNullOrEmpty(configuration.getTargetTopic())) {
			final long count = configuration.getCount();
			long current = 0;
			final long sleep = configuration.getSleep();
			final boolean isCompress = Boolean.parseBoolean(configuration.getCompress());
			while (current++ < count) {
				try (MessageContext context = pool.borrowObject(clientId)) {
					final Message message = Message.create(Files.toString(
							new File(configuration.getFile()), StandardCharsets.UTF_8));
					if (isCompress) {
						message.setProperty(PropertyOption.COMPRESS, true);
					}
					final String id = context.send(configuration.getTopic(), message);
					LOGGER.debug("{}", id);
					context.commit(id);
				} catch (final Exception e) {
					e.printStackTrace();
				}
				if (sleep > 0) {
					try {
						TimeUnit.MILLISECONDS.sleep(sleep);
					} catch (final InterruptedException e) {}
				}
			}
		} else {
			try (MessageContext context = pool.borrowObject(clientId)) {
				String lastMessageId = null;
				PullCode code = null;
				final String description = "消费成功";
				while (true) {
					final Message message = context.fetch(configuration.getTargetTopic(), 60,
							lastMessageId, code, description, true);
					System.out.println(message.getContentAsUtf8String());
					lastMessageId = message.getId();
					code = PullCode.COMMIT;
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		pool.close();
	}

}
