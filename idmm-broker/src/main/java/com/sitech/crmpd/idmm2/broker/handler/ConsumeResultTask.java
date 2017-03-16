package com.sitech.crmpd.idmm2.broker.handler;

import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.net.HostAndPort;
import com.sitech.crmpd.idmm2.broker.config.ConfigConstant;
import com.sitech.crmpd.idmm2.broker.config.Configuration;
import com.sitech.crmpd.idmm2.broker.config.ConfigurationProvider;
import com.sitech.crmpd.idmm2.broker.config.TopicToBle;
import com.sitech.crmpd.idmm2.broker.pool.Key;
import com.sitech.crmpd.idmm2.broker.repository.MessageRepository;
import com.sitech.crmpd.idmm2.broker.utils.CuratorFrameworkHolder;
import com.sitech.crmpd.idmm2.client.BasicMessageContext;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageId;
import com.sitech.crmpd.idmm2.client.api.MessageIdGenerator;
import com.sitech.crmpd.idmm2.client.api.MessageType;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.PullCode;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年10月21日 上午9:47:52
 */
@Component
@Scope("prototype")
public class ConsumeResultTask implements Runnable {
	/** name="{@link com.sitech.crmpd.idmm2.broker.handler.ConsumeResultTask}" */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumeResultTask.class);
	@Autowired
	MessageRepository messageRepository;
	@Autowired
	MessageIdGenerator messageIdGenerator;
	@Autowired
	ConfigurationProvider provider;
	@Autowired
	GenericKeyedObjectPool<Key, BasicMessageContext> pool;
	@Autowired
	ConfigConstant constant;
	@Autowired
	CuratorFrameworkHolder client;
	/**
	 * 计数器，用spring控制为单例
	 */
	@Resource
	AtomicLong messageIdSequence;
	private volatile Message pullMessage;
	private volatile ChannelHandlerContext context;

	/**
	 * 设置{@link #pullMessage}属性的值
	 *
	 * @param pullMessage
	 *            属性值
	 */
	public void setPullMessage(Message pullMessage) {
		this.pullMessage = pullMessage;
	}

	/**
	 * 设置{@link #context}属性的值
	 *
	 * @param context
	 *            属性值
	 */
	public void setContext(ChannelHandlerContext context) {
		this.context = context;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		Key key = null;
		BasicMessageContext messageContext = null;
		final String messageId = pullMessage.getId();
		final MDCCloseable closeable = MDC.putCloseable(MessageId.KEY, messageId);
		try {
			final Message producerMessage = messageRepository.findOne(messageIdGenerator
					.generate(messageId));
			if (producerMessage.existProperty(PropertyOption.REPLY_TO)) {
				final String targetTopicId = producerMessage
						.getStringProperty(PropertyOption.REPLY_TO);
				LOGGER.debug("消费结果回送目标主题 = [{}]", targetTopicId);
				final Configuration configuration = provider.getConfiguration();
				// 根据配置得到BLE的信息
				final TopicToBle topicToBle = configuration.get(TopicToBle.class, targetTopicId);
				// 查询ble地址
				final String data = new String(client.getData(constant.getBleIdPath()
						+ topicToBle.getBleId()));
				LOGGER.trace("ble-data = [{}]", data);
				// 发送到ble
				final HostAndPort address = HostAndPort.fromString(data.split(" ")[0]);
				LOGGER.trace("ble-address = [{}]", address);
				key = Key.create(targetTopicId, address.getHostText(), address.getPort());
				final SocketAddress socketAddress = context.channel().remoteAddress();
				// 生成结果消息id
				final MessageId id = messageIdGenerator.generate((InetSocketAddress) socketAddress,
						pullMessage, messageIdSequence.incrementAndGet());
				final Message message = Message.create(pullMessage
						.existProperty(PropertyOption.CODE_DESCRIPTION) ? pullMessage
						.getStringProperty(PropertyOption.CODE_DESCRIPTION) : "");
				message.setProperty(PropertyOption.CLIENT_ID,
						pullMessage.getStringProperty(PropertyOption.CLIENT_ID));
				message.setProperty(PropertyOption.PRODUCER_MESSAGE_ID, messageId);
				message.setProperty(PropertyOption.CONSUMED_BY,
						pullMessage.getStringProperty(PropertyOption.CLIENT_ID));
				message.setProperty(PropertyOption.PULL_CODE,
						PullCode.valueOf(pullMessage.getStringProperty(PropertyOption.PULL_CODE)));
				message.setId(id.getValue());
				LOGGER.debug("消费结果消息 = [{}]", message);
				messageRepository.save(message);
				messageContext = pool.borrowObject(key);

				final Message commitMessage = Message.create();
				commitMessage.copyProperties(message);
				LOGGER.debug("消费结果索引消息 = [{}]", commitMessage);
				final String clientId = commitMessage.getStringProperty(PropertyOption.CLIENT_ID);
				commitMessage.setProperty(PropertyOption.TARGET_TOPIC, targetTopicId);
				final Message answerMessage = messageContext.trade(clientId,
						MessageType.SEND_COMMIT, commitMessage);
				LOGGER.info("BLE返回消息{}", answerMessage);
			}
		} catch (final Exception e) {
			LOGGER.error("产生结果消息失败", e);
		} finally {
			if (key != null && messageContext != null) {
				pool.returnObject(key, messageContext);
			}
			closeable.close();
		}
	}
}
