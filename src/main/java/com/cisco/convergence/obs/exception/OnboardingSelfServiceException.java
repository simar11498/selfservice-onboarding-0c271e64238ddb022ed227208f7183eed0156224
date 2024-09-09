package com.cisco.convergence.obs.exception;

public class OnboardingSelfServiceException extends RuntimeException {
	
	String errorCode;
	String errorMessage;

	public OnboardingSelfServiceException() {
		super();
		
	}

	public OnboardingSelfServiceException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public OnboardingSelfServiceException(String message) {
		super(message);
		
	}

	public OnboardingSelfServiceException(Throwable cause) {
		super(cause);
		
	}

	public OnboardingSelfServiceException(String errorCode2, String errorMessage2) {
		super(errorMessage2);
		this.errorCode = errorCode2;
	}

	@Override
	public String getMessage() {		
		return super.getMessage();
	}
	
	public String getErrorCode() {
	        return errorCode;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
