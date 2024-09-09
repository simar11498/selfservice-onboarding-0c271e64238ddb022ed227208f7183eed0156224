package com.cisco.convergence.obs.servlet;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;




@WebServlet(name="VerifyEmailServlet", urlPatterns="/verifyIdentity", loadOnStartup=1)
public class VerifyEmailServlet extends HttpServlet {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 490830850405180984L;
	
//	@Inject
//	private ValidateMethodUtil validateMethodUtil;

	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		System.out.println("***************VERIFY EMAIL SERVLET**********************");
		String uId = validateStringType(req.getParameter("uId"));
		// Security related Fix - SA Report
		//String uId = validateStringType(req.getParameter("uId")); 
		System.out.println("***************uId**********************"+uId);
		if(StringUtils.isEmpty(uId)){
			uId = "invalid";
		}
		String lifecycle = validateStringType(System.getProperty("cisco.life"));
		if(StringUtils.isEmpty(lifecycle)){
			lifecycle ="prod";
		}
		// Security related Fix - SA Report
		//String lifecycle = validateMethodUtil.validateMethod(System.getProperty("cisco.life")); 
		String url = null;
		//https://ssueap-dev.cloudapps.cisco.com/ssueap/
		if(lifecycle.equalsIgnoreCase("prod")){
			url = "https://logcso.cloudapps.cisco.com/logcso/#/verifyEmail?uId="+uId;
		}else{
			url = "https://logcso-"+lifecycle+".cloudapps.cisco.com/logcso/#/verifyEmail?uId="+uId;
		}
		resp.sendRedirect(url);
	}
	private String validateStringType(String s) {
	       if(s instanceof String){
	            return s;
	       }
	    return null;
	}
}
