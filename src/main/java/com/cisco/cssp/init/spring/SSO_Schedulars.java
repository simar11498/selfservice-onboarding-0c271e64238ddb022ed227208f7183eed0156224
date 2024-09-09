package com.cisco.cssp.init.spring;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cisco.convergence.obs.exception.OnBoardingException;
import com.cisco.convergence.obs.jpa.dao.OBSDao;
import com.cisco.convergence.obs.jpa.dao.SchedulerDao;
import com.cisco.convergence.obs.service.DAOnboardingService;
import com.cisco.convergence.obs.util.NotificationUtil;

@Service
public class SSO_Schedulars {
	
	private static Logger log = LogManager.getLogger(SSO_Schedulars.class.getName());

	@Inject
	private OBSDao oBSDao;
	
	@Inject
	private SchedulerDao schedulerDao;
	
	@Inject
	NotificationUtil notificationUtil;
	
	@Autowired
	DAOnboardingService onboardService;
	
	//@Scheduled(cron="*/59 * * * * ?") // Every 59 sec
	//@Scheduled(cron = "0 0 12 * * ?")	// Every day once at 12:00 AM
	public void demoServiceMethod()
	{
		System.out.println("Method executed at every 10 seconds. Current time is :: "+ new Date());
		//Map<String,BigDecimal> metrics= schedulerDao.getMetricsForDailyReport();
		//sendMetricEmail(metrics);
		
		
	}
	
	
	public void demoServiceMethodForTesting()
	{
		System.out.println("Method executed when a call is made through sendDailyreport "+ new Date());
		try{
		Map<String,BigDecimal> metrics= schedulerDao.getMetricsForDailyReport();
		sendMetricEmail(metrics);}
		catch(Exception e){
			System.out.println("schedulerDao.getMetricsForDailyReport() method error"+ e);
		}
		
	}
	
	/*@Scheduled(cron = "0 1 * * * ?")	
	public void isCCOIDCachedinAAA()
	{
		System.out.println("Method executed at every 15 minutes. Current time is :: "+ new Date());
		List<String> AAACachedCCOIdsList= schedulerDao.getCCOIdsCachedInAAA();
		if(AAACachedCCOIdsList.size() > 0){
			for(String ccoId : AAACachedCCOIdsList){
				onboardService.sendWelcomeEmailToUser(ccoId);
			}		
		}
	}*/
	
	private void sendMetricEmail(Map<String,BigDecimal> metrics){
		
		String email = "mkoppara@cisco.com"; 
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");		 
		cal.add(Calendar.DATE, -1);
		//Self Service Registration – Daily Summary Report (10/05/2016 )		
		String subject = "Self Service Registration "+"-"+" Daily Summary Report ("+dateFormat.format(cal.getTime())+")";
		
		StringBuilder sb = new StringBuilder();
		sb.append("<br/>");
		sb.append(subject);
		sb.append("<br/><br/>");
		sb.append("Key Events: ").append("<br/>");
		sb.append("#Clicked on Register - "+ metrics.get("clickedOnRigister")).append("<br/>");
		sb.append("#Successfully verified Contract & S/N  - "+ metrics.get("succVerifiedCNSN")).append("<br/>");  
		sb.append("#Has Existing DA - "+ metrics.get("hasExistingDA")).append("<br/>");
		sb.append("#Clicked Become DA - "+ metrics.get("clickedBecomeDA")).append("<br/>");
		sb.append("#Registration Automation Successful - "+ metrics.get("regAutomationSuccessful")).append("<br/>");
		sb.append("#Request Forwarded for Manual Onboarding - "+ metrics.get("reqFwdForManualOnboarding")).append("<br/><br/>");
		
		sb.append("Other Events: ").append("<br/>");
		sb.append("#GUID match - "+ metrics.get("gUIDMatch")).append("<br/>");
		sb.append("#GUID Mismatch - "+ metrics.get("gUIDMismatch")).append("<br/>");
		sb.append("#Error in fetching GUID - "+ metrics.get("errFetchingGUID")).append("<br/>");
		sb.append("#Error in fetching CR Party ID - "+ metrics.get("errFetchingCRPartyId")).append("<br/>");
		sb.append("#Error in DA Nomination API  - "+ metrics.get("errNominateDAAPI")).append("<br/>");
		sb.append("#Error in User to Party association API - "+ metrics.get("errUserPartyAssociationAPI")).append("<br/>");
		sb.append("#Error in Enable BSSLP API - "+ metrics.get("errEnableBSSLPAPI")).append("<br/>");
		sb.append("#Error in Assigning SNTC Admin Role - "+ metrics.get("errAssigningAdminRoleAPI")).append("<br/><br/>"); 
		
		Map<String, List<String>> listOfCCoIds= schedulerDao.getOnBoardedCCOIdsList();
		log.info("listOfCCoIds : listOfCCoIds.get(aAACachedCCOIDsList) : "+ listOfCCoIds.get("aAACachedCCOIDsList").size());
		log.info("listOfCCoIds : listOfCCoIds.get(aAANonCachedCCOIDsList) : "+ listOfCCoIds.get("aAANonCachedCCOIDsList").size());
		String onboardedList=null;
		String aaaCachedList=null;
		String nonaaaCachedList=null;
		if(!listOfCCoIds.isEmpty()){
			 onboardedList = listOfCCoIds.get("successfulOnBoardedCCOIDsList").toString();
			 aaaCachedList = listOfCCoIds.get("aAACachedCCOIDsList").toString();
			 nonaaaCachedList = listOfCCoIds.get("aAANonCachedCCOIDsList").toString();		
		}
		
		sb.append("AAA Cache Sync Metrics: ").append("<br/>");		
		sb.append("#Registration Automation Successful - "+ listOfCCoIds.get("successfulOnBoardedCCOIDsList").size()).append("<br/>");
		sb.append(onboardedList).append("<br/>");
		sb.append("#Cache sync successful  - "+ listOfCCoIds.get("aAACachedCCOIDsList").size()).append("<br/>");
		sb.append(aaaCachedList).append("<br/>");
		sb.append("#Cache sync failures - "+ listOfCCoIds.get("aAANonCachedCCOIDsList").size()).append("<br/>");
		sb.append(nonaaaCachedList).append("<br/><br/>");		
		
		sb.append("Thank you!").append("<br/><br/>");
		
		System.out.println("----------- Email on Metric Count ----------"+sb.toString());
		try {
			notificationUtil.sendCustomMessage(email, sb.toString(), subject,null);
		} catch (OnBoardingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

