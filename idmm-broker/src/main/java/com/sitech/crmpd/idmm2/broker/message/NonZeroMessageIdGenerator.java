package com.sitech.crmpd.idmm2.broker.message;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Value;

import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageId;
import com.sitech.crmpd.idmm2.client.api.MessageIdGenerator;

/**
 * 吉林要求消息实体表下标从1开始
 *
 * @author heihuwudi@gmail.com</br> Created By: 2015年3月30日 下午9:38:52
 */
public class NonZeroMessageIdGenerator extends DefaultMessageIdGenerator {
 
	/**
	 * @see com.sitech.crmpd.idmm2.client.api.MessageIdGenerator#generate(java.net.InetSocketAddress,
	 *      com.sitech.crmpd.idmm2.client.api.Message, long)
	 */
	@Override
	public MessageId generate(InetSocketAddress address, Message message, long sequence)
			throws IOException {
		final StringBuilder builder = new StringBuilder(64);
		return new DefaultMessageId(builder.append(System.currentTimeMillis()).append(SEP)
				.append(sequence).append(SEP).append(address.getHostString()).append(":")
				.append(address.getPort()).append(SEP).append(sequence % bleTableIndexMax) // 倒数第二段为ble索引表的分表数字
				.append(SEP).append(sequence % tableIndexMax + 1).toString());
	}
 
}
