package com.cisco.convergence.obs.notification.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class User extends NsCsoBaseDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6636510680175395257L;
	private boolean isAuthenticated;
	private boolean isAuthorized;
	private String userName;
	private String ccoId;
	//private String userRole;
	private boolean isCSOAdmin;
	
	private boolean isLoggedinUserCSOAdmin;



	private String emailAddress;

	private Long id;


	private String subGroupNames;

	private String resourceURL;

	public String getResourceURL() {
		return resourceURL;
	}

	public void setResourceURL(String resourceURL) {
		this.resourceURL = resourceURL;
	}

	public boolean isAuthenticated() {
		return isAuthenticated;
	}

	public void setAuthenticated(boolean isAuthenticated) {
		this.isAuthenticated = isAuthenticated;
	}

	public boolean isAuthorized() {
		return isAuthorized;
	}

	public void setAuthorized(boolean isAuthorized) {
		this.isAuthorized = isAuthorized;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCcoId() {
		return ccoId;
	}

	public void setCcoId(String ccoId) {
		this.ccoId = ccoId;
	}



	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}


	/**
	 * Constructs a <code>String</code> with all attributes in name = value
	 * format.
	 * 
	 * @return a <code>String</code> representation of this object.
	 */
	public String toString() {
		final String TAB = "    ";

		String retValue = "";

		retValue = "User ( " + super.toString() + TAB + "isAuthenticated = "
				+ this.isAuthenticated + TAB + "isAuthorized = "
				+ this.isAuthorized + TAB + "userName = " + this.userName + TAB
				+ "ccoId = " + this.ccoId + TAB + "application = "
				+ TAB + " )";

		return retValue;
	}


	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the subGroupList
	 */
	public String getSubGroupNames() {
		return subGroupNames;
	}

	/**
	 * @param subGroupList
	 *            the subGroupList to set
	 */
	public void setSubGroupNames(String subGroupNames) {
		this.subGroupNames = subGroupNames;
	}

	/**
	 * @return the isCSOAdmin
	 */
	public boolean isCSOAdmin() {
		return isCSOAdmin;
	}

	/**
	 * @param isCSOAdmin the isCSOAdmin to set
	 */
	public void setCSOAdmin(boolean isCSOAdmin) {
		this.isCSOAdmin = isCSOAdmin;
	}

	/**
	 * @return the isLoggedinUserCSOAdmin
	 */
	public boolean isLoggedinUserCSOAdmin() {
		return isLoggedinUserCSOAdmin;
	}



	/**
	 * @param isLoggedinUserCSOAdmin the isLoggedinUserCSOAdmin to set
	 */
	public void setLoggedinUserCSOAdmin(boolean isLoggedinUserCSOAdmin) {
		this.isLoggedinUserCSOAdmin = isLoggedinUserCSOAdmin;
	}
	
	
}
