/**
 * 
 */
package com.cisco.cstg.ssue.util;


import javax.servlet.http.HttpServletRequest;

import org.owasp.esapi.ESAPI;



/**
 * @author syerrawa
 *
 */
public class SsueapSecurityUtil {
	
	private static String validateStringType(String s) {
	       if(s instanceof String){
	            return s;
	       }
	    return null;
	}
	
	/*
	 * Sanitize request parameter for HTML
	 */
	public String sanitizeParameterHTML ( HttpServletRequest r, String s, String d ) {
		//String val = r.getParameter(s);
		// Security related Fix - SA Report
		String val = validateStringType(r.getParameter(s));
		//sanitation engineering at its finest, provide default handling assignment
		return val != null ? ESAPI.encoder().encodeForHTML( val) : d ;
	}
	
	/*
	 * Sanitize request parameter for JS
	 */
	public String sanitizeParameterJS ( HttpServletRequest r, String s, String d ) {
		//String val = r.getParameter( s);
		// Security related Fix - SA Report
		String val = validateStringType(r.getParameter(s));
		// sanitation engineering at its finest, provide default handling assignment
		return val != null ? ESAPI.encoder().encodeForJavaScript( val) : d ;
	}

}