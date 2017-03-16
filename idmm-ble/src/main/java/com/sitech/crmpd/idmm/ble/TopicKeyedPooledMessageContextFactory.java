package com.sitech.crmpd.idmm.ble;

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.springframework.beans.factory.annotation.Value;

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

	@Value("${ble.request.timeout}")
	private int timeout;
	@Value("${ble.id}")
	private String clientId;

	/**
	 * @see org.apache.commons.pool2.KeyedPooledObjectFactory#makeObject(java.lang.Object)
	 */
	@Override
	public PooledObject<BasicMessageContext> makeObject(Key key) throws Exception {
		BasicMessageContext context = null;
		try {
			context = new BasicMessageContext(key.getHost(), key.getPort(), timeout);
			final Message message = context.trade(clientId, MessageType.QUERY, Message.create());
			final ResultCode code = ResultCode.valueOf(message
					.getProperty(PropertyOption.RESULT_CODE));
			if (code != ResultCode.OK) {
				throw new OperationException(code);
			}
			return new DefaultPooledObject<BasicMessageContext>(context);
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
	public void destroyObject(Key key, PooledObject<BasicMessageContext> p) throws Exception {
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
		System.out.println(context != null && context.isOK());
		return context != null && context.isOK();
	}

	/**
	 * @see org.apache.commons.pool2.KeyedPooledObjectFactory#activateObject(java.lang.Object,
	 *      org.apache.commons.pool2.PooledObject)
	 */
	@Override
	public void activateObject(Key key, PooledObject<BasicMessageContext> p) throws Exception {
	}

	/**
	 * @see org.apache.commons.pool2.KeyedPooledObjectFactory#passivateObject(java.lang.Object,
	 *      org.apache.commons.pool2.PooledObject)
	 */
	@Override
	public void passivateObject(Key key, PooledObject<BasicMessageContext> p) throws Exception {
	}

}