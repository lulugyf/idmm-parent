package com.sitech.crmpd.idmm.ble;

import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

/**
 * 用于确定BLE连接的Key。<br/>
 * 主题名称，IP地址，端口
 *
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月5日 下午10:36:16
 */
public final class Key {

	private static final Map<String, Key> CACHES = Maps.newConcurrentMap();

	/**
	 * 主题名称
	 */
	private final String topic;
	/**
	 * 服务端地址
	 */
	private final String host;
	/**
	 * 服务端端口
	 */
	private final int port;

	/**
	 * 用主题名称、主机地址、监听端口创建一个唯一的key
	 *
	 * @param topic
	 * @param host
	 * @param port
	 */
	private Key(String topic, String host, int port) {
		this.topic = topic;
		this.host = host;
		this.port = port;
	}

	/**
	 * 获取{@link #topic}属性的值
	 *
	 * @return {@link #topic}属性的值
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * 获取{@link #host}属性的值
	 *
	 * @return {@link #host}属性的值
	 */
	public String getHost() {
		return host;
	}

	/**
	 * 获取{@link #port}属性的值
	 *
	 * @return {@link #port}属性的值
	 */
	public int getPort() {
		return port;
	}

	/**
	 * 如果指定数据缓存的Key存在，返回缓存数据，否则新创建一个Key
	 *
	 * @param topic
	 *            主题名称
	 * @param host
	 *            主机地址
	 * @param port
	 *            监听端口
	 * @return 如果指定数据缓存的Key存在，返回缓存数据，否则返回新创建一个Key
	 */
	public static Key create(String topic, String host, int port) {
		final String unique = topic + "@" + host + ":" + port;
		Key key = CACHES.get(unique);
		if (key == null) {
			key = new Key(topic, host, port);
			CACHES.put(unique, key);
		}
		return key;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Key)) {
			return false;
		}
		final Key key = (Key) obj;
		return Objects.equal(topic, key.topic) && Objects.equal(host, key.host) && port == key.port;
	}
}
