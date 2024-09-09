package com.cisco.convergence.obs.exception;

/**
 * Exception to track CDS InventoryUpload Retries
 *
 */
public class LoginException extends OnBoardingException{

	private static final long serialVersionUID = 1L;
	
	public LoginException() {
	}

	public LoginException(String message) {
		super(message);
	}

}