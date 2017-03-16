package com.sitech.crmpd.idmm2.broker.config;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.sitech.crmpd.idmm2.broker.utils.CuratorFrameworkHolder;
import com.sitech.crmpd.idmm2.client.api.ResultCode;
import com.sitech.crmpd.idmm2.client.exception.OperationException;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月26日 下午2:51:46
 */
@Component
@Lazy(false)
public final class ConfigurationProvider {

	/**
	 * name="{@link com.sitech.crmpd.idmm2.broker.config.ConfigurationProvider}"
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationProvider.class);
	private volatile Configuration configuration;
	private volatile byte[] lastVersion;
	@Autowired
	private CuratorFrameworkHolder client;
	@Autowired
	private ConfigurationLoader loader;
	@Autowired
	private ConfigConstant constant;

	/**
	 * @param dataSource
	 * @return {@link NamedParameterJdbcTemplate} 对象实例
	 */
	@Bean
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
		return new NamedParameterJdbcTemplate(dataSource);
	}

	/**
	 * 获取{@link #configuration}属性的值
	 *
	 * @return {@link #configuration}属性的值
	 * @throws OperationException
	 */
	@PostConstruct
	public Configuration getConfiguration() throws OperationException {
		Throwable cause = null;
		// 双检锁，减少锁冲突
		if (configuration == null) {
			synchronized (this) {
				try {
					final byte[] version = client.getData(constant.getConfigVersionPath());
					configuration = loader.load(new String(version));
					lastVersion = version;
				} catch (final Exception e) {
					LOGGER.error("", e);
					cause = e;
				}
			}
		} else {
			try {
				final byte[] version = client.getData(constant.getConfigVersionPath());
				if (!Arrays.equals(version, lastVersion)) { // 前后版本不一致，需要重新加载数据
					synchronized (this) {
						if (!Arrays.equals(version, lastVersion)) {
							configuration = loader.load(new String(version));
							lastVersion = version;
						}
					}
				}
			} catch (final Exception e) {
				LOGGER.error("", e);
				cause = e;
			}
		}

		if (configuration == null) {
			throw new OperationException(ResultCode.INTERNAL_SERVER_ERROR, cause);
		}
		return configuration;
	}

}
