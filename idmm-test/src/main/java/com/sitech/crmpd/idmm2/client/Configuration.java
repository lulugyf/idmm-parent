package com.sitech.crmpd.idmm2.client;

import java.util.List;

import com.beust.jcommander.Parameter;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年11月23日 下午1:31:31
 */
public class Configuration {

	@Parameter(names = "-topic", required = false, description = "原始主题")
	private String topic;
	@Parameter(names = "-targetTopic", required = false, description = "目标主题")
	private String targetTopic;
	@Parameter(names = "-zookeeper", required = true, description = "zookeeper")
	private String zookeeper;
	@Parameter(names = "-clientID", required = true, description = "客户端ID")
	private String clientId;
	@Parameter(names = "-file", required = true, description = "文件路径")
	private String file;
	@Parameter(names = "-compress", description = "是否压缩")
	private String compress;
	@Parameter(names = "-count", required = false, description = "发送次数")
	private Long count = 1L;
	@Parameter(names = "-sleep", required = false, description = "两次发送之间休眠时间")
	private Long sleep = 1000L;
	@Parameter(required = false)
	private List<String> args;

	/**
	 * 获取{@link #topic}属性的值
	 *
	 * @return {@link #topic}属性的值
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * 设置{@link #topic}属性的值
	 *
	 * @param topic
	 *            属性值
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

	/**
	 * 获取{@link #targetTopic}属性的值
	 *
	 * @return {@link #targetTopic}属性的值
	 */
	public String getTargetTopic() {
		return targetTopic;
	}

	/**
	 * 设置{@link #targetTopic}属性的值
	 *
	 * @param targetTopic
	 *            属性值
	 */
	public void setTargetTopic(String targetTopic) {
		this.targetTopic = targetTopic;
	}

	/**
	 * 获取{@link #zookeeper}属性的值
	 *
	 * @return {@link #zookeeper}属性的值
	 */
	public String getZookeeper() {
		return zookeeper;
	}

	/**
	 * 设置{@link #zookeeper}属性的值
	 *
	 * @param zookeeper
	 *            属性值
	 */
	public void setZookeeper(String zookeeper) {
		this.zookeeper = zookeeper;
	}

	/**
	 * 获取{@link #clientId}属性的值
	 *
	 * @return {@link #clientId}属性的值
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * 设置{@link #clientId}属性的值
	 *
	 * @param clientId
	 *            属性值
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * 获取{@link #file}属性的值
	 *
	 * @return {@link #file}属性的值
	 */
	public String getFile() {
		return file;
	}

	/**
	 * 设置{@link #file}属性的值
	 *
	 * @param file
	 *            属性值
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * 获取{@link #compress}属性的值
	 *
	 * @return {@link #compress}属性的值
	 */
	public String getCompress() {
		return compress;
	}

	/**
	 * 设置{@link #compress}属性的值
	 *
	 * @param compress
	 *            属性值
	 */
	public void setCompress(String compress) {
		this.compress = compress;
	}

	/**
	 * 获取{@link #count}属性的值
	 *
	 * @return {@link #count}属性的值
	 */
	public Long getCount() {
		return count;
	}

	/**
	 * 设置{@link #count}属性的值
	 *
	 * @param count
	 *            属性值
	 */
	public void setCount(Long count) {
		this.count = count;
	}

	/**
	 * 获取{@link #sleep}属性的值
	 *
	 * @return {@link #sleep}属性的值
	 */
	public Long getSleep() {
		return sleep;
	}

	/**
	 * 设置{@link #sleep}属性的值
	 *
	 * @param sleep
	 *            属性值
	 */
	public void setSleep(Long sleep) {
		this.sleep = sleep;
	}

}
