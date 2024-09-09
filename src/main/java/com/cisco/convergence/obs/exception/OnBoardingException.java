package com.cisco.convergence.obs.exception;

/**
 * Exception to track CDS InventoryUpload Retries
 *
 */
public class OnBoardingException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public OnBoardingException() {
	}

	public OnBoardingException(String message) {
		super(message);
	}
	
	public OnBoardingException(Exception e) {
		super(e);
	}

}