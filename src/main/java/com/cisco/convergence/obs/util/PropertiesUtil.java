package com.cisco.convergence.obs.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertiesUtil {

	@Value("${notification.baseUrl}")
	private String baseuri;

	@Value("${notification.templatname}")
	private String templateName;

	@Value("${notification.password}")
	private String password;

	@Value("${notification.certpassword}")
	private String certpassword;

	@Value("${notification.path}")
	private String servicePath;

	@Value("${notification.username}")
	private String username;

	@Value("${app.id}")
	private String appId;

	@Value("${notification.tagging.key}")
	private String taggingKey;

	@Value("${notification.tagging.name}")
	private String taggingName;

	@Value("${notification.from.email}")
	private String fromEmail;

	@Value("${notification.keystore.name}")
	private String keyStoreName;

	@Value("${notification.cert.name}")
	private String certName;

	@Value("${notification.metrics.email.alias}")
	private String metricsEmailAlias;

	@Value("${notification.email.verification}")
	private String verificationLink;

	@Value("${notification.tagging.subject.key}")
	private String tagginsSubjectKey;

	@Value("${email.domains}")
	private String emailDomains;
	
	@Value("${VALID_EF_WRITE_ERROR_CODES}")
	private String efValidErrorCodes;
	
	@Value("${notification.debug.email.alias}")
	private String debugEmailAlais;

	private List<String> emailDomainsList = new ArrayList<String>();
	private List<String> efValidErrorCodesList = new ArrayList<String>();

	@Value("${snctPortalRedirectUrl}")
	private String snctPortalRedirectUrl;

	@Value("${valid.gsp}")
	private String validGsps;

	@Value("${notification.ef.default.email.alias}")
	private String efOtherEmailAlias;

	@Value("${notification.ef.zh.email.alias}")
	private String efZhEmailAlias;

	@Value("${notification.default.locale}")
	private String defaultLocale;

//	@Value("${notification.smartnet.community.url}")
//	private String communityUrl;

	@Value("${epm.admin.url}")
	private String epmAdminUrl;

	@Value("${notification.from.subject}")
	private String templateSubject;

	@Value("${clickSignIn.url}")
	private String clickSignInUrl;
	
	@Value("${clickCSAMLink.url}")
	private String clickCSAMLinkUrl;
	
	@Value("${clickCommunityLink.url}")
	private String clickCommunityLinkUrl;
	
	@Value("${adminUILink.url}")
	private String adminUILink;

	@Value("${user.lookup.url}")
	private String userLookupUrl;
	
	@Value("${notification.cinteam.email.alias}")
	private String cinteamEmailAlias;
	
	
	@Value("${notification.turnoff.external}")
	private String turnOffNotificationsToExternal;
	
	@Value("${ADMIN_UI_URI}")
	private String admin_ui_uri;
	
	@Value("${ADMIN_UI_URI_WITH_PRIORITY}")
	private String admin_ui_uri_with_priority;
	/*
	@Value("${notification.ef.usertopartyassociationapi}")
	private String efUserToPartyAssociationApi;
	
	@Value("${notification.ef.usertoroleassociationapi}")
	private String efUserToRoleAssociationApi;
	
	@Value("${notification.ef.nominatedaapi}")
	private String efnominateDAApi;
	
	@Value("${notification.ef.bsslpassociationapi}")
	private String efBsslpAssociationApi;
    */
	private Properties localeProperties = new Properties();

	private Properties contractProperties = new Properties();
	
	// prod user mock
	private Properties testUserProperties = new Properties();
	
	private Properties aaaUserProperties = new Properties();

	private Properties snProperties = new Properties();
	
	private Logger logger = LogManager.getLogger(PropertiesUtil.class.getClass());
   /*
	public String getUserToPartyAssociationApi() {
		return efUserToPartyAssociationApi;
	}
	
	public String getUserToRoleAssociationApi() {
		return efUserToRoleAssociationApi;
	}
	
	public String getnominateDAApi() {
		return efnominateDAApi;
	}
	
	public String getBsslpAssociationApi() {
		return efBsslpAssociationApi;
	}
	*/
	public String getCinteamEmailAlias() {
		return cinteamEmailAlias;
	}
	
	
	public String getBaseuri() {
		return baseuri;
	}

	public String getTemplateName() {
		return templateName;
	}

	public String getPassword() {
		return password;
	}

	public String getCertpassword() {
		return certpassword;
	}

	public String getServicePath() {
		return servicePath;
	}

	public String getUsername() {
		return username;
	}

	public String getAppId() {
		return appId;
	}

	public String getTaggingKey() {
		return taggingKey;
	}

	public String getTaggingName() {
		return taggingName;
	}

	public String getFromEmail() {
		return fromEmail;
	}

	public Properties getResourceBundle(String locale) {
		loadLoclaleProps(locale);
		return localeProperties;
	}

	public String getKeyStoreName() {
		return keyStoreName;
	}

	public String getCertName() {
		return certName;
	}

	public String getVerificationLink() {
		return verificationLink;
	}

	public String getSnctPortalRedirectUrl() {
		return snctPortalRedirectUrl;
	}

	public String getValidGsps() {
		return validGsps;
	}

	public String getDebugEmailAlais() {
		return debugEmailAlais;
	}

	public List<String> getEmailDomains() {
		if (!StringUtils.isEmpty(emailDomains)) {
			String[] domainArrays = emailDomains.split(",");
			emailDomainsList = Arrays.asList(domainArrays);
		}
		return emailDomainsList;
	}

	// ef valid error codes
	public List<String> getEFValidErrorCodes() {
		if (!StringUtils.isEmpty(efValidErrorCodes)) {
			String[] efValidErrorCodesArrays = efValidErrorCodes.split(",");
			efValidErrorCodesList = Arrays.asList(efValidErrorCodesArrays);
		}
		return efValidErrorCodesList;
	}
	
	public String getEfOtherEmailAlias() {
		return efOtherEmailAlias;
	}

	public String getZhEmailAlias() {
		return efZhEmailAlias;
	}

	public String getUserLookupUrl() {
		return userLookupUrl;
	}
	
	public String getAAACacheUrl() {
		return admin_ui_uri;
	}
	
	public String getAAACacheUrlWithPriority() {
		return admin_ui_uri_with_priority;
	}

	public Properties getContractProperties() {
		InputStream contractsStream = null;
		if(contractProperties != null && contractProperties.isEmpty()){
			 contractsStream = PropertiesUtil.class
					.getClassLoader().getResourceAsStream(
							"contracts.properties");
			try {
				contractProperties.load(contractsStream);
			} catch (IOException e) {
				logger.error("getContractProperties",e);
			}finally{ //SA - FIX
				 try {
					contractsStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return contractProperties;
	}
	
	
	//Prod User mock
	 
	 public Properties getTestUserProperties() {
		if(testUserProperties != null && testUserProperties.isEmpty()){
			InputStream testuserStream = PropertiesUtil.class
					.getClassLoader().getResourceAsStream(
							"testuser.properties");
			try {
				testUserProperties.load(testuserStream);
			} catch (IOException e) {
				logger.error("getTestUserProperties",e);
			}finally{ //SA - FIX
				try {
					testuserStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return testUserProperties;
	}
	 
	 public Properties getAAAUserProperties() {
			if(aaaUserProperties != null && aaaUserProperties.isEmpty()){
				InputStream testuserStream = PropertiesUtil.class
						.getClassLoader().getResourceAsStream(
								"aaaCredentialsAndHeaders.properties");
				try {
					aaaUserProperties.load(testuserStream);
				} catch (IOException e) {
					logger.error("getTestUserProperties",e);
				}finally{ //SA - FIX
					try {
						testuserStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return aaaUserProperties;
		}
	 

	public String getTemplateSubject() {
		return templateSubject;
	}

	public Properties getSnProperties() {
		if(snProperties != null && snProperties.isEmpty()){
			InputStream contractsStream = PropertiesUtil.class
					.getClassLoader().getResourceAsStream(
							"sn.properties");
			try {
				snProperties.load(contractsStream);
			} catch (IOException e) {
				logger.error("getSnProperties",e);
			}finally{ //SA - FIX
				try {
					contractsStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return snProperties;
	}

	public String getClickSignInUrl() {
		return clickSignInUrl;
	}

	public String getClickCSAMLinkUrl() {
		return clickCSAMLinkUrl;
	}

	public String getClickCommunityLinkUrl() {
		return clickCommunityLinkUrl;
	}
	public String getAdminUILink() {
		return adminUILink;
	}

	public String getMetricsEmailAlias() {
		return metricsEmailAlias;
	}

	private void loadLoclaleProps(String locale) {

		if (StringUtils.isEmpty(locale)) {
			locale = defaultLocale;
		}
		InputStream obsStream =null;
		String localePropName = "messages_" + locale + ".properties";
		try {
			 obsStream = PropertiesUtil.class.getClassLoader()
					.getResourceAsStream(localePropName);
			if(obsStream != null){
				if(!localeProperties.isEmpty()){
					localeProperties.clear();
				}
				// If browser locale changes then notification content should honor that.
				localeProperties.load(obsStream);
			}
			if(obsStream!=null)
			localeProperties.load(obsStream);
			else
			loadDefaultProperties();
		} catch (Exception e) {
			logger.error("loadLoclaleProps",e);
			//Loading default locale properties
			loadDefaultProperties();
		}finally{ //SA - Fix
			try {
				if(obsStream!=null)
				obsStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(Exception e){
				e.printStackTrace();
			}
		
		}

	}
	
	public String getTurnOffNotificationsToExternal() {
		return turnOffNotificationsToExternal;
	}

	private void loadDefaultProperties(){
		InputStream obsStream = null;
		try {
			String localePropName = "messages_"+defaultLocale+".properties";
			obsStream = PropertiesUtil.class.getClassLoader().getResourceAsStream(localePropName);
			localeProperties.load(obsStream);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ERROR IN LOADING LOCALE:"+e);
		}finally{ //SA - Fix
			try {
				if(obsStream!=null)
				 obsStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public String getTagginsSubjectKey() {
		return tagginsSubjectKey;
	}

	public String getEpmAdminUrl() {
		return epmAdminUrl;
	}

}
