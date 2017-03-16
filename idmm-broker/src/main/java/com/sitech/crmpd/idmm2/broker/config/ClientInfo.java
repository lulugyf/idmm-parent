/**
 *
 */
package com.sitech.crmpd.idmm2.broker.config;

import com.sitech.crmpd.idmm2.broker.validate.UniqueKey;


/**
 * @author Administrator
 *
 */
public final class ClientInfo  implements UniqueKey{

	public ClientInfo(){}
	private String id;
	private String belong;
	private String description;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the belong
	 */
	public String getBelong() {
		return belong;
	}

	/**
	 * @param belong the belong to set
	 */
	public void setBelong(String belong) {
		this.belong = belong;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return id;
	}

}
