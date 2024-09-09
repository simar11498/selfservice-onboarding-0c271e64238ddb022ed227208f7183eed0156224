package com.cisco.convergence.obs.notification.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TagMappingInfo  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9027804780870564221L;
	private String key;
	private String value;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
