package com.cisco.convergence.obs.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PartyInfo {
	private String partyId;
	private String companyName;
	private boolean isDA;
	private String firstName;
	private String lastName;
	private String parentPartyId;
	
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getPartyId() {
		return partyId;
	}
	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}
	public boolean isDA() {
		return isDA;
	}
	public void setDA(boolean isDA) {
		this.isDA = isDA;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getParentPartyId() {
		return parentPartyId;
	}
	public void setParentPartyId(String parentPartyId) {
		this.parentPartyId = parentPartyId;
	}
	
}
