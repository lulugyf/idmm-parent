/**
 *
 */
package com.sitech.crmpd.idmm2.broker.config;

import com.sitech.crmpd.idmm2.broker.validate.NotEmpty;
import com.sitech.crmpd.idmm2.broker.validate.UniqueKey;

/**
 * 原始主题信息
 *
 * @author Administrator
 *
 */
public final class SourceTopic implements UniqueKey {

	@NotEmpty
	private String id;
	private String description;

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
	 * 获取{@link #description}属性的值
	 *
	 * @return {@link #description}属性的值
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 设置{@link #description}属性的值
	 *
	 * @param description
	 *            属性值
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @see com.sitech.crmpd.idmm2.broker.validate.UniqueKey#getKey()
	 */
	@Override
	public String getKey() {
		return id;
	}

}
