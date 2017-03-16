/**
 *
 */
package com.sitech.crmpd.idmm2.broker.config;

import com.sitech.crmpd.idmm2.broker.validate.NotNull;
import com.sitech.crmpd.idmm2.broker.validate.Ref;

/**
 * @author Administrator
 *
 */
public final class PublishRelationship extends RelationshipInfo {

	@NotNull
	@Ref(SourceTopic.class)
	private String topicId;

	/**
	 * @see com.sitech.crmpd.idmm2.broker.config.RelationshipInfo#getKey()
	 */
	@Override
	public String getKey() {
		return getClientId() + "@" + topicId;
	}

	/**
	 * 获取{@link #topicId}属性的值
	 *
	 * @return {@link #topicId}属性的值
	 */
	public String getTopicId() {
		return topicId;
	}

	/**
	 * 设置{@link #topicId}属性的值
	 *
	 * @param topicId
	 *            属性值
	 */
	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}

}