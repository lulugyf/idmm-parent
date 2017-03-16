package com.sitech.crmpd.idmm2.broker.pool;

import java.io.IOException;

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.sitech.crmpd.idmm2.broker.handler.CommitTask;
import com.sitech.crmpd.idmm2.client.BasicMessageContext;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageType;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.ResultCode;
import com.sitech.crmpd.idmm2.client.exception.OperationException;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月5日 下午10:35:19
 */
public class TopicKeyedPooledMessageContextFactory implements
		KeyedPooledObjectFactory<Key, BasicMessageContext> {

	@Value("${ble.timeout:60000}")
	private int timeout;
	@Value("${ble.maxFailed:3}")
	private int maxFailed;
	private int FailCount = 0;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TopicKeyedPooledMessageContextFactory.class);

	/**
	 * @see org.apache.commons.pool2.KeyedPooledObjectFactory#makeObject(java.lang.Object)
	 */
	@Override
	public PooledObject<BasicMessageContext> makeObject(Key key)
			throws Exception {
		BasicMessageContext context = null;
		try {
			context = new BasicMessageContext(key.getHost(), key.getPort(),
					timeout);
			final Message message = context.trade(key.getHost(),
					MessageType.QUERY, Message.create());
			final ResultCode code = ResultCode.valueOf(message
					.getProperty(PropertyOption.RESULT_CODE));
			if (code != ResultCode.OK) {
				throw new OperationException(code);
			}

			DefaultPooledObject<BasicMessageContext> poolObject = new DefaultPooledObject<BasicMessageContext>(
					context);
			FailCount = 0;
			return poolObject;
		} catch (final IOException e) {
			FailCount++;
			LOGGER.error("cannot connect to server.{}:{}", key.getHost(),
					key.getPort(), e);
			if (FailCount > maxFailed) {
				LOGGER.error("exceed max {} retry!process exit!", maxFailed, e);
				System.exit(0);
			}
			throw e;
		} catch (final Exception e) {
			if (context != null) {
				context.close();
			}
			throw e;
		}
	}

	/**
	 * @see org.apache.commons.pool2.KeyedPooledObjectFactory#destroyObject(java.lang.Object,
	 *      org.apache.commons.pool2.PooledObject)
	 */
	@Override
	public void destroyObject(Key key, PooledObject<BasicMessageContext> p)
			throws Exception {
		final BasicMessageContext context = p.getObject();
		if (context != null) {
			context.close();
		}
	}

	/**
	 * @see org.apache.commons.pool2.KeyedPooledObjectFactory#validateObject(java.lang.Object,
	 *      org.apache.commons.pool2.PooledObject)
	 */
	@Override
	public boolean validateObject(Key key, PooledObject<BasicMessageContext> p) {
		final BasicMessageContext context = p.getObject();
		return context != null && context.isOK();
	}

	/**
	 * @see org.apache.commons.pool2.KeyedPooledObjectFactory#activateObject(java.lang.Object,
	 *      org.apache.commons.pool2.PooledObject)
	 */
	@Override
	public void activateObject(Key key, PooledObject<BasicMessageContext> p)
			throws Exception {
	}

	/**
	 * @see org.apache.commons.pool2.KeyedPooledObjectFactory#passivateObject(java.lang.Object,
	 *      org.apache.commons.pool2.PooledObject)
	 */
	@Override
	public void passivateObject(Key key, PooledObject<BasicMessageContext> p)
			throws Exception {
	}

}