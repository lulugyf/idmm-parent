package com.sitech.crmpd.idmm2.client.jms;

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年5月4日 下午4:05:26
 */
public class DefaultSession implements Session {

	private boolean transacted;
	private int acknowledgeMode;

	public DefaultSession(boolean transacted, int acknowledgeMode) {
		this.transacted = transacted;
		this.acknowledgeMode = acknowledgeMode;
	}

	/**
	 * @see javax.jms.Session#createBytesMessage()
	 */
	@Override
	public BytesMessage createBytesMessage() throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createMapMessage()
	 */
	@Override
	public MapMessage createMapMessage() throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createMessage()
	 */
	@Override
	public Message createMessage() throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createObjectMessage()
	 */
	@Override
	public ObjectMessage createObjectMessage() throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createObjectMessage(java.io.Serializable)
	 */
	@Override
	public ObjectMessage createObjectMessage(Serializable object) throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createStreamMessage()
	 */
	@Override
	public StreamMessage createStreamMessage() throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createTextMessage()
	 */
	@Override
	public TextMessage createTextMessage() throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createTextMessage(java.lang.String)
	 */
	@Override
	public TextMessage createTextMessage(String text) throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#getTransacted()
	 */
	@Override
	public boolean getTransacted() throws JMSException {
		return transacted;
	}

	/**
	 * @see javax.jms.Session#getAcknowledgeMode()
	 */
	@Override
	public int getAcknowledgeMode() throws JMSException {
		return acknowledgeMode;
	}

	/**
	 * @see javax.jms.Session#commit()
	 */
	@Override
	public void commit() throws JMSException {
	}

	/**
	 * @see javax.jms.Session#rollback()
	 */
	@Override
	public void rollback() throws JMSException {
	}

	/**
	 * @see javax.jms.Session#close()
	 */
	@Override
	public void close() throws JMSException {
	}

	/**
	 * @see javax.jms.Session#recover()
	 */
	@Override
	public void recover() throws JMSException {
	}

	/**
	 * @see javax.jms.Session#getMessageListener()
	 */
	@Override
	public MessageListener getMessageListener() throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#setMessageListener(javax.jms.MessageListener)
	 */
	@Override
	public void setMessageListener(MessageListener listener) throws JMSException {
	}

	/**
	 * @see javax.jms.Session#run()
	 */
	@Override
	public void run() {
	}

	/**
	 * @see javax.jms.Session#createProducer(javax.jms.Destination)
	 */
	@Override
	public MessageProducer createProducer(Destination destination) throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createConsumer(javax.jms.Destination)
	 */
	@Override
	public MessageConsumer createConsumer(Destination destination) throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createConsumer(javax.jms.Destination, java.lang.String)
	 */
	@Override
	public MessageConsumer createConsumer(Destination destination, String messageSelector)
			throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createConsumer(javax.jms.Destination, java.lang.String, boolean)
	 */
	@Override
	public MessageConsumer createConsumer(Destination destination, String messageSelector,
			boolean NoLocal) throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createQueue(java.lang.String)
	 */
	@Override
	public Queue createQueue(String queueName) throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createTopic(java.lang.String)
	 */
	@Override
	public Topic createTopic(String topicName) throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createDurableSubscriber(javax.jms.Topic, java.lang.String)
	 */
	@Override
	public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createDurableSubscriber(javax.jms.Topic, java.lang.String,
	 *      java.lang.String, boolean)
	 */
	@Override
	public TopicSubscriber createDurableSubscriber(Topic topic, String name,
			String messageSelector, boolean noLocal) throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createBrowser(javax.jms.Queue)
	 */
	@Override
	public QueueBrowser createBrowser(Queue queue) throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createBrowser(javax.jms.Queue, java.lang.String)
	 */
	@Override
	public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createTemporaryQueue()
	 */
	@Override
	public TemporaryQueue createTemporaryQueue() throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#createTemporaryTopic()
	 */
	@Override
	public TemporaryTopic createTemporaryTopic() throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.Session#unsubscribe(java.lang.String)
	 */
	@Override
	public void unsubscribe(String name) throws JMSException {
	}

}
