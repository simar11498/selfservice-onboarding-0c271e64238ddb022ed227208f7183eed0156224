package com.cisco.convergence.obs.jpa.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OnboardStatus {
	
	boolean onboardStatus;
	boolean guidCheck;
	
	public boolean isOnboardStatus() {
		return onboardStatus;
	}
	
	public void setOnboardStatus(boolean onboardStatus) {
		this.onboardStatus = onboardStatus;
	}
	
	public boolean isGuidCheck() {
		return guidCheck;
	}
	
	public void setGuidCheck(boolean guidCheck) {
		this.guidCheck = guidCheck;
	}
	

}
