package com.sitech.crmpd.idmm.ble;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class SpringUtil  {

	private static ApplicationContext applicationContext = null;

	public static void setApplicationContext(ApplicationContext appctnx) throws BeansException {
		applicationContext = appctnx;
	}
	
	/**
	 * 获取applicationContext
	 * 
	 * @return applicationContext
	 */
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * 根据Bean名称获取实例
	 * 
	 * @param name
	 *            Bean注册名称
	 * 
	 * @return bean实例
	 * 
	 * @throws BeansException
	 */
	public static Object getBean(String name) throws BeansException {
		return applicationContext.getBean(name);
	}
	
	public static <T> T getBean(Class<T> c) throws BeansException{
		return applicationContext.getBean(c);
	}

}