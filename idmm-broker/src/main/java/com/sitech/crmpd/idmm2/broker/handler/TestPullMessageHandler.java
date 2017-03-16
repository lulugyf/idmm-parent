package com.sitech.crmpd.idmm2.broker.handler;

import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.codahale.metrics.annotation.Metered;
import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;
import com.sitech.crmpd.idmm2.broker.config.ConfigConstant;
import com.sitech.crmpd.idmm2.broker.config.Configuration;
import com.sitech.crmpd.idmm2.broker.config.ConfigurationProvider;
import com.sitech.crmpd.idmm2.broker.config.TopicToBle;
import com.sitech.crmpd.idmm2.broker.pool.Key;
import com.sitech.crmpd.idmm2.broker.utils.CuratorFrameworkHolder;
import com.sitech.crmpd.idmm2.client.BasicMessageContext;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageIdGenerator;
import com.sitech.crmpd.idmm2.client.api.MessageType;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.ResultCode;
import com.sitech.crmpd.idmm2.client.exception.OperationException;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月1日 上午11:09:12
 */
@Profile("test")
@Component("PULL")
public class TestPullMessageHandler extends RepositoryMessageHandler {

	/** name="{@link com.sitech.crmpd.idmm2.broker.handler.TestPullMessageHandler}" */
	private static final Logger LOGGER = LoggerFactory.getLogger(TestPullMessageHandler.class);
	@Autowired
	MessageIdGenerator messageIdGenerator;
	@Autowired
	GenericKeyedObjectPool<Key, BasicMessageContext> pool;
	@Autowired
	ConfigConstant constant;
	@Autowired
	CuratorFrameworkHolder client;
	@Autowired
	ConfigurationProvider provider;

	/**
	 * @see com.sitech.crmpd.idmm2.broker.handler.MessageHandler#getType()
	 */
	@Override
	public MessageType getType() {
		return MessageType.PULL;
	}

	/**
	 * @see com.sitech.crmpd.idmm2.broker.handler.MessageHandler#getAnswerType()
	 */
	@Override
	public MessageType getAnswerType() {
		return MessageType.PULL_ANSWER;
	}

	/**
	 *
	 * @see com.sitech.crmpd.idmm2.broker.handler.MessageHandler#handle(io.netty.channel.ChannelHandlerContext,
	 *      com.sitech.crmpd.idmm2.client.api.Message)
	 */
	@Override
	@Metered
	public Message handle(ChannelHandlerContext context, Message message) throws OperationException {
		Key key = null;
		BasicMessageContext messageContext = null;
		try {
			// 计算索引信息
			final String targetTopicId = message.getStringProperty(PropertyOption.TARGET_TOPIC);
			final Configuration configuration = provider.getConfiguration();
			// 提交索引信息
			// 根据配置得到BLE的信息
			final TopicToBle topicToBle = configuration.get(TopicToBle.class, targetTopicId);
			// 查询ble地址
			final String data = new String(client.getData(constant.getBleIdPath()
					+ topicToBle.getBleId()));
			LOGGER.trace("ble-data = [{}]", data);
			// 发送到ble
			final HostAndPort address = HostAndPort.fromString(data.split(" ")[0]);
			LOGGER.trace("ble-address = [{}]", address);
			// 发送到ble
			key = Key.create(targetTopicId, address.getHostText(), address.getPort());
			messageContext = pool.borrowObject(key);
			final String clientId = message.getStringProperty(PropertyOption.CLIENT_ID);
			final Message answerMessage = messageContext.trade(clientId, getType(), message);
			LOGGER.info("BLE返回消息{}", answerMessage);
			final String messageId = answerMessage.getId();
			return Strings.isNullOrEmpty(messageId) ? answerMessage : Message.create("test-body")
					.copyProperties(answerMessage);
		} catch (final OperationException e) {
			throw e;
		} catch (final IOException e) {
			if (messageContext != null) {
				messageContext.close();
			}
			throw new OperationException(ResultCode.INTERNAL_DATA_ACCESS_EXCEPTION, e);
		} catch (final Exception e) {
			throw new OperationException(ResultCode.INTERNAL_DATA_ACCESS_EXCEPTION, e);
		} finally {
			if (key != null && messageContext != null) {
				pool.returnObject(key, messageContext);
			}
		}
	}
}
