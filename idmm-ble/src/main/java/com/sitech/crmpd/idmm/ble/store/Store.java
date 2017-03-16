package com.sitech.crmpd.idmm.ble.store;

import java.io.IOException;

public interface Store {
	/**
	 * 保存操作到存储中
	 * 文件方式只是保存操作对象到归档日志中
	 * JDBC方式则是直接对数据库进行操作
	 * @param op
	 * @throws IOException
	 */
	public boolean put(JournalOP op);
	
//	/**
//	 * 设定保存目标
//	 * @param dest_topic_id   目标主题
//	 * @param dest_cli_id    消费者ID
//	 * @param extra          一个额外参数，
//	 *                            对于文件模式， 则是保存文件的主目录，队列数据的存放位置在主目录下按主题和消费者建立目录
//	 *                            对于JDBC模式， 则是数据库连接池
//	 */
	//public void setDest(String dest_topic_id, String dest_cli_id, String extra);

	public void setTopic(String topic_id);
	public void setClient(String client_id);
	public String getTopic();
	public String getClient();
	
	/**
	 * 从存储中恢复数据到内存中
	 * @param q
	 * @throws IOException
	 */
	public void restore(LoadCallback q) throws IOException;
	
	/**
	 * 合并数据对于文件模式，
	 *  对于文件模式，是合并归档日志
	 *  对于JDBC模式， 则什么都不做
	 * @throws IOException
	 */
	public void archive() throws IOException;

	public void archive(PrioQueue q) throws IOException;
	
	/**
	 * 从BLE移除队列时， 调用此函数
	 */
	public void removeQueue();

	
	public void close() throws IOException;
}
