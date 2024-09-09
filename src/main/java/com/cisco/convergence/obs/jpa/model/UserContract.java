package com.cisco.convergence.obs.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "USER_CONTRACT")
public class UserContract {

	@Id
	@Column(name = "user_contract_id", nullable=false)
	@SequenceGenerator(name = "generator1", sequenceName = "USER_CONTRACT_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generator1")
	private Long id;

	@Column(name = "cco_id")
	private String ccoId;

	@Column(name = "contract")
	private String contract;
	
	

	public String getCcoId() {
		return ccoId;
	}

	public void setCcoId(String ccoId) {
		this.ccoId = ccoId;
	}

	public String getContract() {
		return contract;
	}

	public void setContract(String contract) {
		this.contract = contract;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
}
