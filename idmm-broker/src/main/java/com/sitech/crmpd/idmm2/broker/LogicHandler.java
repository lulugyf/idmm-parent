package com.sitech.crmpd.idmm2.broker;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.sitech.crmpd.idmm2.broker.handler.MessageHandler;
import com.sitech.crmpd.idmm2.client.api.FrameMessage;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageType;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.ResultCode;
import com.sitech.crmpd.idmm2.client.exception.OperationException;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年3月24日 下午8:11:10
 */
@Sharable
public class LogicHandler extends SimpleChannelInboundHandler<FrameMessage> implements
		ApplicationContextAware {

	/** name="{@link com.sitech.crmpd.idmm2.broker.LogicHandler}" */
	private static final Logger LOGGER = LoggerFactory.getLogger(LogicHandler.class);
	/**
	 * uuid在mdc中的key
	 */
	public static final String MDC_UUID_KEY = "uuid";
	private Set<SocketAddress> remoteAddresses = Sets.newConcurrentHashSet();
	private ApplicationContext applicationContext;
	/**
	 * 计数器，用spring控制为单例
	 */
	@Resource
	private AtomicLong uuid;

	@Value("${maxconn.per.broker:100000}")
	private int maxconn_per_broker; //单个broker允许的最大连接数
	
	/**
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * @see io.netty.channel.ChannelInboundHandlerAdapter#channelActive(io.netty.channel.ChannelHandlerContext)
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		final SocketAddress s = ctx.channel().remoteAddress();
		if (s != null) {
			remoteAddresses.add(s);
		}
		
		if(remoteAddresses.size()>maxconn_per_broker){
			LOGGER.error("current connects is " + remoteAddresses.size() + ",maxconn_per_broker is [" + maxconn_per_broker +"]", "");
			ctx.close();
		}
	}

	/**
	 * @see io.netty.channel.ChannelInboundHandlerAdapter#channelInactive(io.netty.channel.ChannelHandlerContext)
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		final SocketAddress s = ctx.channel().remoteAddress();
		if (s != null) {
			remoteAddresses.remove(s);
		}
	}

	/**
	 * @see io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty.channel.ChannelHandlerContext,
	 *      java.lang.Object)
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FrameMessage msg) throws Exception {
		LOGGER.debug("收到消息-->[{}]", msg);
		final MDCCloseable closeable = MDC.putCloseable("uuid",
				Long.toString(uuid.incrementAndGet()));
		final MessageType type = msg.getType();
		LOGGER.debug("消息类型-->[{}]", type);
		try {
			if (!applicationContext.containsBean(type.name())) {
				/** 当Handler未找到时，判断下是否在可接受类型里 */
				/** 如果在，则说明是服务端配置出问题，否则是错误的请求 */
				final Message answerMessage = Message.create();
				answerMessage.setProperty(PropertyOption.RESULT_CODE, ResultCode.BAD_REQUEST);
				ctx.writeAndFlush(new FrameMessage(MessageType.ANSWER, answerMessage));
				/** 主动关闭连接 */
				ctx.close();
			} else {
				final MessageHandler messageHandler = applicationContext.getBean(type.name(),
						MessageHandler.class);

				final Message message = msg.getMessage();
				final String clientId = message.getStringProperty(PropertyOption.CLIENT_ID);
				if (Strings.isNullOrEmpty(clientId)) {
					throw new OperationException(ResultCode.BAD_REQUEST,
							"The value of property client-id must not be empty!");
				}
				LOGGER.debug("开始处理客户端[{}]发送的类型为[{}]的消息", clientId, type);

				final Message answerMessage = messageHandler.handle(ctx, message);
				if (!answerMessage.existProperty(PropertyOption.RESULT_CODE)) {
					answerMessage.setProperty(PropertyOption.RESULT_CODE, ResultCode.OK);
				}
				ctx.writeAndFlush(new FrameMessage(messageHandler.getAnswerType(), answerMessage));
			}
		} catch (final OperationException e) {
			LOGGER.error("", e);
			final Message answerMessage = Message.create();
			answerMessage.setProperty(PropertyOption.RESULT_CODE, e.getResultCode());
			answerMessage.setProperty(PropertyOption.CODE_DESCRIPTION, e.getResultDescrition());
			ctx.writeAndFlush(new FrameMessage(MessageType.ANSWER, answerMessage));
		} catch (final Exception e) {
			LOGGER.error("", e);
			final Message answerMessage = Message.create();
			answerMessage.setProperty(PropertyOption.RESULT_CODE, ResultCode.INTERNAL_SERVER_ERROR);
			final String detailMessage = e.getMessage();
			if (!Strings.isNullOrEmpty(detailMessage)) {
				answerMessage.setProperty(PropertyOption.CODE_DESCRIPTION, detailMessage);
			}
			ctx.writeAndFlush(new FrameMessage(MessageType.ANSWER, answerMessage));
		} finally {
			closeable.close();
		}
	}

	/**
	 * @see io.netty.channel.ChannelInboundHandlerAdapter#exceptionCaught(io.netty.channel.ChannelHandlerContext,
	 *      java.lang.Throwable)
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error(Strings.nullToEmpty(cause.getMessage()), cause);
		ctx.close();
	}

	/**
	 * 获取{@link #messageHandlers}属性的值
	 *
	 * @return {@link #messageHandlers}属性的值
	 */
	public Map<String, MessageHandler> getMessageHandlers() {
		return applicationContext.getBeansOfType(MessageHandler.class);
	}

	/**
	 * 获取{@link #remoteAddresses}属性的值
	 *
	 * @return {@link #remoteAddresses}属性的值
	 */
	public Set<SocketAddress> getRemoteAddresses() {
		return remoteAddresses;
	}

}
