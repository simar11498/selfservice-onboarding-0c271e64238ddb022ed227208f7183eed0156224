package com.cisco.convergence.obs.notification.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EmailMember {

	private boolean member;

	public boolean isMember() {
		return member;
	}
	public void setMember(boolean member) {
		this.member = member;
	}
	
}
