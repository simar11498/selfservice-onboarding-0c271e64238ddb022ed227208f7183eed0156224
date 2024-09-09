package com.cisco.convergence.obs.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DANominationStatus {
	
	private boolean nominated= false;
	private String message="";
	private String errorCode;
	private String errorMessage;

	public boolean isNominated() {
		return nominated;
	}

	public void setNominated(boolean nominated) {
		this.nominated = nominated;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		return "DANominationStatus [nominated=" + nominated + ", message="
				+ message + ", errorCode=" + errorCode + ", errorMessage="
				+ errorMessage + "]";
	}

}
