package com.cisco.convergence.obs.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DAStatus {
	
	private boolean hasDA= false;
	private String message="";

	public boolean getHasDA() {
		return hasDA;
	}

	public void setHasDA(boolean hasDA) {
		this.hasDA = hasDA;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
