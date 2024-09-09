package com.cisco.convergence.obs.rest;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Service;

import com.cisco.ata.rest.UserProfileView;
import com.cisco.convergence.obs.exception.OnBoardingException;
import com.cisco.convergence.obs.jpa.dao.EpmUserDao;
import com.cisco.convergence.obs.jpa.dao.UserContractDao;
import com.cisco.convergence.obs.model.EPMStatus;
import com.cisco.convergence.obs.util.AAAClientUtil;
import com.cisco.convergence.obs.util.LoginUtil;
import com.cisco.convergence.obs.util.NotificationUtil;
import com.cisco.convergence.obs.util.ValidateMethodUtil;


//import org.owasp.esapi.ESAPI;
//import org.owasp.esapi.errors.IntrusionException;
//import org.owasp.esapi.errors.ValidationException;

@Service
@Path("/epm")
public class EPMService {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Inject
	NotificationUtil notificationUtil;
	
	@Inject
	UserContractDao userContractDao;
	
	@Inject
	EpmUserDao epmUserDao;
	
	@Inject
	private AAAClientUtil aaaClientUtil;
	
	@Inject
	private LoginUtil loginUtil;
	
	@Inject
	private ValidateMethodUtil validateMethodUtil;
	
	@GET
	@Produces("text/plain")
	public String test() {
		// Return some cliched textual content
		return " On-boarding Service";
	}

	
	@GET
	@Path("/validateProfile/{customerCCOId}/{contract}")
	@Produces(MediaType.APPLICATION_JSON)
	public EPMStatus validateProfile(@Context HttpServletRequest httpServletrequest,
			@PathParam("customerCCOId") String customerCCOId,@PathParam("contract") String contract) throws OnBoardingException{
		if(StringUtils.isEmpty(customerCCOId)){
			throw new OnBoardingException("Missing customer cco id");
		}
		if(StringUtils.isEmpty(contract)){
			throw new OnBoardingException("Missing contract number");
		}
		EPMStatus epmStatus = new EPMStatus();
		String ccoId = loginUtil.getLoginUser(httpServletrequest);
		if(epmUserDao.findEpmUser(ccoId) != null){
			epmStatus.setEPM(true);
		}
		//CHECK IF EMAIL DOMAIN BLACK LISTED
		try {
			UserProfileView userProfileType = aaaClientUtil.getUserProfile(customerCCOId);
			if(userProfileType != null && userProfileType.getRegistrationInfo() != null){
				boolean isBlacklistedDomain = loginUtil.isUserEmailDomainBlackListed(userProfileType.getRegistrationInfo().getEmailAddress());
				if(isBlacklistedDomain){
					epmStatus.setEmailDomainBlackListed(true);
				}
			}
		} catch (Exception e1) {
			logger.error("validateProfile--", e1);
			epmStatus.setMissingUserProfile(true);
		}
		
		//CHECK CONTRACT IN PROFILE
	    boolean isContractInProfile = false;
		try {
			isContractInProfile = aaaClientUtil.isContractInProfileAndActive(customerCCOId, contract);
		} catch (Exception e) {
			logger.error("validateProfile", e);
		}
		if(!isContractInProfile){
			epmStatus.setContractNotInProfile(true);
			return epmStatus;
		}

		return epmStatus;
	}
	
	@GET
	@Path("/sendEmail/{customerCCOId}/{contract}")
	@Produces(MediaType.APPLICATION_JSON)
	public EPMStatus sendNotification(@Context HttpServletRequest httpServletrequest,
			@PathParam("customerCCOId") String customerCCOId,@PathParam("contract") String contract) throws OnBoardingException{
		if(StringUtils.isEmpty(customerCCOId)){
			throw new OnBoardingException("Missing customer cco id");
		}
		if(StringUtils.isEmpty(contract)){
			throw new OnBoardingException("Missing contract number");
		}
		EPMStatus epmStatus = new EPMStatus();
		String ccoId = loginUtil.getLoginUser(httpServletrequest);
		if(epmUserDao.findEpmUser(ccoId) != null){
			epmStatus.setEPM(true);
		}
		try {
			UserProfileView userProfileType = aaaClientUtil.getUserProfile(customerCCOId);
			boolean isBlacklistedDomain  = loginUtil.isUserEmailDomainBlackListed(userProfileType.getRegistrationInfo().getEmailAddress());
			boolean isContractInProfile  = aaaClientUtil.isContractInProfileAndActive(customerCCOId, contract);
			if(!isBlacklistedDomain && isContractInProfile){
				notificationUtil.sendNotification(customerCCOId,false,LoginUtil.getLocale(httpServletrequest),contract);
				epmStatus.setStatus("success");
				epmStatus.setMessage("EMAIL_SENT");
			}else{
				epmStatus.setStatus("failure");
				epmStatus.setMessage("EMAIL_SENT_FAILURE");
			}
		
		} catch (Exception e) {
			logger.error("sendNotification---->", e);
			epmStatus.setStatus("failure");
			epmStatus.setMessage("EMAIL_SENT_FAILURE");
		}
		return epmStatus;
	}
	
	@GET
	@Path("/epmAdminUI")
	@Produces(MediaType.APPLICATION_JSON)
	public EPMStatus epmAdminUI(@Context HttpServletRequest request,@Context HttpServletResponse response) throws OnBoardingException{
		
		String ccoId = loginUtil.getLoginUser(request);
		EPMStatus epmStatus = new EPMStatus();
		if(StringUtils.isEmpty(ccoId)){
			epmStatus.setMessage("Missing cco id in the request");
		}
		//String lifecycle = System.getProperty("cisco.life");
		// Security related Fix - SA Report
		String lifecycle = validateMethodUtil.validateStringType(System.getProperty("cisco.life")); 
		String epmAdminUrl = null;
		if(lifecycle.equalsIgnoreCase("prod")){
			epmAdminUrl = "https://ssueap.cloudapps.cisco.com/ssueap/";
		}else{
			epmAdminUrl = "https://ssueap-"+lifecycle+".cloudapps.cisco.com/ssueap/";
		}
		//if he is EPM Admin redirect to EPM Admin page URL
		if(epmUserDao.findEpmUser(ccoId) != null){
			try {
				//Give web publisher location
				response.sendRedirect(epmAdminUrl);
			} catch (IOException e) {
				logger.error("epmAdminUI---->", e);
			}
		}else{
			epmStatus.setMessage("You are not an EPM User!");
		}
		return epmStatus;
	}


		
}
