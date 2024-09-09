package com.cisco.convergence.obs.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "USER_DA_OPTION")
public class UserDACompany {

	@Id
	@Column(name = "user_da_option_id", nullable=false)
	@SequenceGenerator(name = "generator2", sequenceName = "USER_DA_COMP_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generator2")
	private Long id;

	@Column(name = "ccoid", nullable=false, unique=true)
	private String ccoId;

	@Column(name = "partyid", nullable=false)
	private String partyId;
	
	@Column(name = "GUID_CHECK")
	private String guidCheck = "false";

	public String getCcoId() {
		return ccoId;
	}

	public void setCcoId(String ccoId) {
		this.ccoId = ccoId;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getGuidCheck() {
		return guidCheck;
	}

	public void setGuidCheck(String guidCheck) {
		this.guidCheck = guidCheck;
	}
	
	
}
