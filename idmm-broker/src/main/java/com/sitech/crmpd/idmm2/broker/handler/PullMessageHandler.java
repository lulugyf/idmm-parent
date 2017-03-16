package com.sitech.crmpd.idmm2.broker.handler;

import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.sitech.crmpd.idmm2.client.api.MessageId;
import com.sitech.crmpd.idmm2.client.api.MessageIdGenerator;
import com.sitech.crmpd.idmm2.client.api.MessageType;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.ResultCode;
import com.sitech.crmpd.idmm2.client.exception.OperationException;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月1日 上午11:09:12
 */
@Profile("production")
@Component("PULL")
public class PullMessageHandler extends RepositoryMessageHandler {

	/** name="{@link com.sitech.crmpd.idmm2.broker.handler.PullMessageHandler}" */
	private static final Logger LOGGER = LoggerFactory.getLogger(PullMessageHandler.class);
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
	@Resource
	ExecutorService asyncTaskExecutor;

	@Value("${message.persistent:yes}")
	private boolean persistent; //是否持久化
	
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
	@SuppressWarnings("resource")
	@Override
	@Metered
	public Message handle(ChannelHandlerContext context, Message message) throws OperationException {
		Key key = null;
		BasicMessageContext messageContext = null;
		MDCCloseable closeable = null;
		try {
			final String lastMessageId = message.getId();
			if (!Strings.isNullOrEmpty(lastMessageId)) {
				closeable = MDC.putCloseable(MessageId.KEY, lastMessageId);
			}
			// 计算索引信息
			final String targetTopicId = message.getStringProperty(PropertyOption.TARGET_TOPIC);
			if (Strings.isNullOrEmpty(targetTopicId)) {
				throw new OperationException(ResultCode.BAD_REQUEST,
						"The value of property target-topic must not be empty!");
			}
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
			LOGGER.info("BLE返回消息-->[{}]", answerMessage);
			/** modify by zhr.2016/08/12 消费结果通知消息由BLE实现，此处屏蔽 **/
			/**
			// 产生消费结果消息
			if (message.existProperty(PropertyOption.MESSAGE_ID)
					&& message.existProperty(PropertyOption.PULL_CODE)) {
				final ConsumeResultTask task = applicationContext.getBean(ConsumeResultTask.class);
				task.setContext(context);
				task.setPullMessage(message);
				asyncTaskExecutor.execute(task);
			}
			**/

			final String messageId = answerMessage.getId();
			if (Strings.isNullOrEmpty(messageId)) {
				return answerMessage;
			}
			closeable = MDC.putCloseable(MessageId.KEY, messageId);

			byte[] body = answerMessage.getContent();
			if(body != null && body.length > 0)
				return answerMessage; // 如果返回消息有body， 则直接返回
			
			//add by zhr.2016/08/12.消费结果通知消息BLE在content里返回。如果content不为空，则直接返回ble应答消息给消费者
			if (!Strings.isNullOrEmpty(answerMessage.getContentAsString())) {
				return answerMessage;
			}
			else{
				final long before = jdbcExpectTime > 0 ? System.currentTimeMillis() : 0;
				final Message mresult = messageRepository.findOne(messageIdGenerator
						.generate(messageId));
				if (jdbcExpectTime > 0) {
					final long used = System.currentTimeMillis() - before;
					if (used >= jdbcExpectTime) {
						LOGGER.warn("--find messagebody cost {} ms", used);
					}
				}
				if (answerMessage.existProperty(PropertyOption.CONSUMER_RETRY)) {
					final int retry = answerMessage.getIntegerProperty(PropertyOption.CONSUMER_RETRY);
					mresult.setProperty(PropertyOption.CONSUMER_RETRY, retry);
				}
				return mresult;
			}
		} catch (final OperationException e) {
			if (messageContext != null) {
				messageContext.close();
			}
			throw e;
		} catch (final IOException e) {
			if (messageContext != null) {
				messageContext.close();
			}
			throw new OperationException(ResultCode.INTERNAL_DATA_ACCESS_EXCEPTION, e);
		} catch (final Exception e) {
			if (messageContext != null) {
				messageContext.close();
			}
			throw new OperationException(ResultCode.INTERNAL_DATA_ACCESS_EXCEPTION, e);
		} finally {
			if (key != null && messageContext != null) {
				pool.returnObject(key, messageContext);
			}
			if (closeable != null) {
				closeable.close();
			}
		}
	}

}
