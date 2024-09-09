package com.cisco.convergence.obs.jpa.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "USER_ACTIONS")
public class UserActions {

	@Id
	@SequenceGenerator(name = "generatouc", sequenceName = "USER_ACTIONS_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generatouc")
	@Column(name = "user_action_id")
	private Long id;

	@Column(name = "cco_id")
	private String ccoId;
	
	@Column(name = "user_role")
	private String userRole;
	
	@Column(name = "event_description")
	private String eventDescription;
	
	@Column(name = "contract_number")
	private String contractNumber;
	
	@Column(name = "cr_party_id")
	private String partyId;
	
	@Column(name = "action_id")
	private long actionId;
	
	@Column(name = "create_time")
	private Timestamp createTime;
	
	@Column(name = "serial_number")
	private String serialNumber;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCcoId() {
		return ccoId;
	}

	public void setCcoId(String ccoId) {
		this.ccoId = ccoId;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public String getEventDescription() {
		return eventDescription;
	}

	public void setEventDescription(String eventDescription) {
		this.eventDescription = eventDescription;
	}

	public String getContractNumber() {
		return contractNumber;
	}

	public void setContractNumber(String contractNumber) {
		this.contractNumber = contractNumber;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public long getActionId() {
		return actionId;
	}

	public void setActionId(long actionId) {
		this.actionId = actionId;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	
	
	
}
