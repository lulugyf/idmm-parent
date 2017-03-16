/**
 *
 */
package com.sitech.crmpd.idmm2.broker.config;

import com.sitech.crmpd.idmm2.broker.validate.NotNull;
import com.sitech.crmpd.idmm2.broker.validate.Ref;
import com.sitech.crmpd.idmm2.broker.validate.UniqueKey;

/**
 * @author Administrator
 *
 */
public final class TopicToBle implements UniqueKey {

	@NotNull
	@Ref(TargetTopic.class)
	private String targetTopicId;
	@NotNull
	@Ref(BleInfo.class)
	private String bleId;

	/**
	 * @see com.sitech.crmpd.idmm2.broker.validate.UniqueKey#getKey()
	 */
	@Override
	public String getKey() {
		return targetTopicId;
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
	 * 获取{@link #bleId}属性的值
	 *
	 * @return {@link #bleId}属性的值
	 */
	public String getBleId() {
		return bleId;
	}

	/**
	 * 设置{@link #bleId}属性的值
	 *
	 * @param bleId
	 *            属性值
	 */
	public void setBleId(String bleId) {
		this.bleId = bleId;
	}

}
