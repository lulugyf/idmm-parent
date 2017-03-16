/**
 *
 */
package com.sitech.crmpd.idmm2.broker.config;

import java.util.Map;

import com.sitech.crmpd.idmm2.broker.validate.UniqueKey;

/**
 * 配置容器
 *
 * @author Administrator
 *
 */
public interface Configuration {

	/**
	 * 获取当前配置的版本
	 *
	 * @return 当前配置的版本
	 */
	public String version();

	/**
	 * @param type
	 *            {@link Class} 类型
	 * @param id
	 *            ID值
	 * @return ID为指定值的 {@link Class} 类型的对象实例
	 */
	public <T extends UniqueKey> T get(Class<T> type, String id);

	/**
	 * @param type
	 *            {@link Class} 类型
	 * @return {@link Class} 类型的所有对象实例
	 */
	public <T extends UniqueKey> Map<String, T> get(Class<T> type);

}
