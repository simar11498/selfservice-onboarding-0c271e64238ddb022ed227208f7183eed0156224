package com.cisco.convergence.obs.notification.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CodeImpl implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5461556790070083663L;
	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
