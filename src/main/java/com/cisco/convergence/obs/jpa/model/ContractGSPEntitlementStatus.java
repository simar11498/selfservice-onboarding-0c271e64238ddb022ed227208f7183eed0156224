package com.cisco.convergence.obs.jpa.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ContractGSPEntitlementStatus {
	
	private String gspEntitlementStatus="NO";
	private String contractStatus;
	

	public String getContractStatus() {
		return contractStatus;
	}

	public void setContractStatus(String contractStatus) {
		this.contractStatus = contractStatus;
	}

	public String getGspEntitlementStatus() {
		return gspEntitlementStatus;
	}

	public void setGspEntitlementStatus(String gspEntitlementStatus) {
		this.gspEntitlementStatus = gspEntitlementStatus;
	}
	
}
