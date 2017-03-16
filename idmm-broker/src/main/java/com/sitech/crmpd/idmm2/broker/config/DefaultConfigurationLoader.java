package com.sitech.crmpd.idmm2.broker.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.sitech.crmpd.idmm2.broker.validate.UniqueKey;
import com.sitech.crmpd.idmm2.broker.validate.Validator;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月14日 下午4:58:24
 * @param <T>
 *            只能加载实现 {@link UniqueKey} 接口的数据
 */
@SuppressWarnings("rawtypes")
@Component
public class DefaultConfigurationLoader<T extends UniqueKey> implements ConfigurationLoader {

	@Resource
	private List<SQLEntry<T>> configurationEntries;
	@Resource
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	/**
	 * @see com.sitech.crmpd.idmm2.broker.config.ConfigurationLoader#load(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Configuration load(String version) throws Exception {
		final DefaultConfiguration configuration = new DefaultConfiguration(version);
		for (final SQLEntry<T> entry : configurationEntries) {
			final Map<Object, T> value = entry.load(namedParameterJdbcTemplate, version);
			configuration.set(entry.getEntryType(), value);
		}
		configuration.validate();
		return configuration;
	}

	/**
	 * @author heihuwudi@gmail.com</br> Created By: 2015年4月14日 下午4:55:11
	 */
	@SuppressWarnings("hiding")
	private class DefaultConfiguration<T extends UniqueKey> implements Configuration {

		/**
		 * 控制数据入口只有1个，不涉及并发修改 <br/>
		 *
		 */
		private final Map<Class<? extends UniqueKey>, Map<String, ? extends UniqueKey>> data = Maps
				.newHashMap();
		private final String version;

		public DefaultConfiguration(String version) {
			super();
			this.version = version;
		}

		private void set(Class<T> key, Map<String, T> value) {
			data.put(key, value);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return data.toString();
		}

		/**
		 * @see com.sitech.crmpd.idmm2.broker.config.Configuration#get(java.lang.Class, String)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public <T extends UniqueKey> T get(Class<T> type, String id) {
			final Map<String, T> map = get(type);
			final Object object = map.get(id);
			if (object == null) {
				throw new IllegalArgumentException("No such id[" + id.toString() + "] of type["
						+ type + "] in the config data");
			}
			return (T) object;
		}

		/**
		 * @see com.sitech.crmpd.idmm2.broker.config.Configuration#get(java.lang.Class)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public <T extends UniqueKey> Map<String, T> get(Class<T> type) {
			final Map<String, T> map = (Map<String, T>) data.get(type);
			if (map == null) {
				return Collections.EMPTY_MAP;
			}
			return Collections.unmodifiableMap(map);
		}

		/**
		 * @see com.sitech.crmpd.idmm2.broker.config.Configuration#version()
		 */
		@Override
		public String version() {
			return version;
		}

		public void validate() {
			for (final Entry<Class<? extends UniqueKey>, Map<String, ? extends UniqueKey>> entry : data
					.entrySet()) {
				for (final Entry<String, ? extends UniqueKey> mapEntry : entry.getValue()
						.entrySet()) {
					Validator.instance().isValid(mapEntry.getValue(), data);
				}
			}
		}

	}

}