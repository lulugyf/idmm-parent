package com.sitech.crmpd.idmm2.broker;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.util.concurrent.Futures;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年8月25日 上午9:18:29
 */
public class App {

	/** name="{@link com.sitech.crmpd.idmm2.broker.App}" */
	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		System.setProperty("spring.profiles.active",
				System.getProperty("spring.profiles.active", "production"));
		ClassPathXmlApplicationContext applicationContext = null;
		try {
			if (args.length == 0) {
				applicationContext = new ClassPathXmlApplicationContext("server.xml");
			} else {
				applicationContext = new ClassPathXmlApplicationContext(args);
			}
			Futures.successfulAsList(applicationContext.getBean("futures", List.class)).get();
		} catch (final Exception e) {
			LOGGER.error("", e);
		} finally {
			if (applicationContext != null) {
				applicationContext.close();
			}
		}
	}
}
