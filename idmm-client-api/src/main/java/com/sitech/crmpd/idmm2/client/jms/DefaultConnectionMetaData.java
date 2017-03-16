package com.sitech.crmpd.idmm2.client.jms;

import java.util.Enumeration;

import javax.jms.ConnectionMetaData;
import javax.jms.JMSException;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年5月4日 下午4:09:19
 */
public class DefaultConnectionMetaData implements ConnectionMetaData {

	/**
	 * @see javax.jms.ConnectionMetaData#getJMSVersion()
	 */
	@Override
	public String getJMSVersion() throws JMSException {
		return "1.1";
	}

	/**
	 * @see javax.jms.ConnectionMetaData#getJMSMajorVersion()
	 */
	@Override
	public int getJMSMajorVersion() throws JMSException {
		return 1;
	}

	/**
	 * @see javax.jms.ConnectionMetaData#getJMSMinorVersion()
	 */
	@Override
	public int getJMSMinorVersion() throws JMSException {
		return 1;
	}

	/**
	 * @see javax.jms.ConnectionMetaData#getJMSProviderName()
	 */
	@Override
	public String getJMSProviderName() throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.ConnectionMetaData#getProviderVersion()
	 */
	@Override
	public String getProviderVersion() throws JMSException {
		return "2.0";
	}

	/**
	 * @see javax.jms.ConnectionMetaData#getProviderMajorVersion()
	 */
	@Override
	public int getProviderMajorVersion() throws JMSException {
		return 2;
	}

	/**
	 * @see javax.jms.ConnectionMetaData#getProviderMinorVersion()
	 */
	@Override
	public int getProviderMinorVersion() throws JMSException {
		return 0;
	}

	/**
	 * @see javax.jms.ConnectionMetaData#getJMSXPropertyNames()
	 */
	@Override
	public Enumeration getJMSXPropertyNames() throws JMSException {
		return null;
	}

}
