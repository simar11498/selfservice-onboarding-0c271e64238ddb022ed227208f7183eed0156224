package com.cisco.convergence.obs.notification.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TemplateDTO extends NsCsoBaseDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8060593213240359402L;
	private String templateName;
	
	private String templateSubject;

	public String getTemplateSubject() {
		return templateSubject;
	}

	public void setTemplateSubject(String templateSubject) {
		this.templateSubject = templateSubject;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
}
