package com.cisco.convergence.obs.notification.model;

import java.io.Serializable;

/**
 * NsCsoBaseDTO class is the Base DTO for the Notification CSO DTOs. This
 * contains the data member header of type HeaderDTO to hold the custom header
 * parameters
 * 
 * @author sachinku
 * 
 */
public abstract class NsCsoBaseDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2237992466234131638L;

	private HeaderDTO header;

	/**
	 * @return the header
	 */
	public HeaderDTO getHeader() {
		return header;
	}

	/**
	 * @param header
	 *            the header to set
	 */
	public void setHeader(HeaderDTO header) {
		this.header = header;
	}

}
