/**
 *
 */
package com.sitech.crmpd.idmm2.broker.config;

import com.sitech.crmpd.idmm2.broker.validate.NotNull;
import com.sitech.crmpd.idmm2.broker.validate.UniqueKey;

/**
 * @author Administrator
 *
 */
public final class TargetTopic implements UniqueKey {

	@NotNull
	private String id;

	/**
	 * 获取{@link #id}属性的值
	 *
	 * @return {@link #id}属性的值
	 */
	public String getId() {
		return id;
	}

	/**
	 * 设置{@link #id}属性的值
	 *
	 * @param id
	 *            属性值
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @see com.sitech.crmpd.idmm2.broker.validate.UniqueKey#getKey()
	 */
	@Override
	public String getKey() {
		return id;
	}

}
