package com.cisco.convergence.obs.rest.metricsCollection;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.cisco.convergence.obs.model.AccessLevel;

public class OnBoardingMetricCollectionEvent {

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
	
	private String companyName;
	
	private String userId;
	
	private String email;
	
	private Long id;
	
	private String ccoId;
	
	private Date eventTimestamp;
	
	private String crpartyId;
	
	private String contractNumber;
	
	private String serialNumber;
	
	private OnBoardingEventNotificationType eventDescription;
	
	private AccessLevel accessLevel;
	
	private String context;
	
	public OnBoardingMetricCollectionEvent(String cco, Date date, String cpnyNm, String ctrctNum, String srlNum, OnBoardingEventNotificationType evntDes, AccessLevel acsLvl, String partyId) {
		ccoId = cco;
		eventTimestamp = date;
		companyName = cpnyNm;
		contractNumber = ctrctNum;
		serialNumber = srlNum;
		eventDescription = evntDes;
		accessLevel = acsLvl;
		crpartyId = partyId;
	}
	
	public OnBoardingMetricCollectionEvent(String cco, Date date, String cpnyNm, String ctrctNum, String srlNum, OnBoardingEventNotificationType evntDes, AccessLevel acsLvl, String partyId, String ctxt) {
		this(cco, date, cpnyNm, ctrctNum, srlNum, evntDes, acsLvl, partyId);
		this.context = ctxt;
	}
	
	public OnBoardingMetricCollectionEvent(String cco, Date date, String cpnyNm, String ctrctNum, String srlNum, OnBoardingEventNotificationType evntDes, AccessLevel acsLvl, String partyId, String userID, String emailAddress) {
		ccoId = cco;
		eventTimestamp = date;
		companyName = cpnyNm;
		contractNumber = ctrctNum;
		serialNumber = srlNum;
		eventDescription = evntDes;
		accessLevel = acsLvl;
		crpartyId = partyId;
		userId=userID;
		email=emailAddress;
		
	}
	
	
	public String getContext() {
		return context;
	}
	
	public void setContext(String ctxt) {
		this.context = ctxt;
	}
	
	public String getCcoId() {
		return ccoId;
	}

	public void setCcoId(String ccoId) {
		this.ccoId = ccoId;
	}

	public String getEventTimestamp() {
		String formattedDate = dateFormat.format(eventTimestamp);
		return formattedDate;
	}
	
	public Date getEventTimestampAsDate() {
		return this.eventTimestamp;
	}

	public void setEventTimestamp(Date eventTimestamp) {
		this.eventTimestamp = eventTimestamp;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getContractNumber() {
		return contractNumber;
	}

	public void setContractNumber(String contractNumber) {
		this.contractNumber = contractNumber;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	
	public OnBoardingEventNotificationType getEventDescription() {
		return eventDescription;
	}

	public void setEventDescription(OnBoardingEventNotificationType eventDescription) {
		this.eventDescription = eventDescription;
	}

	public AccessLevel getAccessLevel() {
		return accessLevel;
	}

	public void setAccessLevel(AccessLevel accessLevel) {
		this.accessLevel = accessLevel;
	}

	public String getCrpartyId() {
		return crpartyId;
	}

	public void setCrpartyId(String crpartyId) {
		this.crpartyId = crpartyId;
	}

	/*
	 * Create a comma separated string which can be loaded into Excel
	 * 
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getCcoId()).append(", "); // 1
		buffer.append(getEventTimestamp()).append(", "); // 2
		buffer.append(getEventDescription().name()).append(", "); // 3
		buffer.append(getCompanyName()).append(", ");  // 4
		buffer.append(getContractNumber()).append(", "); // 5
		buffer.append(getSerialNumber()).append(", "); // 6
		if(accessLevel != null) {
			buffer.append(getAccessLevel().name());
		}
		else {
			buffer.append(AccessLevel.UNKNOWN.name());
		}
		if (context != null) {
			buffer.append(", ").append(context);
		}
		buffer.append("\n<br/><br/>");
		return buffer.toString();
	}

}
