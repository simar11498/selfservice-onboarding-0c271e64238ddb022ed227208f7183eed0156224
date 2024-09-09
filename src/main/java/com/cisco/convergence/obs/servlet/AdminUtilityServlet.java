package com.cisco.convergence.obs.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.cisco.convergence.obs.util.ValidateMethodUtil;


@WebServlet(name="AdminUtilityServlet", urlPatterns="/adminUI", loadOnStartup=1)
public class AdminUtilityServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 490830850405180984L;
	
	@Inject
	private ValidateMethodUtil validateMethodUtil;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	System.out.println("***************Admin Utility Servlet SERVLET**********************");
		
		String lifecycle = validateMethodUtil.validateStringType(System.getProperty("cisco.life"));
		if(StringUtils.isEmpty(lifecycle)){
			lifecycle ="prod";
		}
		// Security related Fix - SA Report
		//String lifecycle = validateStringType(System.getProperty("cisco.life"));
		String url = null;
		//https://ssueap-dev.cloudapps.cisco.com/ssueap/
		if(lifecycle.equalsIgnoreCase("prod")){
			url = "https://logcso.cloudapps.cisco.com/logcso/#/adminUI";
		}else{
			url = "https://logcso-"+lifecycle+".cloudapps.cisco.com/logcso/#/adminUI";
		}
		resp.sendRedirect(url);
	}
}
