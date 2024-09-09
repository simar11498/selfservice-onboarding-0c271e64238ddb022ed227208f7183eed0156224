package com.cisco.convergence.obs.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.cisco.ata.rest.PartyProfileView;
import com.cisco.ata.rest.UserProfileView;
import com.cisco.ca.csp.ef.update.model.StatusResult;
import com.cisco.convergence.obs.exception.LoginException;
import com.cisco.convergence.obs.exception.OnBoardingException;
import com.cisco.convergence.obs.jpa.dao.EpmUserDao;
import com.cisco.convergence.obs.jpa.dao.MetricDao;
import com.cisco.convergence.obs.jpa.dao.OneViewDao;
import com.cisco.convergence.obs.jpa.dao.UserContractDao;
import com.cisco.convergence.obs.jpa.model.ContractGSPEntitlementStatus;
import com.cisco.convergence.obs.jpa.model.ContractInfo;
import com.cisco.convergence.obs.model.AccessLevel;
import com.cisco.convergence.obs.model.Company;
import com.cisco.convergence.obs.model.ContractStatus;
import com.cisco.convergence.obs.model.DANominationStatus;
import com.cisco.convergence.obs.model.EmailStatus;
import com.cisco.convergence.obs.model.PartyDA;
import com.cisco.convergence.obs.model.UserProfile;
import com.cisco.convergence.obs.rest.metricsCollection.OnBoardingEventNotificationType;
import com.cisco.convergence.obs.rest.metricsCollection.OnBoardingMetricCollectionEvent;
import com.cisco.convergence.obs.service.DAOnboardingService;
import com.cisco.convergence.obs.util.AAAClientUtil;
import com.cisco.convergence.obs.util.CPRUtil;
import com.cisco.convergence.obs.util.LoginUtil;
import com.cisco.convergence.obs.util.MetricsClientUtil;
import com.cisco.convergence.obs.util.NotificationUtil;
import com.cisco.convergence.obs.util.PropertiesUtil;
import com.cisco.cssp.init.spring.SSO_Schedulars;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;



@Service
@Path("/obs")
public class EntitlementService {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Inject
	NotificationUtil notificationUtil;
	
	@Inject
	CPRUtil cprUtil;
	
	@Inject
	UserContractDao userContractDao;
	
	@Inject
	EpmUserDao epmUserDao;
	
	@Inject
	OneViewDao oneViewDao;
	
	@Inject
	private PropertiesUtil propertiesUtil;
	
	@Inject
	private LoginUtil loginUtil;
	
	@Inject
	private AAAClientUtil aaaClientUtil;
	
	
	@Inject
	private MetricsClientUtil metricsClientUtil;
	
	@Inject
	private SSO_Schedulars sso_scheadulars;
	
	@Autowired
	private MetricDao metricDao;
	
	@Value("${da.ccoid}")
	private String daConfiguredCcoId;
	
	@Autowired
	DAOnboardingService onboardService;
	
	
	@GET
	@Path("/testEntitlementService")
	@Produces("text/plain")
	public String test() {
		// Return some cliched textual content
		return "On-boarding Service is Active";
	}
	

	@GET
	@Path("/testMetrics/{ccoId}")
	@Produces(MediaType.APPLICATION_JSON)
	public UserProfile testMetrics(@Context HttpServletRequest httpServletrequest,@PathParam("ccoId") String ccoId)throws OnBoardingException {
		UserProfile userProfile = new UserProfile();
		List<OnBoardingMetricCollectionEvent> eventList = new ArrayList<OnBoardingMetricCollectionEvent>();

		OnBoardingMetricCollectionEvent onM = 
				new OnBoardingMetricCollectionEvent("vpriyata", Calendar.getInstance().getTime(), "Test", "1234",
						"FXCV", OnBoardingEventNotificationType.SSO_Address_Verification_Email_Sent, AccessLevel.EMPLOYEE, "54123", "vpriyata", "vpriyata@cisco.com");
		eventList.add(onM);
		System.out.println("***************TEST METRICS**********");
		metricDao.insertMetricEvent(eventList);
		System.out.println("***************insertMetricEvent**********"+onM);
		return userProfile;
	}
	
	@GET
	@Path("/serviceLineInfo/{contractNumber}")
	@Produces(MediaType.APPLICATION_JSON)
	public ContractInfo getServiceLineInfo(@Context HttpServletRequest httpServletrequest,
			@PathParam("contractNumber") String contractNumber)throws OnBoardingException {
		return oneViewDao.findServiceLine(contractNumber);
	}
	
	
	@GET
	@Path("/contractStatus/gspEntitlement/{contractNumber}/{sn}")
	@Produces(MediaType.APPLICATION_JSON)
	public ContractGSPEntitlementStatus checkContractStatus(@Context HttpServletRequest httpServletrequest,
			@PathParam("contractNumber") String contractNumber,@PathParam("sn") String sn)throws OnBoardingException {
		
		String ccoId = loginUtil.getLoginUser(httpServletrequest);
		String originalLoginUser = loginUtil.getLoginOriginalUser(httpServletrequest);
		ContractGSPEntitlementStatus contractGSPEntitlementStatus = new ContractGSPEntitlementStatus();
		
		System.out.println("<<<<<<<<<<<<<----------contractStatus------------>>>>>>>>>>");
		//fetching Contract status
		//TODO remove this when you do a check at the isContractActive
		if(aaaClientUtil.isContractInProfile(ccoId, contractNumber)){
			contractGSPEntitlementStatus.setContractStatus("ACTIVE");
		}else{
			contractGSPEntitlementStatus.setContractStatus("OTHER_THAN_ACTIVE");
		}
		
		if(! "ACTIVE".equalsIgnoreCase(contractGSPEntitlementStatus.getContractStatus())){
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Inactive_Contract_Number, contractNumber, sn, new Date(), null);
		}
		
		//fetching GSP Entitlement status for the selected service lines
		List<String> contractTypeList = aaaClientUtil.getContractTypeBy(contractNumber,ccoId);
		logger.info("::::: validateContractAndSN() ::::::: ccoId = "+ccoId+", originalLoginUser = "+originalLoginUser+", contractTypeList = "+contractTypeList);
		
		if(!contractTypeList.isEmpty()){
			contractGSPEntitlementStatus.setGspEntitlementStatus(userContractDao.checkGSPEntitlementEnable(contractTypeList));
		}
		if( "NO".equalsIgnoreCase(contractGSPEntitlementStatus.getGspEntitlementStatus())){
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Contract_Is_Not_Smart_Entitled, contractNumber, sn, new Date(), null);
		}
		return contractGSPEntitlementStatus;
	}
	
	
	@GET
	@Path("/partyInfo/{contractNumber}/{sn}")
	@Produces(MediaType.APPLICATION_JSON)
	public ContractInfo getCrPartyBySiteUseId(@Context HttpServletRequest httpServletrequest,
			@PathParam("contractNumber") String contractNumber,@PathParam("sn") String sn)throws OnBoardingException {
		String ccoId = loginUtil.getLoginUser(httpServletrequest);
		String siteUseId = aaaClientUtil.getSiteUseIdByContractAndSN(contractNumber,sn);
		logger.info("install-site-id "+ siteUseId + " for CCOID : "+ ccoId);
		ContractInfo contactInfo = new ContractInfo();
		//Mocked for testing metrics logging
		if(!StringUtils.isEmpty(siteUseId)){
			long id = Long.valueOf(siteUseId);
			contactInfo.setSiteUseId(id);
			contactInfo.setCrPartyId(aaaClientUtil.getCrPartyBySiteUseId(id,ccoId));
		}
		/*try{			
			metricsClientUtil.captureMetricDataWithContext(ccoId,OnBoardingEventNotificationType.SSO_User_Installsite_PartyID, siteUseId,contactInfo.getCrPartyId() , new Date(),null, null);
		
		}catch(Exception e){
			logger.info("Error in logging metric for install site id and partyid ");
		}*/
		return contactInfo;
	}

	
	@GET
	@Path("/userPartyInfo/{ccoId}")
	@Produces(MediaType.APPLICATION_JSON)
	public UserProfile getUserPartyInfo(@Context HttpServletRequest httpServletrequest,
			@PathParam("ccoId") String ccoId)throws OnBoardingException {
		UserProfile userProfile = new UserProfile();
		userProfile.setCompanyName("Teest");
		userProfile.setEmail("test@test.com");
		PartyDA partyDA = new PartyDA();
		partyDA.setCompanyName("Test Party DA");
		userProfile.setPartyDA(partyDA);
		return userProfile;
	}
	
	
	@GET
	@Path("/userProfileData")
	@Produces(MediaType.APPLICATION_JSON)
	public UserProfile getUserProfileData(@Context HttpServletRequest httpServletrequest)throws OnBoardingException {
		//TODO uncomment this before deploying in SSUE. SSUE provide the cco id in the header not as an API.
	
		String ccoId = loginUtil.getLoginUser(httpServletrequest);		
		String originalLoginUser = loginUtil.getLoginOriginalUser(httpServletrequest);
		
		logger.info("::::: validateContractAndSN() ::::::: ccoId = "+ccoId+", originalLoginUser = "+originalLoginUser);
		
		if(StringUtils.isEmpty(ccoId)){
			throw new LoginException("MISSING_CCO_ID");
		}
		UserProfile userProfile = new UserProfile();
		
		userProfile.setCcoId(ccoId);
		
		//instrumentation for production testing - prod user mock 
		//Overriding isTestUser property to true, if logged in user is testuser which defined in testuser.properties
		logger.info("Instrumentation for production testing : Logged in User : "+ccoId+": Is test user "+ userProfile.isTestUser() );
		//logger.info("instrumentation for production testing : Before - isTestUser : "+userProfile.isTestUser());
		
		if(ccoId != null && ccoId.equalsIgnoreCase((String)propertiesUtil.getTestUserProperties().get("test.user"))){
				userProfile.setTestUser(true);
		}
		logger.info("instrumentation for production testing :After - isTestUser : "+userProfile.isTestUser());
		if(epmUserDao.findEpmUser(ccoId) != null){
			userProfile.setEpmUser(true);
		}
		

		//Because DPL FLAG API access can be accessed by SelfServ.gen only
		UserProfileView userProfileType=null;
		logger.info("=========== before getUserProfileForDPLFlag call----------");
		userProfileType = cprUtil.getUserProfileForDPLFlag(ccoId);
      logger.info("=========== After getUserProfileForDPLFlag call----------");
		
		//set account status
		if(userProfileType != null && userProfileType.getUserEntitlementInfo() != null)
		{
		String accountStatus = userProfileType.getUserEntitlementInfo().getAccountStatus();
		userProfile.setAccountStatus(accountStatus);
		logger.info("Account status for ccoid is "+ ccoId + " is "+ accountStatus);
		}
		else
		{			
			logger.info("Couldn't get userprofileType while setting accountstatus");
		}
		if(userProfileType.getEFValidatedPartyID() != null && userProfileType.getEFValidatedPartyID().getPartyID() != null){
			String efValidatedPartyId = userProfileType.getEFValidatedPartyID().getPartyID().getIdentifier();
			userProfile.setEfValidatedPartyId(efValidatedPartyId);
			logger.info("EF Validated Party Id :: "+efValidatedPartyId);
			userProfile.setEfValidatedAccountStatus(aaaClientUtil.isPartyAssociatedToSNTC(userProfile.getEfValidatedPartyId()));
			Company efValidatedComp = new Company();
			PartyProfileView partyProfile =  aaaClientUtil.getPartyProfile(efValidatedPartyId);
			if(partyProfile != null)
			{
				efValidatedComp.setCompanyName(partyProfile.getPartyName().getName());
				efValidatedComp.setAddress(partyProfile.getPrimaryPostalAddress().getStreetAddress().getAddressContent().toString());
				efValidatedComp.setCity(partyProfile.getPrimaryPostalAddress().getCity());
				efValidatedComp.setState(partyProfile.getPrimaryPostalAddress().getState());
				efValidatedComp.setCountry(partyProfile.getPrimaryPostalAddress().getCountry());
				userProfile.setCompany(efValidatedComp);
			}
			else
				logger.info("Couldn't get PartyProfile for party id : "+efValidatedPartyId);
			
		}		
		
		
		//logger.info("EF Validated Status:"+userProfileType.getEFValidatedGUPartyID().getAttributeStatus());
		
		
		//DPL Check attributes
		if(userProfileType != null){
			
			if(userProfileType.getRegistrationInfo() != null && userProfileType.getRegistrationInfo().getDplAddressFlag() != null){
				logger.info("UserProfileType.getRegistrationInfo not NULL");
				userProfile.setDPLAddressFlag(userProfileType.getRegistrationInfo().getDplAddressFlag());
			}else{
				userProfile.setDPLAddressFlag("false");
				
			}
			if(userProfile.getDPLAddressFlag() != null && !userProfile.getDPLAddressFlag().equals("FULL_ADDRESS_VALIDATED") ){
				logger.info("Sending DPL metric for invalid address");
				metricsClientUtil.captureMetricDataWithContext(originalLoginUser,OnBoardingEventNotificationType.SSO_DPL_Check_Failed, " ", " ", new Date(),userProfile.getDPLAddressFlag(), null);
			}
			
			if(ccoId != null && ccoId.equalsIgnoreCase((String)propertiesUtil.getTestUserProperties().get("test.user"))){
				userProfile.setDPLAddressFlag("FULL_ADDRESS_VALIDATED");
		    }
			
			if(userProfileType.getOrgAttributes() != null && userProfileType.getOrgAttributes().getCompanyName() != null)
			{
				userProfile.setCompanyName(userProfileType.getOrgAttributes().getCompanyName());
				logger.info("ComapnyName: "+userProfileType.getOrgAttributes().getCompanyName());
			
			}else{
				userProfile.setCompanyName("Missing CompanyName");
				logger.info("No comapny name");
			}
			//logger.info("DPL Address flag : "+userProfile.getDPLAddressFlag());
		//	logger.info("Company Name : "+userProfile.getCompanyName());
		}
		
		//User has SNTC Access
		if(aaaClientUtil.doesUserhasAccesstoSNTC(ccoId)){
			userProfile.setSntcAccess(true);
			logger.info("**********CCO ID*********"+ccoId+
									"userProfileType.getEFValidatedPartyID():"
										+userProfileType.getEFValidatedPartyID());
			//Call DA service only when customer has EF validated party id
			if(userProfileType.getEFValidatedPartyID() != null && 
					userProfileType.getEFValidatedPartyID().getPartyID() != null){
				PartyDA partyDA = aaaClientUtil.doesUserPartyHasDA(ccoId);
				userProfile.setUserHasDA(partyDA.isDA());
				partyDA.setFirstName("");
				partyDA.setLastName("");
				//setCompanyName("");//making this as empty as we don't want to show this to user.
				userProfile.setPartyDA(partyDA);
				String partyId = userProfileType.getEFValidatedPartyID().getPartyID().getIdentifier();
				setCompanyName(userProfile, partyId);
				
			}else{
				userProfile.setDaRedirectToPortal(true);
				metricsClientUtil.captureMetricData(originalLoginUser,OnBoardingEventNotificationType.SSO_Missing_EF_Validated_Party_ID, "", "", new Date(), null);
			}
		
			userProfile.setSnctPortalRedirectUrl(propertiesUtil.getSnctPortalRedirectUrl());
			setUserProfile(userProfile, userProfileType);
			userProfile.setHasCustomerAdminRole(aaaClientUtil.isAdminRole(ccoId));
			
			logger.info("UserProfile for CCOID:"+ccoId+" "+userProfile.toString());	
			//capturing Metrics
			metricsClientUtil.captureMetricData(originalLoginUser,OnBoardingEventNotificationType.SSO_User_Is_Already_Onboarded_To_SNTC, "", "", new Date(), null);
			return userProfile;
		}else{
			AccessLevel accessLevel = aaaClientUtil.getAccessLevel(userProfileType);
			userProfile.setAccessLevel(accessLevel.name());
			//Populating user profile with first,last,address etc.
			setUserProfile(userProfile, userProfileType);
			//Getting company name
			String companyName = "Missing company name in user profile";
			if(userProfileType != null){
				String partyId = AAAClientUtil.getPartyId(userProfileType);
				System.out.println("************PARTY ID***********"+partyId);
				if(!StringUtils.isEmpty(partyId)){
					try{
					//Setting company name
					companyName = aaaClientUtil.getUserCompany(partyId);
					userProfile.setPartyId(partyId);
					}catch(Exception e){
						logger.error("getUserProfileData-------",e);
					}
				}
				else
				{
					logger.info("CrPartyIdNotFound:"+ccoId);
					//onboardService.sendEmailToCINTeamWithCustomMessage(httpServletrequest, ccoId, null, "crPartyIdNotFound", null, null);
				}
				   
			}
			userProfile.setCompanyName(companyName);
			
			
			//Customer or CBR check email domain
			if((accessLevel == AccessLevel.CUSTOMER || accessLevel == AccessLevel.CBR || accessLevel == AccessLevel.GUEST) && (userProfileType.getRegistrationInfo() != null)){
				
					boolean isBlacklistedDomain = loginUtil.isUserEmailDomainBlackListed(userProfileType.getRegistrationInfo().getEmailAddress());
					if(ccoId != null && ccoId.equalsIgnoreCase((String)(propertiesUtil.getTestUserProperties().get("test.user")))){
						isBlacklistedDomain = false;
					}
					userProfile.setBlackListedEmailDomain(isBlacklistedDomain);
					if(isBlacklistedDomain){
						metricsClientUtil.captureMetricDataWithContext(originalLoginUser, OnBoardingEventNotificationType.SSO_User_Email_Domain_Blacklisted, "", "", new Date(), userProfileType.getRegistrationInfo().getEmailAddress(), null);
					}				
			}

			//Guest check contracts
			if(accessLevel == AccessLevel.GUEST){
				userProfile.setContractNumbers(aaaClientUtil.getContracts(ccoId));
			}
			logger.info("UserProfile for CCOID:"+ccoId+" "+userProfile.toString());			
			//Capturing metrics
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_User_Clicked_On_Register, "", "", new Date(), null);
			return userProfile;
		}
	}

	private void setCompanyName(UserProfile userProfile, String partyId) {
		String companyName = aaaClientUtil.getUserCompany(partyId);
		userProfile.setPartyId(partyId);
		userProfile.setCompanyName(companyName);
	}

	private void setUserProfile(UserProfile userProfile,
			UserProfileView userProfileType) {
		if(userProfileType != null && userProfileType.getRegistrationInfo() != null){
			userProfile.setFirstName(userProfileType.getRegistrationInfo().getFirstName());
			userProfile.setLastName(userProfileType.getRegistrationInfo().getLastName());
			userProfile.setEmail(userProfileType.getRegistrationInfo().getEmailAddress());
			if(userProfileType.getRegistrationInfo().getLocations() != null && 
					userProfileType.getRegistrationInfo().getLocations().getCommunicationAddress() != null){
				userProfile.setPhoneNumber(userProfileType.getRegistrationInfo().getLocations().
						getCommunicationAddress().getDefaultTelePhone().getTelephoneNumberWithCountryCode());
			}
			if(userProfileType.getRegistrationInfo().getLocations() != null && 
					userProfileType.getRegistrationInfo().getLocations().getPostalAddresses() != null &&
							userProfileType.getRegistrationInfo().getLocations().getPostalAddresses().getPrimaryPostalAddress() != null) {
				userProfile.setCountry(userProfileType.
						getRegistrationInfo().getLocations().getPostalAddresses().
						getPrimaryPostalAddress().getCountry());
			}
		}
	}

	/**
	 * 
	 * @param httpServletrequest
	 * @param paramSurveyAnswer
	 * @param paramPartnerName
	 * @return
	 * @throws OnBoardingException
	 */
	@GET
	@Path("/capturePartnerMetrics/{cn}/{sn}/{surveyAnswer}/{partnername}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getPartnerList(@Context HttpServletRequest httpServletRequest, @PathParam("cn") String paramCN,
			@PathParam("sn") String paramSN, @PathParam("surveyAnswer") String paramSurveyAnswer,
			@PathParam("partnername") String paramPartnerName) throws OnBoardingException {
		String returnString = "failure";
		String loggedInCustomer = loginUtil.getLoginOriginalUser(httpServletRequest);
		
		logger.info("::::: getPartnerList() ::::::: loggedInCustomer = "+loggedInCustomer);
		
		Date dateObject = new Date();
		if(null != paramSurveyAnswer && "yes".equals(paramSurveyAnswer.toLowerCase())){
			metricsClientUtil.captureMetricDataWithContext(loggedInCustomer,OnBoardingEventNotificationType.SSO_Partner_Details_Captured, paramCN, paramSN,dateObject,paramPartnerName, null);
			logger.info("Partner details captured with <<" + loggedInCustomer + " Partner Name <<" + paramPartnerName + " surveyAnswer" + paramSurveyAnswer);
			returnString = "success";
		}else{
			metricsClientUtil.captureMetricDataWithContext(loggedInCustomer,OnBoardingEventNotificationType.SSO_Partner_Details_Not_Captured, paramCN, paramSN,dateObject,"", null);
			logger.info("Partner details not captured with  <<" + loggedInCustomer + " Partner Name <<" + paramPartnerName + " surveyAnswer" + paramSurveyAnswer);
			returnString = "success";
		}
		return returnString;
	}
	
	@GET
	@Path("/validateContractAndSN/{contract}/{sn}")
	@Produces(MediaType.APPLICATION_JSON)
	public ContractStatus validateContractAndSN(@Context HttpServletRequest httpServletrequest
			,@PathParam("contract") String contract,
			@PathParam("sn") String sn) throws OnBoardingException{
		
		String ccoId = loginUtil.getLoginUser(httpServletrequest);
		String originalLoginUser = loginUtil.getLoginOriginalUser(httpServletrequest);
		  
		System.out.println("<<<<<<<<<<<<<----------validateContractAndSN------------>>>>>>>>>>");
		logger.info("::::: validateContractAndSN() ::::::: ccoId = "+ccoId+", originalLoginUser = "+originalLoginUser+", contract = "+contract+", sn = "+sn);
		Date now = new Date();	
		if(StringUtils.isEmpty(contract)) {
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Missing_Contract_Number, "", sn, now, null);
			throw new OnBoardingException("Missing contract number");
		}
		if(StringUtils.isEmpty(sn)) {
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Missing_Serial_Number, contract, "", now, null);
			throw new OnBoardingException("Missing Serial number");
		}
		

			try {
				
				ContractStatus contractStatus = new ContractStatus();
				if(aaaClientUtil.isContractInProfile(ccoId, contract)){
					contractStatus.setIsContractInProfile("Yes");
					if(aaaClientUtil.isContractInProfileAndActive(ccoId, contract)){
						contractStatus.setValidContract("Yes");
						userContractDao.insertUserContract(ccoId, contract);
						if(aaaClientUtil.isSNInContract(sn,contract,false)) {
							contractStatus.setValidSN("Yes");
							metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Successfully_Verified_Contract_And_Serial_Number, contract, sn, now, null);
						}else if(aaaClientUtil.isSNInContract(sn,contract,true)){//if expired serial number we should send a different flag so that UI can display different message
							contractStatus.setSnExpired("Yes");
						}else {
							metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Invalid_Serial_Number, contract, sn, now, null);
						}
					}else{
						//TODO add another event which says contract in profile and not active
//						OnBoardingEventNotificationType.SSO_Valid_Contract_Number_NotActive
						metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Invalid_Contract_Number, contract, sn, now, null);
					}
				}
				else {
					metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Invalid_Contract_Number, contract, sn, now, null);
				}
				
				return contractStatus;
			}
			catch (Exception e) {
				logger.error("validateContractAndSN-------",e);
				throw new OnBoardingException("Invalid Serial Number");
			}
	}
	
	
	@GET
	@Path("/sendEmail")
	@Produces(MediaType.APPLICATION_JSON)
	public EmailStatus sendNotification(@Context HttpServletRequest httpServletrequest) throws OnBoardingException{
		EmailStatus emailStatus = new EmailStatus();
		String ccoId = loginUtil.getLoginUser(httpServletrequest);
		String originalLoginUser = loginUtil.getLoginOriginalUser(httpServletrequest);
		if(StringUtils.isEmpty(ccoId)){
			System.out.println("---------Missing cco id in the request --------------");
			throw new OnBoardingException("Missing user profile");
		}
		Date now = new Date();
		try {
			System.out.println("-------sendEmail REQUEST --------------"+ccoId);
			notificationUtil.sendNotification(ccoId,LoginUtil.getLocale(httpServletrequest));
			emailStatus.setStatus("success");
			emailStatus.setMessage("OK");
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Address_Verification_Email_Sent, "", "", now, null);
		} catch (Exception e) {
			logger.error("-----sendNotification-------", e);
			emailStatus.setStatus("failure");
			emailStatus.setMessage(e.getMessage());
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Failed_To_Send_Address_Verification_Email, "", "", now, null);
		}
		return emailStatus;
	}
	//Writing a rest service for sending daily report
	@GET
	@Path("/sendDailyreport")
	@Produces("text/plain")
	public String sendDailyReport(@Context HttpServletRequest httpServletrequest) throws OnBoardingException{
		try{
		
		sso_scheadulars.demoServiceMethodForTesting();
		}catch(Exception e){
			logger.error("-----sendDailyReport-------", e);
			
			return "False";
		}
		return "True";
		
	}
	
	@GET
	@Path("/nominateTestDA/{partyId}")
	@Produces(MediaType.APPLICATION_JSON)
	public StatusResult nominateTestDA(@Context HttpServletRequest httpServletrequest,
			@PathParam("partyId") String partyId) throws OnBoardingException{
		String ccoId = loginUtil.getLoginUser(httpServletrequest);
		return cprUtil.nominateDA(ccoId, partyId);
	}
	
	@GET
	@Path("/nominateDA")
	@Produces(MediaType.APPLICATION_JSON)
	public DANominationStatus nominateDA(@Context HttpServletRequest httpServletrequest) throws OnBoardingException{
		DANominationStatus daNominationStatus = new DANominationStatus();
		
		String ccoId = loginUtil.getLoginUser(httpServletrequest);
		String originalLoginUser = loginUtil.getLoginOriginalUser(httpServletrequest);
		if(StringUtils.isEmpty(ccoId)){
			System.out.println("---------Missing cco id in the request --------------");
			throw new OnBoardingException("Missing user profile");
		}
		metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Nominate_DA_Clicked, "", "", new Date(), null);
		try {
			daNominationStatus = cprUtil.nominateDA(ccoId);
		} catch (Exception e) {
			logger.info("context", e);
			daNominationStatus.setMessage("Failure");
			daNominationStatus.setErrorMessage(e.getMessage());
		}
		logger.info("$$$$$$$$$$$$$$$$START DA NOMINATE USER$$$$$$$$$$$$"+daConfiguredCcoId+":"+ccoId);
		//for configured test users we say it is success
		if(daConfiguredCcoId.equals(ccoId)){
			daNominationStatus.setNominated(true);
		}
		if(!daNominationStatus.isNominated() ){ 
			//IF DA Nomination fails then don't send emails. Capture the metrics
			Date now = new Date();
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_DA_Nomination_Rejected_By_EF, "", "", now, null);
			
			notificationUtil.sendCustomMessage(ccoId,propertiesUtil.getDebugEmailAlais(),
					getDAErrorMessage(ccoId,daNominationStatus), 
					"DA Nomination for existing SNTC Customer error","Dear Support");
			return daNominationStatus;
		}
		logger.info("$$$$$$$$$$$$$$$$END DA NOMINATE USER$$$$$$$$$$$$"+daConfiguredCcoId+":"+ccoId);
		Date now = new Date();
		try {
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_DA_Nomination_Successful, "", "", now, null);
			//Sending email after nominating as DA
			UserProfileView userProfileType = aaaClientUtil.getUserProfile(ccoId);
			String email = null;
			String fromUserString = "Dear Customer";
			if(userProfileType != null && userProfileType.getRegistrationInfo() != null){
				email = userProfileType.getRegistrationInfo().getEmailAddress();
				fromUserString = "Dear "+userProfileType.getRegistrationInfo().getFirstName()+" "+
						userProfileType.getRegistrationInfo().getLastName();
			}
			logger.info("$$$$$$$$$$$$$$$$DA EAMIL$$$$$$$$$$$"+email);
			if(StringUtils.isEmpty(email)){
				metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Failed_To_Send_DA_Confirmation_Email, "", "", now, null);
			}
			Properties resourceBundle = propertiesUtil.getResourceBundle("en");
			String subject = resourceBundle.getProperty("notification.msg.da.subject");
			
			StringBuilder sb= new StringBuilder();
			String message1 = resourceBundle.getProperty("notification.msg.da.nomination");
			sb.append(message1).append("<br/><br/>");
			String message2 = resourceBundle.getProperty("notification.msg.da.nomination1");
			sb.append(message2).append("<br/>");
			
			sb.append(resourceBundle.getProperty("notification.da.hyp1")).append("<br/>");
			sb.append(resourceBundle.getProperty("notification.da.hyp2")).append("<br/>");
			sb.append(resourceBundle.getProperty("notification.da.hyp3")).append("<br/><br/>");
			sb.append(resourceBundle.getProperty("notification.da.hyp4")).append("<br/><br/>");
			
			String sincerly = resourceBundle.getProperty("notification.msg.end");
			String footer = resourceBundle.getProperty("notification.from.footer");
			sb.append(sincerly).append("<br/>").append(footer);
			
			logger.info("$$$$$$$$$$$$$$$$MESSAGE$$$$$$$$$$$"+sb.toString());
			
			notificationUtil.sendCustomMessage(ccoId,email, sb.toString(), subject,fromUserString);
			//Sending email to support team.
			//DA Nomination for existing SNTC Customer successful
			notificationUtil.sendCustomMessage(ccoId,propertiesUtil.getMetricsEmailAlias(),getDAMessage(ccoId),
					"DA Nomination for existing SNTC Customer successful",fromUserString);
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Successfully_Sent_DA_Confirmation_Email, "", "", now, null);
			
		} catch (Exception e) {
			logger.error("-----sendNotification-------", e);
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Failed_To_Send_DA_Confirmation_Email, "", "", now, null);
		}
		return daNominationStatus;
	}
	
	
/*	@GET
	@Path("/sendEmailToUserAndDA/{da_fname}/{da_lname}/{da_email}/{da_cmpName}/{da_cmpAddress}/{da_city}/{da_state}/{da_country}")
	@Produces(MediaType.APPLICATION_JSON) 
	public String sendEmailToUserAndDA(@Context HttpServletRequest httpServletrequest,@PathParam("da_fname") String da_fname,
			@PathParam("da_lname") String da_lname,@PathParam("da_email") String da_email,
			@PathParam("da_cmpName") String da_cmpName,
			@PathParam("da_cmpAddress") String da_cmpAddress,
			@PathParam("da_city") String da_city,
			@PathParam("da_state") String da_state,
			@PathParam("da_country") String da_country) throws OnBoardingException */
	@POST
	@Path("/sendEmailToUserAndDA")
	@Consumes(MediaType.APPLICATION_JSON)
	public String sendEmailToUserAndDA(@Context HttpServletRequest httpServletrequest, @RequestBody Company company) throws OnBoardingException{
		
		//CUSTOMER CCO ID
		String ccoId = loginUtil.getLoginUser(httpServletrequest);
		
		//LOGIN TEST USER ID
		String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
		if(StringUtils.isEmpty(ccoId)){
			throw new LoginException("MISSING_CCO_ID");
		}
		if(company!=null){
			System.out.println("User Has DA true");
		}
		else
		{
			System.out.println("Empty company detail");
			return "false";
		}
		String da_fname=company.getDAFirstName();
		String da_lname=company.getDALastName();
		String da_email=company.getDAEmailAddress();
		String da_cmpName=company.getCompanyName();
		String da_cmpAddress=company.getAddress();
		String da_state=company.getState();
		String da_country=company.getCountry();
		String da_city=company.getCity();
		
		//IF LOGGED IN USER IS TEST USER THEN NO NOTIFICATIONS AT ALL.
	/*	if(!StringUtils.isEmpty(originalLoginUser) && propertiesUtil.getTestUserProperties().get("test.user").equals(originalLoginUser)){
			return "true";
		} */		
			
			
		String status="false";
		String userStatus=sendEmailToUser(ccoId,da_fname,da_lname,da_email,da_cmpName,da_cmpAddress,da_city,da_state,da_country);
		String DAStatus=sendEmailToDA(ccoId,da_fname,da_lname,da_email);
		if("success".equals(userStatus) && "success".equals(DAStatus)){
			UserProfileView userProfileType = aaaClientUtil.getUserProfile(ccoId);
			String userEmail= userProfileType.getRegistrationInfo().getEmailAddress();
			metricsClientUtil.captureMetricData(originalLoginUser,OnBoardingEventNotificationType.SSO_Email_Successfully_Sent_To_Registering_User, userEmail, "", new Date(), null);
			metricsClientUtil.captureMetricData(originalLoginUser,OnBoardingEventNotificationType.SSO_Email_Successfully_Sent_To_Existing_DA, da_email, da_fname+" "+da_lname, new Date(), null);
			return "true";
		}
		return status;
	}
	
	private String sendEmailToUser(String ccoId, String DAFirstName,String DALastName, String DAEmail,
			String da_cmpName, String da_cmpAddress, String da_city, String da_state, String da_country){
		//Sending email to user
		try {
		UserProfileView userProfileType = aaaClientUtil.getUserProfile(ccoId);
		String email = null;
		String fromUserString = "Dear Customer";
		if(userProfileType != null && userProfileType.getRegistrationInfo() != null){
			email = userProfileType.getRegistrationInfo().getEmailAddress();
			fromUserString = "Dear "+userProfileType.getRegistrationInfo().getFirstName()+" "+
					userProfileType.getRegistrationInfo().getLastName()+"";
		}
		
		System.out.println("$$$$$$$$$$$$$$$$USER EAMIL$$$$$$$$$$$"+email);
		Properties resourceBundle = propertiesUtil.getResourceBundle("en");
		String subject = userProfileType.getRegistrationInfo().getFirstName()+" "+userProfileType.getRegistrationInfo().getLastName()+", "+resourceBundle.getProperty("notification.user.da.msg.subject");
		
		StringBuilder sb = new StringBuilder();
		String message1 = resourceBundle.getProperty("notification.user.da.msg.line1");
		sb.append(message1).append("<br/><br/>");
		String message2 = resourceBundle.getProperty("notification.user.da.msg.line2");
		sb.append(message2).append("<br/><br/>");
		// insert DA firstname last name
		sb.append(DAFirstName).append(" ").append(DALastName).append("<br/>");
		// insert DA email - cmpName
		
		//Attaching DAEmail only if domain of DA email and User email are same
		if(email.split("@")[1].equalsIgnoreCase(DAEmail.split("@")[1]))
		{
		 sb.append(DAEmail).append("<br/>");
		 sb.append(da_cmpName).append("<br/>");
		 sb.append(da_cmpAddress).append("<br/>").append(da_city+"   "+da_state+"   "+da_country);
		}
		else
		{
		 logger.info("Email domains are not matching"); 
		 sb.append(da_cmpName).append("<br/>");
		}
		
		//sb.append(da_cmpAddress).append("<br/>").append(da_city+"   "+da_state+"   "+da_country)
		sb.append("<br/>");
		
		sb.append(resourceBundle.getProperty("notification.user.da.msg.line3")).append("<br/><br/>");
		sb.append(resourceBundle.getProperty("notification.user.da.msg.line4")).append("<br/><br/>");
		
		String sincerly = resourceBundle.getProperty("notification.user.da.msg.end1");
		String footer = resourceBundle.getProperty("notification.user.da.msg.end2");
		sb.append(sincerly).append("<br/>").append(footer);
		
		System.out.println("$$$$$$$$$$$$$$$$MESSAGE$$$$$$$$$$$"+sb.toString());
		
		notificationUtil.sendCustomMessage(ccoId,email, sb.toString(), subject,fromUserString);
		
		}catch (Exception e) {
			logger.error("-----sendNotification-------", e);
			return "failure";
		}
		return "success";
	}
	
	private String sendEmailToDA(String ccoId, String DAFirstName,String DALastName, String DAEmail){
		//Sending email to DA
		try {
			UserProfileView userProfileType = aaaClientUtil.getUserProfile(ccoId);
			String da_email=DAEmail;
			String email = null;
			String emailMatch=" ";
			String fromUserString = "Dear Customer";
			if(userProfileType != null && userProfileType.getRegistrationInfo() != null){
				email = userProfileType.getRegistrationInfo().getEmailAddress();
				fromUserString = "Dear "+DAFirstName+" "+DALastName+"";
			}
			System.out.println("$$$$$$$$$$$$$$$$TO DA EAMIL$$$$$$$$$$$"+email);
			Properties resourceBundle = propertiesUtil.getResourceBundle("en");
			String subject = resourceBundle.getProperty("notification.already.da.msg.subject");
			
			StringBuilder sb = new StringBuilder();
			String userFirstNameLastName=userProfileType.getRegistrationInfo().getFirstName()+" "+userProfileType.getRegistrationInfo().getLastName();
			String userEmail=email;
			String userCCOID=userProfileType.getRegistrationInfo().getCCOID();
			//String message1 = userFirstNameLastName+" ("+userEmail+") "+resourceBundle.getProperty("notification.already.da.msg.line1");
			
			//Attach userEmail if the domains are same
			if(userEmail.split("@")[1].equalsIgnoreCase(DAEmail.split("@")[1])) {
			 emailMatch=" ("+userEmail+", CCOID: "+userCCOID+") ";
			 logger.info("User CCOID=========> "+ userCCOID);
			}
			else {
				logger.info("Email domains are not matching "+ emailMatch);
				emailMatch=" (CCOID: "+userCCOID+") ";
			}
			String message1 = userFirstNameLastName+emailMatch+resourceBundle.getProperty("notification.already.da.msg.line1");
			sb.append(message1).append("<br/>").append(resourceBundle.getProperty("notification.already.da.msg.line1.1")).append("<br/><br/>");
			String message2 = resourceBundle.getProperty("notification.already.da.msg.line2.1");
			sb.append(message2).append("<br/><br/>");
			sb.append(resourceBundle.getProperty("notification.already.da.msg.line2.2")).append("<br/><br/>");	
			
			sb.append(resourceBundle.getProperty("notification.already.da.msg.line2.3")).append("<br/>");
			sb.append(resourceBundle.getProperty("notification.already.da.msg.line3")).append("<br/>");
			sb.append(resourceBundle.getProperty("notification.already.da.msg.line4")).append("<br/>");
			sb.append(resourceBundle.getProperty("notification.already.da.msg.line5")).append("<br/><br/>");
			
			sb.append(resourceBundle.getProperty("notification.already.da.msg.line13")).append("<br/>");
			sb.append(resourceBundle.getProperty("notification.already.da.msg.line14")).append("<br/>");
			sb.append(resourceBundle.getProperty("notification.already.da.msg.line15")).append("<br/>");
			sb.append(resourceBundle.getProperty("notification.already.da.msg.line16")).append("<br/><br/>");
						
			sb.append(resourceBundle.getProperty("notification.already.da.msg.line6")).append("<br/>");
			sb.append(resourceBundle.getProperty("notification.already.da.msg.line7")).append("<br/>");
			sb.append(resourceBundle.getProperty("notification.already.da.msg.line9")).append("<br/><br/>");
			
			sb.append(resourceBundle.getProperty("notification.already.da.msg.line11")).append("<br/><br/>");
			
			String sincerly = resourceBundle.getProperty("notification.already.da.msg.end1");
			String footer = resourceBundle.getProperty("notification.already.da.msg.end2");
			sb.append(sincerly).append("<br/>").append(footer);
			
			System.out.println("$$$$$$$$$$$$$$$$MESSAGE TO DA$$$$$$$$$$$"+sb.toString());
			//TODO adding to for testing purpose. It will not harm in the production
			if(propertiesUtil.getContractProperties().containsKey(ccoId)){
				da_email = "samtperformancetest17@yopmail.com";
			}
		
		notificationUtil.sendCustomMessage(ccoId, da_email, sb.toString(), subject,fromUserString);
		}catch (Exception e) {
			logger.error("-----sendNotification-------", e);
			return "failure";
		}
		return "success";
	}
	
	
	@GET
	@Path("/clickSignIn")
	public String clickSignIn(@Context HttpServletRequest request,@Context HttpServletResponse response) throws OnBoardingException{
		try {
			String originalLoginUser =loginUtil.getLoginOriginalUser(request);
			Date now = new Date();
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_User_Clicked_On_SignIn, "", "", now, null);
			response.sendRedirect(propertiesUtil.getClickSignInUrl());
		} catch (IOException e) {
			logger.error("-----goMobile-------", e);
			System.err.println("Go Mobile Error:"+e.getMessage());
		}
		return "SUCCESS";
	}	

	
	@GET
	@Path("/clickCSAMLink")
	public String clickCSAMLink(@Context HttpServletRequest request,@Context HttpServletResponse response) throws OnBoardingException{
		try {
			String originalLoginUser =loginUtil.getLoginOriginalUser(request);
			Date now = new Date();
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_User_Clicked_On_CSAM_Link, "", "", now, null);
			response.sendRedirect(propertiesUtil.getClickCSAMLinkUrl());
		} catch (IOException e) {
			logger.error("-----goMobile-------", e);
			System.err.println("Go Mobile Error:"+e.getMessage());
		}
		return "SUCCESS";
	}	
	
	@GET
	@Path("/clickCommunityLink")
	public String clickCommunityLink(@Context HttpServletRequest request,@Context HttpServletResponse response) throws OnBoardingException{
		try {
			String originalLoginUser =loginUtil.getLoginOriginalUser(request);
			Date now = new Date();
			metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_User_Clicked_On_Community_Link, "", "", now, null);
			response.sendRedirect(propertiesUtil.getClickCommunityLinkUrl());
		} catch (IOException e) {
			logger.error("-----goMobile-------", e);
			System.err.println("Go Mobile Error:"+e.getMessage());
		}
		return "SUCCESS";
	}
	
	@GET
	@Path("/adminUI")
	public String adminUI(@Context HttpServletRequest request,@Context HttpServletResponse response) throws OnBoardingException{
		try {
			String originalLoginUser =loginUtil.getLoginOriginalUser(request);
			Date now = new Date();
			System.out.println("AdminUI page Login By: "+originalLoginUser+" Date :"+now);
			response.sendRedirect(propertiesUtil.getAdminUILink());
		} catch (IOException e) {
			logger.error("-----goMobile-------", e);
			System.err.println("Go Mobile Error:"+e.getMessage());
		}
		return "SUCCESS";
	}
	
	@GET
	@Path("/becomeDA")
	public String becomeDA(@Context HttpServletRequest request) throws OnBoardingException{
		String originalLoginUser =loginUtil.getLoginOriginalUser(request);
		Date now = new Date();
		metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_User_Clicked_Continue_Registration_On_Overview_Page, "", "", now, null);
		return "SUCCESS";
	}
	
	
	@GET
	@Path("/partyHasDA/{partyId}/")
	@Produces(MediaType.APPLICATION_JSON)
	public PartyDA partyHasDA(@Context HttpServletRequest request,@PathParam("partyId") String partyId) throws OnBoardingException{
		if(StringUtils.isEmpty(partyId)){
			throw new OnBoardingException("Missing party id in the request");
		}
		return aaaClientUtil.doesPartyHasDA(partyId);
	}
	
	@GET
	@Path("/userHasDA")
	@Produces(MediaType.APPLICATION_JSON)
	public PartyDA userHasDA(@Context HttpServletRequest request) throws OnBoardingException{
		String ccoId = loginUtil.getLoginUser(request);
		if(StringUtils.isEmpty(ccoId)){
			throw new OnBoardingException("Missing cco id in the request");
		}
		return aaaClientUtil.doesUserPartyHasDA(ccoId);
	}
	
	@GET
	@Path("/verifyEmail/{uId}")
	@Produces(MediaType.APPLICATION_JSON)
	public EmailStatus verifyEmail(@Context HttpServletRequest httpServletrequest, @PathParam("uId") String uId) throws OnBoardingException{
		
		EmailStatus emailStatus = new EmailStatus();
		//String ccoId = loginUtil.getLoginUser(httpServletrequest);
		String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
		Date now = new Date();
		if(StringUtils.isEmpty(uId)){
			throw new OnBoardingException("Missing Unique Id");
		}
		/*
		
		 * 1) IF any error in any of the case send error message to internal support team NO CIN team 
		 */
		try{
		UserProfileView  userProfileType = aaaClientUtil.getUserProfile(originalLoginUser);
		 if(userProfileType != null && userProfileType.getRegistrationInfo() != null){
			 emailStatus.setFirstName(userProfileType.getRegistrationInfo().getFirstName());
			 emailStatus.setLastName(userProfileType.getRegistrationInfo().getLastName());
		 }
		}catch (Exception e) {
			System.err.println("*********verifyEmail*********"+e.getMessage());
			logger.error("verifyEmail", e);
			emailStatus.setStatus("failure");
			emailStatus.setMessage(e.getMessage());
			onboardService.sendEmailToDebugAlais(httpServletrequest,e.getMessage());
		}
		 // hack fix - 
		if(notificationUtil.isVisited(originalLoginUser, uId)) {
			 emailStatus.setStatus("expired");
			 emailStatus.setMessage("OK");
			return emailStatus;
		}
		metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_User_Clicked_On_Link_In_Verfication_Email, "", "", now, null);

		try {
			 
			if(notificationUtil.verifyEmail(originalLoginUser, uId)) {
				emailStatus.setStatus("expired");
				metricsClientUtil.captureMetricData(originalLoginUser, OnBoardingEventNotificationType.SSO_Link_In_Verfication_Email_Has_Expired, "", "", now, null);
			}else{
				 emailStatus.setStatus("success");
				 emailStatus.setMessage("OK");
			}
		}
		catch (Exception e) {
			System.err.println("*********verifyEmail*********"+e.getMessage());
			logger.error("verifyEmail", e);
			emailStatus.setStatus("failure");
			emailStatus.setMessage(e.getMessage());
			onboardService.sendEmailToDebugAlais(httpServletrequest,e.getMessage());
		}
		return emailStatus;
	}

	private String getDAErrorMessage(String ccoId,DANominationStatus nominationStatus){
		StringBuilder sb = new StringBuilder();
		sb.append(getDAMessage(ccoId));
		sb.append("<br/>");
		sb.append(nominationStatus.toString());
		return sb.toString();
	}

	private String getDAMessage(String ccoId) {
		StringBuilder sb = new StringBuilder();
		sb.append("CCO ID:").append(ccoId).append("<br/>");
		try {
			sb.append("SNTC ACCESS:").append(aaaClientUtil.doesUserhasAccesstoSNTC(ccoId)).append("<br/>");
			UserProfileView userProfileType =  aaaClientUtil.getUserProfile(ccoId);
			if(userProfileType != null && userProfileType.getRegistrationInfo() != null){
				sb.append("First Name:").append(userProfileType.getRegistrationInfo().getFirstName()).append("<br/>");
				sb.append("Last Name:").append(userProfileType.getRegistrationInfo().getLastName()).append("<br/>");
				sb.append("Email:").append(userProfileType.getRegistrationInfo().getEmailAddress()).append("<br/>");
				String partyId = AAAClientUtil.getPartyId(userProfileType);
				sb.append("PartyId:").append(partyId).append("<br/>");
				if(!StringUtils.isEmpty(partyId)){
					try{
						//Setting company name
						sb.append("Company Name:").append(aaaClientUtil.getUserCompany(partyId)).append("<br/>");
					}catch(Exception e){
						logger.error("getUserProfileData-------",e);
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	/*
	 * page: Confirm Forward to DA Page
	 * Event: Clicked "Back Button" 
	   Action :	Capture Metric	 
	 */	
	@GET
	@Path("/confirmForwardDA/clickedBack")
	public String saveMetric(@Context HttpServletRequest httpServletrequest) {		
		try
		{			
			String originalLoginUser =loginUtil.getLoginOriginalUser(httpServletrequest);
			metricsClientUtil.captureMetricDataWithContext(originalLoginUser, OnBoardingEventNotificationType.SSO_Back_Button_Clicked_On_Request_DA_Form, "", "", new Date(), "Back button clicked on Confirm Forward DA page", "");

		} catch (LoginException e) {
			logger.error("Exception occurred while capturing metrics for Back button clicked on confirm forward to DA page.");
		}
		return "SUCCESS";
	}
	
}
