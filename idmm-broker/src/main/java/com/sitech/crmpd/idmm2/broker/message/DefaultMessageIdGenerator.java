package com.sitech.crmpd.idmm2.broker.message;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Value;

import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageId;
import com.sitech.crmpd.idmm2.client.api.MessageIdGenerator;

/**
 * 和老官暂定用时间+序号+ip+表序号记录
 *
 * @author heihuwudi@gmail.com</br> Created By: 2015年3月30日 下午9:38:52
 */
public class DefaultMessageIdGenerator implements MessageIdGenerator {

	@Value("${store.tableIndexMax}")
	int tableIndexMax;
	@Value("${store.bleTableIndexMax}")
	int bleTableIndexMax; // ble索引分表数， 用于为ble指定索引保存的分表数字

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
				.append(SEP).append(sequence % tableIndexMax).toString());
	}

	/**
	 * @see com.sitech.crmpd.idmm2.client.api.MessageIdGenerator#generate(java.lang.String)
	 */
	@Override
	public MessageId generate(String text) throws IOException {
		return new DefaultMessageId(text);
	}

}
