package com.sitech.crmpd.idmm2.broker.handler;

import io.netty.channel.ChannelHandlerContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codahale.metrics.annotation.Metered;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.sitech.crmpd.idmm2.broker.utils.CuratorFrameworkHolder;
import com.sitech.crmpd.idmm2.broker.utils.FlumeLogger;
import com.sitech.crmpd.idmm2.broker.utils.Util;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageType;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.ResultCode;
import com.sitech.crmpd.idmm2.client.exception.OperationException;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年3月29日 下午2:33:39
 */
@Component("SEND_COMMIT")
public class SendCommitMessageHandler extends SendMessageHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(SendCommitMessageHandler.class);
	
	@Autowired
	ListeningExecutorService executorService;

	/**
	 * @see com.sitech.crmpd.idmm2.broker.handler.MessageHandler#getType()
	 */
	@Override
	public MessageType getType() {
		return MessageType.SEND_COMMIT;
	}

	/**
	 *
	 * @see com.sitech.crmpd.idmm2.broker.handler.SendMessageHandler#handle(io.netty.channel.ChannelHandlerContext,
	 *      com.sitech.crmpd.idmm2.client.api.Message)
	 */
	@Override
	@Metered
	public Message handle(ChannelHandlerContext context, Message message) throws OperationException {
		return handleAndAnswer(message, getType());
	}

	Message handleAndAnswer(Message message, MessageType type) throws OperationException {
		final String[] batchIds = message.existProperty(PropertyOption.BATCH_MESSAGE_ID) ? message
				.getArray(PropertyOption.BATCH_MESSAGE_ID, new String[0]) : new String[] { message
				.getStringProperty(PropertyOption.MESSAGE_ID) };
		try {
			final List<ListenableFuture<?>> futures = Lists.newArrayList();
			// 计算索引信息
			for (final String id : batchIds) {
				final CommitTask task = applicationContext.getBean(CommitTask.class);
				task.setMessage(message);
				task.setMessageType(type);
				task.setId(id);
				StringBuffer result = new StringBuffer("-1");
				task.setTaskResult(result);
				futures.add(executorService.submit(task,result));
			}
			List<Object> li = (List<Object>)Futures.successfulAsList(futures).get();
			
			final Message answerMessage = Message.create();
			answerMessage.setProperty(PropertyOption.RESULT_CODE, ResultCode.OK);
			for (Object o:li){
				if(o.toString().equals("-1")){
					answerMessage.setProperty(PropertyOption.RESULT_CODE, ResultCode.INTERNAL_DATA_ACCESS_EXCEPTION);
					break;
				}
			}
			return answerMessage;
		} catch (final Exception e) {
			throw new OperationException(ResultCode.INTERNAL_DATA_ACCESS_EXCEPTION, e);
		} finally {
			for (final String id : batchIds) {
				messageCache.evict(id);
			}
		}
	}
}
