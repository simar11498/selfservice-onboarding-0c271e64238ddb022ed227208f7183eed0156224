package com.cisco.convergence.obs.util;

import org.springframework.stereotype.Component;

@Component
public class ValidateMethodUtil {

	public String validateStringType(String s) {
	       if(s instanceof String){
	            return s;
	       }
	    return null;
	}
	
	
//	public Cookie[] validateCookies(Cookie[] cookies){
//		 if(cookies instanceof Cookie[]){
//	            return cookies;
//	       }
//	    return null;
//	}
	

}
