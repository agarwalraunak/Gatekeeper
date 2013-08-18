/**
 * 
 */
package com.device.service.rest.representation;

import java.util.Map;

/**
 * @author raunak
 *
 */
public class UserAccessServiceResponse  {
	
	private String encAuthenticator;				//Encrypted with User Session ID
	private Map<String, String> data;			//Encrypted with User Session ID
	/**
	 * @return the encAuthenticator
	 */
	public String getEncAuthenticator() {
		return encAuthenticator;
	}
	/**
	 * @param encAuthenticator the encAuthenticator to set
	 */
	public void setEncAuthenticator(String encAuthenticator) {
		this.encAuthenticator = encAuthenticator;
	}
	/**
	 * @return the data
	 */
	public Map<String, String> getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	public void setData(Map<String, String> data) {
		this.data = data;
	}
}
