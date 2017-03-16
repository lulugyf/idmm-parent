package com.sitech.crmpd.idmm.ble;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInboundHandlerAdapter;

import com.sitech.crmpd.idmm2.client.api.FrameMessage;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageType;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.ResultCode;


/**
 * ble 通讯入口
 * 
 * broker 与 ble的通讯过程：
 * 	1.  发送消息 (target-topic)  以及应答
 *  2.  消费消息 (target-topic + client-id)  以及应答
 *  3.  消费确认 (message-id)  以及应答
 */
@Sharable
public class BLEHandler extends ChannelInboundHandlerAdapter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BLEHandler.class);
	
	private ConcurrentHashMap<ChannelHandlerContext, String> brokerids = new ConcurrentHashMap<ChannelHandlerContext, String>();

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error("===channel read failed, close it", cause);
		ctx.channel().close();
		//super.exceptionCaught(ctx, cause);
	}

	@Resource
	private BLEEntry entry;
	
	/**
	 * @see io.netty.channel.ChannelInboundHandlerAdapter#channelRead(io.netty.channel.ChannelHandlerContext,
	 *      java.lang.Object)
	 *      
	 *  增删改应答answer
	 *	查应答pull_answer
	 */
	@Override
	@com.codahale.metrics.annotation.Metered
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		FrameMessage frameMessage = (FrameMessage) msg;
		MessageType mtype = frameMessage.getType();
		MessageType rmtype = MessageType.ANSWER;
		Message message = frameMessage.getMessage();
		Message ret = null;
		//System.out.println(frameMessage.getType());
		String broker_id = null;
		if(brokerids.containsKey(ctx))
			broker_id = brokerids.get(ctx);
		switch(mtype){
		case SEND_COMMIT:
			ret = entry.messageSend(message, broker_id);
			break;
		case PULL:
			ret = entry.messagePull(message, broker_id);
			rmtype = MessageType.PULL_ANSWER;
			break;
		case DELETE:
			ret = entry.messageDelete(message, broker_id);
			break;
		case QUERY:
			broker_id = message.getStringProperty(PropertyOption.CLIENT_ID);
			brokerids.put(ctx, broker_id);
			ret = Message.create();
			ret.setProperty(PropertyOption.RESULT_CODE, ResultCode.OK);
			break;
		case UNLOCK:
			ret = entry.messageUnlock(message, broker_id);
			break;
		default:
			ret = Message.create();
			ret.setProperty(PropertyOption.RESULT_CODE, ResultCode.UNSUPPORTED_MESSAGE_TYPE);
		}
		if(ret != null)
			ctx.writeAndFlush(new FrameMessage(rmtype, ret)); //以相同的messagetype做应答

		/*
		System.out.println(message.getPropertiesAsString());
		System.out.println(message.getContentAsString());
		System.out
		.println(message.getBooleanProperty(PropertyOption.COMPRESS) == true);

		int priority = message.getIntegerProperty(PropertyOption.PRIORITY);
		System.out.println(priority);

		Message answerMessage = Message.create("hello world from ble!".getBytes());
		FrameMessage answerFrameMessage = new FrameMessage(MessageType.ANSWER,
				answerMessage);
		ctx.writeAndFlush(answerFrameMessage); */
		
		
		LOGGER.debug("hello----{} {}", System.currentTimeMillis(), ret != null);
		
		
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		try{
			LOGGER.info("==new connect from {}", ctx.channel().remoteAddress());
		}catch(Throwable e){
			LOGGER.error("", e);
		}
		entry.addClient(1);
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if(brokerids.containsKey(ctx))
			brokerids.remove(ctx);
		entry.addClient(-1);
		super.channelInactive(ctx);
	}
}
