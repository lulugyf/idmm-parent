package com.sitech.crmpd.idmm2.broker.handler;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * 能够根据消息内容自动应答的消息
 *
 * @author heihuwudi@gmail.com</br> Created By: 2015年3月31日 下午9:51:49
 */
public abstract class BaseMessageHandler implements MessageHandler {

	ApplicationContext applicationContext;

	/**
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
