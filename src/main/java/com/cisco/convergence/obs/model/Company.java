package com.cisco.convergence.obs.model;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
public class Company {
	
	String companyName;
	String address;
	String city;
	String country;
	String state;
	String partyId;
	boolean hasDA;
	int level;
	String DAFirstName;
	String  DALastName;
	String DAEmailAddress;
	
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getPartyId() {
		return partyId;
	}
	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}
	public boolean getHasDA() {
		return hasDA;
	}
	public void setHasDA(boolean hasDA) {
		this.hasDA = hasDA;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getDAFirstName() {
		return DAFirstName;
	}
	public void setDAFirstName(String dAFirstName) {
		DAFirstName = dAFirstName;
	}
	public String getDALastName() {
		return DALastName;
	}
	public void setDALastName(String dSLastName) {
		DALastName = dSLastName;
	}
	public String getDAEmailAddress() {
		return DAEmailAddress;
	}
	public void setDAEmailAddress(String dAEmailAddress) {
		DAEmailAddress = dAEmailAddress;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	
	

}
