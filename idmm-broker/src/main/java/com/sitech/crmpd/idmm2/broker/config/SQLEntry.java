package com.sitech.crmpd.idmm2.broker.config;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.common.collect.Maps;
import com.sitech.crmpd.idmm2.broker.validate.UniqueKey;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月13日 上午11:48:57
 * @param <T>
 */
public final class SQLEntry<T extends UniqueKey> {

	private final Class<T> entryType;
	private final String sql;
	private final RowMapper<T> rowMapper;

	/**
	 * @param entryType
	 * @param sql
	 */
	public SQLEntry(Class<T> entryType, String sql) {
		this(entryType, sql, BeanPropertyRowMapper.newInstance(entryType));
	}

	/**
	 * @param entryType
	 * @param sql
	 * @param rowMapper
	 */
	public SQLEntry(Class<T> entryType, String sql, RowMapper<T> rowMapper) {
		super();
		this.entryType = entryType;
		this.sql = sql;
		this.rowMapper = rowMapper;
	}

	/**
	 * 获取{@link #entryType}属性的值
	 *
	 * @return {@link #entryType}属性的值
	 */
	public Class<T> getEntryType() {
		return entryType;
	}

	/**
	 * 获取{@link #sql}属性的值
	 *
	 * @return {@link #sql}属性的值
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * @param template
	 * @param version
	 * @return ID与对象的全量映射表
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Map<Object, T> load(NamedParameterJdbcTemplate template, String version)
			throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		final List<T> list = template.query(String.format(sql, version), rowMapper);
		final Map<Object, T> map = Maps.newLinkedHashMap();
		for (final T t : list) {
			map.put(((UniqueKey) t).getKey(), t);
		}
		return map;
	}

}
