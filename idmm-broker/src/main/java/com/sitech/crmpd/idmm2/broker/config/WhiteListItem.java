package com.sitech.crmpd.idmm2.broker.config;

import com.sitech.crmpd.idmm2.broker.validate.NotEmpty;
import com.sitech.crmpd.idmm2.broker.validate.UniqueKey;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年9月25日 下午8:32:45
 */
public class WhiteListItem implements UniqueKey {

	@NotEmpty
	private String ip;

	/**
	 * 设置{@link #ip}属性的值
	 *
	 * @param ip
	 *            属性值
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @see com.sitech.crmpd.idmm2.broker.validate.UniqueKey#getKey()
	 */
	@Override
	public String getKey() {
		return ip;
	}

}
