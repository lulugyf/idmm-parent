/**
 *
 */
package com.sitech.crmpd.idmm2.broker.config;

/**
 * 配置管理接口 <br/>
 * 统一配置的出口和人口
 *
 * @author Administrator
 *
 */
public interface ConfigurationLoader {

	/**
	 * 从配置服务器加载指定版本号的配置
	 * 
	 * @param version
	 *            版本号
	 *
	 * @return {@link Configuration} 对象实例
	 * @throws Exception
	 */
	Configuration load(String version) throws Exception;

}
