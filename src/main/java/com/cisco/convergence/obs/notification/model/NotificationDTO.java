package com.cisco.convergence.obs.notification.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NotificationDTO extends NsCsoBaseDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2773852282426173211L;
	private String contentType;
	private boolean delayed;
	
	private EnumDeliveryChannelExtImpl deliveryChannel;
	
	private HeaderDTO header;
	
	private TemplateDTO template;
	
	private List<User> toUsers;
	
	private List<User> bccUsers;

	private boolean replaceFromAddressFlag;
	
	private boolean replaceReplyToAddressFlag=false;
	
	private int totalAttachmentsSize=0;
	

	private User sender;
	
	private List<TagMappingInfo> listTagInputs;
	
	private boolean toBeNotifiedSecurely;

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public boolean isDelayed() {
		return delayed;
	}

	public void setDelayed(boolean delayed) {
		this.delayed = delayed;
	}

	public EnumDeliveryChannelExtImpl getDeliveryChannel() {
		return deliveryChannel;
	}

	public void setDeliveryChannel(EnumDeliveryChannelExtImpl deliveryChannel) {
		this.deliveryChannel = deliveryChannel;
	}

	public HeaderDTO getHeader() {
		return header;
	}

	public void setHeader(HeaderDTO header) {
		this.header = header;
	}

	public TemplateDTO getTemplate() {
		return template;
	}

	public void setTemplate(TemplateDTO template) {
		this.template = template;
	}

	public List<User> getToUsers() {
		return toUsers;
	}

	public void setToUsers(List<User> toUsers) {
		this.toUsers = toUsers;
	}

	public boolean isReplaceFromAddressFlag() {
		return replaceFromAddressFlag;
	}

	public void setReplaceFromAddressFlag(boolean replaceFromAddressFlag) {
		this.replaceFromAddressFlag = replaceFromAddressFlag;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public List<TagMappingInfo> getListTagInputs() {
		return listTagInputs;
	}

	public void setListTagInputs(List<TagMappingInfo> listTagInputs) {
		this.listTagInputs = listTagInputs;
	}

	public boolean isToBeNotifiedSecurely() {
		return toBeNotifiedSecurely;
	}

	public void setToBeNotifiedSecurely(boolean toBeNotifiedSecurely) {
		this.toBeNotifiedSecurely = toBeNotifiedSecurely;
	}

	public List<User> getBccUsers() {
		return bccUsers;
	}

	public void setBccUsers(List<User> bccUsers) {
		this.bccUsers = bccUsers;
	}
	@Override
	public String toString() {
		return "NotificationDTO [contentType=" + contentType + ", delayed="
				+ delayed + ", deliveryChannel=" + deliveryChannel
				+ ", header=" + header + ", template=" + template
				+ ", toUsers=" + toUsers + ", replaceFromAddressFlag="
				+ replaceFromAddressFlag + ", sender=" + sender
				+ ", listTagInputs=" + listTagInputs
				+ ", toBeNotifiedSecurely=" + toBeNotifiedSecurely + "]";
	}

	public boolean isReplaceReplyToAddressFlag() {
		return replaceReplyToAddressFlag;
	}

	public void setReplaceReplyToAddressFlag(boolean replaceReplyToAddressFlag) {
		this.replaceReplyToAddressFlag = replaceReplyToAddressFlag;
	}
	public int getTotalAttachmentsSize() {
		return totalAttachmentsSize;
	}

	public void setTotalAttachmentsSize(int totalAttachmentsSize) {
		this.totalAttachmentsSize = totalAttachmentsSize;
	}

	
}
