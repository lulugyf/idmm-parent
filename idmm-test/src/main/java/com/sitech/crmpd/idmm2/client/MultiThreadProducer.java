package com.sitech.crmpd.idmm2.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import com.google.common.base.Strings;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.exception.OperationException;
import com.sitech.crmpd.idmm2.client.pool.PooledMessageContextFactory;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年3月13日 下午8:33:11
 */
public class MultiThreadProducer implements Runnable {

	private static final GenericKeyedObjectPoolConfig CONFIG = new GenericKeyedObjectPoolConfig();
	static {
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

	private static final KeyedObjectPool<String, MessageContext> POOL = new GenericKeyedObjectPool<String, MessageContext>(
			new PooledMessageContextFactory(System.getProperty("brokerURL", "172.21.3.98:22181"),
					60000), CONFIG);

	private String clientID;
	private String topic;
	private CountDownLatch endLatch;
	private int index;
	private AtomicInteger counter;

	MultiThreadProducer(String clientID, String topic, AtomicInteger counter,
			CountDownLatch endLatch, int index) throws OperationException {
		super();
		this.clientID = clientID;
		this.topic = topic;
		this.counter = counter;
		this.endLatch = endLatch;
		this.index = index;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final String clientID = System.getProperty("clientID");
		final String topic = System.getProperty("topic");

		if (Strings.isNullOrEmpty(clientID) || Strings.isNullOrEmpty(topic)) {
			System.err.println("[clientID | topic] must be set!");
			System.exit(1);
		}

		final int maximumPoolSize = Integer.getInteger("maximumPoolSize", 500);
		final Thread[] threads = new Thread[maximumPoolSize];
		final int total = Integer.getInteger("total", 1000000);
		final AtomicInteger counter = new AtomicInteger(total);
		final CountDownLatch endLatch = new CountDownLatch(total);
		for (int i = 0; i < maximumPoolSize; i++) {
			try {
				threads[i] = new Thread(new MultiThreadProducer(clientID, topic, counter, endLatch,
						i));
				threads[i].setDaemon(true);
			} catch (final OperationException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < maximumPoolSize; i++) {
			threads[i].start();
		}
		try {
			endLatch.await();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (counter.decrementAndGet() > 0) {
			MessageContext context = null;
			try {
				context = POOL.borrowObject(clientID);
				final Message message = Message.create("This is :" + index);
				final String id = context.send(topic, message);
				System.out.println(id);
				context.commit(id);
			} catch (final Exception e) {
				e.printStackTrace();
			} finally {
				if (context != null) {
					try {
						POOL.returnObject(clientID, context);
					} catch (final Exception e) {}
				}
				endLatch.countDown();
			}
		}
	}
}
