package com.cisco.convergence.obs.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PartyDA {
	private boolean isDA = false;
	private String firstName;
	private String lastName;
	private String companyName;
	private String emailAddress;
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
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	@Override
	public String toString() {
		return "PartyDA [isDA=" + isDA + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", companyName=" + companyName
				+ "]";
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
		
	}
	public String getEmailAddress(){
		return this.emailAddress;
	}
	
}
