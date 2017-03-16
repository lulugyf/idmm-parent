package com.sitech.crmpd.idmm2.client.jms.message;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年5月5日 上午10:15:09
 */
public class DefaultObjectMessage extends DefaultMessage implements ObjectMessage {

	/**
	 * @see javax.jms.ObjectMessage#setObject(java.io.Serializable)
	 */
	@Override
	public void setObject(Serializable object) throws JMSException {
	}

	/**
	 * @see javax.jms.ObjectMessage#getObject()
	 */
	@Override
	public Serializable getObject() throws JMSException {
		return null;
	}

}
