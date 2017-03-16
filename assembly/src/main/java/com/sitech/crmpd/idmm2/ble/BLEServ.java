package com.sitech.crmpd.idmm2.ble;

import com.sitech.crmpd.idmm.ble.BLEConfig;
import com.sitech.crmpd.idmm.ble.BLEEntry;
import com.sitech.crmpd.idmm.ble.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.util.concurrent.Futures;
import com.sitech.crmpd.idmm2.transport.BootstrapBuilder;

public class BLEServ {

	private static final Logger log = LoggerFactory.getLogger(BLEServ.class);

	private void startup(String appxml, String propfile) throws Exception {
		ClassPathXmlApplicationContext applicationContext = null;

		try {
			applicationContext = new ClassPathXmlApplicationContext(appxml);
			SpringUtil.setApplicationContext(applicationContext);
			BLEEntry entry = applicationContext.getBean(BLEEntry.class);
			BLEConfig cfg = applicationContext.getBean(BLEConfig.class);
			
			cfg.initialize();

			if (!entry.initialize()) {
				applicationContext.close();
				return;
			}
//			Futures.successfulAsList(applicationContext.getBean("futures", List.class)).get();
			log.info("BLE {} initialized!!", cfg.getId());
			
			Futures.successfulAsList(applicationContext.getBean(BootstrapBuilder.class).futures()).get();
		} catch (final Exception e) {
			log.error("", e);
		} finally {
			applicationContext.close();
		}
	}

	public static void main(String[] args) throws Exception {
		String appxml = "server-ble.xml";
		final String propfile = "/server-ble.properties";

		if (args.length > 0) {
			appxml = args[0];
		}

		new BLEServ().startup(appxml, propfile);
	}

}
