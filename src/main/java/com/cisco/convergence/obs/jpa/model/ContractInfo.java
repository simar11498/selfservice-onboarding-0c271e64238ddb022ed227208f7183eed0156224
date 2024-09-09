package com.cisco.convergence.obs.jpa.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ContractInfo {
	private String contractNumber;
	private List<String> serviceLine;
	private long siteUseId;
	private String crPartyId;
	
	public String getContractNumber() {
		return contractNumber;
	}
	public void setContractNumber(String contractNumber) {
		this.contractNumber = contractNumber;
	}

	public long getSiteUseId() {
		return siteUseId;
	}
	public void setSiteUseId(long siteUseId) {
		this.siteUseId = siteUseId;
	}
	public List<String> getServiceLine() {
		return serviceLine;
	}
	public void setServiceLine(List<String> serviceLine) {
		this.serviceLine = serviceLine;
	}
	public String getCrPartyId() {
		return crPartyId;
	}
	public void setCrPartyId(String crPartyId) {
		this.crPartyId = crPartyId;
	}
	
}
