package com.sitech.crmpd.idmm2.broker.repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageId;
import com.sitech.crmpd.idmm2.client.api.MessageIdGenerator;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年3月29日 下午8:10:44
 */
@Configuration
public class MessageRepositoryJdbcImpl implements MessageRepository {

	/** name="{@link com.sitech.crmpd.idmm2.broker.repository.MessageRepositoryJdbcImpl}" */
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageRepositoryJdbcImpl.class);
	/**
	 * JDBC工具
	 */
	@Resource
	private NamedParameterJdbcTemplate storeJdbcTemplate;
	@Resource
	/**
	 * 插入消息的SQL
	 */
	private String jdbcInsertMessageSQL;
	@Resource
	/**
	 * 查询消息的SQL
	 */
	private String jdbcQueryMessageSQL;

	@Resource
	private String jdbcInsertMessageNotFoundSQL;
	@Resource
	/**
	 * 插入延迟消息的SQL
	 */
	private String jdbcInsertDelayMessageSQL;

	/**
	 * 查询结果映射成 {@link Message} 对象的映射
	 */
	@Autowired
	private RowMapper<Message> messageRowMapper;

	/**
	 * 表序号与查询SQL的缓存表
	 *
	 * @see LoadingCache
	 * @see CacheBuilder#newBuilder()
	 * @see CacheBuilder#weakKeys()
	 * @see CacheBuilder#weakValues()
	 * @see CacheBuilder#expireAfterAccess(long, TimeUnit)
	 * @see CacheLoader#load(Object)
	 */
	private LoadingCache<String, String> jdbcQueryMessageSQLCache = CacheBuilder.newBuilder()
			.weakKeys().weakValues().expireAfterAccess(10, TimeUnit.MINUTES)
			.build(new CacheLoader<String, String>() {

				@Override
				public String load(String key) throws Exception {
					return String.format(jdbcQueryMessageSQL, key);
				}
			});

	/**
	 * 表序号与插入SQL的缓存表
	 *
	 * @see LoadingCache
	 * @see CacheBuilder#newBuilder()
	 * @see CacheBuilder#weakKeys()
	 * @see CacheBuilder#weakValues()
	 * @see CacheBuilder#expireAfterAccess(long, TimeUnit)
	 * @see CacheLoader#load(Object)
	 */
	private LoadingCache<String, String> jdbcInsertMessageSQLCache = CacheBuilder.newBuilder()
			.weakKeys().weakValues().expireAfterAccess(10, TimeUnit.MINUTES)
			.build(new CacheLoader<String, String>() {

				@Override
				public String load(String key) throws Exception {
					return String.format(jdbcInsertMessageSQL, key);
				}
			});

	/**
	 * @see org.springframework.data.repository.PagingAndSortingRepository#findAll(org.springframework.data.domain.Sort)
	 */
	@Override
	public Iterable<Message> findAll(Sort sort) {
		return null;
	}

	/**
	 * @see org.springframework.data.repository.PagingAndSortingRepository#findAll(org.springframework.data.domain.Pageable)
	 */
	@Override
	public Page<Message> findAll(Pageable pageable) {
		return null;
	}

	/**
	 * @see org.springframework.data.repository.CrudRepository#save(java.lang.Object)
	 */
	@Override
	public <S extends Message> S save(S entity) {
		final Map<String, Object> paramMap = Maps.newHashMap();
		final String idValue = entity.getId();
		paramMap.put("id", idValue);
		paramMap.put("properties", entity.getPropertiesAsString());
		paramMap.put("systemProperties", entity.getSystemPropertiesAsString());
		paramMap.put("content", entity.getContent());

		final List<String> values = MessageIdGenerator.SPLITTER.splitToList(idValue);
		final String createTime = values.get(0);
		final String clientAddress = values.get(2);
		paramMap.put("createTime", createTime);
		paramMap.put("clientAddress", clientAddress);
		paramMap.put("currentTimeMillis", System.currentTimeMillis());
		final String index = values.get(4);
		final String sql = jdbcInsertMessageSQLCache.getUnchecked(index);
		LOGGER.trace("execute SQL [{}][{}]", sql, paramMap);
		final int rows = storeJdbcTemplate.update(sql, paramMap);
		if (rows > 0) {
			return entity;
		}
		throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(jdbcInsertMessageSQL, 1, rows);
	}

	/**
	 * @see org.springframework.data.repository.CrudRepository#save(java.lang.Iterable)
	 */
	@Override
	public <S extends Message> Iterable<S> save(Iterable<S> entities) {
		return null;
	}

	/**
	 * @see org.springframework.data.repository.CrudRepository#findOne(java.io.Serializable)
	 */
	@Override
	public Message findOne(MessageId id) {
		final Map<String, Object> paramMap = Maps.newHashMap();
		final String idValue = id.getValue();
		paramMap.put("id", idValue);

		final String index = idValue.substring(idValue.lastIndexOf("::") + 2);
		final String sql = jdbcQueryMessageSQLCache.getUnchecked(index);
		LOGGER.trace("execute SQL [{}][{}]", sql, paramMap);
		try {
			return storeJdbcTemplate.queryForObject(sql, paramMap, messageRowMapper);
		} catch (final EmptyResultDataAccessException e) {
			LOGGER.error("", e);
			try {
				paramMap.put("foundTime", System.currentTimeMillis());
				paramMap.put("nextScanTime", System.currentTimeMillis() + 60000);
				paramMap.put("scanRetries", 0);
				LOGGER.trace("execute SQL [{}][{}]", sql, paramMap);
				storeJdbcTemplate.update(jdbcInsertMessageNotFoundSQL, paramMap);
			} catch (final DataAccessException ee) {
				LOGGER.trace("", ee);
			}
			throw e;
		}
	}

	/**
	 * @see org.springframework.data.repository.CrudRepository#exists(java.io.Serializable)
	 */
	@Override
	public boolean exists(MessageId id) {
		return findOne(id) == null;
	}

	/**
	 * @see org.springframework.data.repository.CrudRepository#findAll()
	 */
	@Override
	public Iterable<Message> findAll() {
		return null;
	}

	/**
	 * @see org.springframework.data.repository.CrudRepository#findAll(java.lang.Iterable)
	 */
	@Override
	public Iterable<Message> findAll(Iterable<MessageId> ids) {
		return null;
	}

	/**
	 * @see org.springframework.data.repository.CrudRepository#count()
	 */
	@Override
	public long count() {
		return 0;
	}

	/**
	 * @see org.springframework.data.repository.CrudRepository#delete(java.io.Serializable)
	 */
	@Override
	public void delete(MessageId id) {
	}

	/**
	 * @see org.springframework.data.repository.CrudRepository#delete(java.lang.Object)
	 */
	@Override
	public void delete(Message entity) {
	}

	/**
	 * @see org.springframework.data.repository.CrudRepository#delete(java.lang.Iterable)
	 */
	@Override
	public void delete(Iterable<? extends Message> entities) {
	}

	/**
	 * @see org.springframework.data.repository.CrudRepository#deleteAll()
	 */
	@Override
	public void deleteAll() {
	}

	/**
	 * @see com.sitech.crmpd.idmm2.broker.repository.MessageRepository#saveDelay(com.sitech.crmpd.idmm2.client.api.Message)
	 */
	@Override
	public <S extends Message> S saveDelay(S entity) {
		final Map<String, Object> paramMap = Maps.newHashMap();
		final String idValue = entity.getId();
		paramMap.put("id", idValue);
		paramMap.put("properties", entity.getPropertiesAsString());
		paramMap.put("systemProperties", entity.getSystemPropertiesAsString());
		paramMap.put("content", entity.getContent());

		final List<String> values = MessageIdGenerator.SPLITTER.splitToList(idValue);
		final String createTime = values.get(0);
		final String clientAddress = values.get(2);
		paramMap.put("createTime", createTime);
		paramMap.put("clientAddress", clientAddress);
		paramMap.put("currentTimeMillis", System.currentTimeMillis());
		paramMap.put("effectiveTime", entity.getLongProperty(PropertyOption.EFFECTIVE_TIME));
		LOGGER.trace("execute SQL [{}][{}]", jdbcInsertDelayMessageSQL, paramMap);
		final int rows = storeJdbcTemplate.update(jdbcInsertDelayMessageSQL, paramMap);
		if (rows > 0) {
			return entity;
		}
		throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(jdbcInsertDelayMessageSQL, 1,
				rows);
	}

}
