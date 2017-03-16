package com.sitech.crmpd.idmm2.broker.repository;

import static com.google.common.base.Preconditions.checkArgument;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年6月25日 下午4:53:34
 */
@Configuration
public class BeanFactory implements ApplicationContextAware {

	/**
	 * 初始化SQL，比如建表等
	 */
	@Resource
	private Map<String, String> tableInitSQLs;
	@Autowired(required = false)
	@Qualifier("tableIndexMins")
	private Map<String, Integer> tableIndexMins;
	@Resource
	private Map<String, Integer> tableIndexMaxs;
	@Resource
	private DataSource storeDataSource;

	private ApplicationContext applicationContext;
	private static final String[] TABLE_TYPE = new String[] { "TABLE" };
	private static final String TABLE_NAME = "TABLE_NAME";

	/**
	 * @return {@link NamedParameterJdbcTemplate} 对象实例
	 */
	@Bean
	@DependsOn("storeDataSource")
	public NamedParameterJdbcTemplate storeJdbcTemplate() {
		return new NamedParameterJdbcTemplate(storeDataSource);
	}

	/**
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * @return 当前数据库所有的表名字
	 * @throws SQLException
	 */
	@Bean
	@DependsOn("storeDataSource")
	public List<String> tableNames() throws SQLException {
		Connection connection = null;
		try {
			connection = DataSourceUtils.getConnection(storeDataSource);
			final DatabaseMetaData metaData = connection.getMetaData();
			final ResultSet resultSet = metaData.getTables(null, null, "%", TABLE_TYPE);
			final List<String> tableNames = Lists.newArrayList();
			while (resultSet.next()) {
				tableNames.add(resultSet.getString(TABLE_NAME).toLowerCase());
			}
			return tableNames;
		} finally {
			DataSourceUtils.releaseConnection(connection, storeDataSource);
		}
	}

	/**
	 * @return 自动创建的表数量
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	@DependsOn("tableNames")
	@Bean
	public int createCount() throws SQLException {
		final List<String> tableNames = applicationContext.getBean("tableNames", List.class);
		int createCount = 0;
		for (final Entry<String, String> entry : tableInitSQLs.entrySet()) {
			if (tableIndexMaxs.containsKey(entry.getKey())) {
				final int tableIndexMax = tableIndexMaxs.get(entry.getKey());
				final int tableIndexMin = tableIndexMins == null ? 0 : tableIndexMins
						.containsKey(entry.getKey()) ? tableIndexMins.get(entry.getKey()) : 0;
				for (int i = tableIndexMin; i <= tableIndexMax; i++) {
					final String tableName = String.format(entry.getKey(), i);
					if (!tableNames.contains(tableName.toLowerCase())) {
						createCount += createTable(String.format(entry.getValue(), i));
					}
				}
			} else {
				final String tableName = entry.getKey().toLowerCase();
				if (!tableNames.contains(tableName)) {
					createCount += createTable(entry.getValue());
				}
			}
		}
		return createCount;
	}

	private int createTable(String sql) {
		checkArgument(!Strings.isNullOrEmpty(sql));
		final JdbcTemplate jdbcTemplate = new JdbcTemplate(storeDataSource);
		jdbcTemplate.execute(sql);
		return 1;
	}
}
