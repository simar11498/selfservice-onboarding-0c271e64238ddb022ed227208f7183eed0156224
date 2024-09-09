package com.cisco.convergence.obs.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cisco.ca.csp.ef.update.exception.EFUpdateException;
import com.cisco.convergence.obs.exception.LoginException;
import com.cisco.convergence.obs.exception.OnBoardingException;
import com.cisco.convergence.obs.exception.OnboardingSelfServiceException;
import com.cisco.convergence.obs.jpa.dao.UserContractDao;
import com.cisco.convergence.obs.jpa.model.OnboardStatus;
import com.cisco.convergence.obs.jpa.model.UserContract;
import com.cisco.convergence.obs.jpa.model.UserDACompany;
import com.cisco.convergence.obs.model.Company;
import com.cisco.convergence.obs.rest.metricsCollection.OnBoardingEventNotificationType;
import com.cisco.convergence.obs.service.DAOnboardingService;
import com.cisco.convergence.obs.util.LoginUtil;
import com.cisco.convergence.obs.util.MetricsClientUtil;
import com.cisco.convergence.obs.util.PropertiesUtil;

@Service
@Path("/daobs")
public class DAOnboardingWS {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	
	@Autowired
	DAOnboardingService onboardService;
	
	@Autowired
	private LoginUtil loginUtil;
	

	@Inject
	private MetricsClientUtil metricsClientUtil;

	@Autowired
	private PropertiesUtil propertiesUtil;
	
	@Autowired
	private UserContractDao userContractDao;
	
	
	@GET
	@Path("/yourAreNowDApage/clickedPortalAccess")
	public void clickedPortalAccess(@Context HttpServletRequest httpServletrequest){
		onboardService.metricsCallForPortalAccess(httpServletrequest);
		//onboardService.sendWelcomeEmailToUser(httpServletrequest);
	}
	
	/*
	 * page: Verify your Account Page
	 * Event: Clicked "Next" Button - Back end validation check SiteId and CrPartyId rest call is failed
	   Action :	send email to CIN Team 	 
	 */	
	@GET
	@Path("/ignoreAddressVerificationEmail/{fromDate}/{toDate}")
	@Produces("text/plain")
	public String ignoredAddressVerificationEmail( @Context HttpServletRequest httpServletrequest,@PathParam("fromDate") String fromDate,@PathParam("toDate") String toDate) {
		String ccoId = loginUtil.getLoginUser(httpServletrequest); 	
		String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
		List<String> ccoIdlist= new ArrayList<String>();
		String info="List of users from "+fromDate+" to "+toDate;
		
		
		try {			
			 // metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_User_CR_Party_Lookup, "", "", new Date(), null);
			
			ccoIdlist=userContractDao.getIgnoredEmailLinks(fromDate, toDate);
			if(ccoIdlist==null)
				return "Failure";
		} catch (Exception e1) {
			logger.error("Exception occurred while getting list of NOT_CLIKED_EMAIL_LINK"+ e1);
			return "Failure";
		}
		try{
		onboardService.sendEmailIgnoredVerificationLink(httpServletrequest,fromDate,toDate, ccoIdlist);
		}catch(Exception e){
			logger.error("Exception occurred while sending email"+ e);
			return "Failure";
		}
		
		return "Success";
		
	}
	@GET
	@Path("/mailToCINTeam/siteIdCrPartyIdFailed/{contractNumber}/{siteId}/{crPartyId}")
	public String sendEmailToCINTeam( @Context HttpServletRequest httpServletrequest,@PathParam("contractNumber") String contractNumber,@PathParam("siteId") String siteId, @PathParam("crPartyId") String crPartyId) {
		String ccoId = loginUtil.getLoginUser(httpServletrequest); 	
		String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
		try {			
			 // metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_User_CR_Party_Lookup, "", "", new Date(), null);
			metricsClientUtil.captureMetricDataWithContext(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_User_CR_Party_Lookup, siteId, crPartyId, new Date(), "siteIdCrPartyIdFailed","");
		} catch (LoginException e1) {
			logger.error("Exception occurred while capturing metrics");
		}
		onboardService.sendEmailToCINTeamWithCustomMessage(httpServletrequest,ccoId, contractNumber, "eitherSiteIdOrCrPartyIdWrong", siteId, crPartyId);
	    return "SUCCESS";
	}
	
	@GET
	@Path("/mailToCINTeam/onGetComaniesListEmpty/{contractNumber}/{siteId}/{crPartyId}")
	@Produces("text/plain")
	public String sendEmailToCINTeamWithCompanyListEmpty( @Context HttpServletRequest httpServletrequest,@PathParam("contractNumber") String contractNumber,@PathParam("siteId") String siteId, @PathParam("crPartyId") String crPartyId) {
		String ccoId = loginUtil.getLoginUser(httpServletrequest); 	
		String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
		try {
			//metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_User_CR_Party_Lookup, "", "", new Date(), null);
			metricsClientUtil.captureMetricDataWithContext(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_User_CR_Party_Lookup, siteId, crPartyId, new Date(), "onGetComaniesListEmpty", "");
		} catch (LoginException e1) {
			logger.error("Exception occurred while capturing metrics");
		}
		onboardService.sendEmailToCINTeamWithCustomMessage(httpServletrequest,ccoId, contractNumber, "CRPartyHierarchyEmpty", siteId, crPartyId);
	    return "SUCCESS";
	}
	
	@GET
	@Path("/mailToCPRSupportTeam")
	@Produces("text/plain")
	public String sendEmailToCINTeamWithCompanyListEmpty( @Context HttpServletRequest httpServletrequest) {
		String ccoId = loginUtil.getLoginUser(httpServletrequest); 	
		String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
		try {
			onboardService.sendEmailToPSSupportTeam(httpServletrequest, ccoId, null, "crPartyIdNotFound", null, null, null);
		} catch (Exception e1) {
			logger.error("Exception occurred while capturing metrics");
			return "Failure";
		}
		
	    return "SUCCESS";
	}
	
	@GET
	@Path("/mailToPSSupport/onErrorFound/{condition}/{crPartyId}")
	@Produces("text/plain")
	public String sendEmailToPSSupport( @Context HttpServletRequest httpServletrequest,@PathParam("condition") String condition, @PathParam("crPartyId") String crPartyId) {
		String ccoId = loginUtil.getLoginUser(httpServletrequest); 	
		String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
		String inputXML="<textarea><name>Input XML</name></textarea>"+"<textarea><name>Input XML</name><place>Input XML</place></textarea>";
		String outputXML="<textarea>"+"<name>OutPut XML</name>"+"</textarea>";
		try {
			onboardService.sendEmailToPSSupportTeam(httpServletrequest,ccoId,crPartyId , condition, inputXML,outputXML, crPartyId);
		} catch (Exception e1) {
			logger.error("Exception occurred while capturing metrics");
			return "Failure: "+e1;
		}
		//onboardService.sendEmailToPSSupportTeam(httpServletrequest,ccoId,crPartyId , condition, inputXML,outputXML, crPartyId);
		return "SUCCESS";
	}

	/**
	 * send eamil to CIN Team
	 * @param httpServletrequest
	 * @param contractNumber
	 * @param crPartyId
	 * @return
	 */
	@GET
	@Path("/mailToCINTeam/clickedHere/{contractNumber}/{crPartyId}")
	public String sendEmailToCINTeam( @Context HttpServletRequest httpServletrequest,@PathParam("contractNumber") String contractNumber,@PathParam("crPartyId") String crPartyId) {
		String ccoId = loginUtil.getLoginUser(httpServletrequest); 	
		String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
		try {
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Clicked_Support_On_Request_To_Be_Delegated_Admin_Form, "", "", new Date(), null);
		} catch (LoginException e1) {
			logger.error("Exception occurred while capturing metrics");
		}
		onboardService.sendEmailToCINTeamWithCustomMessage(httpServletrequest,ccoId, contractNumber, "clickedHere", null, crPartyId);
	    return "SUCCESS";
	}
		
	/*
	 * page: Become DA Page
	 * Event: Clicked "Back Button" 
	   Action :	Capture Metric	 
	 */	
	@GET
	@Path("/becomeDApage/clickedBack")
	public String saveMetric(@Context HttpServletRequest httpServletrequest) {
		
		String ccoId = loginUtil.getLoginUser(httpServletrequest); 
		onboardService.metricsCallForClickBackButton(httpServletrequest,ccoId);
	    return "SUCCESS";
	}
	
	
	/**
	 * 
	 * @param httpServletrequest
	 * @return
	 */
	@GET
	@Path("/onboardUser")
	@Produces(MediaType.APPLICATION_JSON)
	public OnboardStatus onboardUser(@Context HttpServletRequest httpServletrequest) throws OnBoardingException{
			
		boolean success=true;
		
		String ccoId = loginUtil.getLoginUser(httpServletrequest);
		String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
		String contractNumber = "NONE";
		try {
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_User_Clicked_Continue_After_Email_Verified, "", "", new Date(), null);
		} catch (LoginException e1) {
			logger.error("Exception occurred while capturing metrics");
		}
		
		try {
			//instrumentation for production testing 
			// return true if ccoId is prod test user, defined in testuser.properties
			logger.info("instrumentation for production testing : Logged in User : "+ccoId);
			if(!StringUtils.isEmpty(ccoId) && propertiesUtil.getTestUserProperties().get("test.user").equals(ccoId)){
						
				return createOnboardStatus(true,true);				
			}
			
			//Reading user company from DB
			UserDACompany userDaCompany = onboardService.fetchUserDACompany(httpServletrequest, ccoId);
			if(userDaCompany == null){
				logger.error("Exception occurred while finding User/contract:"+ccoId);
				throw new OnBoardingException("User/contract information not found in the DB");
			}
			
			boolean guidCheck = Boolean.valueOf(userDaCompany.getGuidCheck());			
			UserContract userContract = userContractDao.findByCcoid(ccoId);			
			if(userContract != null){
				contractNumber = userContract.getContract();
			}
						
			/*
			 * 2) IF GUID CHECK false or party id -1 then send to email CIN team. 
			 * 3) IF GUID CHECK true and party id != -1 then call the API's
			 * 4) IF any error in any of the case send error message to internal support team NO CIN team 
			 */
			
			int partyId = Integer.parseInt(userDaCompany.getPartyId());
			//User selected none option in the UI
			if( partyId == -1){
				// User_DA_option table does not has the contract number column, so contract num empty in email.
				onboardService.sendEmailToCINTeamWithCustomMessage(httpServletrequest,ccoId, contractNumber, "companyNotSelected", null, userDaCompany.getPartyId());
				return createOnboardStatus(false,false);
			}
			
			if(!guidCheck){
				// User_DA_option table does not has the contract number column, so contract num and site id empty in email. 
				onboardService.sendEmailToCINTeamWithCustomMessage(httpServletrequest,ccoId, contractNumber, "guidCheckFail", null, userDaCompany.getPartyId());
				return createOnboardStatus(false,false);
			}
			
			if(guidCheck && partyId != -1 ){			
				onboardService.onboardUser(httpServletrequest,ccoId);
			}
		} catch (EFUpdateException e) {
			success=false;	
			logger.error("Exception in EF Update Client");
			logger.error(e.getMessage());
			String errorMessage=e.getMessage();					
			errorMessage=metricsClientUtil.getTruncatedErrorMessage(errorMessage);			
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Error_In_Onboard_User,
					"", "", new Date(), errorMessage,"");				
			//send email to CIN team in case of error in onboarding
			onboardService.sendEmailToCINTeamWithCustomMessage(httpServletrequest,ccoId, contractNumber, "OnboardingError", e.getMessage(), "");
		} catch (OnboardingSelfServiceException e) {
			success=false;			
			logger.error("Onboarding Self Service Exception. One of the criteria failed");
			logger.error(e.getErrorCode());
			logger.error(e.getMessage());			
			//send email to CIN team in case of error in onboarding
			onboardService.sendEmailToCINTeamWithCustomMessage(httpServletrequest,ccoId, contractNumber, "OnboardingError",e.getMessage(), "");
		}
		
		if(success){
			try {
				metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_User_Registration_Successful, "", "", new Date(), null);
				onboardService.sendWelcomeEmailToUser(httpServletrequest);
				return createOnboardStatus(true,true);
			} catch (LoginException e1) {
				logger.error("Exception occurred while capturing metrics");
			}
		}
		
		return createOnboardStatus(false,false);
		
	}

	private OnboardStatus createOnboardStatus(boolean guidCheck,boolean onboardStatus) {
		OnboardStatus obsStatus = new OnboardStatus();
		obsStatus.setGuidCheck(guidCheck);
		obsStatus.setOnboardStatus(onboardStatus);
		return obsStatus;
	}
	
	
	/**
	 * 
	 * @param httpServletrequest
	 * @param partyId
	 * @return
	 */
	@GET
	@Path("/persistUserSelCompany/{partyId}/{contractNumber}")
	@Produces(MediaType.APPLICATION_JSON)
	public String persistUserDACompany(@Context HttpServletRequest httpServletrequest, @PathParam("partyId") String partyId,@PathParam("contractNumber") String contractNumber) {
		
		String success="true";
		String ccoId = loginUtil.getLoginUser(httpServletrequest);
		
		try{
			onboardService.insertDACompPartyId(httpServletrequest,ccoId, partyId, contractNumber);
		}catch(OnboardingSelfServiceException e){
			success="false";
			logger.error(e.getMessage());
		}
		
		return success;
	}
	
	/**
	 * 
	 * @param httpServletrequest
	 * @param crPartyId
	 * @return
	 */
	@GET
	@Path("/getEligibleCompanies/{crPartyId}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Company> getEligibleCompanies(@Context HttpServletRequest httpServletrequest, @PathParam("crPartyId") String crPartyId){
	
		String ccoId = loginUtil.getLoginUser(httpServletrequest);
		//List<Company> listOfCompanies = new ArrayList<Company>();		
		//SA - Fix
		
		logger.info(":::::: getEligibleCompanies() ::::::: crPartyId = "+crPartyId);
		List<Company> listOfCompanies = onboardService.getEligibleCompanies(httpServletrequest,crPartyId, ccoId);
		
		logger.info(":::::::: getEligibleCompanies() :::::::: listOfCompanies ("+listOfCompanies.size()+") = "+listOfCompanies) ;
		
		if(listOfCompanies == null){
			return new ArrayList<Company>();
		}
		
		return listOfCompanies;
	}
	
	
	/**
	 * This method checks if the user is a direct customer or not
	 * @param request
	 * @param crPartyId
	 * @return
	 * @throws OnBoardingException
	 */
	@GET
	@Path("/compareGUID/{contractNumber}/{crPartyId}")
	public String compareGUID(@Context HttpServletRequest request,@PathParam("contractNumber") String contractNumber, @PathParam("crPartyId") String crPartyId){
		
		boolean atleastOneGuidMatch = false;//this must be made to false
		
		String ccoId = loginUtil.getLoginUser(request);

		atleastOneGuidMatch = onboardService.compareUserGUIDtoPartyGUID(request,ccoId,contractNumber, crPartyId);
		return String.valueOf(atleastOneGuidMatch);
	}
	
	/**
	 * This method is to check AAA cache trigger
	 * @param request
	 * @param crPartyId
	 * @return
	 * @throws OnBoardingException
	 */
	@GET
	@Path("/enableCrPartyIdInAAACache/{crPartyId}")
	public String testAAACache(@PathParam("crPartyId") String crPartyId){
		
		String result  = onboardService.updateAAACache(crPartyId);
		return result;
	}
	
	
}
