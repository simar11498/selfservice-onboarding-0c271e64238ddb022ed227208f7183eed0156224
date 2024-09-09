package com.cisco.convergence.obs.jpa.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


@Repository
@Transactional("imsApplTransactionManager")
public class SchedulerDao {
	
	private static Logger log = LogManager.getLogger(SchedulerDao.class.getName());
	
	@PersistenceContext(unitName="hibernatePU")
	private EntityManager entityManager1;

		
		@Transactional(value="imsApplTransactionManager")
		public Map<String, BigDecimal> getMetricsForDailyReport(){	
			
		Map<String,BigDecimal> metricCount = new HashMap<String,BigDecimal>();		
		
		try{
			
			//144	SSO_User_Clicked_On_Register
			String clickedOnRigister_sql = "select count(CCO_ID) from  ANF_ADMIN.USER_ACTIONS where ACTION_ID=144 and to_char(CREATE_TIME,'MM/DD/YYYY') = to_char(sysdate-1,'MM/DD/YYYY')";
			
			//105	SSO_Successfully_Verified_Contract_And_Serial_Number
			String succVerifiedCNSN_sql = "select count(CCO_ID) from  ANF_ADMIN.USER_ACTIONS where ACTION_ID=105 and to_char(CREATE_TIME,'MM/DD/YYYY') = to_char(sysdate-1,'MM/DD/YYYY')";
			
			//127	SSO_User_Company_Has_DA
			String hasExistingDA_sql = "select count(CCO_ID) from  ANF_ADMIN.USER_ACTIONS where ACTION_ID=127 and to_char(CREATE_TIME,'MM/DD/YYYY') = to_char(sysdate-1,'MM/DD/YYYY')";
			
			//126	SSO_Submit_Button_Clicked_To_Become_DA
			String clickedBecomeDA_sql = "select count(CCO_ID) from  ANF_ADMIN.USER_ACTIONS where ACTION_ID=126 and to_char(CREATE_TIME,'MM/DD/YYYY') = to_char(sysdate-1,'MM/DD/YYYY')";
			
			//138	SSO_User_Registration_Successful
			String regAutomationSuccessful_sql = "select count(CCO_ID) from  ANF_ADMIN.USER_ACTIONS where ACTION_ID=138 and to_char(CREATE_TIME,'MM/DD/YYYY') = to_char(sysdate-1,'MM/DD/YYYY')";
			
			//135	SSO_Request_Forwarded_For_Manual_Onboarding
			String reqFwdForManualOnboarding_sql = "select count(CCO_ID) from  ANF_ADMIN.USER_ACTIONS where ACTION_ID=135 and to_char(CREATE_TIME,'MM/DD/YYYY') = to_char(sysdate-1,'MM/DD/YYYY')";
			
			//159	SSO_GUID_Match_Success
			String gUIDMatch_sql = "select count(CCO_ID) from  ANF_ADMIN.USER_ACTIONS where ACTION_ID=159 and to_char(CREATE_TIME,'MM/DD/YYYY') = to_char(sysdate-1,'MM/DD/YYYY')";
			
			//157	SSO_GUID_Mismatch
			String gUIDMismatch_sql = "select count(CCO_ID) from  ANF_ADMIN.USER_ACTIONS where ACTION_ID=157 and to_char(CREATE_TIME,'MM/DD/YYYY') = to_char(sysdate-1,'MM/DD/YYYY')";
			
			//158	SSO_Error_Fetching_GUID
			String errFetchingGUID_sql = "select count(CCO_ID) from  ANF_ADMIN.USER_ACTIONS where ACTION_ID=158 and to_char(CREATE_TIME,'MM/DD/YYYY') = to_char(sysdate-1,'MM/DD/YYYY')";
			
			//134	SSO_Error_In_User_CR_Party_Lookup
			String errFetchingCRPartyId_sql = "select count(CCO_ID) from  ANF_ADMIN.USER_ACTIONS where ACTION_ID=134 and to_char(CREATE_TIME,'MM/DD/YYYY') = to_char(sysdate-1,'MM/DD/YYYY')";
			
			//130	SSO_Error_In_Nominate_DA_API
			String errNominateDAAPI_sql = "select count(CCO_ID) from  ANF_ADMIN.USER_ACTIONS where ACTION_ID=130 and to_char(CREATE_TIME,'MM/DD/YYYY') = to_char(sysdate-1,'MM/DD/YYYY')";
			
			//131	SSO_Error_In_User_To_Party_Association_API
			String errUserPartyAssociationAPI_sql = "select count(CCO_ID) from  ANF_ADMIN.USER_ACTIONS where ACTION_ID=131 and to_char(CREATE_TIME,'MM/DD/YYYY') = to_char(sysdate-1,'MM/DD/YYYY')";
			
			// 133	SSO_Error_In_Enabling_Party_BSSLP_API
			String errEnableBSSLPAPI_sql = "select count(CCO_ID) from  ANF_ADMIN.USER_ACTIONS where ACTION_ID=133 and to_char(CREATE_TIME,'MM/DD/YYYY') = to_char(sysdate-1,'MM/DD/YYYY')";
			
			// 132	SSO_Error_In_User_To_Role_Association_API
			String errAssigningAdminRoleAPI_sql = "select count(CCO_ID) from  ANF_ADMIN.USER_ACTIONS where ACTION_ID=132 and to_char(CREATE_TIME,'MM/DD/YYYY') = to_char(sysdate-1,'MM/DD/YYYY')";
			
			//regAutomationSuccess
			
			//cacheSyncSuccess
			
			//cacheSyncFailure
			
						
			
			Query clickedOnRigister_Query = entityManager1.createNativeQuery(clickedOnRigister_sql);
			List clickedOnRigister_List = clickedOnRigister_Query.getResultList();			
			log.info("clickedOnRigister_List : "+ clickedOnRigister_List.get(0)+" : "+ clickedOnRigister_List.toString());			
			metricCount.put("clickedOnRigister", (BigDecimal) clickedOnRigister_List.get(0));
			
			
			Query succVerifiedCNSN_Query = entityManager1.createNativeQuery(succVerifiedCNSN_sql);
			List succVerifiedCNSN_List = succVerifiedCNSN_Query.getResultList();
			log.info("succVerifiedCNSN_List : "+succVerifiedCNSN_List.get(0)+" : "+ succVerifiedCNSN_List.toString());
			metricCount.put("succVerifiedCNSN", (BigDecimal)succVerifiedCNSN_List.get(0));
			
			
			Query hasExistingDA_Query = entityManager1.createNativeQuery(hasExistingDA_sql);
			List hasExistingDA_List = hasExistingDA_Query.getResultList();
			log.info("hasExistingDA_List : "+hasExistingDA_List.get(0)+" : "+ hasExistingDA_List.toString());
			metricCount.put("hasExistingDA", (BigDecimal)hasExistingDA_List.get(0));
			
			
			Query clickedBecomeDA_Query = entityManager1.createNativeQuery(clickedBecomeDA_sql);
			List clickedBecomeDA_List = clickedBecomeDA_Query.getResultList();
			log.info("clickedBecomeDA_List : "+clickedBecomeDA_List.get(0)+" : "+ clickedBecomeDA_List.toString());
			metricCount.put("clickedBecomeDA", (BigDecimal)clickedBecomeDA_List.get(0));
			
			
			Query regAutomationSuccessful_Query = entityManager1.createNativeQuery(regAutomationSuccessful_sql);
			List regAutomationSuccessful_List = regAutomationSuccessful_Query.getResultList();
			log.info("regAutomationSuccessful_List : "+regAutomationSuccessful_List.get(0)+" : "+ regAutomationSuccessful_List.toString());
			metricCount.put("regAutomationSuccessful", (BigDecimal)regAutomationSuccessful_List.get(0));
			
			
			Query reqFwdForManualOnboarding_Query = entityManager1.createNativeQuery(reqFwdForManualOnboarding_sql);
			List reqFwdForManualOnboarding_List = reqFwdForManualOnboarding_Query.getResultList();
			log.info("reqFwdForManualOnboarding_List : "+reqFwdForManualOnboarding_List.get(0)+" : "+ reqFwdForManualOnboarding_List.toString());
			metricCount.put("reqFwdForManualOnboarding", (BigDecimal)reqFwdForManualOnboarding_List.get(0));
			
			
			
			
			Query gUIDMatch_Query = entityManager1.createNativeQuery(gUIDMatch_sql);
			List gUIDMatch_List = gUIDMatch_Query.getResultList();
			log.info("gUIDMatch_List : "+gUIDMatch_List.get(0)+" : "+ gUIDMatch_List.toString());
			metricCount.put("gUIDMatch", (BigDecimal)gUIDMatch_List.get(0));
			
			
			Query gUIDMismatch_Query = entityManager1.createNativeQuery(gUIDMismatch_sql);
			List gUIDMismatch_List = gUIDMismatch_Query.getResultList();
			log.info("gUIDMismatch_List : "+gUIDMismatch_List.get(0)+" : "+ gUIDMismatch_List.toString());
			metricCount.put("gUIDMismatch", (BigDecimal)gUIDMismatch_List.get(0));
			
			
			Query errFetchingGUID_Query = entityManager1.createNativeQuery(errFetchingGUID_sql);
			List errFetchingGUID_List = errFetchingGUID_Query.getResultList();
			log.info("errFetchingGUID_List : "+errFetchingGUID_List.get(0)+" : "+ errFetchingGUID_List.toString());
			metricCount.put("errFetchingGUID", (BigDecimal)errFetchingGUID_List.get(0));
			
			
			Query errFetchingCRPartyId_Query = entityManager1.createNativeQuery(errFetchingCRPartyId_sql);
			List errFetchingCRPartyId_List = errFetchingCRPartyId_Query.getResultList();
			log.info("errFetchingCRPartyId_List : "+errFetchingCRPartyId_List.get(0)+" : "+ errFetchingCRPartyId_List.toString());
			metricCount.put("errFetchingCRPartyId", (BigDecimal)errFetchingCRPartyId_List.get(0));
			
			
			Query errNominateDAAPI_Query = entityManager1.createNativeQuery(errNominateDAAPI_sql);
			List errNominateDAAPI_List = errNominateDAAPI_Query.getResultList();
			log.info("errNominateDAAPI_List : "+errNominateDAAPI_List.get(0)+" : "+ errNominateDAAPI_List.toString());
			metricCount.put("errNominateDAAPI", (BigDecimal)errNominateDAAPI_List.get(0));
			
			
			Query errUserPartyAssociationAPI_Query = entityManager1.createNativeQuery(errUserPartyAssociationAPI_sql);
			List errUserPartyAssociationAPI_List = errUserPartyAssociationAPI_Query.getResultList();
			log.info("errUserPartyAssociationAPI_List : "+errUserPartyAssociationAPI_List.get(0)+" : "+ errUserPartyAssociationAPI_List.toString());
			metricCount.put("errUserPartyAssociationAPI", (BigDecimal)errUserPartyAssociationAPI_List.get(0));
			
			
			Query errEnableBSSLPAPI_Query = entityManager1.createNativeQuery(errEnableBSSLPAPI_sql);
			List errEnableBSSLPAPI_List = errEnableBSSLPAPI_Query.getResultList();
			log.info("errEnableBSSLPAPI_List : "+errEnableBSSLPAPI_List.get(0)+" : "+ errEnableBSSLPAPI_List.toString());
			metricCount.put("errEnableBSSLPAPI", (BigDecimal)errEnableBSSLPAPI_List.get(0));
			
			
			Query errAssigningAdminRoleAPI_Query = entityManager1.createNativeQuery(errAssigningAdminRoleAPI_sql);			
			List errAssigningAdminRoleAPI_List = errAssigningAdminRoleAPI_Query.getResultList();
			log.info("errAssigningAdminRoleAPI_List : "+errAssigningAdminRoleAPI_List.get(0)+" : "+ errAssigningAdminRoleAPI_List.toString());
			metricCount.put("errAssigningAdminRoleAPI", (BigDecimal)errAssigningAdminRoleAPI_List.get(0));
					
			
		}catch(Exception e){			
			log.error(" Error: While fetching data from USER_ACTIONS - getMetricsForDailyReport. ");	
			e.printStackTrace();
		}		
		return metricCount;
	}
		
		/*	get map of ccoids 
		 * 	- who on-boarded successfully
		 *	- aaa cached list of ccoids
		 *	- aaa non-cached list of ccoids
		 */ 
		@SuppressWarnings("unchecked")
		public Map<String,List<String>> getOnBoardedCCOIdsList(){
			Map<String,List<String>> onBoardedCCOIdsMap = new HashMap<String, List<String>>();
			ArrayList<String> successfulOnBoardedCCOIDsList = new ArrayList<String>();
			ArrayList<String> aAACachedCCOIDsList = new ArrayList<String>();
			ArrayList<String> aAANonCachedCCOIDsList = new ArrayList<String>();
			
			String regAutomationSuccessful_sql = "select CCO_ID from  ANF_ADMIN.USER_ACTIONS where ACTION_ID=138 and to_char(CREATE_TIME,'MM/DD/YYYY') = to_char(sysdate-1,'MM/DD/YYYY')";
			Query regAutomationSuccessful_Query = entityManager1.createNativeQuery(regAutomationSuccessful_sql);
			successfulOnBoardedCCOIDsList = (ArrayList<String>) regAutomationSuccessful_Query.getResultList();
			onBoardedCCOIdsMap.put("successfulOnBoardedCCOIDsList", successfulOnBoardedCCOIDsList);
			
			if(successfulOnBoardedCCOIDsList.size() > 0){
				for(String ccoId : successfulOnBoardedCCOIDsList ){
					String checkCCOIdsInAAACachequery = "select cco_id from aaa_admin.C_USER_PARTNER_CUSTOMER_ASSOC where cco_id = '"+ccoId+"'";	
					Query check_CCOIds_In_AAACache_Query = entityManager1.createNativeQuery(checkCCOIdsInAAACachequery);	
					List<String> list = check_CCOIds_In_AAACache_Query.getResultList();
					if(list.isEmpty()){
						aAANonCachedCCOIDsList.add(ccoId);
					}else{
						aAACachedCCOIDsList.add(ccoId);
					}
				}
			}
			
			onBoardedCCOIdsMap.put("aAACachedCCOIDsList", aAACachedCCOIDsList);
			onBoardedCCOIdsMap.put("aAANonCachedCCOIDsList", aAANonCachedCCOIDsList);
			
			return onBoardedCCOIdsMap;
		}
		
		/*
		 * 1. Get list1 of users who are all on-boarded successfully from metrics table -- SSO_User_Registration_Successful
		 * 2. Get list2 of users to whom we sent welcome mail from metrics table
		 * 3. Remove list2 users from list1 ==> we will get non-cached list of users
		 * 4. check updated list1 with AAA cache table, if ccoid exist in AAA send welcome email else ignore.
		 */
		
		public List<String> getCCOIdsCachedInAAA(){
			
			String onBoardedUsersListQuery = "select CCO_ID from anf_admin.user_actions where action_id=138 and to_char(create_time,'MM/DD/YYYY')>=to_char(sysdate,'MM/DD/YYYY')";
			
			//1-step
			Query onBoardedUsersList_Query = entityManager1.createNativeQuery(onBoardedUsersListQuery);	
			List<String> onBoardedUsers_List = new ArrayList<String>();
			onBoardedUsers_List = onBoardedUsersList_Query.getResultList();
			log.info("Today on-boarded CCOID list : "+ onBoardedUsers_List.toString());
			
			//2-step
			String wel_mail_sent_listQuery= "select CCO_ID from anf_admin.user_actions where action_id=148 and to_char(create_time,'MM/DD/YYYY')>=to_char(sysdate,'MM/DD/YYYY')";
			Query wel_MailSentList_Query = entityManager1.createNativeQuery(wel_mail_sent_listQuery);			
			List<String> wel_MailSent_List = new ArrayList<String>();
			wel_MailSent_List = wel_MailSentList_Query.getResultList();
			log.info("Welcome Email Sent - CCOID List "+ wel_MailSent_List);
			
			//3-step - list of user whom not sent welcome mail
			onBoardedUsers_List.removeAll(wel_MailSent_List);
			log.info("List : welcome mail not sent :  "+ onBoardedUsers_List);
			
			
			//4-step
			String idList = onBoardedUsers_List.toString();
			String listOfCCOIds_csv = idList.substring(1, idList.length() - 1).replace(", ", ",");			
			String checkCCOIdsInAAACachequery = "select cco_id from aaa_admin.C_USER_PARTNER_CUSTOMER_ASSOC where cco_id in("+listOfCCOIds_csv+")";	
			Query check_CCOIds_In_AAACache_Query = entityManager1.createNativeQuery(checkCCOIdsInAAACachequery);	
			List<String> AAACached_Users_List = new ArrayList<String>();
			AAACached_Users_List = wel_MailSentList_Query.getResultList();
			log.info("List : AAA Cached :  "+ AAACached_Users_List);
			
			return AAACached_Users_List;
		}
}
