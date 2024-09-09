package com.cisco.convergence.obs.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EPMStatus {
	
	private boolean isEPM= false;
	private String status;
	private String message="";
	private boolean missingUserProfile = false;
	private boolean emailDomainBlackListed = false;
	private boolean contractNotInProfile = false;
	
	public boolean isEPM() {
		return isEPM;
	}
	public void setEPM(boolean isEPM) {
		this.isEPM = isEPM;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public boolean isMissingUserProfile() {
		return missingUserProfile;
	}
	public void setMissingUserProfile(boolean missingUserProfile) {
		this.missingUserProfile = missingUserProfile;
	}
	public boolean isEmailDomainBlackListed() {
		return emailDomainBlackListed;
	}
	public void setEmailDomainBlackListed(boolean emailDomainBlackListed) {
		this.emailDomainBlackListed = emailDomainBlackListed;
	}
	public boolean isContractNotInProfile() {
		return contractNotInProfile;
	}
	public void setContractNotInProfile(boolean contractNotInProfile) {
		this.contractNotInProfile = contractNotInProfile;
	}
	
	
}
