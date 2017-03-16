package com.sitech.crmpd.idmm2.client.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年5月4日 下午4:02:18
 */
public class DefaultConnectionFactory implements ConnectionFactory {

	/**
	 * @see javax.jms.ConnectionFactory#createConnection()
	 */
	@Override
	public Connection createConnection() throws JMSException {
		return new DefaultConnection();
	}

	/**
	 * @see javax.jms.ConnectionFactory#createConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public Connection createConnection(String userName, String password) throws JMSException {
		return new DefaultConnection(userName, password);
	}

}
