package com.sitech.crmpd.idmm2.broker.utils;

import java.util.List;

import com.google.common.base.Splitter;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年9月1日 下午3:56:24
 */
public final class Splitters {

	private static final Splitter SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

	/**
	 * 将默认构造方法私有化，防止实例化后使用
	 */
	private Splitters() {
	}

	/**
	 * @param sequence
	 * @return list of String
	 */
	public static List<String> split(CharSequence sequence) {
		return SPLITTER.splitToList(sequence);
	}
}
