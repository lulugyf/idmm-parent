package com.sitech.crmpd.idmm2.broker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月26日 下午3:18:50
 */
@Component
public class ConfigConstant {

	@Value("${configConstant.keySep:@}")
	private String keySep;
	@Value("${configConstant.value.ignore:_ignore}")
	private String valueIgnore;
	@Value("${configConstant.value.all:_all}")
	private String valueAll;
	@Value("${configConstant.value.default:_default}")
	private String valueDefault;
	@Value("${configConstant.zookeeper.bleIdPath:/idmm2/ble/id.}")
	private String bleIdPath;
	@Value("${configConstant.zookeeper.brokerPath:/idmm2/broker}")
	private String brokerPath;
	@Value("${configConstant.zookeeper.configVersionPath:/idmm2/configServer/version}")
	private String configVersionPath;

	/**
	 * 获取{@link #keySep}属性的值
	 *
	 * @return {@link #keySep}属性的值
	 */
	public String getKeySep() {
		return keySep;
	}

	/**
	 * 设置{@link #keySep}属性的值
	 *
	 * @param keySep
	 *            属性值
	 */
	public void setKeySep(String keySep) {
		this.keySep = keySep;
	}

	/**
	 * 获取{@link #valueIgnore}属性的值
	 *
	 * @return {@link #valueIgnore}属性的值
	 */
	public String getValueIgnore() {
		return valueIgnore;
	}

	/**
	 * 设置{@link #valueIgnore}属性的值
	 *
	 * @param valueIgnore
	 *            属性值
	 */
	public void setValueIgnore(String valueIgnore) {
		this.valueIgnore = valueIgnore;
	}

	/**
	 * 获取{@link #valueAll}属性的值
	 *
	 * @return {@link #valueAll}属性的值
	 */
	public String getValueAll() {
		return valueAll;
	}

	/**
	 * 设置{@link #valueAll}属性的值
	 *
	 * @param valueAll
	 *            属性值
	 */
	public void setValueAll(String valueAll) {
		this.valueAll = valueAll;
	}

	/**
	 * 获取{@link #valueDefault}属性的值
	 *
	 * @return {@link #valueDefault}属性的值
	 */
	public String getValueDefault() {
		return valueDefault;
	}

	/**
	 * 设置{@link #valueDefault}属性的值
	 *
	 * @param valueDefault
	 *            属性值
	 */
	public void setValueDefault(String valueDefault) {
		this.valueDefault = valueDefault;
	}

	/**
	 * 获取{@link #bleIdPath}属性的值
	 *
	 * @return {@link #bleIdPath}属性的值
	 */
	public String getBleIdPath() {
		return bleIdPath;
	}

	/**
	 * 设置{@link #bleIdPath}属性的值
	 *
	 * @param bleIdPath
	 *            属性值
	 */
	public void setBleIdPath(String bleIdPath) {
		this.bleIdPath = bleIdPath;
	}

	/**
	 * 获取{@link #brokerPath}属性的值
	 *
	 * @return {@link #brokerPath}属性的值
	 */
	public String getBrokerPath() {
		return brokerPath;
	}

	/**
	 * 设置{@link #brokerPath}属性的值
	 *
	 * @param brokerPath
	 *            属性值
	 */
	public void setBrokerPath(String brokerPath) {
		this.brokerPath = brokerPath;
	}

	/**
	 * 获取{@link #configVersionPath}属性的值
	 *
	 * @return {@link #configVersionPath}属性的值
	 */
	public String getConfigVersionPath() {
		return configVersionPath;
	}

	/**
	 * 设置{@link #configVersionPath}属性的值
	 *
	 * @param configVersionPath
	 *            属性值
	 */
	public void setConfigVersionPath(String configVersionPath) {
		this.configVersionPath = configVersionPath;
	}
}
