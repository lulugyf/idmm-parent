/**
 *
 */
package com.sitech.crmpd.idmm2.broker.config;

import com.sitech.crmpd.idmm2.broker.validate.NotEmpty;
import com.sitech.crmpd.idmm2.broker.validate.NotNull;
import com.sitech.crmpd.idmm2.broker.validate.Ref;
import com.sitech.crmpd.idmm2.broker.validate.UniqueKey;

/**
 * @author Administrator
 *
 */
public final class ConsumerInOrder implements UniqueKey {
	@NotNull
	@Ref(SourceTopic.class)
	private String sourceTopicId;
	private String propertyKey;
	@NotEmpty
	private String propertyValue;
	@NotNull
	@Ref(TargetTopic.class)
	private String targetTopicId;

	/**
	 * 获取{@link #sourceTopicId}属性的值
	 *
	 * @return {@link #sourceTopicId}属性的值
	 */
	public String getSourceTopicId() {
		return sourceTopicId;
	}

	/**
	 * 设置{@link #sourceTopicId}属性的值
	 *
	 * @param sourceTopicId
	 *            属性值
	 */
	public void setSourceTopicId(String sourceTopicId) {
		this.sourceTopicId = sourceTopicId;
	}

	/**
	 * 获取{@link #propertyKey}属性的值
	 *
	 * @return {@link #propertyKey}属性的值
	 */
	public String getPropertyKey() {
		return propertyKey;
	}

	/**
	 * 设置{@link #propertyKey}属性的值
	 *
	 * @param propertyKey
	 *            属性值
	 */
	public void setPropertyKey(String propertyKey) {
		this.propertyKey = propertyKey;
	}

	/**
	 * 获取{@link #propertyValue}属性的值
	 *
	 * @return {@link #propertyValue}属性的值
	 */
	public String getPropertyValue() {
		return propertyValue;
	}

	/**
	 * 设置{@link #propertyValue}属性的值
	 *
	 * @param propertyValue
	 *            属性值
	 */
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

	/**
	 * 获取{@link #targetTopicId}属性的值
	 *
	 * @return {@link #targetTopicId}属性的值
	 */
	public String getTargetTopicId() {
		return targetTopicId;
	}

	/**
	 * 设置{@link #targetTopicId}属性的值
	 *
	 * @param targetTopicId
	 *            属性值
	 */
	public void setTargetTopicId(String targetTopicId) {
		this.targetTopicId = targetTopicId;
	}

	/**
	 * @see com.sitech.crmpd.idmm2.broker.validate.UniqueKey#getKey()
	 */
	@Override
	public String getKey() {
		return sourceTopicId + "@" + propertyKey + "@" + propertyValue + "@" + targetTopicId;
	}

}
