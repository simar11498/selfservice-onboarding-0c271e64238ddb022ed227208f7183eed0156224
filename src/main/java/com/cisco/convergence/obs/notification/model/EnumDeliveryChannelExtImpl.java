package com.cisco.convergence.obs.notification.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EnumDeliveryChannelExtImpl  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5849885393985474349L;
	private CodeImpl codeImpl;

	public CodeImpl getCodeImpl() {
		return codeImpl;
	}

	public void setCodeImpl(CodeImpl codeImpl) {
		this.codeImpl = codeImpl;
	}
}
