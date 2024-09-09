/**
 * 
 */
package com.cisco.cstg.ssue.util;


/**
 * Class to indicate exception condition during external HTTP calls
 */
// IMS Change 26-Jun-2012: New class added
public class HttpUtilException extends Exception {

	private static final long serialVersionUID = 3023082971304135766L;

	public HttpUtilException() {
		super();
	}

	public HttpUtilException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpUtilException(String message) {
		super(message);
	}

	public HttpUtilException(Throwable cause) {
		super(cause);
	}


}
