package com.cisco.convergence.obs.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ContractStatus {
	
	private String validContract="No";
	private String validSN="No";
	private String snExpired="No";
	private String isContractInProfile="No";
	public String getIsContractInProfile() {
		return isContractInProfile;
	}
	public void setIsContractInProfile(String isContractInProfile) {
		this.isContractInProfile = isContractInProfile;
	}
	public String getValidContract() {
		return validContract;
	}
	public void setValidContract(String validContract) {
		this.validContract = validContract;
	}
	public String getValidSN() {
		return validSN;
	}
	public void setValidSN(String validSN) {
		this.validSN = validSN;
	}
	public String getSnExpired() {
		return snExpired;
	}
	public void setSnExpired(String snExpired) {
		this.snExpired = snExpired;
	}
}
