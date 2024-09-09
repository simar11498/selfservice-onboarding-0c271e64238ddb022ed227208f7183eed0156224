package com.cisco.cstg.ssue.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author: Infosys
 * Modified for security bug
 * Changed the usage of loading property file
 * Last Updated Date: 15-Sept-2012 
 */
public class Environment {

	private static Logger log = LogManager.getLogger(Environment.class.getName());

	private static Properties ssueapProps = null;

	/*
	 * Using static initializer as avoid synchronization issue.
	 * This will do early initialization on class load instead of lazy loading but this should not 
	 * be an issue as this property file will be used early in the application life cycle. 
	 */
	 
	static {
		try {
			ssueapProps = initializeProperties();
		} catch (IOException e) {
			log.error("IOException in initializing ssueap properties file",e);
		}
	}
	public static String getProperty(String propertyKey) throws IOException {
		if(ssueapProps == null){
			throw new IllegalStateException("ssueap properties is not initialized");
		}
		return ssueapProps.getProperty(propertyKey);
	}
	/*
	 * Using synchronized keyword to avoid multiple threads loading the prop file again. 
	 */
	private static synchronized Properties initializeProperties() throws IOException {
		InputStream propInputStream = null;
		Properties properties = new Properties();

		try {
			propInputStream = Environment.class.getResourceAsStream("/ssueap.properties");
			properties.load(propInputStream);
		} finally {
			if(propInputStream != null){
				propInputStream.close();
			}
		}
		return properties;
	}

	public static String getEnvironment() {
		try {
			return getProperty("ssue.env");
		} catch (Exception exception) {
			log.error("exception",exception);
			return "production";
		}
	}

	/** getLifeCycleLink - Parses a URL/link value and replaces the $env_XX placeholder with the environment value
	 *  @author santnair
	 *  @param targetEnv - the env placeholder to be replaced $env_gen2
	 *  @param linkValue - the link value to be massaged
	 *  @return a String with the env specific link value
	 */

	/*
	 * Define a List of all supported env placeholders
	 */
	static List <String> allEnvPlaceHolders = new ArrayList<String>(Arrays.asList("#env_gen2", "#env_ccix"));
	/*
	 * Define a Map to hold exception values for env
	 */
	static Map <String, String> envExceptions = new HashMap <String, String>();
	/*
	 * Define exceptional replacement values for env
	 */
	static {
		envExceptions.put("#env_gen2.stage","stg");
	}

	public static String getLifeCycleLink (String linkValue) {

		//amvijaya/syerrawa -- if linkValue is null, return ""
		if (null == linkValue) return "";

		String env = validateMethod(System.getProperty("cisco.life"));
		
		env = (env == null) ? "prod" : env;
		String envLinkValue = linkValue;

		for (String curPlaceHolder : allEnvPlaceHolders) {
			String exKey = curPlaceHolder + "." + env;
			String envValForLink = envExceptions.containsKey(exKey) ? envExceptions.get(exKey) : env;
			//case insensitive replace the env value in the link value
			envLinkValue = envLinkValue.replaceAll("(?i)" + curPlaceHolder, envValForLink);
		}

		//special case for prod env - remove prod from the URL so that xxx-prod.cisco.com becomes xxx.cisco.com
		if (env.equals("prod")) envLinkValue = envLinkValue.replaceAll("(?i)-prod", "");
		return envLinkValue;
	}

	public static boolean enableAAATabAuthorization() {
		try {
			return Boolean.parseBoolean(getProperty("aaa.enableTabAuthorization"));
		} catch (Exception exception) {
			log.error("exception",exception);
			return false;
		}
	}

	/*
	 *  adding changes for AAA Integration
	 */
	public static String getAAAIdUserRoleUrl() {
		try {
			return getProperty("aaa.userRole");
		} catch (Exception exception) {
			log.error("exception",exception);
			return null;
		} 
	}
	/*
	 *  adding changes for AAA Integration	 
	 */
	public static String getAAAIdUserRoleChangeUrl() {
		try {
			return getProperty("aaa.roleChange");
		} catch (Exception exception) {
			log.error("exception",exception);
			return null;
		} 
	}

	public static boolean enableAAALinkAuthorization() {
		try {
			return Boolean.parseBoolean(getProperty("aaa.enableLinkAuthorization"));
		} catch (Exception exception) {
			log.error("exception",exception);
			return false;
		}
	}

	/*
	 * To Get the AAA Name of application selected from the application dropdown
	 */
	public static String getSelectedAppName(String appValue) {
		try {
			return getProperty(appValue);
		} catch (Exception exception) {
			log.error("exception",exception);
			return null;
		}
	}

	/*
	 * new method created for handling locale from worldwide drop down selected locale as well as browser locale
	 */
	public static void handleLocale(HttpServletRequest arg0, HttpServletResponse arg1)
	{
		System.out.println("Inside Environment: handleLocale >>");
		Locale ssueLocale = null;
		/*
		 *  reading in locale from request
		 */
		ssueLocale = arg0.getLocale();
		arg0.getSession().setAttribute("org.apache.struts.action.LOCALE", ssueLocale);
	}

	private static String validateMethod(String s) {
	       if(s instanceof String){
	            return s;
	       }
	    return null;
	}
}
