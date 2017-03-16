/**
 *
 */
package com.sitech.crmpd.idmm2.broker.config;

import com.sitech.crmpd.idmm2.broker.validate.Min;
import com.sitech.crmpd.idmm2.broker.validate.NotNull;
import com.sitech.crmpd.idmm2.broker.validate.Ref;

/**
 * @author Administrator
 *
 */
public final class SubscribeRelationship extends RelationshipInfo {
	@NotNull
	@Ref(TargetTopic.class)
	private String topicId;
	@NotNull
	@Min(1)
	private int concurrents;

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

	/**
	 * 获取{@link #concurrents}属性的值
	 *
	 * @return {@link #concurrents}属性的值
	 */
	public int getConcurrents() {
		return concurrents;
	}

	/**
	 * 设置{@link #concurrents}属性的值
	 *
	 * @param concurrents
	 *            属性值
	 */
	public void setConcurrents(int concurrents) {
		this.concurrents = concurrents;
	}

}