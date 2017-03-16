package com.sitech.crmpd.idmm2.broker.handler;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Scope;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.net.HostAndPort;
import com.sitech.crmpd.idmm2.broker.config.ConfigConstant;
import com.sitech.crmpd.idmm2.broker.config.Configuration;
import com.sitech.crmpd.idmm2.broker.config.ConfigurationProvider;
import com.sitech.crmpd.idmm2.broker.config.ConsumerInOrder;
import com.sitech.crmpd.idmm2.broker.config.TopicMapping;
import com.sitech.crmpd.idmm2.broker.config.TopicToBle;
import com.sitech.crmpd.idmm2.broker.pool.Key;
import com.sitech.crmpd.idmm2.broker.repository.MessageRepository;
import com.sitech.crmpd.idmm2.broker.utils.CuratorFrameworkHolder;
import com.sitech.crmpd.idmm2.broker.utils.FlumeLogger;
import com.sitech.crmpd.idmm2.broker.utils.Util;
import com.sitech.crmpd.idmm2.client.BasicMessageContext;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageId;
import com.sitech.crmpd.idmm2.client.api.MessageIdGenerator;
import com.sitech.crmpd.idmm2.client.api.MessageType;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.ResultCode;
import com.sitech.crmpd.idmm2.client.exception.OperationException;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年6月19日 上午11:32:14
 */
@Component
@Scope("prototype")
public class CommitTask implements Runnable {
	/** name="{@link com.sitech.crmpd.idmm2.broker.handler.CommitTask}" */
	private static final Logger LOGGER = LoggerFactory.getLogger(CommitTask.class);
	/**
	 *
	 */
	public static final Method PROPERTYOPTION_VALUEOF = ClassUtils.getMethod(PropertyOption.class,
			"valueOf", String.class);
	/**
	 *
	 */
	public static final ExpressionParser EXPRESSIONPARSER = new SpelExpressionParser();
	/**
	 * 与 的缓存表
	 *
	 * @see LoadingCache
	 * @see CacheBuilder#newBuilder()
	 * @see CacheBuilder#weakKeys()
	 * @see CacheBuilder#weakValues()
	 * @see CacheBuilder#expireAfterAccess(long, TimeUnit)
	 * @see CacheLoader#load(Object)
	 */
	private static final LoadingCache<Long, StandardEvaluationContext> EVALUATION_CONTEXT = CacheBuilder
			.newBuilder().weakKeys().weakValues().expireAfterAccess(10, TimeUnit.MINUTES)
			.build(new CacheLoader<Long, StandardEvaluationContext>() {

				@Override
				public StandardEvaluationContext load(Long key) throws Exception {
					final StandardEvaluationContext context = new StandardEvaluationContext();
					context.registerFunction("valueOf", PROPERTYOPTION_VALUEOF);
					context.setVariable("P", PropertyOption.class);
					return context;
				}
			});

	@Autowired
	private GenericKeyedObjectPool<Key, BasicMessageContext> pool;
	@Autowired
	private ConfigConstant constant;
	@Autowired
	private CuratorFrameworkHolder client;
	@Autowired
	private ConfigurationProvider provider;
	@Autowired
	private Cache messageCache;
	@Autowired
	private MessageRepository messageRepository;
	@Autowired
	private MessageIdGenerator messageIdGenerator;
	@Autowired(required = false)
	@Qualifier("bleNotFoundJdbcTemplate")
	private NamedParameterJdbcTemplate bleNotFoundJdbcTemplate;
	@Autowired(required = false)
	@Qualifier("bleNotFoundInsertSQL")
	private String bleNotFoundInsertSQL;
	@Autowired
	private FlumeLogger flumeLogger;
	/** 需要set的值 */
	private volatile Message message;
	private volatile MessageType messageType;
	private volatile String id;
	private volatile StringBuffer TaskResult;
	@Value("${message.persistent:yes}")
	private boolean persistent; //是否持久化

	private static final LoadingCache<String, java.util.regex.Pattern> MATCHERS = CacheBuilder
			.newBuilder().weakKeys().weakValues().expireAfterAccess(10, TimeUnit.MINUTES)
			.build(new CacheLoader<String, java.util.regex.Pattern>() {

				@Override
				public java.util.regex.Pattern load(String key) {
					return java.util.regex.Pattern.compile(key);
				}

			});

	private static final LoadingCache<String, Expression> EXPRESSIONS = CacheBuilder.newBuilder()
			.weakKeys().weakValues().expireAfterAccess(10, TimeUnit.MINUTES)
			.build(new CacheLoader<String, Expression>() {

				@Override
				public Expression load(String key) {
					return EXPRESSIONPARSER.parseExpression(key);
				}

			});

	/**
	 * 设置{@link #message}属性的值
	 *
	 * @param message
	 *            属性值
	 */
	public void setMessage(Message message) {
		this.message = message;
	}

	/**
	 * 设置{@link #messageType}属性的值
	 *
	 * @param messageType
	 *            属性值
	 */
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	/**
	 * 设置{@link #id}属性的值
	 *
	 * @param id
	 *            属性值
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * 设置{@link #TaskResult}的值
	 *
	 * @param id
	 *            属性值
	 */
	public void setTaskResult(StringBuffer TaskResult) {
		this.TaskResult = TaskResult;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		final MDCCloseable closeable = MDC.putCloseable(MessageId.KEY, id);
		int retry = 3;
		while (retry-- > 0) {
			try {
				call();
				TaskResult.replace(0, TaskResult.length(), "0");
				break;
			} catch (final Exception e) {
				if (retry == 0) {
					LOGGER.error("", e);
				} else {
					LOGGER.warn("", e);
				}
			}
		}
		closeable.close();
	}

	/**
	 * @throws Exception
	 */
	public void call() throws Exception {
		// commit时没有主题信息，只能从send消息的缓存里取
		Message cache = messageCache.get(id, Message.class);
		if (cache == null) {
			cache = messageRepository.findOne(messageIdGenerator.generate(id));
		}
		LOGGER.trace("cache = [{}]", cache);
		// 定时消息
		if (messageType == MessageType.SEND_COMMIT
				&& cache.existProperty(PropertyOption.EFFECTIVE_TIME)) {
			LOGGER.trace("定时消息");
			messageRepository.saveDelay(cache);
			return;
		}

		final String sourceTopicId = cache.getStringProperty(PropertyOption.TOPIC);
		LOGGER.trace("sourceTopicId = [{}]", sourceTopicId);

		final Configuration configuration = provider.getConfiguration();
		final Map<String, TopicMapping> topicMappings = configuration.get(TopicMapping.class);
		LOGGER.trace("topicMappings = [{}]", topicMappings);
		// 先不考虑缓存
		final Map<String, TopicMapping> maps = Maps.newHashMap();
		final Set<String> matched = Sets.newHashSet();
		final Map<String, ConsumerInOrder> orders = configuration.get(ConsumerInOrder.class);
		LOGGER.trace("orders = [{}]", orders);

		// 把commit所带的消息属性覆盖到send消息上
		cache.copyProperties(message);
		for (final Entry<String, TopicMapping> entry : topicMappings.entrySet()) {
			final TopicMapping bean = entry.getValue();
			if (!bean.getSourceTopicId().equalsIgnoreCase(sourceTopicId)) {
				continue;
			}

			// 判断是否需要忽略
			if (constant.getValueIgnore().equalsIgnoreCase(bean.getTargetTopicId())) {
				// 什么都不做
				continue;
			}
			final String propertyKey = bean.getPropertyKey();
			LOGGER.trace("propertyKey = [{}]", propertyKey);
			final String propertyValue = bean.getPropertyValue();
			LOGGER.trace("propertyValue = [{}]", propertyValue);
			if (constant.getValueDefault().equals(propertyValue)) {
				// 默认值不在循环里面匹配
				maps.put(propertyKey, bean);
				LOGGER.trace("默认值不在循环里面匹配");
				continue;
			}

			// 判断是进行简单值匹配还是进行复杂的表达式计算
			final String targetTopicId = bean.getTargetTopicId();
			if (Strings.isNullOrEmpty(propertyKey)) {
				// 表达式计算，改用SPEL，spring环境下支持更好
				final StandardEvaluationContext context = EVALUATION_CONTEXT.get(Thread
						.currentThread().getId());
				context.registerFunction("valueOf", PROPERTYOPTION_VALUEOF);
				context.setVariable("P", PropertyOption.class);
				EXPRESSIONS.get(propertyValue).getValue(context, Boolean.class);
				// 表达式结果为false
				if (!EXPRESSIONS.get(propertyValue).getValue(context, Boolean.class)) {
					LOGGER.trace("表达式匹配结果为false");
					continue;
				}
			} else {

				// 简单值匹配
				final String value = cache.existProperty(propertyKey) ? cache.getProperty(
						propertyKey).toString() : cache.getStringProperty(PropertyOption
						.valueOf(propertyKey));

				LOGGER.trace("value = [{}]", value);

				// 属性值可以是“_default”，表示如果生产者没有传递这个属性信息或者是定义的各个属性值都不符合要求
				if (value == null) {
					LOGGER.trace("未取到值不匹配");
					continue;
				}

				// 值匹配不成功
				if (!MATCHERS.getUnchecked(propertyValue).matcher(value).matches()) {
					continue;
				}
			}
			call(configuration, cache, sourceTopicId, targetTopicId, orders, propertyKey,
					propertyValue);
			matched.add(propertyKey);
		}
		LOGGER.trace("matched = [{}]", matched);
		LOGGER.trace("maps = [{}]", maps);
		for (final Entry<String, TopicMapping> entry : maps.entrySet()) {
			if (!matched.contains(entry.getKey())) {
				// 默认值条件
				final TopicMapping bean = entry.getValue();
				final String targetTopicId = bean.getTargetTopicId();
				final String propertyKey = bean.getPropertyKey();
				final String propertyValue = bean.getPropertyValue();
				call(configuration, cache, sourceTopicId, targetTopicId, orders, propertyKey,
						propertyValue);
			}
		}
		
		//INFO级别时，发送端到端日志到flume服务器
		if (LOGGER.isInfoEnabled() && cache.existProperty(PropertyOption.valueOf("trace_id")) && 
				cache.existProperty(PropertyOption.valueOf("call_id"))){
			LOGGER.info("记录端到端日志-->>>{}{}", cache.existProperty(PropertyOption.valueOf("trace_id")), cache.existProperty(PropertyOption.valueOf("call_id")));
			flumeLogger.sendFlumeLog(cache.getProperty(PropertyOption.valueOf("trace_id")).toString(), 
					cache.getId(), cache.getProperty(PropertyOption.valueOf("call_id")).toString(), 
					Util.getTime(), Util.getTime(), Util.getlocalip());
		}
	}

	private void call(Configuration configuration, Message cache, String sourceTopicId,
			String targetTopicId, Map<String, ConsumerInOrder> orders, String propertyKey,
			String propertyValue) throws Exception {
		LOGGER.trace("targetTopicId = [{}]", targetTopicId);
		
		/** modify by zhr.2016/08/03 支持文件持久。消息内容先放缓存，send commit时带上消息内容发给ble 
		final Message index = Message.create();
		index.copyProperties(message);
		**/
		Message index = null;
		if (persistent){
			index = Message.create();
			index.copyProperties(message);
		}
		else{
			index = Message.create(cache.getPropertiesAsString(), cache.getContentAsUtf8String());
		}
		
		if (messageType == MessageType.SEND_COMMIT) {
			// 从send消息复制属性
			index.copyProperties(cache);
			if (orders.containsKey(sourceTopicId + constant.getKeySep() + propertyKey
					+ constant.getKeySep() + propertyValue + constant.getKeySep() + targetTopicId)) {
				// 在消费顺序表有配置，将匹配成功的key、value加到索引信息中
				index.setProperty(PropertyOption.CURRENT_PROPERTY_KEY, propertyKey);
				index.setProperty(PropertyOption.CURRENT_PROPERTY_VALUE, propertyValue);
			}
		}
		index.setId(id);

		// 根据配置得到BLE的信息
		final TopicToBle topicToBle = configuration.get(TopicToBle.class, targetTopicId);
		// 查询ble地址
		try {
			final String data = new String(client.getData(constant.getBleIdPath()
					+ topicToBle.getBleId()));
			LOGGER.trace("ble-data = [{}]", data);
			// 发送到ble
			final HostAndPort address = HostAndPort.fromString(data.split(" ")[0]);
			LOGGER.trace("ble-address = [{}]", address);
			call(index, targetTopicId, address.getHostText(), address.getPort());
		} catch (final NoNodeException e) {
			// 没有可用BLE，先存到BLE_NOT_FOUND表
			if (bleNotFoundJdbcTemplate != null && StringUtils.hasText(bleNotFoundInsertSQL)) {
				insertToBleNotFound(targetTopicId, index);
			}
		} catch (final OperationException e) {
			if (e.getResultCode() == ResultCode.SERVICE_ADDRESS_NOT_FOUND) {
				insertToBleNotFound(targetTopicId, index);
			} else {
				throw e;
			}
		}

	}

	/**
	 * @param message
	 * @param targetTopicId
	 * @param host
	 * @param port
	 * @throws Exception
	 */
	public void call(Message message, String targetTopicId, String host, int port) throws OperationException {
		Key key = null;
		BasicMessageContext messageContext = null;
		try {
			key = Key.create(targetTopicId, host, port);
			messageContext = pool.borrowObject(key);
			final String clientId = message.getStringProperty(PropertyOption.CLIENT_ID);
			message.setProperty(PropertyOption.TARGET_TOPIC, targetTopicId);
			final Message answerMessage = messageContext.trade(clientId, messageType, message);
			LOGGER.info("BLE返回消息{}", answerMessage);
		} catch (final OperationException e) {
			throw e;
		} catch (final IOException e) {
			if (messageContext != null) {
				messageContext.close();
			}
			throw new OperationException(ResultCode.INTERNAL_DATA_ACCESS_EXCEPTION, e);
		} catch (final Exception e) {
			throw new OperationException(ResultCode.INTERNAL_DATA_ACCESS_EXCEPTION, e);
		} finally {
			if (key != null && messageContext != null) {
				pool.returnObject(key, messageContext);
			}
		}
	}

	private void insertToBleNotFound(String targetTopicId, Message index) {
		final Map<String, String> paramMap = Maps.newHashMap();
		paramMap.put("id", id);
		paramMap.put("targetTopicId", targetTopicId);
		paramMap.put("properties", index.getPropertiesAsString());
		final int rows = bleNotFoundJdbcTemplate.update(bleNotFoundInsertSQL, paramMap);
		if (rows != 1) {
			throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(bleNotFoundInsertSQL, 1,
					rows);
		}
	}

}