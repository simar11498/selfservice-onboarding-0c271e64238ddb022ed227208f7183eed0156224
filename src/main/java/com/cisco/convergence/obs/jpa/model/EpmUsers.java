package com.cisco.convergence.obs.jpa.model;

import java.sql.Clob;

//@Entity
//@Table(name = "EPM_USERS")
public class EpmUsers {
//
//	@Id
//	@SequenceGenerator(name = "generator", sequenceName = "EPM_USERS_SEQ", allocationSize = 1)
//	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generator")
	private Long id;

//	@Column(name = "cco_id")
	private String ccoId;

//	@Column(name = "PROFILE")
	private Clob profile;

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

	public Clob getProfile() {
		return profile;
	}

	public void setProfile(Clob profile) {
		this.profile = profile;
	}
	
}
