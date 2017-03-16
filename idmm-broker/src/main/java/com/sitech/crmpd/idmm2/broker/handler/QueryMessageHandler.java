package com.sitech.crmpd.idmm2.broker.handler;

import io.netty.channel.ChannelHandlerContext;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codahale.metrics.annotation.Metered;
import com.sitech.crmpd.idmm2.broker.config.ConfigConstant;
import com.sitech.crmpd.idmm2.broker.utils.CuratorFrameworkHolder;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageType;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.ResultCode;
import com.sitech.crmpd.idmm2.client.exception.OperationException;

/**
 * 处理 {@link MessageType#QUERY}
 *
 * @author heihuwudi@gmail.com</br> Created By: 2015年3月27日 上午10:57:40
 */
@Component("QUERY")
public class QueryMessageHandler extends BaseMessageHandler {

	@Autowired
	ConfigConstant constant;
	@Autowired
	CuratorFrameworkHolder client;

	/**
	 * @see com.sitech.crmpd.idmm2.broker.handler.MessageHandler#getType()
	 */
	@Override
	public MessageType getType() {
		return MessageType.QUERY;
	}

	/**
	 * @see com.sitech.crmpd.idmm2.broker.handler.MessageHandler#getAnswerType()
	 */
	@Override
	public MessageType getAnswerType() {
		return MessageType.ANSWER;
	}

	/**
	 * @see com.sitech.crmpd.idmm2.broker.handler.MessageHandler#handleAndAnswer(io.netty.channel.ChannelHandlerContext,
	 *      com.sitech.crmpd.idmm2.client.api.Message)
	 */
	@Override
	@Metered
	public Message handle(ChannelHandlerContext context, Message message) throws OperationException {
		// final Message answerMessage = Message.create();
		try {
			final List<String> address = client.getChildren(constant.getBrokerPath());
			// 对取得的地址列表进行随机排序
			Collections.shuffle(address);
			final Message answerMessage = Message.create();
			answerMessage.copyProperties(message);
			answerMessage.setProperty(PropertyOption.ADDRESS, address.toArray(new String[0]));
			return answerMessage;
		} catch (final Exception e) {
			throw new OperationException(ResultCode.valueOfCode(""), e);
		}
	}

}
