/**
 *
 */
package com.sitech.crmpd.idmm2.broker.config;

import com.sitech.crmpd.idmm2.broker.validate.Ref;
import com.sitech.crmpd.idmm2.broker.validate.UniqueKey;

/**
 * @author Administrator
 *
 */
public class RelationshipInfo implements UniqueKey {

	@Ref(ClientInfo.class)
	private String clientId;
	private String password;

	/**
	 * @see com.sitech.crmpd.idmm2.broker.validate.UniqueKey#getKey()
	 */
	@Override
	public String getKey() {
		return getClientId();
	}

	/**
	 * 获取{@link #clientId}属性的值
	 *
	 * @return {@link #clientId}属性的值
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * 设置{@link #clientId}属性的值
	 *
	 * @param clientId
	 *            属性值
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * 获取{@link #password}属性的值
	 *
	 * @return {@link #password}属性的值
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * 设置{@link #password}属性的值
	 *
	 * @param password
	 *            属性值
	 */
	public void setPassword(String password) {
		this.password = password;
	}

}
