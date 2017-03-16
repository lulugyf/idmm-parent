package com.sitech.crmpd.idmm2.broker.utils;

import io.netty.util.AttributeKey;

/**
 * 公共的上下文属性名
 *
 * @author heihuwudi@gmail.com</br> Created By: 2015年3月30日 下午11:01:12
 */
public final class AttributeKeys {

	/**
	 * 用于存储客户端唯一标识
	 */
	public static final AttributeKey<String> CLIENT_ID_KEY = AttributeKey.valueOf("client.id");

	/**
	 * 将默认构造方法私有化，防止实例化后使用
	 */
	private AttributeKeys() {
	}
}
