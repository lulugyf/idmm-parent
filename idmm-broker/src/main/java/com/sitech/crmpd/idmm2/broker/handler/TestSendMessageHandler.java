package com.sitech.crmpd.idmm2.broker.handler;

import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.codahale.metrics.annotation.Metered;
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
@Profile("test")
@Component("SEND")
public class TestSendMessageHandler extends RepositoryMessageHandler {

	@Autowired
	MessageIdGenerator messageIdGenerator;
	@Autowired
	Cache messageCache;

	/**
	 * 计数器，用spring控制为单例
	 */
	@Resource
	AtomicLong messageIdSequence;

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
	 *
	 * @see com.sitech.crmpd.idmm2.broker.handler.MessageHandler#handle(io.netty.channel.ChannelHandlerContext,
	 *      com.sitech.crmpd.idmm2.client.api.Message)
	 */
	@Override
	@Metered
	public Message handle(ChannelHandlerContext context, Message message) throws OperationException {
		MDCCloseable closeable = null;
		try {
			final SocketAddress address = context.channel().remoteAddress();
			final MessageId id = messageIdGenerator.generate((InetSocketAddress) address, message,
					messageIdSequence.incrementAndGet());
			closeable = MDC.putCloseable(MessageId.KEY, id.toString());
			message.setId(id.getValue());
			// 只需要缓存消息头即可
			final Message cache = Message.create();
			cache.copyProperties(message);
			messageCache.put(id.getValue(), cache);
			final Message answerMessage = Message.create();
			answerMessage.setProperty(PropertyOption.MESSAGE_ID, id.toString());
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
