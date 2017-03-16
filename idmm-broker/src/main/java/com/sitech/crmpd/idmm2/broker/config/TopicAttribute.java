/**
 *
 */
package com.sitech.crmpd.idmm2.broker.config;

import java.sql.Date;

/**
 * @author Administrator
 *
 */
public final class TopicAttribute {

	private final SourceTopic topic;
	private final String key;

	public TopicAttribute(SourceTopic topic,
			String key) {

		this.topic = topic;
		this.key = key;
	}

	public SourceTopic getTopic() {
		return topic;
	}

	public String getKey() {
		return key;
	}
}
