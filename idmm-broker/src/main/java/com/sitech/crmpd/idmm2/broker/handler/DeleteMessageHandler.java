package com.sitech.crmpd.idmm2.broker.handler;

import io.netty.channel.ChannelHandlerContext;

import org.springframework.stereotype.Component;

import com.codahale.metrics.annotation.Metered;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageType;
import com.sitech.crmpd.idmm2.client.exception.OperationException;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年5月6日 上午9:20:58
 */
@Component("DELETE")
public class DeleteMessageHandler extends SendCommitMessageHandler {

	/**
	 * @see com.sitech.crmpd.idmm2.broker.handler.MessageHandler#getType()
	 */
	@Override
	public MessageType getType() {
		return MessageType.DELETE;
	}

	/**
	 *
	 * @see com.sitech.crmpd.idmm2.broker.handler.SendCommitMessageHandler#handle(io.netty.channel.ChannelHandlerContext,
	 *      com.sitech.crmpd.idmm2.client.api.Message)
	 */
	@Override
	@Metered
	public Message handle(ChannelHandlerContext context, Message message) throws OperationException {
		return handleAndAnswer(message, getType());
	}
}
