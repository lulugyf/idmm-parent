/**
 *
 */
package com.sitech.crmpd.idmm2.broker.config;

import java.sql.Date;

/**
 * @author Administrator
 *
 */
public final class ConsumerResultNotify {

	private final ClientInfo producer;
	private final ClientInfo consumer;
	private final SourceTopic source;
	private final TargetTopic target;

	public ConsumerResultNotify(String who, Date when, String comments, boolean valid,
			ClientInfo producer, ClientInfo consumer, SourceTopic source, TargetTopic target) {

		this.producer = producer;
		this.consumer = consumer;
		this.source = source;
		this.target = target;
	}

	public ClientInfo getProducer() {
		return producer;
	}

	public ClientInfo getConsumer() {
		return consumer;
	}

	public SourceTopic getSource() {
		return source;
	}

	public TargetTopic getTarget() {
		return target;
	}

}
