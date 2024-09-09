package com.cisco.convergence.obs.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "USER_EXPIRY")
public class UserExpiry {

	@Id
	@Column(name = "user_expiry_id", nullable=false)
	@SequenceGenerator(name = "generator", sequenceName = "USER_EXPIRY_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generator")
	private Long id;

	@Column(name = "cco_id")
	private String ccoId;

	@Column(name = "unique_id")
	private String unique_id;
	
	
	@Column(name = "VISITED")
	private String visited;
	
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

	public String getUnique_id() {
		return unique_id;
	}

	public void setUnique_id(String unique_id) {
		this.unique_id = unique_id;
	}

	public String getVisited() {
		return visited;
	}

	public void setVisited(String visited) {
		this.visited = visited;
	}



}
