package com.cisco.convergence.obs.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.cisco.ata.rest.PartyHierarchyView;
import com.cisco.ata.rest.PartyProfileView;
import com.cisco.ata.rest.UserProfileView;
import com.cisco.ca.csp.ef.update.client.EFUpdateClient;
import com.cisco.ca.csp.ef.update.exception.EFUpdateException;
import com.cisco.ca.csp.ef.update.model.StatusResult;
import com.cisco.convergence.obs.exception.LoginException;
import com.cisco.convergence.obs.exception.OnboardingSelfServiceException;
import com.cisco.convergence.obs.jpa.dao.OBSDao;
import com.cisco.convergence.obs.jpa.model.UserDACompany;
import com.cisco.convergence.obs.model.Company;
import com.cisco.convergence.obs.model.PartyDA;
import com.cisco.convergence.obs.rest.metricsCollection.OnBoardingEventNotificationType;
import com.cisco.convergence.obs.util.AAAClientUtil;
import com.cisco.convergence.obs.util.LoginUtil;
import com.cisco.convergence.obs.util.MetricsClientUtil;
import com.cisco.convergence.obs.util.NotificationUtil;
import com.cisco.convergence.obs.util.PropertiesUtil;

@Component
public class DAOnboardingService {
	private Logger logger = LogManager.getLogger(this.getClass());

	private static final String BSSLP_NAME = "SNTC";
	private static final String ROLE_NAME = "CustomerAdmin";
	private static final String INVALID_PARTY_TO_ONBOARD = "Invalid Party to Onboard";
	private static final String ERROR_TECH_ONBOARD = "Technical Error while onboarding";
	private static final String CREATED_BY = "SNTC Self Onboarding";
	private static final String CONTRACT_NUMBER = "Contract #:";
	private static final String NAME_OF_USER="Name of User:";
	private static final String COMPANY_NAME="Company Name:";
	private static final String EMAIL="Email:";
	private static final String HTML_BREAK_2="<br/><br/>";


	@Autowired
	private EFUpdateClient efUpdateClient;

	@Autowired
	OBSDao onboardDAO;

	@Autowired
	private AAAClientUtil aaaClientUtil;

	@Inject
	private MetricsClientUtil metricsClientUtil; 

	@Autowired
	private PropertiesUtil propertiesUtil;

	@Autowired
	NotificationUtil notificationUtil;

	@Autowired
	private LoginUtil loginUtil;

	@Value("${VALID_EF_WRITE_ERROR_CODES}")
	private String validEFWriteErrorCodes;

	@Value("${notification.debug.email.alias}")
	private String debugEmailAlais;

	//Email to CIN team containing list of users not acted upon email.
	public void sendEmailIgnoredVerificationLink(HttpServletRequest httpServletrequest,String fromDate,String toDate, List<String> list){
		String originalLoginUser = loginUtil.getLoginOriginalUser(httpServletrequest);

		
		try{
			String fromUserString = "Dear Cisco Agent,";

			Properties resourceBundle = propertiesUtil.getResourceBundle("en");
			String toemail= propertiesUtil.getDebugEmailAlais();
			//String subject = resourceBundle.getProperty("notification.to.debug.user.subject");
			System.out.println("List :"+list.toString());
			/*List<String> list1=new ArrayList<String>(); 
			list1.add("mmiragi");
			list1.add("selfonboardtestuser");
			list1.add("samtperformancetest17");
			list1.add("samtperformancetest5");
			list1.add("samtperformancetest5"); */
			String subject = resourceBundle.getProperty("notification.cin.emailnotverified.subject");
					
			String info=resourceBundle.getProperty("notification.cin.emailnotverified.description");
			StringBuilder message = new StringBuilder();
			message.append(info).append("<br/>");
			String appAnalyticsLink=resourceBundle.getProperty("notification.cin.emailnotverified.appAnalyticLink");
					
		/*	for(int i=0;i<list1.size();i++){
				message.append(appAnalyticsLink+list1.get(i)+"'>").append(list1.get(i)).append("</a>").append("<br/>");
			} */
			if(list!=null && list.size()>0)
				for(int i=0;i<list.size();i++){
					message.append(appAnalyticsLink+list.get(i)+"'>").append(list.get(i)).append("</a>").append("<br/>");
				}
			else
				message.append("No Users in this Date Range").append("<br/>");
				
			
			message.append("<br/><br/>").append(resourceBundle.getProperty("notification.to.debug.user.end1")).append("<br/>");
			message.append(resourceBundle.getProperty("notification.to.debug.user.end2")).append("<br/><br/>");
			notificationUtil.sendCustomMessage(originalLoginUser,toemail, message.toString(), subject,fromUserString);	
			//metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Welcome_Email_Sent_Successfully, "", "",new Date(), null, null);
		}catch (Exception e) {
			//metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_Sending_Welcome_Email, "", "",new Date(), null, null);	
			logger.error("-----sendNotification to DEBUG user-------", e);			
		}
	}	
	// email to debug user
	public void sendEmailToDebugAlais(HttpServletRequest httpServletrequest, String errorMsg){
		String originalLoginUser = loginUtil.getLoginOriginalUser(httpServletrequest);

		UserProfileView UserProfileView = null;
		try {
			UserProfileView = aaaClientUtil.getUserProfile(originalLoginUser);
		} catch (LoginException e) {			
			logger.error("sendEmailToDebugUser", e);			
		}

		String firstName = "NONE";
		String lastName = "NONE";
		String userEmail  = "NONE";

		try{
			if(UserProfileView != null && UserProfileView.getRegistrationInfo() != null){
				firstName = (UserProfileView.getRegistrationInfo().getFirstName());
				lastName = (UserProfileView.getRegistrationInfo().getLastName());
				userEmail  = (UserProfileView.getRegistrationInfo().getEmailAddress());
			}


			String fromUserString = "Dear Support Engineer,";

			Properties resourceBundle = propertiesUtil.getResourceBundle("en");
			String toemail= propertiesUtil.getDebugEmailAlais();
			String subject = resourceBundle.getProperty("notification.to.debug.user.subject");

			StringBuilder message = new StringBuilder();
			message.append(resourceBundle.getProperty("notification.to.debug.user.line1")).append("<br/><br/>");
			message.append("User First Name: ").append(firstName).append("<br/>");
			message.append("User Last Name: ").append(lastName).append("<br/>");
			message.append("User Email: ").append(userEmail).append("<br/>");
			message.append("Error Description: ").append(errorMsg).append("<br/><br/>");
			message.append(resourceBundle.getProperty("notification.to.debug.user.end1")).append("<br/>");
			message.append(resourceBundle.getProperty("notification.to.debug.user.end2")).append("<br/><br/>");

			notificationUtil.sendCustomMessage(originalLoginUser,toemail, message.toString(), subject,fromUserString);	
			//metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Welcome_Email_Sent_Successfully, "", "",new Date(), null, null);
		}catch (Exception e) {
			//metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_Sending_Welcome_Email, "", "",new Date(), null, null);	
			logger.error("-----sendNotification to DEBUG user-------", e);			
		}
	}

	// welcome email to user
	public void sendWelcomeEmailToUser(HttpServletRequest httpServletrequest){
		String originalLoginUser = loginUtil.getLoginOriginalUser(httpServletrequest);

		UserProfileView UserProfileView = null;
		try {
			UserProfileView = aaaClientUtil.getUserProfile(originalLoginUser);
		} catch (LoginException e) {			
			logger.error("sendWelcomeEmailToUser", e);			
		}
		if(UserProfileView == null || UserProfileView.getRegistrationInfo() == null){
			logger.error("user profile object is null in send welcome email to user...");
			return;
		}
		try{
			String firstName = (UserProfileView.getRegistrationInfo().getFirstName());
			String lastName = (UserProfileView.getRegistrationInfo().getLastName());
			String toemail  = (UserProfileView.getRegistrationInfo().getEmailAddress());

			String fromUserString = "Dear "+firstName+" "+lastName;

			Properties resourceBundle = propertiesUtil.getResourceBundle("en");
			String subject = resourceBundle.getProperty("notification.welcome.email.subject");

			StringBuilder message = new StringBuilder();
			message.append(resourceBundle.getProperty("notification.welcome.email.line1")).append("<br/><br/>");
			message.append(resourceBundle.getProperty("notification.welcome.email.line2")).append("<br/><br/>");
			message.append(resourceBundle.getProperty("notification.welcome.email.line3")).append("<br/><br/>");
			message.append(resourceBundle.getProperty("notification.welcome.email.line4")).append("<br/><br/>");
			message.append(resourceBundle.getProperty("notification.welcome.email.end1")).append("<br/>");
			message.append(resourceBundle.getProperty("notification.welcome.email.end2")).append("<br/><br/>");

			notificationUtil.sendCustomMessage(originalLoginUser,toemail, message.toString(), subject,fromUserString);	
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Welcome_Email_Sent_Successfully, "", "",new Date(), null, null);
		}catch (Exception e) {
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_Sending_Welcome_Email, "", "",new Date(), null, null);	
			logger.error("-----sendNotification Welcome Email-------", e);			
		}
	}


	/**
	 * // send email to CIN with custom message
	 * @param ccoId
	 * @param contractNumber
	 * @param condition
	 * @param crPartyId
	 */
	public void sendEmailToCINTeamWithCustomMessage( HttpServletRequest httpServletrequest ,String ccoId, String contractNumber, String condition, String siteId, String crPartyId){
		String originalLoginUser = loginUtil.getLoginOriginalUser(httpServletrequest);
		UserProfileView UserProfileView = null;
		try {
			UserProfileView = aaaClientUtil.getUserProfile(ccoId);
		} catch (LoginException e) {

			logger.error("sendEmail", e);

		}	

		// send mail to cin team
		try {
			String fromUserString = "Dear Cisco Agent";

			Properties resourceBundle = propertiesUtil.getResourceBundle("en");
			String subject = resourceBundle.getProperty("notification.msg.generic.subject");
			String toemail = propertiesUtil.getCinteamEmailAlias();

			String firstName = (UserProfileView.getRegistrationInfo().getFirstName());
			String lastName = (UserProfileView.getRegistrationInfo().getLastName());
			String email  = (UserProfileView.getRegistrationInfo().getEmailAddress());
			String companyName = getCompanyName(UserProfileView);		
			
			StringBuilder message = new StringBuilder();
			message.append("CCO ID:").append(ccoId).append("<br/>");


			StringBuilder desc = new StringBuilder();
			if(condition != null){				
				switch(condition){
				
				case "eitherSiteIdOrCrPartyIdWrong" :
					message.append(CONTRACT_NUMBER).append(contractNumber).append("<br/>");
					message.append(NAME_OF_USER).append(firstName).append(" ").append(lastName).append("<br/>");
					message.append(COMPANY_NAME).append(companyName).append("<br/>");
					message.append(EMAIL).append(email).append(HTML_BREAK_2);	
					desc.append("Data issues with either user party id or SiteId").append("<br/>");
					desc.append("Site Id:").append(siteId).append("<br/>");
					desc.append("CR Party Id:").append(crPartyId).append("<br/>");
					desc.append("Due to data issues, user email was not verified. Please verify email address before on-boarding the user.").append("<br/>");
					break;

				case "CRPartyHierarchyEmpty" :
					message.append(CONTRACT_NUMBER).append(contractNumber).append("<br/>");
					message.append(NAME_OF_USER).append(firstName).append(" ").append(lastName).append("<br/>");
					message.append(COMPANY_NAME).append(companyName).append("<br/>");
					message.append(EMAIL).append(email).append(HTML_BREAK_2);	
					desc.append("Data issues with either user party id or SiteId").append("<br/>");
					desc.append("Site Id:").append(siteId).append("<br/>");
					desc.append("CR Party Id:").append(crPartyId).append("<br/>");
					desc.append("CR Party hierarchy is empty").append("<br/>");
					desc.append("Due to data issues, user has not been on-boarded. Request your attention in this regard.").append("<br/>");
					break;

				case "OnboardingError" :
					message.append(CONTRACT_NUMBER).append(contractNumber).append("<br/>");
					message.append(NAME_OF_USER).append(firstName).append(" ").append(lastName).append("<br/>");
					message.append(COMPANY_NAME).append(companyName).append("<br/>");
					message.append(EMAIL).append(email).append(HTML_BREAK_2);	
					desc.append("Error occured while onboarding").append("<br/>");
					//in this case siteid hold the error description
					desc.append(siteId).append("<br/>");
					break;	
					//in this case siteId contains the reason
				case "guidCheckFail" :
					message.append(CONTRACT_NUMBER).append(contractNumber).append("<br/>");
					message.append(NAME_OF_USER).append(firstName).append(" ").append(lastName).append("<br/>");
					message.append(COMPANY_NAME).append(companyName).append("<br/>");
					message.append(EMAIL).append(email).append(HTML_BREAK_2);
					//this field contains the reason for failure
					//desc.append("GUID Mismatch - ").append(siteId).append("<br/>");					
					desc.append("User selected CR PartyId :").append(crPartyId).append("<br/><br/>");
					desc.append("GUID Mismatch").append("<br/>");
					break;

				case "companyNotSelected" :
					message.append(CONTRACT_NUMBER).append(contractNumber).append("<br/>");
					message.append(NAME_OF_USER).append(firstName).append(" ").append(lastName).append("<br/>");
					message.append(COMPANY_NAME).append(companyName).append("<br/>");
					message.append(EMAIL).append(email).append(HTML_BREAK_2);	
					desc.append("User selected \'None of the above\' option.").append("<br/>");
					break;

				default :
					desc.append("");
					break;

				}
				message.append("Description:").append(desc);				
			}		
			message.append(HTML_BREAK_2).append("Sincerely,");
			message.append("<br/>").append("Cisco Smart Net Total Care");

			logger.debug("mail to cin team : ccoId : "+ccoId); 
			logger.debug("mail to cin team : toemail : "+toemail); 
			logger.debug("mail to cin team : subject : "+subject);
			logger.debug("mail to cin team : fromUserString : "+fromUserString);
			notificationUtil.sendCustomMessage(ccoId, toemail, message.toString(), subject,fromUserString);

			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Request_Forwarded_For_Manual_Onboarding, crPartyId, "",new Date(), null, null);

		}catch (Exception e) {
			logger.error("-----sendNotification-------"+e.getMessage());
			e.printStackTrace();

			try{
				metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_Sending_Email_For_Manual_Onboarding, "", "", new Date(),null, null);
			} catch (Exception le) {
				logger.error(le.getMessage());
				throw new OnboardingSelfServiceException("Error occurred while capturing metrics - SSO_Error_In_Sending_Email_For_Manual_Onboarding - "+ crPartyId);
			}

		}


	}
	
	
	public void sendEmailToPSSupportTeam( HttpServletRequest httpServletrequest ,String ccoId, String contractNumber, String condition, String inputXML,String outputXML,String crPartyId){
		String originalLoginUser = loginUtil.getLoginOriginalUser(httpServletrequest);
		UserProfileView UserProfileView = null;
		try {
			UserProfileView = aaaClientUtil.getUserProfile(ccoId);
		} catch (LoginException e) {

			logger.error("sendEmail", e);

		}	

		// send mail to cin team
		try {
			logger.info("CCOID "+ccoId+"Condition "+condition);
            logger.info("Input XML :"+inputXML);
            logger.info("Output XML :"+outputXML);
            Properties resourceBundle = propertiesUtil.getResourceBundle("en");
            
            String fromUserString=null;
            String subject=null;
            String toemail=null;
            String companyName=null;
            
            if(condition.equals("crPartyIdNotFound"))
            {           	
            
            	 fromUserString = resourceBundle.getProperty("notification.cprteam.email.greetings");			
				 toemail = resourceBundle.getProperty("notification.cprteam.email.alias");
				 subject = resourceBundle.getProperty("notification.cprteam.email.subject");
				 
            }
            else   
            {
            	 fromUserString = resourceBundle.getProperty("notification.psteam.email.greetings");			
				 subject = resourceBundle.getProperty("notification.psteam.email.subject");			
				 toemail = resourceBundle.getProperty("notification.psteam.email.alias");
				 companyName = getCompanyName(UserProfileView);
            }
			
			String firstName = (UserProfileView.getRegistrationInfo().getFirstName());
			String lastName = (UserProfileView.getRegistrationInfo().getLastName());
			String email  = (UserProfileView.getRegistrationInfo().getEmailAddress());
			
			
			StringBuilder message = new StringBuilder();
			message.append("User Details").append("<br/>");
			message.append("CCO ID:").append(ccoId).append("<br/>");


			StringBuilder desc = new StringBuilder();
			if(condition != null){				
				switch(condition){

				case "UserToPartyAssociation" :
					//message.append(CONTRACT_NUMBER).append(contractNumber).append("<br/>");
					message.append(NAME_OF_USER).append(" "+firstName).append(" ").append(lastName).append("<br/>");
					message.append(COMPANY_NAME).append(" "+companyName).append("<br/>");
					message.append(EMAIL).append(" "+email).append(HTML_BREAK_2);	
					desc.append("Error Occured while calling EF Api ").append(condition).append("<br/>");
					desc.append("Api : ").append(resourceBundle.getProperty("notification.ef.usertopartyassociationapi")).append("<br/>");
					desc.append("Onboarding Party Id : ").append(crPartyId).append("<br/>");
					desc.append("InputXML : ").append("<textarea>").append(inputXML).append("</textarea>").append("<br/>");
					desc.append("OutputXML : ").append("<textarea>").append(outputXML).append("</textarea>").append("<br/>");
					desc.append("").append("<br/>");
					break;

				case "UpdateUserToRoleAssociation" :
					//message.append(CONTRACT_NUMBER).append(contractNumber).append("<br/>");
					message.append(NAME_OF_USER).append(" "+firstName).append(" ").append(lastName).append("<br/>");
					message.append(COMPANY_NAME).append(" "+companyName).append("<br/>");
					message.append(EMAIL).append(" "+email).append(HTML_BREAK_2);	
					desc.append("Error Occured while calling EF Api ").append(condition).append("<br/>");
					desc.append("Api : ").append(resourceBundle.getProperty("notification.ef.usertoroleassociationapi")).append("<br/>");
					desc.append("Onboarding Party Id : ").append(crPartyId).append("<br/>");
					desc.append("InputXML : ").append("<textarea>").append(inputXML).append("</textarea>").append("<br/>");
					desc.append("OutputXML : ").append("<textarea>").append(outputXML).append("</textarea>").append("<br/>");
					desc.append("").append("<br/>");
					break;

				case "nominateDA" :
					//message.append(CONTRACT_NUMBER).append(contractNumber).append("<br/>");
					message.append(NAME_OF_USER).append(" "+firstName).append(" ").append(lastName).append("<br/>");
					message.append(COMPANY_NAME).append(" "+companyName).append("<br/>");
					message.append(EMAIL).append(" "+email).append(HTML_BREAK_2);	
					desc.append("Error Occured while calling EF Api ").append(condition).append("<br/>");
					desc.append("Api : ").append(resourceBundle.getProperty("notification.ef.nominatedaapi")).append("<br/>");
					desc.append("Onboarding Party Id : ").append(crPartyId).append("<br/>");
					desc.append("InputXML : ").append("<textarea>").append(inputXML).append("</textarea>").append("<br/>");
					desc.append("OutputXML : ").append("<textarea>").append(outputXML).append("</textarea>").append("<br/>");
					desc.append("").append("<br/>");
					break;	
					//in this case siteId contains the reason
				case "UpdatePartyToBsslpAssocation" :
					//message.append(CONTRACT_NUMBER).append(contractNumber).append("<br/>");
					message.append(NAME_OF_USER).append(" "+firstName).append(" ").append(lastName).append("<br/>");
					message.append(COMPANY_NAME).append(" "+companyName).append("<br/>");
					message.append(EMAIL).append(" "+email).append(HTML_BREAK_2);
					desc.append("Error Occured while calling EF Api ").append(condition).append("<br/>");
					desc.append("Api : ").append(resourceBundle.getProperty("notification.ef.bsslpassociationapi")).append("<br/>");
					desc.append("Onboarding Party Id : ").append(crPartyId).append("<br/>");
					desc.append("InputXML : ").append("<textarea>").append(inputXML).append("</textarea>").append("<br/>");
					desc.append("OutputXML : ").append("<textarea>").append(outputXML).append("</textarea>").append("<br/>");
					desc.append("").append("<br/>");
					break;

				case "GUIDMismatch" :
					//message.append(CONTRACT_NUMBER).append(contractNumber).append("<br/>");
					message.append(NAME_OF_USER).append(" "+firstName).append(" ").append(lastName).append("<br/>");
					message.append(COMPANY_NAME).append(" "+companyName).append("<br/>");
					message.append(EMAIL).append(" "+email).append(HTML_BREAK_2);	
					desc.append("User selected \'None of the above\' option.").append("<br/>");
					break;
					
				case "crPartyIdNotFound" :
					//message.append(CONTRACT_NUMBER).append(contractNumber).append("<br/>");
					message.append(NAME_OF_USER).append(" "+firstName).append(" ").append(lastName).append("<br/>");
					message.append(EMAIL).append(" "+email).append(HTML_BREAK_2);	
					desc.append("CrPartyId is not found for this ccoId in CPR profile when trying to onboard via Self Service Onboarding tool.").append("<br/>");					
					desc.append("Please verify the user profile and take necessary actions.").append("<br/>");
					break;

				default :
					desc.append("");
					break;

				}
				message.append("Case Description").append("<br/>").append(desc);				
			}		
			message.append(HTML_BREAK_2).append("Sincerely,");
			message.append("<br/>").append("Cisco Smart Net Total Care");

			logger.debug("mail to pss support team : ccoId : "+ccoId); 
			logger.debug("mail to cin team : toemail : "+toemail); 
			logger.debug("mail to cin team : subject : "+subject);
			logger.debug("mail to cin team : fromUserString : "+fromUserString);
			
			notificationUtil.sendCustomMessage(ccoId, toemail, message.toString(), subject,fromUserString);

			//metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Request_Forwarded_For_Manual_Onboarding, crPartyId, "",new Date(), null, null);

		}catch (Exception e) {
			logger.error("-----sendNotification-------"+e.getMessage());
			e.printStackTrace();

			try{
				metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_Sending_Email_For_Manual_Onboarding, "", "", new Date(),null, null);
			} catch (Exception le) {
				logger.error(le.getMessage());
				throw new OnboardingSelfServiceException("Error occurred while capturing metrics - SSO_Error_In_Sending_Email_For_Manual_Onboarding - "+ crPartyId);
			}

		}


	}

	/**
	 * 
	 * @param UserProfileView
	 * @return
	 */
	private String getCompanyName(UserProfileView UserProfileView) {
		String companyName = "Missing company name in user profile";
		try {
			String partyId = AAAClientUtil.getPartyId(UserProfileView);
			if(!StringUtils.isEmpty(partyId)){
				companyName= aaaClientUtil.getUserCompany(partyId);
			}
		} catch (Exception e) {
			logger.error("getCompanyName",e);
		}
		return companyName;
	}


	/**
	 * 
	 * @param ccoid
	 * @throws EFUpdateException
	 * @throws OnboardingSelfServiceException
	 */
	public void onboardUser(HttpServletRequest httpServletrequest, String ccoid) throws EFUpdateException, OnboardingSelfServiceException {

		UserDACompany userDaCompany = fetchUserDACompany(httpServletrequest, ccoid);

		String crPartyId = userDaCompany.getPartyId();

		if (crPartyId == null) {
			throw new OnboardingSelfServiceException(INVALID_PARTY_TO_ONBOARD, "Invalid partyid to Onboard");
		}
		/*
		nominateDA(httpServletrequest,crPartyId, ccoid);
		efUpdatePartyToBsslpAssocation(httpServletrequest,crPartyId, ccoid);
		efUpdateUserToPartyAssociation(httpServletrequest,crPartyId, ccoid);
		efUpdateUserToRoleAssociation(httpServletrequest,crPartyId, ccoid); */

		//efUpdateUserToPartyAssociation(httpServletrequest,crPartyId, ccoid);
		
		nominateDA(httpServletrequest,crPartyId, ccoid);
		efUpdatePartyToBsslpAssocation(httpServletrequest,crPartyId, ccoid);
		efUpdateUserToRoleAssociation(httpServletrequest,crPartyId, ccoid);
        
		// update the AAA cache for this partyid
		updateAAACache(crPartyId);

		logger.info("User is successfully onboarded");
	}

	@Value("${aaa.cache.url}")
	private String aaaCacheURL;

	public String updateAAACache(String CUSTOMERID) {
		try {

			MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
			map.add("partyId", CUSTOMERID);
			map.add("event", "ADD");

			RestTemplate restTemplate = new RestTemplate();
			logger.info("aaaCacheURL is ::" + aaaCacheURL);
			ResponseEntity<String> result = restTemplate.postForEntity(aaaCacheURL, map, String.class);

			logger.info("updateAAACache result : "+result.getBody().toString());
			return result.getBody().toString();

		} catch (Exception e) {
			logger.error("updateAAACache",e );
			e.printStackTrace();
		}
		return "Failed";
	}

	/**
	 * THis CR party is persisted in DB based on user company selection in
	 * Become DA page
	 * 
	 * @param ccoid
	 * @return
	 */
	public UserDACompany fetchUserDACompany(HttpServletRequest httpServletrequest,String ccoid) {
		String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
		String partyId = null;
		UserDACompany daCompany = null;
		try {
			daCompany = onboardDAO.findByCcoid(ccoid);
			if (daCompany != null) {
				return daCompany;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("fetchCRPartyId",e);
			try {
				String errorMessage="Error in fetchUserDACompany: " +e.getMessage();
				errorMessage=metricsClientUtil.getTruncatedErrorMessage(errorMessage);
				metricsClientUtil.captureMetricDataWithContext(ccoid, OnBoardingEventNotificationType.SSO_Error_In_User_CR_Party_Lookup, partyId, "", new Date(), errorMessage, null);
				//metricsClientUtil.captureMetricData(originalLoginUser, , partyId, "",, null, null);
			} catch (Exception le) {
				logger.error(e.getMessage());

				throw new OnboardingSelfServiceException(
						"Error occurred while capturing metrics - Error_In_User_CR_Party_Lookup - for ccoid " + ccoid);
			}
		}
		return null;
	}

	/**
	 * 
	 * @param ccoid
	 * @param partyId
	 */
	public void insertDACompPartyId(HttpServletRequest httpServletrequest, String ccoId, String partyId, String contractNumber) throws OnboardingSelfServiceException {
		try {
			String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
			String compName = aaaClientUtil.getUserCompany(partyId);
			metricsClientUtil.captureMetricData(originalLoginUser,
					OnBoardingEventNotificationType.SSO_Submit_Button_Clicked_To_Become_DA,
					partyId, "", new Date(), "", compName);
			boolean guidCheck = false;
			if(Integer.parseInt(partyId) != -1 ){
				guidCheck = compareGUID(httpServletrequest,ccoId,partyId, originalLoginUser, contractNumber);
			}
			onboardDAO.insertUserDACompany(ccoId, partyId, guidCheck);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("insertDACompPartyId", e);
			throw new OnboardingSelfServiceException(ERROR_TECH_ONBOARD);
		}
	}

	/**
	 * 
	 * @param ccoId
	 * @param partyId
	 */
	private boolean compareGUID(HttpServletRequest httpServletrequest,String ccoId, String contractPartyId, String originalLoginUser, String contractNumber) {
		boolean guidMatch = false;

		logger.info("********New method for GUID check******");
		//check if he is a test user
		if (propertiesUtil.getContractProperties().get(ccoId) != null &&
				propertiesUtil.getContractProperties().get(ccoId).equals(contractNumber)) {
			return true;
		}

		if (StringUtils.isEmpty(ccoId)) {
			throw new OnboardingSelfServiceException("MISSING_CCO_ID");
		}

		logger.debug("******GETTING USER PROFILE******");
		UserProfileView UserProfileView;
		try {
			UserProfileView = aaaClientUtil.getUserProfile(ccoId);
		} catch (LoginException e) {
			logger.error("Exception in fetching user profile",e);
			throw new OnboardingSelfServiceException("Error retrieving user profile type for ccoId when compareGUID:" + ccoId);
		}

		//in this scenario show the generic error page
		if (UserProfileView.getRegistrationInfo() == null) {
			logger.error("getRegistrationInfo is null for ccoId :" + ccoId);
			throw new OnboardingSelfServiceException("Error retrieving user profile type for ccoId compareGUID:" + ccoId);
		}

		//get the GUID for this contract party profile party id 
		String guidOfContract = null;
		String reason = null;
		String userProfilePartyId ="";
		//Contract And SN -> Party ID
		PartyProfileView partyProfileTypeofContract = aaaClientUtil.getPartyProfile(contractPartyId);
		if (partyProfileTypeofContract != null && partyProfileTypeofContract.getGuPartyID() != null) {
			guidOfContract = partyProfileTypeofContract.getGuPartyID().getIdentifier();
		}else{
			logger.error(" contract Party profile is null for contract party id ::" + partyProfileTypeofContract);
			//send email to CIN team
			reason = "Contract Party profile is null for contract party id ::" + partyProfileTypeofContract;
		}

		String user_profile_guid = null;
		if(!StringUtils.isEmpty(guidOfContract)){

			//Fetch the CR PARTY ID from USER PROFILE
			if(UserProfileView.geteFValidatedCRPartySiteID() != null && 
					UserProfileView.geteFValidatedCRPartySiteID().getPartyID() != null){
				userProfilePartyId = UserProfileView.geteFValidatedCRPartySiteID().getPartyID().getIdentifier();
				logger.info("user profile party id :: " + userProfilePartyId);
				//get the GUID for this user profile party id from PartyProfile
				PartyProfileView partyProfileType = aaaClientUtil.getPartyProfile(userProfilePartyId);

				if (partyProfileType != null && partyProfileType.getGuPartyID() != null) {
					// USER PROFILE GUID
					user_profile_guid = partyProfileType.getGuPartyID().getIdentifier();
					logger.info("user profile GUID for party id :: " + user_profile_guid);
					if(user_profile_guid.equalsIgnoreCase(guidOfContract)){
						guidMatch = true;
						metricsClientUtil.captureMetricData(originalLoginUser,OnBoardingEventNotificationType.SSO_GUID_Match_Success, guidOfContract, user_profile_guid, new Date(), null, null);
					}
				}else{
					logger.info(" Party profile is null for user profile party id ::" + userProfilePartyId);
					reason = "Party profile is null for user profile party id ::" + userProfilePartyId;
				}
			}else{
				logger.info("User profile doesnt have CR Party Id");
				//send email to CIN team
				reason = "User profile does not have CR Party Id";
			}
		}


		if(!guidMatch){
			//capture metrics in case it is not a direct customer
			try {
				if(StringUtils.isEmpty(reason)){
					metricsClientUtil.captureMetricDataWithContext(originalLoginUser, OnBoardingEventNotificationType.SSO_GUID_Mismatch, user_profile_guid, guidOfContract, new Date(), aaaClientUtil.getUserCompany(contractPartyId), null);
					//metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_GUID_Mismatch,
					//"", aaaClientUtil.getUserCompany(contractPartyId), new Date(), null, null);
					reason ="guid of the user profile";
				}else{
					metricsClientUtil.captureMetricDataWithContext(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_Fetching_GUID, userProfilePartyId, contractPartyId, new Date(), reason, null);
					//metricsClientUtil.captureMetricData(originalLoginUser,OnBoardingEventNotificationType.SSO_Error_Fetching_GUID, guidOfContract, user_profile_guid, new Date(), null, null);	
				}

			} catch (Exception e) {
				logger.error(e.getMessage());
				throw new OnboardingSelfServiceException(
						"Error occurred while capturing metrics - SSO_Partner_Onboarding_For_Customer- for crPartyId : "
								+ contractPartyId);
			}
		}
		logger.info("********END New method for GUID check******");

		return guidMatch;


	}

	/**
	 * 
	 * @param crPartyId
	 * @param ccoid
	 * @throws EFUpdateException
	 * @throws OnboardingSelfServiceException
	 */
	private void efUpdatePartyToBsslpAssocation(HttpServletRequest httpServletrequest, String crPartyId, String ccoId)
			throws EFUpdateException, OnboardingSelfServiceException {
		String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);

		StatusResult status = efUpdateClient.enablePartyToBsslp(crPartyId, BSSLP_NAME, CREATED_BY);
		String compName = aaaClientUtil.getUserCompany(crPartyId);

		if (status.isStatus()) {
			// EF update is successful
			logger.info("Bsslp to Party Assocation is successful for crPartyId : "+ crPartyId);
		} else {

			String errorCode = status.getErrorCode();
			String errorMessage = status.getErrorMessage();
			String inputXML=status.getRequestXML();
			String outputXML=status.getResponseXML();
			logger.error("efUpdatePartyToBsslpAssocation inputXml :" + inputXML);
			logger.error("efUpdatePartyToBsslpAssocation ouputXml :" + outputXML);
			logger.error("efUpdatePartyToBsslpAssocation error code is :: " + errorCode);
			logger.error("efUpdatePartyToBsslpAssocation error message is :: " + errorMessage);
			List<String> efValidErrorCodes= propertiesUtil.getEFValidErrorCodes();
			if(!efValidErrorCodes.contains(errorCode)){
				// log the error and throw custom exception
				try {
					String truncatedErrorMessage=metricsClientUtil.getTruncatedErrorMessage(errorMessage);
					metricsClientUtil.captureMetricDataWithContext(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_Enabling_Party_BSSLP_API,
							crPartyId, "ErrorCode:"+errorCode, new Date(), truncatedErrorMessage,"");
					logger.error("Sending mail to pss support :"+ccoId+":"+crPartyId);
					sendEmailToPSSupportTeam(httpServletrequest,ccoId, crPartyId, "UpdatePartyToBsslpAssocation", inputXML,outputXML,crPartyId);
					/*
					metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_Enabling_Party_BSSLP_API,crPartyId, "ErrorCode:"+errorCode, new Date(), "", compName);
					 */
				} catch (Exception e) {
					logger.error("efUpdatePartyToBsslpAssocation",e);
					throw new OnboardingSelfServiceException(
							"Error occurred while capturing metrics - Error_In_Enabling_Party_BSSLP_API - for crPartyId "
									+ crPartyId);
				}
				throw new OnboardingSelfServiceException(errorCode, "Party to Bsslp Association for PartyId:"+ crPartyId+ " - "+ errorMessage);
			}
		}

	}

	/**
	 * 
	 * @param crPartyId
	 * @param ccoid
	 * @throws EFUpdateException
	 * @throws OnboardingSelfServiceException
	 */
	private void efUpdateUserToPartyAssociation(HttpServletRequest httpServletrequest,String crPartyId, String ccoId)
			throws EFUpdateException, OnboardingSelfServiceException {
		String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
		StatusResult status = efUpdateClient.assignUserToParty(ccoId, crPartyId);
		String compName = aaaClientUtil.getUserCompany(crPartyId);
		if (status.isStatus()) {
			// EF update is successful
			logger.info("Party to User Assocation is successful for crPartyId : "+crPartyId);
		} else {

			String errorCode = status.getErrorCode();
			String errorMessage = status.getErrorMessage();
			String inputXML=status.getRequestXML();
			String outputXML=status.getResponseXML();
			logger.error("efUpdateUserToPartyAssociation inputXml :" + inputXML);
			logger.error("efUpdateUserToPartyAssociation ouputXml :" + outputXML);
			logger.error("efUpdateUserToPartyAssociation error code is :: " + errorCode);
			logger.error("efUpdateUserToPartyAssociation error message is :: " + errorMessage);
			List<String> efValidErrorCodes= propertiesUtil.getEFValidErrorCodes();
			if(!efValidErrorCodes.contains(errorCode)){
				// log the error and throw custom exception
				try {
					String truncatedErrorMessage=metricsClientUtil.getTruncatedErrorMessage(errorMessage);
					metricsClientUtil.captureMetricDataWithContext(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_User_To_Party_Association_API,
							crPartyId, "ErrorCode:"+errorCode, new Date(), truncatedErrorMessage,"");
					logger.error("Sending mail to pss support :"+ccoId+":"+crPartyId);
					sendEmailToPSSupportTeam(httpServletrequest,ccoId, crPartyId, "UserToPartyAssociation", inputXML,outputXML,crPartyId);
					
					/*
					 metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_User_To_Party_Association_API,
							crPartyId, "ErrorCode:"+errorCode, new Date(), "", compName);
					 */
				} catch (Exception e) {
					logger.error("efUpdateUserToPartyAssociation", e);
					throw new OnboardingSelfServiceException(
							"Error occurred while capturing metrics - Error_In_User_To_Party_Association_API - for crPartyId "
									+ crPartyId);
				}
				throw new OnboardingSelfServiceException(errorCode, "User to Party Association for PartyId "+ crPartyId+ " - "+errorMessage);
			}
		}
	}

	private boolean isAlreadyAssociated(String ccoId, String crPartyId) {
		// TODO Auto-generated method stub
		return false;
	}	

	/**
	 * 
	 * @param crPartyId
	 * @param ccoid
	 * @throws EFUpdateException
	 * @throws OnboardingSelfServiceException
	 */
	private void efUpdateUserToRoleAssociation(HttpServletRequest httpServletrequest,String crPartyId, String ccoId)
			throws EFUpdateException, OnboardingSelfServiceException {
		String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
		// change the string to int for crPartyId

		int iCrPartyId = Integer.parseInt(crPartyId);
		StatusResult status = efUpdateClient.assignUserToRole(ccoId, ROLE_NAME, iCrPartyId, CREATED_BY);
		String compName = aaaClientUtil.getUserCompany(crPartyId);

		if (status.isStatus()) {
			// EF update is successful
			logger.info("User to Role Assocation is successful for crPartyId : "+ crPartyId);
		} else {

			String errorCode = status.getErrorCode();
			String errorMessage = status.getErrorMessage();
			String inputXML=status.getRequestXML();
			String outputXML=status.getResponseXML();
			logger.error("efUpdateUserToRoleAssociation inputXml :" + inputXML);
			logger.error("efUpdateUserToRoleAssociation ouputXml :" + outputXML);
			logger.error("efUpdateUserToRoleAssociation error code is :: " + errorCode);
			logger.error("efUpdateUserToRoleAssociation error message is :: " + errorMessage);
			List<String> efValidErrorCodes= propertiesUtil.getEFValidErrorCodes();
			if(!efValidErrorCodes.contains(errorCode)){
				// log the error and throw custom exception
				try {
					String truncatedErrorMessage=metricsClientUtil.getTruncatedErrorMessage(errorMessage);
					metricsClientUtil.captureMetricDataWithContext(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_User_To_Role_Association_API,
							crPartyId, "ErrorCode:"+errorCode, new Date(), truncatedErrorMessage,"");
					
					logger.error("Sending mail to pss support :"+ccoId+":"+crPartyId);
					sendEmailToPSSupportTeam(httpServletrequest,ccoId, crPartyId, "UpdateUserToRoleAssociation", inputXML,outputXML,crPartyId);
					/*
					metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_User_To_Role_Association_API,
							crPartyId, "ErrorCode:"+errorCode, new Date(), "", compName);
					 */
				} catch (Exception e) {
					logger.error("efUpdateUserToRoleAssociation",e);
					throw new OnboardingSelfServiceException(
							"Error occurred while capturing metrics - Error_In_User_To_Role_Association_API - for crPartyId "
									+ crPartyId);
				}
				throw new OnboardingSelfServiceException(errorCode, "User to Role Association for partyid "+ crPartyId+ " - "+ errorMessage);
			}
		}

	}

	/**
	 * 
	 * @param crPartyId
	 * @param ccoid
	 * @throws EFUpdateException
	 * @throws OnboardingSelfServiceException
	 */
	private void nominateDA(HttpServletRequest httpServletrequest,String crPartyId, String ccoId) throws EFUpdateException, OnboardingSelfServiceException {
		String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);

		StatusResult status = efUpdateClient.nominateDA(ccoId, crPartyId);
		String compName = aaaClientUtil.getUserCompany(crPartyId);
		if (status.isStatus()) {
			// EF update is successful
			logger.info("DA nomination successful for crPartyId : "+crPartyId);
		} else {

			String errorCode = status.getErrorCode();
			String errorMessage = status.getErrorMessage();
			String inputXML=status.getRequestXML();
			String outputXML=status.getResponseXML();
			logger.error("nominateDA inputXml :" + inputXML);
			logger.error("nominateDA ouputXml :" + outputXML);
			logger.error("nominateDA error code is :: " + errorCode);
			logger.error("nominateDA error message is :: " + errorMessage);
			List<String> efValidErrorCodes= propertiesUtil.getEFValidErrorCodes();
			if(!efValidErrorCodes.contains(errorCode)){
				// log the error and throw custom exception
				try {
					String truncatedErrorMessage=metricsClientUtil.getTruncatedErrorMessage(errorMessage);
					metricsClientUtil.captureMetricDataWithContext(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_Nominate_DA_API,
							crPartyId, "ErrorCode:"+errorCode, new Date(), truncatedErrorMessage,"");
					logger.error("Sending mail to pss support :"+ccoId+":"+crPartyId);
					sendEmailToPSSupportTeam(httpServletrequest,ccoId, crPartyId, "nominateDA", inputXML,outputXML,crPartyId);
					
					/*metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_Nominate_DA_API, crPartyId, "ErrorCode:"+errorCode,
							new Date(), "", compName);
					 */
				} catch (Exception e) {
					logger.error(e.getMessage());
					logger.error("nominateDA",e);
					throw new OnboardingSelfServiceException(
							"Error occurred while capturing metrics - Error_In_Nominate_DA_API - for crPartyId "
									+ crPartyId);
				}
				throw new OnboardingSelfServiceException(errorCode, "Nominate DA for partyid "+ crPartyId+ " - " +errorMessage);
			}
		}

	}

	/**
	 * Determine company hierarchy within bounds
	 * 
	 * 
	 * @param crPartyId
	 * @return
	 */
	public List<Company> getEligibleCompanies(HttpServletRequest httpServletrequest,String crPartyId, String ccoId) {

		String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
		List<Company> listOfCompanies = new ArrayList<Company>();

		// if the parent country is outside the crPartyId country [country level check is removed, company list is shown globally]OR if the GUID
		// is same as crPartyId indicating that the current level doesnt have
		// hierarchy
		int i = 0;
		PartyProfileView partyProfile = null;
		try {
			partyProfile = aaaClientUtil.getPartyProfile(crPartyId);
		} catch (Exception e1) {
			//TODO log
			logger.error("getEligibleCompanies",e1);
		}
		if(partyProfile == null || partyProfile.getGuPartyID() == null || partyProfile.getPrimaryPostalAddress() == null){
			return null;
		}

		String guId = partyProfile.getGuPartyID().getIdentifier();
		String originalCountry = partyProfile.getPrimaryPostalAddress().getCountry();

		System.out.println("----GUID--"+guId+"------party id-----"+crPartyId);
		logger.info("----GUID--"+guId+"------party id-----"+crPartyId);
		listOfCompanies.add(populateCompany(crPartyId,partyProfile,originalCountry,i,ccoId));
		//IF GUID MATCHES TO THE CR PARTY ID THEN BREAK THE LOOP

		while (true){
			if(guId.equals(crPartyId)){
				break;
			}
			i++;
			PartyHierarchyView partyHierarchyType = null;
			try {
				partyHierarchyType = aaaClientUtil.getPartyHierarchyType(crPartyId);
			}
			catch (Exception e) {
				logger.error("getEligibleCompanies",e);
			}
			if(partyHierarchyType == null) break;
			
			//Get Parent party profile
			crPartyId = partyHierarchyType.getParentID().getIdentifier();
			System.out.println("-----parent pary id...>"+crPartyId);
			logger.info("-----parent pary id...>"+crPartyId);
			try {
				partyProfile = aaaClientUtil.getPartyProfile(crPartyId);
			} catch (Exception e) {
				logger.error("getEligibleCompanies",e);
				partyProfile =null;
			}
			if(partyProfile == null){
				System.out.println("PartyProfileType is null for "+ crPartyId);
				break;
			}
			
			//IF THE COUNTRY DOESNT MATCH WITH PARETN COUNTRY BREAK THE LOOP
			/*
			if(partyProfile.getPrimaryPostalAddress() != null && !partyProfile.getPrimaryPostalAddress().getCountry().equals(originalCountry)){
				break;
			}
			*/
			Company company = populateCompany(crPartyId,partyProfile,partyProfile.getPrimaryPostalAddress().getCountry(),i,ccoId);
			logger.info("*****************************************populate company---->"+company.getCompanyName());
			listOfCompanies.add(company);
		} 


		// check hasDA true in listofcompanies

		if(!listOfCompanies.isEmpty()){
			for (Company c : listOfCompanies) {
				if (c.getHasDA() == true) {
					try {
						metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_User_Company_Has_DA,
								c.getPartyId(), "", new Date(), "", c.getCompanyName());
					} catch (Exception e) {
						logger.error(e.getMessage());
						logger.error("getEligibleCompanies",e);
						throw new OnboardingSelfServiceException(
								"Error occurred while - capturing metrics -User_Company_Has_Delegated_Admin - for crPartyId : "
										+ crPartyId);

					}
				}
			}
		}

		return listOfCompanies;

	}

	private Company populateCompany(String crPartyId,PartyProfileView partyProfile,String country,int level,String ccoId){
		Company c1 = new Company();
		c1.setPartyId(crPartyId);
		c1.setCity(partyProfile.getPrimaryPostalAddress().getCity());
		c1.setCompanyName(partyProfile.getPartyName().getName());
		c1.setCountry(country);
		c1.setState(partyProfile.getPrimaryPostalAddress().getState());
		String streetAddress = partyProfile.getPrimaryPostalAddress().getStreetAddress().getAddressContent().toString();
		c1.setAddress(streetAddress.substring(1, streetAddress.length() - 1));
		c1.setLevel(level);

		PartyDA partyDa = aaaClientUtil.doesPartyHasDA(crPartyId);

		String userName = propertiesUtil.getTestUserProperties().get("sanity.test.user").toString();
		if(!StringUtils.isEmpty(userName) && userName.equalsIgnoreCase(ccoId)){
			partyDa.setDA(false);
		}

		//IF PARTY HAS DA THEN BREAK THE LOOP
		if (partyDa != null && partyDa.isDA()) {
			String emailAddress = partyDa.getEmailAddress();
			String firstName = partyDa.getFirstName();
			String lastName = partyDa.getLastName();
			c1.setDAEmailAddress(emailAddress);
			c1.setDAFirstName(firstName);
			c1.setDALastName(lastName);
			c1.setHasDA(true);
		}



		return c1;
	}
	/**
	 * 
	 * @param ccoId
	 * @param crPartyId
	 * @return
	 * @throws OnboardingSelfServiceException
	 * 
	 * 1) Get CR PARTY ID FOR GIVEN CONTRACT AND SN (SITE UUSE ID-> CRPARTY ID)
	 * 2) GET GUID FOR THAT CR PARTY ID FROM PARTY PROFILE
	 * 3) GET USER PROFILE CR PARTY ID
	 * 4) GET GUID FOR THE USER PROFILE CR PARTY ID FROM PARTY PROFILE
	 * 
	 * 
	 */
	public boolean compareUserGUIDtoPartyGUID(HttpServletRequest httpServletrequest,String ccoId,String contractNumber, String crPartyId) throws OnboardingSelfServiceException {
		boolean guidMatch = true;
		/*
		boolean guidMatch = false;
		String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
		logger.debug("******INVOKING compare GUID******");
		// TODO adding to for testing purpose till we get Test Users. It will
		// not harm in the production

		if (propertiesUtil.getContractProperties().get(ccoId) != null &&
				propertiesUtil.getContractProperties().get(ccoId).equals(contractNumber)) {
			return true;
		}

		if (StringUtils.isEmpty(ccoId)) {
			throw new OnboardingSelfServiceException("MISSING_CCO_ID");
		}

		logger.debug("******GETTING USER PROFILE******");
		UserProfileView UserProfileView;
		try {
			UserProfileView = aaaClientUtil.getUserProfile(ccoId);
		} catch (LoginException e) {
			logger.error("Exception in fetching user profile",e);
			throw new OnboardingSelfServiceException("Error retrieving user profile type for ccoId when compareUserGUIDtoPartyGUID:" + ccoId);
		}

		//in this scenario show the generic error page
		if (UserProfileView.getRegistrationInfo() == null) {
			logger.error("getRegistrationInfo is null for ccoId :" + ccoId);
			throw new OnboardingSelfServiceException("Error retrieving user profile type for ccoId compareUserGUIDtoPartyGUID:" + ccoId);
		}

		//get the GUID for this contract party profile party id 
		String guidOfContract = null;
		String reason = null;
		//Contract And SN -> Party ID
		PartyProfileType partyProfileTypeofContract = aaaClientUtil.getPartyProfile(crPartyId);
		if (partyProfileTypeofContract != null && partyProfileTypeofContract.getGuPartyID() != null) {
			guidOfContract = partyProfileTypeofContract.getGuPartyID().getIdentifier();
		}else{
			logger.error(" contract Party profile is null for contract party id ::" + partyProfileTypeofContract);
			//send email to CIN team
			reason = "Contract Party profile is null for contract party id ::" + partyProfileTypeofContract;
		}
		if(!StringUtils.isEmpty(guidOfContract)){
			String user_profile_guid = null;
			//Fetch the CR PARTY ID from USER PROFILE
			if(UserProfileView.getEFCRPartySiteID() != null && 
					UserProfileView.getEFCRPartySiteID().getPartyID() != null){
				 String userProfilePartyId = UserProfileView.getEFCRPartySiteID().getPartyID().getIdentifier();
				 logger.info("user profile party id :: " + userProfilePartyId);
				//get the GUID for this user profile party id from PartyProfile
				PartyProfileType partyProfileType = aaaClientUtil.getPartyProfile(userProfilePartyId);

				if (partyProfileType != null && partyProfileType.getGuPartyID() != null) {
					// USER PROFILE GUID
					user_profile_guid = partyProfileType.getGuPartyID().getIdentifier();
					logger.info("user profile GUID for party id :: " + user_profile_guid);
					if(user_profile_guid.equalsIgnoreCase(guidOfContract)){
						guidMatch = true;
					}
				}else{
						logger.error(" Party profile is null for user profile party id ::" + userProfilePartyId);
						reason = "Party profile is null for user profile party id ::" + userProfilePartyId;
				}
			}else{
				logger.error("User profile doesnt have CR Party Id");
				//send email to CIN team
				 reason = "User profile does not have CR Party Id";
			}
		}


		if(!guidMatch){
			//capture metrics in case it is not a direct customer
			try {
				if(StringUtils.isEmpty(reason)){
					metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_GUID_MISTMATCH,
							"", aaaClientUtil.getUserCompany(crPartyId), new Date(), null, null);
					reason ="guid of the user profile";
				}else{
					metricsClientUtil.captureMetricData(originalLoginUser,OnBoardingEventNotificationType.SSO_ERROR_FECTHING_GUID, guidOfContract, "", new Date(), null, null);	
				}
				//send email to CIN team - this NEEDS BE TO FINALIZED
				sendEmailToCINTeamWithCustomMessage(httpServletrequest,ccoId, contractNumber, "guidCheckFail", reason, guidOfContract);
			} catch (Exception e) {
				logger.error(e.getMessage());
				throw new OnboardingSelfServiceException(
						"Error occurred while capturing metrics - SSO_Partner_Onboarding_For_Customer- for crPartyId : "
								+ crPartyId);
			}

		}

		return guidMatch;
		 */
		return guidMatch;
	}

	public void metricsCallForClickBackButton(HttpServletRequest httpServletrequest,String ccoid) {
		try {
			String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
			metricsClientUtil.captureMetricData(originalLoginUser,
					OnBoardingEventNotificationType.SSO_Back_Button_Clicked_On_Request_DA_Form,
					"", "", new Date(), null, null);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new OnboardingSelfServiceException(
					"Error occurred while capturing metrics - SSO_Back_Button_Clicked_On_Request_DA_Form - ccoid : "
							+ ccoid);
		}
	}

	public void metricsCallForPortalAccess(HttpServletRequest httpServletrequest) {
		try {
			String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_User_Clicked_On_Proceed_To_Portal_Link, "", "",
					new Date(), null, null);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new OnboardingSelfServiceException("Error occurred while capturing metrics - SSO_User_Clicked_On_Proceed_To_Portal_Link");
		}
	}

}
