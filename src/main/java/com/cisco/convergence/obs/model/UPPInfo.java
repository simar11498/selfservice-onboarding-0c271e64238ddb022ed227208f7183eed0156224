package com.cisco.convergence.obs.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UPPInfo {
	private boolean sntcAccess;
	private String accessLevel;
	private String firstName;
	private String lastName;
	private String email;
	private String partyId;
	private String companyName;
	private String phoneNumber;
	private boolean blackListedEmailDomain;
	private boolean userHasDA = false;
	private String country;
	private String userRole;
	private String ccoId;
	private List<PartyInfo> partyInfoList;
	private String message;
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean isSntcAccess() {
		return sntcAccess;
	}
	public void setSntcAccess(boolean sntcAccess) {
		this.sntcAccess = sntcAccess;
	}
	public String getAccessLevel() {
		return accessLevel;
	}
	public void setAccessLevel(String accessLevel) {
		this.accessLevel = accessLevel;
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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPartyId() {
		return partyId;
	}
	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public boolean isBlackListedEmailDomain() {
		return blackListedEmailDomain;
	}
	public void setBlackListedEmailDomain(boolean blackListedEmailDomain) {
		this.blackListedEmailDomain = blackListedEmailDomain;
	}
	public boolean isUserHasDA() {
		return userHasDA;
	}
	public void setUserHasDA(boolean userHasDA) {
		this.userHasDA = userHasDA;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getUserRole() {
		return userRole;
	}
	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}
	public List<PartyInfo> getPartyInfoList() {
		return partyInfoList;
	}
	public void setPartyInfoList(List<PartyInfo> partyInfoList) {
		this.partyInfoList = partyInfoList;
	}
	public String getCcoId() {
		return ccoId;
	}
	public void setCcoId(String ccoId) {
		this.ccoId = ccoId;
	}
	
}
