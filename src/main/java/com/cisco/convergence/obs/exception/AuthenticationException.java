package com.cisco.convergence.obs.exception;

/**
 * Exception to track CDS InventoryUpload Retries
 *
 */
public class AuthenticationException extends OnBoardingException{

	private static final long serialVersionUID = 1L;
	
	public AuthenticationException() {
	}

	public AuthenticationException(String message) {
		super(message);
	}

}