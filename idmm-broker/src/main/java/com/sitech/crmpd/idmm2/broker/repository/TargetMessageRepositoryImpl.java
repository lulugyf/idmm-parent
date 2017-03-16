package com.sitech.crmpd.idmm2.broker.repository;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageId;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年9月11日 下午3:30:28
 */
public class TargetMessageRepositoryImpl implements TargetMessageRepository {

	/** name="{@link com.sitech.crmpd.idmm2.broker.repository.TargetMessageRepositoryImpl}" */
	private static final Logger LOGGER = LoggerFactory.getLogger(TargetMessageRepositoryImpl.class);
	/**
	 * JDBC工具
	 */
	@Resource
	private NamedParameterJdbcTemplate storeJdbcTemplate;
	@Resource
	/**
	 * 插入目标主题消息的SQL
	 */
	private String jdbcInsertTargetMessageSQL;

	/**
	 * 查询结果映射成 {@link Message} 对象的映射
	 */
	@Autowired
	private RowMapper<Message> messageRowMapper;

	/**
	 * @see org.springframework.data.repository.CrudRepository#save(java.lang.Object)
	 */
	@Override
	public <S extends Message> S save(S entity) {
		return null;
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
		return null;
	}

	/**
	 * @see org.springframework.data.repository.CrudRepository#exists(java.io.Serializable)
	 */
	@Override
	public boolean exists(MessageId id) {
		return false;
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

}
