package com.sitech.crmpd.idmm2.client.jms.message;

import javax.jms.JMSException;
import javax.jms.TextMessage;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年5月5日 上午10:15:27
 */
public class DefaultTextMessage extends DefaultMessage implements TextMessage {

	/**
	 * @see javax.jms.TextMessage#setText(java.lang.String)
	 */
	@Override
	public void setText(String string) throws JMSException {
	}

	/**
	 * @see javax.jms.TextMessage#getText()
	 */
	@Override
	public String getText() throws JMSException {
		return null;
	}

}
