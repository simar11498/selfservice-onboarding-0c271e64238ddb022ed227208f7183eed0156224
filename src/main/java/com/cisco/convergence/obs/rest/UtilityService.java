package com.cisco.convergence.obs.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
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
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import com.cisco.ata.rest.UserProfileView;
import com.cisco.convergence.obs.exception.LoginException;
import com.cisco.convergence.obs.exception.OnBoardingException;
import com.cisco.convergence.obs.jpa.dao.UserContractDao;
import com.cisco.convergence.obs.model.AccessLevel;
import com.cisco.convergence.obs.model.Company;
import com.cisco.convergence.obs.model.EventByAgent;
import com.cisco.convergence.obs.model.PartyDA;
import com.cisco.convergence.obs.model.UPPInfo;
import com.cisco.convergence.obs.model.UserProfile;
import com.cisco.convergence.obs.notification.model.EmailMember;
import com.cisco.convergence.obs.rest.metricsCollection.OnBoardingEventNotificationType;
import com.cisco.convergence.obs.util.AAAClientUtil;
import com.cisco.convergence.obs.util.LoginUtil;
import com.cisco.convergence.obs.util.MetricsClientUtil;
import com.cisco.convergence.obs.util.PropertiesUtil;
import com.cisco.convergence.obs.util.ValidateMethodUtil;
import com.cisco.cstg.ssue.util.HttpUtil;
import com.cisco.cstg.ssue.util.HttpUtilException;




@Service
@Path("/tool")
public class UtilityService {

	private Logger logger = Logger.getLogger(this.getClass());

	@Inject
	private AAAClientUtil aaaClientUtil;

	@Inject
	UserContractDao userContractDao;
	
	@Inject
	private LoginUtil loginUtil;

	@Inject
	private PropertiesUtil propertiesUtil;
	
	@Inject
	private ValidateMethodUtil validateMethodUtil;
	
	@Inject
	private MetricsClientUtil metricsClientUtil;
	/*@Inject
	private EpmUserDao dao;*/

	@GET
	@Path("/testService")
	@Produces("text/plain")
	public String test() {
		// Return some cliched textual content
		return "On-boarding Admin Tool";
	}
	// MYCODECHANGES starts	
	@GET
	@Path("/checkAdminDetails/{ccoId}/{customerid}")
	@Produces("text/plain")
	public String checkAdminDetails(@Context HttpServletRequest httpServletrequest,
			@PathParam("ccoId") String ccoId,@PathParam("customerid") String customerid) throws OnBoardingException {
		// Return some clicked textual content
		String exists=userContractDao.checkAAAcache(ccoId,customerid);
		if(exists.equals("true"))
			return "true";
		else if(exists.equals("false"))
			return  "false";
		else
			return "error";

	}
	
	@GET
	@Path("/checkPartyDetailsInAAA/{ccoId}/{customerid}")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject checkPartyDetailsInAAA(@Context HttpServletRequest httpServletrequest,
			@PathParam("ccoId") String ccoId,@PathParam("customerid") String customerid) throws OnBoardingException {
		// Return some clicked textual content
		try{
		JSONObject obj = new JSONObject();
		String PartyInSync=userContractDao.checkPartyInAAAcache(ccoId,customerid);
		obj.put("PartyInSync", PartyInSync);
		String CcoIdInSync="false";
		if(PartyInSync.equals("true"))
		{
			
			CcoIdInSync=userContractDao.checkAAAcache(ccoId,customerid);
			obj.put("CcoIdInSync", CcoIdInSync);
			
			return obj; //200
    				
		}
		else if(PartyInSync.equals("false"))
			{
			obj.put("CcoIdInSync", "false");
			return obj;
			}
		else
		{
			obj.put("CcoIdInSync", "error");
			obj.put("PartyInSync", "error");
			return obj; 
		}
		}catch(Exception e){
			System.out.println("--json response error--"+e.getMessage());
			return null;
		}

	}
	
	@GET
	@Path("/sendRequestToAAA/{ccoId}/{customerid}")
	@Produces("text/plain")
	public String sendRequestToAAA(@Context HttpServletRequest httpServletrequest,
			@PathParam("ccoId") String ccoId,@PathParam("customerid") String customerid) throws OnBoardingException {
		// Return some clicked textual content
		String exists=userContractDao.checkAAAcache(ccoId,customerid);
		if(exists.equals("true"))
			return "ALREADY_EXISTS";
		else if(exists.equals("false"))
		{
			try {
				
				// Rest Template
				String url =propertiesUtil.getAAACacheUrlWithPriority();
				//String url="http://v01app-dev019.cisco.com:8611/FactoryServices-dev019/checkerSvc/addCustomer";			 
				MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
				map.add("partyId", customerid);
				map.add("event", "ADD");  
				  			
				RestTemplate restTemplate = new RestTemplate();
			    ResponseEntity<String> result = restTemplate.postForEntity(url, map, String.class);		    		
			     
			    System.out.println(result.getBody().toString());
			    return result.getBody().toString();
			    
			} catch (Exception e) {
				logger.error("An exception while adding sending request to AAA Cache", e);
				throw new OnBoardingException(e.getMessage());
			}
			
		}
		else
			return "error";

	}
	// MYCODECHANGES ends
	@GET
	@Path("/userName")
	@Produces(MediaType.APPLICATION_JSON)
	public UserProfile getUserProfileData(@Context HttpServletRequest httpServletrequest)throws OnBoardingException {
		//TODO uncomment this before deploying in SSUE. SSUE provide the cco id in the header not as an API.
		String ccoId = loginUtil.getLoginUser(httpServletrequest);
		if(StringUtils.isEmpty(ccoId)){
			throw new LoginException("MISSING_CCO_ID");
		}
		UserProfile userProfile = new UserProfile();
	
		//Capture access level
		UserProfileView userProfileType = aaaClientUtil.getUserProfile(ccoId);
		if(userProfileType !=  null && userProfileType.getRegistrationInfo() != null){
			userProfile.setFirstName(userProfileType.getRegistrationInfo().getFirstName());
			userProfile.setLastName(userProfileType.getRegistrationInfo().getLastName());
		}
		return userProfile;
	}
	
			
	@GET
	@Path("/userProfileInfo/{ccoId}")
	@Produces(MediaType.APPLICATION_JSON)
	public UPPInfo userProfileInfo(
			@Context HttpServletRequest httpServletrequest,
			@Context HttpServletResponse response,
			@PathParam("ccoId") String ccoId) throws OnBoardingException {
		if (StringUtils.isEmpty(ccoId)) {
			throw new OnBoardingException("Missing customer cco id");
		}

		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods",
				"GET, POST, DELETE, PUT");
		response.addHeader("Access-Control-Allow-Headers",
				"X-Requested-With, Content-Type, X-Codingpedia");

		UPPInfo uppInfo = new UPPInfo();
		
		//SA - Fix
		//String partyId="";

		// First check if user has correct access level and subscribed to the email alias.
		String loggedInUser = loginUtil.getLoginUser(httpServletrequest);
		String originalLoginUser = loginUtil.getLoginOriginalUser(httpServletrequest);
		UserProfileView loggedInUserProfileType = aaaClientUtil.getUserProfile(loggedInUser);
	
		if (loggedInUserProfileType == null) {
			return uppInfo;
		}
		
		AccessLevel loggedInUserAccessLevel = aaaClientUtil.getAccessLevel(loggedInUserProfileType);
		
		// If user is not internal and not member of the look-up group then don't get DA info 
		// MOCKEDTHINGS mocking for test.user 
		if (!doesUserHasAccess(httpServletrequest) && !originalLoginUser.equals((String)propertiesUtil.getTestUserProperties().get("test.user"))) {
			uppInfo.setMessage("Access Denied");
			return uppInfo;
		}

		try {
			uppInfo.setCcoId(ccoId);
			UserProfileView userProfileType = aaaClientUtil.getUserProfile(ccoId);
			setUserProfile(uppInfo, userProfileType);
			if (userProfileType != null && userProfileType.getRegistrationInfo() != null) {
				boolean isBlacklistedDomain = loginUtil.isUserEmailDomainBlackListed
						(userProfileType.getRegistrationInfo().getEmailAddress());
				if (isBlacklistedDomain) {
					uppInfo.setBlackListedEmailDomain(true);
				}
			}
			if (userProfileType != null) {
				AccessLevel accessLevel = aaaClientUtil.getAccessLevel(userProfileType);
				uppInfo.setAccessLevel(accessLevel.name());
			}
			if (userProfileType.getEFValidatedPartyID() != null
					&& userProfileType.getEFValidatedPartyID().getPartyID() != null) {
				uppInfo.setPartyId(userProfileType.getEFValidatedPartyID()
						.getPartyID().getIdentifier());
				uppInfo.setCompanyName(aaaClientUtil.getUserCompany(userProfileType.getEFValidatedPartyID()
						.getPartyID().getIdentifier()));
				PartyDA partyDA = aaaClientUtil.doesUserPartyHasDA(ccoId);
				uppInfo.setUserHasDA(partyDA.isDA());
				uppInfo.setPartyInfoList(aaaClientUtil
						.getPartyInfo(userProfileType.getEFValidatedPartyID()
								.getPartyID().getIdentifier()));
			}
			uppInfo.setSntcAccess(aaaClientUtil.doesUserhasAccesstoSNTC(ccoId));
		} catch (Exception e1) {
			logger.error("validateProfile--", e1);
		}
		
		//check if the user is in AAA cache
		//dao.findUserInAAACache(ccoId, partyId);
		
		
		return uppInfo;
	}
	
	//For capturing metric MYCODECHANGES starts
/*	@GET
	@Path("/captureEventMetric/{UserCCOID}/{EventName}/{case_number}/{comments}")
	@Produces("text/plain")
	public String captureEventMetric(@Context HttpServletRequest httpServletrequest,
			@PathParam("EventName") String EventName,@PathParam("UserCCOID") String UserCCOID,@PathParam("case_number") String case_number, @PathParam("comments") String comments) throws OnBoardingException {
		// Return some cliched textual content */
	@POST
	@Path("/captureEventMetric")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("text/plain")
	public String captureEventMetric(@Context HttpServletRequest httpServletrequest, @RequestBody  EventByAgent event) throws OnBoardingException{
		String response="";
		String originalLoginUser = loginUtil.getLoginUser(httpServletrequest);
		if(StringUtils.isEmpty(originalLoginUser)){
			throw new LoginException("MISSING_CCO_ID");
		}
		String event_name="";
		String case_number="";
		String comments="";
		String user_ccoid="";
		String party_id="";
		if(event!=null){
			user_ccoid=event.getUser_ccoid();
			event_name=event.getEvent_name();
			case_number=event.getCase_number();
			comments=event.getComments();
			party_id=event.getParty_id();
			System.out.println("Agent sent Details: "+user_ccoid+"-"+case_number+"-"+event_name+"-"+comments+"-"+party_id);
		}
		else{
			
			System.out.println("Empty EventByAgent Object");
			return "false";
		}
		
		
		if(event_name!=null && event_name!="undefined" ){
			switch(event_name){
			case "SSO_Agent_Onboarding_Successful":
				String aaa_status=sendRequestToAAAMethod(user_ccoid,party_id);
				System.out.println("SSO_Agent_Onboarding_Successful Event: "+aaa_status);
				metricsClientUtil.captureMetricDataWithContext(user_ccoid,OnBoardingEventNotificationType.SSO_Agent_Onboarding_Successful,originalLoginUser,case_number, new Date(),comments, null);
				response="true";
							
				break;
			case "SSO_Agent_Onboarding_Shared_Account":
				metricsClientUtil.captureMetricDataWithContext(user_ccoid,OnBoardingEventNotificationType.SSO_Agent_Onboarding_Shared_Account,originalLoginUser,case_number, new Date(),comments, null);
				response="true";
				break;		
			case "SSO_Agent_Onboarding_Other":
				metricsClientUtil.captureMetricDataWithContext(user_ccoid,OnBoardingEventNotificationType.SSO_Agent_Onboarding_Other,originalLoginUser,case_number, new Date(),comments, null);
				response="true";
				break;
			case "SSO_Agent_Onboarding_AAA_Cache_Sync":
				String aaa_status1=sendRequestToAAAMethod(user_ccoid,party_id);
				System.out.println("SSO_Agent_Onboarding_Successful Event: "+aaa_status1);
				metricsClientUtil.captureMetricDataWithContext(user_ccoid,OnBoardingEventNotificationType.SSO_Agent_Onboarding_AAA_Cache_Sync,originalLoginUser,case_number, new Date(),comments, null);
				response="true";
				break;
			
            case "SSO_Agent_Onboarding_Need_Additional_Information":
            	metricsClientUtil.captureMetricDataWithContext(user_ccoid,OnBoardingEventNotificationType.SSO_Agent_Onboarding_Need_Additional_Information,originalLoginUser,case_number, new Date(),comments, null);
            	response="true";
				break;
	        case "SSO_Agent_Onboarding_User_Not_From_Customer_Company":
	        	metricsClientUtil.captureMetricDataWithContext(user_ccoid,OnBoardingEventNotificationType.SSO_Agent_Onboarding_User_Not_From_Customer_Company,originalLoginUser,case_number, new Date(),comments, null);
	        	response="true";
				break;
            default:
            //	metricsClientUtil.captureMetricDataWithContext(UserCCOID,OnBoardingEventNotificationType.SSO_Agent_Onboarding_Successful,case_number , comments, new Date(),originalLoginUser, null);
            	response="Invalid event selection";
            	
				break;
				
			}
			
		}
		
		return response;
	}
	
	//For capturing metric MYCODECHANGES ends
	private void setUserProfile(UPPInfo userProfile,
			UserProfileView userProfileType) {
		if (userProfileType != null
				&& userProfileType.getRegistrationInfo() != null) {
			userProfile.setFirstName(userProfileType.getRegistrationInfo()
					.getFirstName());
			userProfile.setLastName(userProfileType.getRegistrationInfo()
					.getLastName());
			userProfile.setEmail(userProfileType.getRegistrationInfo()
					.getEmailAddress());
			if (userProfileType.getRegistrationInfo().getLocations() != null
					&& userProfileType.getRegistrationInfo().getLocations()
							.getCommunicationAddress() != null) {
				userProfile.setPhoneNumber(userProfileType
						.getRegistrationInfo().getLocations()
						.getCommunicationAddress().getDefaultTelePhone()
						.getTelephoneNumberWithCountryCode());
			}
			if (userProfileType.getRegistrationInfo().getLocations() != null
					&& userProfileType.getRegistrationInfo().getLocations()
							.getPostalAddresses() != null
					&& userProfileType.getRegistrationInfo().getLocations()
							.getPostalAddresses().getPrimaryPostalAddress() != null) {
				userProfile.setCountry(userProfileType.getRegistrationInfo()
						.getLocations().getPostalAddresses()
						.getPrimaryPostalAddress().getCountry());
			}

		}
	}
	private String sendRequestToAAAMethod(String ccoId, String customerid) throws OnBoardingException {
		// Return some clicked textual content
		String exists=userContractDao.checkAAAcache(ccoId,customerid);
		// commented the check codition based on requirements from product management
		//Monitor events and make appropriate decision. Clean up in case if check is not required.
		/*if(exists.equals("true"))
			return "ALREADY_EXISTS";
		else if(exists.equals("false"))
		{ */
		 System.out.println("Customerid: "+customerid +"exists: "+exists);
			try {
				
				// Rest Template
				String url =propertiesUtil.getAAACacheUrlWithPriority();
				//String url="http://v01app-dev019.cisco.com:8611/FactoryServices-dev019/checkerSvc/addCustomer";			 
				MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
				map.add("partyId", customerid);
				map.add("event", "ADD");  
				  			
				RestTemplate restTemplate = new RestTemplate();
			    ResponseEntity<String> result = restTemplate.postForEntity(url, map, String.class);		    		
			     
			    System.out.println(result.getBody().toString());
			    return result.getBody().toString();
			    
			} catch (Exception e) {
				logger.error("An exception while adding sending request to AAA Cache", e);
				throw new OnBoardingException(e.getMessage());
			}
			
	
		
			

	}
	private boolean doesUserHasAccess(HttpServletRequest request) {
		String obSSOCookieValue = getObSSOCookieValue(request);
		System.out.println("<<<<<<<<<<<<<OBS SSO CookieValue>>>>>>>>>>>"+obSSOCookieValue);
		HashMap<String, String> headerMap = null;
		if (StringUtils.isNotBlank(obSSOCookieValue)) {
			// add the ObSSOCookie value to the header for authentication
			if (headerMap == null) {
				headerMap = new HashMap<String, String>();
			}
			String httpUrl = propertiesUtil.getUserLookupUrl();
			headerMap.put(HttpUtil.OBSSOCOOKIE_PARAM,
					HttpUtil.OBSSOCOOKIE_PARAM + "=" + obSSOCookieValue);
			EmailMember emailMemeber = null;
			try {
				String response = HttpUtil.runGet(httpUrl, null, headerMap,10000, 10000, null);
				System.out.println("<<<<<<<<<<<<<RESPONSE>>>>>>>>>>>"+response);
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					emailMemeber = objectMapper.readValue(response,EmailMember.class);
					System.out.println("<<<<<<<<<<<<<EMAIL MEMEBER>>>>>>>>>>>"+response);
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (HttpUtilException e) {
				e.printStackTrace();
			}
			if (emailMemeber != null && emailMemeber.isMember()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Read the ObSSOCookie value from the HttpServletRequest object
	 * 
	 * 
	 */
	public String getObSSOCookieValue(HttpServletRequest request) {
		String obssoCookieVal = null;
		obssoCookieVal = getCookieFromRequest(request,
				HttpUtil.OBSSOCOOKIE_PARAM);
		return obssoCookieVal;
	}

	/**
	 * getCookieFromRequest - gets a cookie value from request
	 * 
	 * @param request
	 * @param cookieName
	 * @return
	 */
	private String getCookieFromRequest(HttpServletRequest request,
			String cookieName) {
		String cookieValue = null;

		Cookie[] cookies = null;
		if (request != null) {
			cookies = validateCookies(request.getCookies());
		}

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookieName.equalsIgnoreCase(validateMethodUtil.validateStringType(cookie.getName()))) {
					cookieValue = validateMethodUtil.validateStringType(cookie.getValue());
					continue;
				}
			}
		}

		if (cookieValue != null) {
			try {
				cookieValue = URLDecoder.decode(cookieValue, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.warn("Could not decode value of " + cookieName
						+ " cookie - " + cookieValue);
			}
		}

		return cookieValue;
	}
	private Cookie[] validateCookies(Cookie[] cookies){
	 if(cookies instanceof Cookie[]){
           return cookies;
      }
   return null;
}
}
