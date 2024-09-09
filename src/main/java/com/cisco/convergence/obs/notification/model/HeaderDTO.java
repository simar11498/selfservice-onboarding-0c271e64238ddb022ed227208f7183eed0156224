package com.cisco.convergence.obs.notification.model;

import java.io.Serializable;

public class HeaderDTO  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4274701611308047986L;
//	private String userId;

	private String appId;

	private Boolean isFlexGuiRequest;

	
//	/**
//	 * @return the userId
//	 */
//	public String getUserId() {
//		return userId;
//	}
//
//	/**
//	 * @param userId
//	 *            the userId to set
//	 */
//	public void setUserId(String userId) {
//		this.userId = userId;
//	}

	/**
	 * @return the appId
	 */
	public String getAppId() {
		return appId;
	}

	/**
	 * @param appId
	 *            the appId to set
	 */
	public void setAppId(String appId) {
		this.appId = appId;
	}

	/**
	 * @return the isFlexGuiRequest
	 */
	public Boolean getIsFlexGuiRequest() {
		return isFlexGuiRequest;
	}

	/**
	 * @param isFlexGuiRequest
	 *            the isFlexGuiRequest to set
	 */
	public void setIsFlexGuiRequest(Boolean isFlexGuiRequest) {
		this.isFlexGuiRequest = isFlexGuiRequest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((appId == null) ? 0 : appId.hashCode());
		result = prime
				* result
				+ ((isFlexGuiRequest == null) ? 0 : isFlexGuiRequest.hashCode());
		result = prime * result ;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof HeaderDTO)) {
			return false;
		}
		HeaderDTO other = (HeaderDTO) obj;
		if (appId == null) {
			if (other.appId != null) {
				return false;
			}
		} else if (!appId.equals(other.appId)) {
			return false;
		}
		if (isFlexGuiRequest == null) {
			if (other.isFlexGuiRequest != null) {
				return false;
			}
		} else if (!isFlexGuiRequest.equals(other.isFlexGuiRequest)) {
			return false;
		}
//		if (userId == null) {
//			if (other.userId != null) {
//				return false;
//			}
//		} else if (!userId.equals(other.userId)) {
//			return false;
//		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NsCsoBaseDto [appId=").append(appId).append(
				", isFlexGuiRequest=").append(isFlexGuiRequest).append("]");
		return builder.toString();
	}

	
}
