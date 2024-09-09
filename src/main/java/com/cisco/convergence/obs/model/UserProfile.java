package com.cisco.convergence.obs.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

@XmlRootElement
public class UserProfile {
	private boolean sntcAccess;
	private String accessLevel;
	private String firstName;
	private String lastName;
	private String email;
	private String partyId;
	private String companyName;
	private String phoneNumber;
	private boolean blackListedEmailDomain;
	private boolean hasPartyAssociated=true;
	private boolean userHasDA = false;
	private String country;
	private boolean isEpmUser = false;
	private boolean hasCustomerAdminRole= false;
	private boolean daRedirectToPortal = false;
	private PartyDA partyDA;
	private boolean isTestUser = false;
	private String ccoId;
	private String DPLAddressFlag;
	private String accountStatus;
	private String efValidatedPartyId;
	private boolean efValidatedAccountStatus;
	private String efValidatedCompanyName;
	
	private Company company;
	
	
	
	public Company getCompany() {
		return company;
	}
	public void setCompany(Company company) {
		this.company = company;
	}
	public String getEfValidatedCompanyName() {
		return efValidatedCompanyName;
	}
	public void setEfValidatedCompanyName(String efValidatedCompanyName) {
		this.efValidatedCompanyName = efValidatedCompanyName;
	}
	public boolean getEfValidatedAccountStatus() {
		return efValidatedAccountStatus;
	}
	public void setEfValidatedAccountStatus(boolean efValidatedAccountStatus) {
		this.efValidatedAccountStatus = efValidatedAccountStatus;
	}
	public String getEfValidatedPartyId() {
		return efValidatedPartyId;
	}
	public void setEfValidatedPartyId(String efValidatedPartyId) {
		this.efValidatedPartyId = efValidatedPartyId;
	}
	public String geteFValidatedCompanyName() {
		return efValidatedCompanyName;
	}
	public void seteFValidatedCompanyName(String eFValidatedCompanyName) {
		this.efValidatedCompanyName = eFValidatedCompanyName;
	}
	public String getAccountStatus() {
		return accountStatus;
	}
	public void setAccountStatus(String accountStatus) {
		this.accountStatus = accountStatus;
	}
	
	
	public String getDPLAddressFlag() {
		return DPLAddressFlag;
	}
	public void setDPLAddressFlag(String dPLAddressFlag) {
		DPLAddressFlag = dPLAddressFlag;
	}
	public String getCcoId() {
		return ccoId;
	}
	public void setCcoId(String ccoId) {
		this.ccoId = ccoId;
	}
	public boolean isTestUser() {
		return isTestUser;
	}
	public void setTestUser(boolean isTestUser) {
		this.isTestUser = isTestUser;
	}
	public PartyDA getPartyDA() {
		return partyDA;
	}
	public void setPartyDA(PartyDA partyDA) {
		this.partyDA = partyDA;
	}
	public boolean isDaRedirectToPortal() {
		return daRedirectToPortal;
	}
	public void setDaRedirectToPortal(boolean daRedirectToPortal) {
		this.daRedirectToPortal = daRedirectToPortal;
	}
	public boolean isEpmUser() {
		return isEpmUser;
	}
	public void setEpmUser(boolean isEpmUser) {
		this.isEpmUser = isEpmUser;
	}
	private String snctPortalRedirectUrl;
	
	private List<String> contractNumbers;
	
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
	public List<String> getContractNumbers() {
		return contractNumbers;
	}
	public void setContractNumbers(List<String> contractNumbers) {
		this.contractNumbers = contractNumbers;
	}
	public boolean isHasPartyAssociated() {
		return hasPartyAssociated;
	}
	public void setHasPartyAssociated(boolean hasPartyAssociated) {
		this.hasPartyAssociated = hasPartyAssociated;
	}
	public String getSnctPortalRedirectUrl() {
		return snctPortalRedirectUrl;
	}
	public void setSnctPortalRedirectUrl(String snctPortalRedirectUrl) {
		this.snctPortalRedirectUrl = snctPortalRedirectUrl;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getPartyId() {
		if(StringUtils.isEmpty(partyId)){
			return "UNKNOWN";
		}
		return partyId;
	}
	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}
	public boolean isUserHasDA() {
		return userHasDA;
	}
	public void setUserHasDA(boolean userHasDA) {
		this.userHasDA = userHasDA;
	}
	public boolean isHasCustomerAdminRole() {
		return hasCustomerAdminRole;
	}
	public void setHasCustomerAdminRole(boolean hasCustomerAdminRole) {
		this.hasCustomerAdminRole = hasCustomerAdminRole;
	}
	@Override
	public String toString() {
		return "UserProfile [sntcAccess=" + sntcAccess + ", accessLevel="
				+ accessLevel + ", firstName=" + firstName + ", lastName="
				+ lastName + ", email=" + email + ", partyId=" + partyId
				+ ", companyName=" + companyName + ", phoneNumber="
				+ phoneNumber + ", blackListedEmailDomain="
				+ blackListedEmailDomain + ", userHasDA=" + userHasDA
				+ ", country=" + country + ", hasCustomerAdminRole="
				+ hasCustomerAdminRole + ", partyDA=" + partyDA + "]";
	}
	
}
