package com.sitech.crmpd.idmm2.broker.handler;

import io.netty.channel.ChannelHandlerContext;

import org.springframework.stereotype.Component;

import com.codahale.metrics.annotation.Metered;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageType;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.ResultCode;
import com.sitech.crmpd.idmm2.client.exception.OperationException;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年3月29日 下午2:33:39
 */
@Component("SEND_ROLLBACK")
public class SendRollbackMessageHandler extends SendMessageHandler {

	/**
	 * @see com.sitech.crmpd.idmm2.broker.handler.MessageHandler#getType()
	 */
	@Override
	public MessageType getType() {
		return MessageType.SEND_ROLLBACK;
	}

	/**
	 *
	 * @see com.sitech.crmpd.idmm2.broker.handler.SendMessageHandler#handle(io.netty.channel.ChannelHandlerContext,
	 *      com.sitech.crmpd.idmm2.client.api.Message)
	 */
	@Override
	@Metered
	public Message handle(ChannelHandlerContext context, Message message) throws OperationException {
		final String[] batchIds = message.getArray(PropertyOption.BATCH_MESSAGE_ID, new String[0]);
		try {
			// 现阶段先丢弃处理，不用传递给BLE
			// 需求清理缓存，直接返回成功
			return Message.create();
		} catch (final Exception e) {
			throw new OperationException(ResultCode.INTERNAL_SERVER_ERROR, e);
		} finally {
			for (final String id : batchIds) {
				messageCache.evict(id);
			}
		}
	}
}
