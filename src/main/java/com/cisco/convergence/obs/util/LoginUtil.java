package com.cisco.convergence.obs.util;

import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Component;


@Component
public class LoginUtil {

	@Inject
	private PropertiesUtil propertiesUtil;
	
	@Inject
	private ValidateMethodUtil validateMethodUtil;
	
	private Logger logger= LogManager.getLogger(LoginUtil.class.getClass());


	public String getLoginUser(HttpServletRequest inRequest) {
		String myCcoid = getLoginUserCommon(inRequest);
		/* override CCOID for test user on prod */
		//instrumentation for production testing - Prod User mock
		logger.info("instrumentation for production testing : Before Overriding : "+myCcoid);
		if(!StringUtils.isEmpty(myCcoid) && propertiesUtil.getTestUserProperties().get("test.user").equals(myCcoid)){
			//String customrCcoid = inRequest.getParameter("ccoIdToTest");
			String customrCcoid = validateMethodUtil.validateStringType(inRequest.getParameter("ccoIdToTest"));
			if(!StringUtils.isEmpty(customrCcoid)){
				myCcoid = customrCcoid;
			}
		}

		logger.info("instrumentation for production testing : After Overriding : "+myCcoid);
		return myCcoid;
	}

	public String getLoginOriginalUser(HttpServletRequest inRequest) {
		String myCcoid = getLoginUserCommon(inRequest);
		return myCcoid;
	}

	private String getLoginUserCommon(HttpServletRequest inRequest) {
		if (inRequest == null)
			return null;

		String myCcoid = null;
		if (myCcoid == null) {
			//myCcoid = inRequest.getRemoteUser();
			myCcoid = validateMethodUtil.validateStringType(inRequest.getRemoteUser());
		}
//		String cookieValue = inRequest.getHeader("cookie");
		
		/*
		 * Next, try to get it from Request Header (For Tomcat Factory
		 * environment)
		 */
		String headerName = "REMOTE_USER";
		if (StringUtils.isEmpty(myCcoid)) {
			//myCcoid = inRequest.getHeader(headerName);
			myCcoid = validateMethodUtil.validateStringType(inRequest.getHeader(headerName));
		}

		/* Next, try to set userId into the session else get it from the session */
		HttpSession session = inRequest.getSession();
		if (session != null) {
			if (myCcoid == null || myCcoid.indexOf("null") > -1) {
				myCcoid = (String) session.getAttribute(headerName);
			}
		}
		return myCcoid;
	}
	
	public static void main(String[] args){
		System.out.println("---black listed:");
//		LoginUtil.isUserEmailDomainBlackListed("samtperformancetest17@gmail.com"));
	}
	
	public boolean isUserEmailDomainBlackListed(String email){
		if(StringUtils.isEmpty(email) || (email.indexOf("@") == -1)){
			return true;
		}
		String emailDomain = null;
		try {
			emailDomain = email.substring(email.indexOf("@")+1);
		} catch (Exception e) {
			logger.info("Invalid Email format", e);
		}
		System.out.println("**************emailDomain************"+emailDomain);
		if(!StringUtils.isEmpty(emailDomain) && 
				!propertiesUtil.getEmailDomains().contains(emailDomain)){
			return false;
		}
		System.out.println("**************coming here?************");
		return true;

	}
	
	public static String getLocale(HttpServletRequest httpServletRequest){
		Locale locale = httpServletRequest.getLocale();
		String language = locale.getLanguage();
		System.out.println("**************LANGUAGE************"+language);
		return language;
	}

}
