package com.sitech.crmpd.idmm2.client.jms;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年5月4日 下午4:02:53
 */
public class DefaultConnection implements Connection {

	private String clientID;
	private String userName;
	private String password;

	public DefaultConnection() {
	}

	public DefaultConnection(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	/**
	 * @see javax.jms.Connection#createSession(boolean, int)
	 */
	@Override
	public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
		return new DefaultSession(transacted, acknowledgeMode);
	}

	/**
	 * @see javax.jms.Connection#getClientID()
	 */
	@Override
	public String getClientID() throws JMSException {
		return clientID;
	}

	/**
	 * @see javax.jms.Connection#setClientID(java.lang.String)
	 */
	@Override
	public void setClientID(String clientID) throws JMSException {
		this.clientID = clientID;
	}

	/**
	 * @see javax.jms.Connection#getMetaData()
	 */
	@Override
	public ConnectionMetaData getMetaData() throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Connection#getExceptionListener()
	 */
	@Override
	public ExceptionListener getExceptionListener() throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Connection#setExceptionListener(javax.jms.ExceptionListener)
	 */
	@Override
	public void setExceptionListener(ExceptionListener listener) throws JMSException {
	}

	/**
	 * @see javax.jms.Connection#start()
	 */
	@Override
	public void start() throws JMSException {
	}

	/**
	 * @see javax.jms.Connection#stop()
	 */
	@Override
	public void stop() throws JMSException {
	}

	/**
	 * @see javax.jms.Connection#close()
	 */
	@Override
	public void close() throws JMSException {
	}

	/**
	 * @see javax.jms.Connection#createConnectionConsumer(javax.jms.Destination, java.lang.String,
	 *      javax.jms.ServerSessionPool, int)
	 */
	@Override
	public ConnectionConsumer createConnectionConsumer(Destination destination,
			String messageSelector, ServerSessionPool sessionPool, int maxMessages)
					throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Connection#createDurableConnectionConsumer(javax.jms.Topic, java.lang.String,
	 *      java.lang.String, javax.jms.ServerSessionPool, int)
	 */
	@Override
	public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName,
			String messageSelector, ServerSessionPool sessionPool, int maxMessages)
					throws JMSException {
		return null;
	}

}
