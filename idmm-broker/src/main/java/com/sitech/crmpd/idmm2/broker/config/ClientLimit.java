/**
 *
 */
package com.sitech.crmpd.idmm2.broker.config;


import com.sitech.crmpd.idmm2.broker.validate.Ref;
import com.sitech.crmpd.idmm2.broker.validate.UniqueKey;

/**
 * @author Administrator
 *
 */
public final class ClientLimit  implements UniqueKey {
	
	@Ref(ClientInfo.class)
	private String clientId;
	private String limitKey;
	private String limitValue;

	
	/**
	 * @see com.sitech.crmpd.idmm2.broker.config.ClientLimit#getKey()
	 */
	@Override
	public String getKey() {
		return getClientId() + "@" + limitKey;
	}
	
	
	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the limitKey
	 */
	public String getLimitKey() {
		return limitKey;
	}

	/**
	 * @param limitKey the limitKey to set
	 */
	public void setLimitKey(String limitKey) {
		this.limitKey = limitKey;
	}

	/**
	 * @return the limitValue
	 */
	public String getLimitValue() {
		return limitValue;
	}

	/**
	 * @param limitValue the limitValue to set
	 */
	public void setLimitValue(String limitValue) {
		this.limitValue = limitValue;
	}

	

}
