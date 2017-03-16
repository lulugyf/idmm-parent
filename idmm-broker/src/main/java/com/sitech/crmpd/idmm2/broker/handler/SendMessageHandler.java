package com.sitech.crmpd.idmm2.broker.handler;

import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.codahale.metrics.annotation.Metered;
import com.google.common.base.Strings;
import com.sitech.crmpd.idmm2.broker.config.ClientLimit;
import com.sitech.crmpd.idmm2.broker.config.Configuration;
import com.sitech.crmpd.idmm2.broker.config.ConfigurationProvider;
import com.sitech.crmpd.idmm2.broker.config.PublishRelationship;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageId;
import com.sitech.crmpd.idmm2.client.api.MessageIdGenerator;
import com.sitech.crmpd.idmm2.client.api.MessageType;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.ResultCode;
import com.sitech.crmpd.idmm2.client.exception.OperationException;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年3月29日 下午2:33:39
 */
@Profile("production")
@Component("SEND")
public class SendMessageHandler extends RepositoryMessageHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageHandler.class);

	@Autowired
	private ConfigurationProvider provider;
	@Autowired
	MessageIdGenerator messageIdGenerator;
	@Autowired
	Cache messageCache;

	/**
	 * 计数器，用spring控制为单例
	 */
	@Resource
	AtomicLong messageIdSequence;
	

	@Value("${message.persistent:yes}")
	private boolean persistent; //是否持久化

	/**
	 * @see com.sitech.crmpd.idmm2.broker.handler.MessageHandler#getType()
	 */
	@Override
	public MessageType getType() {
		return MessageType.SEND;
	}

	/**
	 * @see com.sitech.crmpd.idmm2.broker.handler.MessageHandler#getAnswerType()
	 */
	@Override
	public MessageType getAnswerType() {
		return MessageType.ANSWER;
	}
	
	
	/**
	 * @param paramArrayOfByte
	 * @return
	 */
	public String byte2Hex(byte[] paramArrayOfByte) {
        StringBuffer localStringBuffer = new StringBuffer();
        String str = "";
        for (int i = 0; i < paramArrayOfByte.length; ++i) {
            str = Integer.toHexString(paramArrayOfByte[i] & 0xFF);
            if (str.length() == 1)
                localStringBuffer.append("0");
            localStringBuffer.append(str);
        }
        return localStringBuffer.toString();
    }

	/**
	 *
	 * @see com.sitech.crmpd.idmm2.broker.handler.MessageHandler#handle(io.netty.channel.ChannelHandlerContext,
	 *      com.sitech.crmpd.idmm2.client.api.Message)
	 */
	@Override
	@Metered
	public Message handle(ChannelHandlerContext context, Message message) throws OperationException {
		final Configuration configuration = provider.getConfiguration();
		final Map<String, PublishRelationship> publishRelationshipMappings = configuration
				.get(PublishRelationship.class);
		LOGGER.trace("publishRelationshipMappings = [{}]", publishRelationshipMappings);
		final Map<String, ClientLimit> ClientLimitMappings = configuration
				.get(ClientLimit.class);
		LOGGER.trace("ClientLimitMappings = [{}]", ClientLimitMappings);
		final String clientId = message.getStringProperty(PropertyOption.CLIENT_ID);
		final String topic = message.getStringProperty(PropertyOption.TOPIC);
		if (Strings.isNullOrEmpty(topic)) {
			throw new OperationException(ResultCode.BAD_REQUEST,
					"The value of property topic must not be empty!");
		}
		final PublishRelationship relationship = publishRelationshipMappings.get(clientId + "@"
				+ topic);
		if (relationship == null) {
			throw new OperationException(ResultCode.BAD_REQUEST, "The value of property topic ["
					+ topic + "] is not valid with client [" + clientId + "]!");
		}
		
		MDCCloseable closeable = null;
		
		try {
			/** add by zhr.增加密码校验。  **/
			final String password = message.getStringProperty(PropertyOption.VISIT_PASSWORD);
			final ClientLimit clientlimit = ClientLimitMappings.get(clientId + "@password");
			if (clientlimit != null) {
				if (password == null){
					throw new OperationException(ResultCode.BAD_REQUEST, "error. client [" + clientId +"] password is not null!");
				}
				else{
					byte[] md5 = MessageDigest.getInstance("MD5").digest(password.getBytes("utf-8"));
					if (!byte2Hex(md5).equals(clientlimit.getLimitValue())){
						LOGGER.trace("client md5=[" + byte2Hex(md5) + "],configiration is [" + clientlimit.getLimitValue() + "]");
						throw new OperationException(ResultCode.BAD_REQUEST, "error. client [" + clientId +"] password is not correct!");
					}
				}
			}
		
			final SocketAddress address = context.channel().remoteAddress();
			final MessageId id = messageIdGenerator.generate((InetSocketAddress) address, message,
					messageIdSequence.incrementAndGet());
			closeable = MDC.putCloseable(MessageId.KEY, id.toString());
			message.setId(id.getValue());
			final long before = jdbcExpectTime > 0 ? System.currentTimeMillis() : 0;
			
			if (persistent){
				messageRepository.save(message);
			}
			
			if (jdbcExpectTime > 0) {
				final long used = System.currentTimeMillis() - before;
				if (used >= jdbcExpectTime) {
					LOGGER.warn("--save message cost {} ms", used);
				}
			}
			// 只需要缓存消息头即可
			Message cache = null;
			
			if (!persistent){
				cache = Message.create(message.getPropertiesAsString(), message.getContentAsUtf8String());
			}
			else {
				cache = Message.create();
				cache.copyProperties(message);
			}
			messageCache.put(id.getValue(), cache);
			final Message answerMessage = Message.create();
			answerMessage.setProperty(PropertyOption.MESSAGE_ID, id.toString());
			answerMessage.setProperty(PropertyOption.RESULT_CODE, ResultCode.OK);
			return answerMessage;
		} catch (final Exception e) {
			throw new OperationException(ResultCode.INTERNAL_DATA_ACCESS_EXCEPTION, e);
		} finally {
			if (closeable != null) {
				closeable.close();
			}
		}
	}
}
