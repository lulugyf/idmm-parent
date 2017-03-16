package com.sitech.crmpd.idmm2.broker;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.timeout.IdleState;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.net.MediaType;
import com.sitech.crmpd.idmm2.broker.config.Configuration;
import com.sitech.crmpd.idmm2.broker.config.ConfigurationProvider;
import com.sitech.crmpd.idmm2.broker.config.WhiteListItem;
import com.sitech.crmpd.idmm2.broker.handler.MessageHandler;
import com.sitech.crmpd.idmm2.broker.utils.ReloadableResourceBundle;
import com.sitech.crmpd.idmm2.broker.utils.Splitters;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.ResultCode;
import com.sitech.crmpd.idmm2.client.exception.OperationException;

/**
 * Http协议的支持。<br/>
 * body为json格式
 *
 * @author heihuwudi@gmail.com</br> Created By: 2015年3月24日 下午8:11:10
 */
@Sharable
public class HttpLogicHandler extends SimpleChannelInboundHandler<HttpObject> implements
		ApplicationContextAware {

	/** name="{@link com.sitech.crmpd.idmm2.broker.HttpLogicHandler}" */
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpLogicHandler.class);
	private final FullHttpResponse CONTINUE_HTTP_RESPONSE = new DefaultFullHttpResponse(HTTP_1_1,
			CONTINUE);
	private final FullHttpResponse NOT_FOUND_HTTP_RESPONSE = new DefaultFullHttpResponse(HTTP_1_1,
			NOT_FOUND);
	private final FullHttpResponse FORBIDDEN_HTTP_RESPONSE = new DefaultFullHttpResponse(HTTP_1_1,
			FORBIDDEN);
	private Set<SocketAddress> remoteAddresses = Sets.newConcurrentHashSet();
	private ApplicationContext applicationContext;
	@Autowired(required = false)
	@Qualifier("authorization")
	private ReloadableResourceBundle authorization;
	@Autowired
	private ConfigurationProvider provider;
	@Value("${http.concurrent:20000}")
	private int concurrent;
	/**
	 * 计数器，用spring控制为单例
	 */
	@Resource
	private AtomicLong uuid;
	private boolean isBasicAuth;
	private static final String[] PROXY_KEYS = { "x-forwarded-for", "Proxy-Client-IP",
			"WL-Proxy-Client-IP" };

	/**
	 *
	 */
	@PostConstruct
	public void init() {
		isBasicAuth = authorization != null && !authorization.isEmpty();
	}

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
		if (remoteAddresses.size() == concurrent) {
			// 流量控制，如果连接数过多，直接拒绝连接
			ctx.close();
		} else {
			super.channelActive(ctx);
			final SocketAddress s = ctx.channel().remoteAddress();
			if (s != null) {
				remoteAddresses.add(s);
			}
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

	@Override
	/**
	 *
	 * @see io.netty.channel.ChannelInboundHandlerAdapter#userEventTriggered(io.netty.channel.ChannelHandlerContext, java.lang.Object)
	 */
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleState) {
			final IdleState e = (IdleState) evt;
			if (e == IdleState.READER_IDLE || e == IdleState.WRITER_IDLE) {
				LOGGER.info("Closing idle channel: {}", e);
				ctx.close();
			}
		}
	}

	private void sendResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String data,
			boolean keepAlive) {
		final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status,
				Unpooled.wrappedBuffer(data.getBytes(StandardCharsets.UTF_8)));
		sendResponse(ctx, response, keepAlive);
	}

	private void sendResponse(ChannelHandlerContext ctx, FullHttpResponse response,
			boolean keepAlive) {
		response.headers().set(CONTENT_TYPE, MediaType.PLAIN_TEXT_UTF_8);
		response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
		if (keepAlive) {
			response.headers().set(CONNECTION, Values.KEEP_ALIVE);
			ctx.writeAndFlush(response);
		} else {
			response.headers().set(CONNECTION, Values.CLOSE);
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}
	}

	/**
	 *
	 * @see io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty.channel.ChannelHandlerContext,
	 *      java.lang.Object)
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		final MDCCloseable closeable = MDC.putCloseable("uuid",
				Long.toString(uuid.incrementAndGet()));
		final FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
		if (HttpHeaders.is100ContinueExpected(fullHttpRequest)) {
			ctx.write(CONTINUE_HTTP_RESPONSE);
		}

		final HttpHeaders httpHeaders = fullHttpRequest.headers();
		for (final Entry<String, String> entry : httpHeaders) {
			LOGGER.trace("{}: {}", entry.getKey(), entry.getValue());
		}

		String type = fullHttpRequest.getUri();
		if ("/favicon.ico".equals(type)) {
			ctx.writeAndFlush(NOT_FOUND_HTTP_RESPONSE).addListener(ChannelFutureListener.CLOSE);
			return;
		}

		while (type.startsWith("/")) {
			type = type.length() == 1 ? "" : type.substring(1);
		}
		LOGGER.debug("消息类型-->[{}]", type);
		final boolean keepAlive = HttpHeaders.isKeepAlive(fullHttpRequest);
		try {
			if (!applicationContext.containsBean(type)) {
				/** 当Handler未找到时，判断下是否在可接受类型里 */
				/** 如果在，则说明是服务端配置出问题，否则是错误的请求 */
				final Map<String, Object> map = Maps.newHashMap();
				map.put(PropertyOption.RESULT_CODE.toString(), ResultCode.BAD_REQUEST);
				sendResponse(ctx, BAD_REQUEST, JSON.toJSONString(map), keepAlive);
			} else {
				if (isBasicAuth) {
					final String auth = httpHeaders.get(Names.AUTHORIZATION);
					final boolean matched = StringUtils.hasText(auth)
							&& authorization.containsKey(auth);
					if (!matched) {
						LOGGER.trace("权限校验不通过 = [{}]", auth);
						sendResponse(ctx, FORBIDDEN_HTTP_RESPONSE, false);
						return;
					}
				}

				final Configuration configuration = provider.getConfiguration();
				final Map<String, WhiteListItem> whiteListItemMap = configuration
						.get(WhiteListItem.class);
				if (!whiteListItemMap.isEmpty()) {
					final String ip = getIp(httpHeaders, ctx);
					LOGGER.trace("IP地址 = [{}]", ip);
					if (!whiteListItemMap.containsKey(ip)) {
						LOGGER.trace("IP地址不在白名单 = [{}]", ip);
						sendResponse(ctx, FORBIDDEN, "IP address [" + ip + "] rejected", false);
						return;
					}
				}

				final String contentLength = httpHeaders.get(Names.CONTENT_LENGTH);
				if (Strings.isNullOrEmpty(contentLength)) {
					throw new OperationException(ResultCode.BAD_REQUEST, "The value of "
							+ Names.CONTENT_LENGTH + " is not found");
				}

				if (Integer.parseInt(contentLength) == 0) {
					throw new OperationException(ResultCode.BAD_REQUEST, "The value of "
							+ Names.CONTENT_LENGTH + " should not be zero");
				}

				String encoding = httpHeaders.get(Names.CONTENT_TYPE);
				if (!Strings.isNullOrEmpty(encoding)) {
					final int index = encoding.indexOf(Values.CHARSET);
					encoding = index == -1 ? null : encoding
							.substring(index + Values.CHARSET.length()).replaceAll("\"", "")
							.replaceAll("=", "").trim();
				}

				httpHeaders.get(Names.CONTENT_LENGTH);

				final String fullContent = fullHttpRequest.content().toString(
						encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding));
				LOGGER.trace(fullContent);
				final JSONObject json = JSON.parseObject(fullContent);
				final String clientId = json.getString(PropertyOption.CLIENT_ID.toString());
				if (Strings.isNullOrEmpty(clientId)) {
					throw new OperationException(ResultCode.BAD_REQUEST,
							"The value of property client-id must not be empty!");
				}
				LOGGER.debug("开始处理客户端[{}]发送的类型为[{}]的消息", clientId, type);
				final String content = json == null ? "" : json.containsKey("content") ? json
						.remove("content").toString() : null;
				Message message = json == null ? Message.create() : Message.create(
						json.toJSONString(), content);
				final MessageHandler messageHandler = applicationContext.getBean(type,
						MessageHandler.class);
				message = messageHandler.handle(ctx, message);
				if (!message.existProperty(PropertyOption.RESULT_CODE)) {
					message.setProperty(PropertyOption.RESULT_CODE, ResultCode.OK);
				}
				sendResponse(ctx, OK, message.toRestfulString(), keepAlive);
			}
		} catch (final Exception e) {
			LOGGER.error("", e);
			final Map<String, Object> map = Maps.newHashMap();
			if (e instanceof OperationException) {
				map.put(PropertyOption.RESULT_CODE.toString(),
						((OperationException) e).getResultCode());
			} else {
				map.put(PropertyOption.RESULT_CODE.toString(), ResultCode.INTERNAL_SERVER_ERROR);
			}
			final String detailMessage = e.getMessage();
			if (!Strings.isNullOrEmpty(detailMessage)) {
				map.put(PropertyOption.CODE_DESCRIPTION.toString(), detailMessage);
			}
			sendResponse(ctx, INTERNAL_SERVER_ERROR, JSON.toJSONString(map), false);
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
	 * @return {@link #messageHandlers} 属性的值
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

	private String getIp(HttpHeaders headers, ChannelHandlerContext context) {
		for (final String name : PROXY_KEYS) {
			final String address = headers.get(name);
			if (StringUtils.hasText(address)) {
				// 如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值
				// 取 X-Forwarded-For中第一个非unknown的有效IP字符串
				final List<String> list = Splitters.split(address);
				for (final String ip : list) {
					if (!"unknown".equalsIgnoreCase(ip)) {
						return ip;
					}
				}
			}
		}
		return ((InetSocketAddress) context.channel().remoteAddress()).getAddress().toString()
				.replaceAll("/", "");
	}

}
