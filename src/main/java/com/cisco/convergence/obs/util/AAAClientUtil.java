package com.cisco.convergence.obs.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

import com.cisco.aaaintegration.AAAConstants;
import com.cisco.eb.request.*;
import com.cisco.eb.response.Asset_;
import com.cisco.eb.response.Data;
import com.cisco.eb.response.GetAssetsResponse;
import com.cisco.aaaintegration.AAAConstants.ACCESS_DECISION_ID_TYPE;
import com.cisco.aaaintegration.AAAIntegrationClient;
import com.cisco.aaaintegration.AAAIntegrationFactory;
import com.cisco.accessdecision.AccessDecisionResponseType;
import com.cisco.ata.rest.PartyHierarchyView;
import com.cisco.ata.rest.PartyProfileView;
import com.cisco.ata.rest.UserProfileView;
import com.cisco.convergence.obs.exception.AuthenticationException;
import com.cisco.convergence.obs.exception.LoginException;
import com.cisco.convergence.obs.exception.OnBoardingException;
import com.cisco.convergence.obs.model.AccessLevel;
import com.cisco.convergence.obs.model.PartyDA;
import com.cisco.convergence.obs.model.PartyInfo;
import com.cisco.convergence.obs.rest.metricsCollection.MetricEventDeliveryService;
import com.cisco.convergence.obs.rest.metricsCollection.OnBoardingEventNotificationType;
import com.cisco.services.bsslp.AssocStatusType;
import com.cisco.services.bsslp.ViewBsslpPartyAssociationsRequest;
import com.cisco.services.bsslp.ViewBsslpPartyAssociationsResponse;
import com.cisco.services.contracts.ccoid.response.Record;
import com.cisco.services.contracts.serialnum.request.CplInactiveStatusList;
import com.cisco.services.contracts.serialnum.request.SerialNumList;
import com.cisco.services.cr.bysiteuseid.ContractSiteUseIdList;
import com.cisco.services.cr.bysiteuseid.CrByErpPartyDetail;
import com.cisco.services.cr.bysiteuseid.CrPartiesByErpPartiesRequest;
import com.cisco.services.cr.bysiteuseid.CrPartiesByErpPartiesResponse;
import com.cisco.services.cr.bysiteuseid.EndCustomerSiteUseIdList;
import com.cisco.services.cr.bysiteuseid.GetCRPartyByERPParties;
import com.cisco.services.cr.bysiteuseid.GetCRPartyByERPPartyResponse;
import com.cisco.services.rolemanagement.AppNames;
import com.cisco.services.rolemanagement.AssocPartyIds;
import com.cisco.services.rolemanagement.RoleAssocStatus;
import com.cisco.services.rolemanagement.UserRoleType;
import com.cisco.services.rolemanagement.ViewUserRolesRequest;
import com.cisco.services.rolemanagement.ViewUserRolesResponse;

@Component
public class AAAClientUtil {
	
	private static final String RESOURCE_NAME= "CAEntitlement:SSUE:Services:SNTC";
	private static final String CUSTOMER_ADMIN_ROLE="CustomerAdmin";
	private static final String DA= "DA";
	private static final String DELEGATED_ADMIN = "DelegatedAdministration";
	private static final String SNTC_APP="SNTC";
	private static final String CSAM_APP="CSAM";
	private static final String GENERIC_ERROR = "Entilement Service is down!";
	
	private static Logger LOG = LogManager.getLogger(AAAClientUtil.class.getClass());
	
	@Inject
	private PropertiesUtil propertiesUtil;
	
	@Inject
	private MetricsClientUtil metricsClientUtil; 
	
	@Value("${makeadmin.ccoid}")
	private String byPassDACcoId;

	//@Inject
	//private EFUpdateClient efUpdateClient;
	
	@Inject
	private LoginUtil loginUtil;
	
	@Inject
	private MetricEventDeliveryService metricEventDeliveryService;
	
	
	public  boolean doesUserhasAccesstoSNTC(String ccoId)throws AuthenticationException{
		try {
			AAAIntegrationClient integrationClient= getAAAClient();
			
			AccessDecisionResponseType respType = integrationClient.getAccessDecision
					(ccoId,ACCESS_DECISION_ID_TYPE.User,null,RESOURCE_NAME,null);
			//USer has access on SNTC
			if(respType.getResult().getDecision() != null && respType.getResult().getDecision().booleanValue()){
				return true;
			}
		} catch (Exception e) {
			System.err.println("***********FAILED TO  isUserhasAccesstoSNTC************"+e.getMessage());
			AuthenticationException loginException = new AuthenticationException(GENERIC_ERROR);
			LOG.error("***********FAILED AT  doesUserhasAccesstoSNTC************",e);
			throw loginException;
		}
		return false;
	}
	public UserProfileView getUserProfile(String ccoId)throws LoginException{
		try {
			//System.out.println("********efUpdateClient*******"+efUpdateClient);
			return AAAClientUtil.getAAAClient().getUserProfileViewByCcoIdGET(ccoId);
			
		} catch (Exception e) {
			System.err.println("**********FAILED TO GET USER PROFILE*********"+e.getMessage());
			LOG.error("***********FAILED TO GET USER PROFILE************",e);
			throw new LoginException(GENERIC_ERROR);
		}
	}
	

	public String getUserCompany(String crPartyId){
		String company = "Missing company name in user profile";
		if(StringUtils.isEmpty(crPartyId) || crPartyId.equals("UNKNOWN")){
			return ("Missing Party id");
		}

		AAAIntegrationClient aaaIntegrationClient =  AAAClientUtil.getAAAClient();
		try {
			PartyProfileView PartyProfileView = aaaIntegrationClient.getPartyProfileViewGET(crPartyId);
			if(PartyProfileView != null && PartyProfileView.getPartyName() != null){
				//If we find party name then return
				return PartyProfileView.getPartyName().getName();
			}
		} catch (Exception e) {
			System.err.println("**********FAILED TO getUserCompany*********"+e.getMessage());
			LOG.error("***********FAILED TO getUserCompany************",e);
		}

		return company;
	}
	public boolean isAdminRole(String userId){
		
			AAAIntegrationClient integrationClient= getAAAClient();
			boolean roleExists = false;
			try {
				ViewUserRolesRequest reqObj = new ViewUserRolesRequest();
				reqObj.setUser(userId);
				reqObj.setAssocStatus(RoleAssocStatus.ACTIVE);
				
				AppNames appNames = new AppNames();
				appNames.getAppName().add(SNTC_APP);
				
				reqObj.setAppNames(appNames);

				ViewUserRolesResponse response = integrationClient.getUserRoles(reqObj);
				
				List<UserRoleType> userRoleTypeLst = response.getUserRoles().getUserrole();
				for(UserRoleType roleRec : userRoleTypeLst)
				{
					if(roleRec.getRoleName().equals(CUSTOMER_ADMIN_ROLE)){
						roleExists = true;
						break;
					}
				}
			} catch (Exception e) {
				System.err.println("***********FAILED TO  isAdminRole************"+e.getMessage());
				LOG.error("***********FAILED TO isAdminRole************",e);
			}
			
		return roleExists;
		
	}
	public boolean isPartyAssociatedToSNTC(String crPartyId){
		AAAIntegrationClient integrationClient= getAAAClient();
		boolean isPartyAssociated = false;
		try {
			ViewBsslpPartyAssociationsRequest.Bsslp bsslp = new ViewBsslpPartyAssociationsRequest.Bsslp();
			bsslp.setBsslpName(AAAConstants.APP_NAME.SNTC.name());
			
			ViewBsslpPartyAssociationsRequest.Party party = new ViewBsslpPartyAssociationsRequest.Party();
			party.setEntitledCRPartyId(new BigDecimal(crPartyId));
			ViewBsslpPartyAssociationsRequest request = new ViewBsslpPartyAssociationsRequest();

			request.getAssocStatus().add(AssocStatusType.A);
			request.setParty(party);
			request.setBsslp(bsslp);
			ViewBsslpPartyAssociationsResponse response = integrationClient.viewBsslpPartyAssociations(request);
			if(response != null && response.getTotalCount().longValue() > 0){
				isPartyAssociated = true;
			}
		} catch (Exception e) {
			System.err.println("***********FAILED TO GET isPartyAssociatedToSNTC************"+e.getMessage());
			LOG.error("***********FAILED TO GET isPartyAssociatedToSNTC************",e);
		}
		return isPartyAssociated;
	
	}
	public boolean isContractInProfileAndActive(String ccoId,String contract)throws OnBoardingException {
		boolean isContractActive = false;
		try {
			//TODO make the gspCheck flag true when giving 3.2.1 patch build
			GetAssetsResponse response = getContractsResponse(ccoId, contract,false,true);
			isContractActive = hasContracts(response);
			//TODO adding to for testing purpose till we get Test Users. It will not harm in the production
			if(propertiesUtil.getContractProperties().containsKey(ccoId)){
				String contractNum = propertiesUtil.getContractProperties().get(ccoId).toString();
				if(!StringUtils.isEmpty(contractNum) && contractNum.equals(contract)){
					return true;
				}
			}
			
		} catch (Exception e) {
			System.err.println("**********FAILED TO isContractInProfileAndActive*********"+e.getMessage());
			LOG.error("***********FAILED TO isContractInProfileAndActive************",e);
			throw new OnBoardingException(GENERIC_ERROR);
		}
		return isContractActive;
	}
	public boolean isContractInProfile(String ccoId,String contract)throws OnBoardingException {
		boolean isContractActive = false;
		try {
			//TODO make the gspCheck flag true when giving 3.2.1 patch build
			GetAssetsResponse response = getContractsResponse(ccoId, contract,false,false);
			isContractActive = hasContracts(response);
			//TODO adding to for testing purpose till we get Test Users. It will not harm in the production
			if(propertiesUtil.getContractProperties().containsKey(ccoId)){
				String contractNum = propertiesUtil.getContractProperties().get(ccoId).toString();
				if(!StringUtils.isEmpty(contractNum) && contractNum.equals(contract)){
					return true;
				}
			}
			
		} catch (Exception e) {
			System.err.println("**********FAILED TO isContractInProfile*********"+e.getMessage());
			LOG.error("***********FAILED TO isContractInProfileAndActive************",e);
			throw new OnBoardingException(GENERIC_ERROR);
		}
		return isContractActive;
	}
	
	private boolean hasContracts(GetAssetsResponse response) {
		if(response != null){
			Iterator<com.cisco.eb.response.Asset> assetList = response.getAsset().iterator();
			while(assetList.hasNext())
			{
				com.cisco.eb.response.Asset asset= assetList.next();
				if(asset!=null)
				{
						Data data= asset.getData();
						if(data.getTotalEntries()!=null&&data.getTotalEntries()>0)
							return true;
						else 
							return false;
				}
				else
					return false;
			}
			
		}
		return false;
	}
	public List<String> getContracts(String ccoId){
		List<String> contractList = new ArrayList<String>();
		GetAssetsResponse response;
		try {
			response = getContractsResponse(ccoId,null,false,true);
			if(response!=null)
			{
			Iterator<com.cisco.eb.response.Asset> assetList = response.getAsset().iterator();
			while(assetList.hasNext())
			{
					com.cisco.eb.response.Asset asset = assetList.next();
					if(asset.getAssetType().equals("contract"))
					{
					Iterator<com.cisco.eb.response.Asset_> contractListIterator = asset.getData().getAsset().iterator();
					while(contractListIterator.hasNext())
					contractList.add(contractListIterator.next().getContractNumber());
					}
				}
			}
				
		} catch (Exception e) {
			System.err.println("***********FAILED TO  getContracts************"+e.getMessage());
			LOG.error("************FAILED TO  getContracts************",e);
		}
		
		return contractList;
	}
	private GetAssetsResponse getContractsResponse(
			String ccoId, String contract,boolean gspCheck,boolean checkActive) throws Exception {
		AAAIntegrationClient integrationClient= getAAAClient();
		GetAssetsRequest request= new GetAssetsRequest();
		String needTotalCount = "Y";
		
		List<String> entitlementMethods = new ArrayList<String>();
		entitlementMethods.add("Right to Technical Support.Open case");
		Asset asset = new Asset();
		String assetType = "contract";
		asset.setAssetType(assetType);
		
		List<User> userList = new ArrayList<User>();
		User userObj =  new User();
		userObj.setId(ccoId);
		userObj.setType("CCO");
		userList.add(userObj);
		
		FilterBy filterBy = new FilterBy();
		List<Object> contractNumberList = new ArrayList<Object>();
		contractNumberList.add(contract);
		filterBy.setContractNumber(contractNumberList);
		if(gspCheck){
			List<Object> serviceLineName = new ArrayList<Object>();
			String gsps = propertiesUtil.getValidGsps();
			String[] gspArray = gsps.split(",");
			for (int i = 0; i < gspArray.length; i++) {
				serviceLineName.add(gspArray[i]);	
			}
			filterBy.setServiceLineName(serviceLineName);
		}
		if(checkActive){
		List<Object> stsCodeList = new ArrayList<Object>();
		stsCodeList.add("ACTIVE");
		filterBy.setStsCode(stsCodeList);
		}
		asset.setFilterBy(filterBy);
				
		List<Asset> assetList = new ArrayList<Asset>();
		assetList.add(asset);
		
		request.setNeedTotalCount(needTotalCount);
		request.setAsset(assetList);
		request.setLoggedInId(ccoId);
		request.setCurrentPage("1");
		request.setEntitlementMethod(entitlementMethods);
		request.setPerPage("10");
		request.setUser(userList);
		
		
		GetAssetsResponse response = integrationClient.getAssets(request);
		return response;
	}	

	public String getSiteUseIdByContractAndSN(String contract,String sn)throws OnBoardingException{
		return getContractDetailsFor("SITEUSEID", sn, contract);
	}
	
	public List<String> getContractTypeBy(String contract,String ccoId)throws OnBoardingException{
		List<String> serviceLineList = new ArrayList<String>();
		try {
			AAAIntegrationClient integrationClient= getAAAClient();
			GetAssetsRequest request = getContractRequest(contract, ccoId);
			System.out.println("*******Before calling EB************");
			System.out.println("Request :"+request.toString());
			System.out.println("Request entitlement method :"+ request.getEntitlementMethod());

			GetAssetsResponse response = integrationClient.getAssets(request);
			if(response!=null)
			{
			Iterator<com.cisco.eb.response.Asset> assetList = response.getAsset().iterator();
			while(assetList.hasNext())
			{
				com.cisco.eb.response.Asset asset = assetList.next();
				if(asset.getAssetType().equals("contract"))
				{
					System.out.println("::Inside contract condition");
					Iterator<Asset_> contractList = asset.getData().getAsset().iterator();
					while(contractList.hasNext())
					{
						System.out.println("::Inside contractList Iterator");
					serviceLineList.add(contractList.next().getServiceLineName());
					}
				}
				}
			}
			//System.out.println("****************ServiceLineName*****************************"+response.getAsset().get(0).getData().getAsset().get(0).getServiceLineName()+"*******************");
			} catch (Exception e) {
			System.err.println("************FAILED TO getContractTypeBy**********"+e.getMessage());
			LOG.error("***********FAILED TO getContractTypeBy************",e);
			throw new OnBoardingException(GENERIC_ERROR);
		}
		return serviceLineList;
	}
	
	private String getContractDetailsFor(String value,String sn,String contract)throws OnBoardingException{
		String propertyValue = null;
		try {
			AAAIntegrationClient integrationClient= getAAAClient();
			System.out.println("*******Before calling getContractsForSerialNumberRequest ************");
			GetAssetsRequest request = getContractsForSerialNumberRequest(sn,contract);
			System.out.println("Request :"+request.toString());
			System.out.println("Request entitlement method :"+ request.getEntitlementMethod());
			GetAssetsResponse response = integrationClient.getAssets(request);
			if(response!=null)
			{
			Iterator<com.cisco.eb.response.Asset> assetList = response.getAsset().iterator();
			while(assetList.hasNext())
			{
				com.cisco.eb.response.Asset asset = assetList.next();
				if(asset.getAssetType().equals("contract"))
				{
					System.out.println("::Inside contract condition");
					Iterator<Asset_> contractList = asset.getData().getAsset().iterator();
					while(contractList.hasNext())
					{
						Asset_ assetValue= contractList.next();
						if(value.equalsIgnoreCase("SITEUSEID")){
							propertyValue= assetValue.getEndCustomerSiteUseId();
						}
						if(value.equalsIgnoreCase("CONTRACTTYPE")){
							propertyValue = assetValue.getServiceLineName();
						}
						System.out.println("::Inside contractList Iterator");
						
					}
				}
				}
			}
			
		} catch (Exception e) {
			System.err.println("************FAILED TO getSiteUseIdByContractAndSN**********"+e.getMessage());
			LOG.error("***********FAILED TO getSiteUseIdByContractAndSN************",e);
			throw new OnBoardingException(GENERIC_ERROR);
		}
		System.out.println("*******after  calling getContractsForSerialNumberRequest ************" + propertyValue);
		return propertyValue;
	}
	
	public boolean isSNInContract(String sn,String contract,boolean checkExpired) throws OnBoardingException{
	
		try {
			AAAIntegrationClient integrationClient= getAAAClient();
			
			/*com.cisco.services.contracts.serialnum.request.EntitlementRequest entitlementRequest = new com.cisco.services.contracts.serialnum.request.EntitlementRequest();
			
			
			entitlementRequest.setGetContracts(getContracts);*/
			com.cisco.services.contracts.serialnum.request.GetContracts getContracts = populateContractRequest(sn, contract);
			GetAssetsRequest request = getContractsForSerialNumberRequest(sn,contract);
			System.out.println("<<<<<<<<<<<<     Inside  isSNInContract in AAAClientUtil.java     >>>>>>>>>>>>>");
			if(checkExpired){
				System.out.println("<<<<<<<<<<<< checkExpired = false >>>>>>>>>");
				boolean checkStatus = false;
				checkStatus = isSerialNumberBelongsToContract(getContracts,request,integrationClient,"EXPIRED");
				if(!checkStatus){
					checkStatus = isSerialNumberBelongsToContract(getContracts,request,integrationClient,"TERMINATED");
				}
				return checkStatus;
			}
			
			GetAssetsResponse response = integrationClient.getAssets(request);	
			if(response!=null)
			{
			Iterator<com.cisco.eb.response.Asset> assetList = response.getAsset().iterator();
			while(assetList.hasNext())
			{
				com.cisco.eb.response.Asset asset = assetList.next();
				if(asset.getAssetType().equals("contract"))
				{
					Iterator<Asset_> contractList = asset.getData().getAsset().iterator();
					while(contractList.hasNext())
					{
					return true;
					}
				}
				}
			}
			//com.cisco.services.contracts.serialnum.response.GetContractsResponse response = integrationClient.getContractsForSerialNumber(entitlementRequest);
			//TODO for testing in prod and stage
			if(propertiesUtil.getSnProperties().containsKey(contract)){
				System.out.println("propertiesUtil.getSnProperties().containsKey(contract) == null ");
				String serialNum = propertiesUtil.getSnProperties().get(contract).toString();
				if(!StringUtils.isEmpty(serialNum) && serialNum.equals(sn)){
					return true;
				}
			}
			

		} catch (Exception e) {
			System.err.println("************FAILED TO isSNInContract**********"+e.getMessage());
			LOG.error("***********FAILED TO isSNInContract************",e);
			throw new OnBoardingException(GENERIC_ERROR);
		}
		return false;
	}
	private com.cisco.services.contracts.serialnum.request.GetContracts populateContractRequest(
			String sn, String contract) {
		com.cisco.services.contracts.serialnum.request.GetContracts getContracts = new com.cisco.services.contracts.serialnum.request.GetContracts();
				
		SerialNumList snList = new SerialNumList();
		snList.setSerialNumber(sn);
		getContracts.getSerialNumList().add(snList);
			
		com.cisco.services.contracts.serialnum.request.ContractNumberList contractNumberList= new com.cisco.services.contracts.serialnum.request.ContractNumberList();
		contractNumberList.getContractNumber().add(contract);
		getContracts.setContractNumberList(contractNumberList);
		return getContracts;
	}
	private boolean isSerialNumberBelongsToContract(com.cisco.services.contracts.serialnum.request.GetContracts getContracts,
			GetAssetsRequest request,AAAIntegrationClient integrationClient,String status)throws Exception{
		CplInactiveStatusList cplInactiveStatusList = new CplInactiveStatusList();
//		cplInactiveStatusList.setCplInactiveStatus("TERMINATED");
		cplInactiveStatusList.setCplInactiveStatus(status);
		getContracts.setCplInactiveStatusList(cplInactiveStatusList);
		System.out.println("<<<<<<<<<<<<     Before  calling AAA jar in AAAClientUtil.java     >>>>>>>>>>>>>");
		GetAssetsResponse response  = integrationClient.getAssets(request);
		System.out.println("<<<<<<<<<<<<     Response from  AAA jar    >>>>>>>>>>>>>");
	//	System.out.println("RESPONSECODE="+response.getRESPONSECODE()+"\nSEARCHRESULTS.getRecord() "+response.getSEARCHRESULTS().getRecord());
		/*if(response != null && response.getSEARCHRESULTS() != null &&
				response.getSEARCHRESULTS().getRecord() != null && response.getSEARCHRESULTS().getRecord().size() > 0){
			System.out.println("Got NULL reponse from AAA jar");	
			return true;
		}*/
		
		if(response!=null)
		{
		Iterator<com.cisco.eb.response.Asset> assetList = response.getAsset().iterator();
		while(assetList.hasNext())
		{
			com.cisco.eb.response.Asset asset = assetList.next();
			if(asset.getAssetType().equals("contract"))
			{
				Iterator<Asset_> contractList = asset.getData().getAsset().iterator();
				while(contractList.hasNext())
				{
				return true;
				}
			}
			}
		}
		
		
		System.out.println("<<<<<<<<<<<<     After  calling AAA jar in AAAClientUtil.java     >>>>>>>>>>>>>");
		return false;
	}
	/**
	 * find if this profile has EF party id if not take cr party id 
	 * @param UserProfileView
	 * @return
	 */
	public static  String getPartyId(UserProfileView UserProfileView) {
		String partyId = null;
		/*if(UserProfileView.getEFValidatedPartyID() != null){
				partyId = UserProfileView.getEFValidatedPartyID().getPartyID().getIdentifier();
		}else*/ if(UserProfileView.geteFValidatedCRPartySiteID() != null){
			partyId = UserProfileView.geteFValidatedCRPartySiteID().getPartyID().getIdentifier();
		}
		return partyId;
	}
	/**
	 * Method to get AAAIntegrationClient
	 * @return AAAIntegrationClient
	 */
	public static AAAIntegrationClient getAAAClient(){
		 return AAAIntegrationFactory.getInstance();
	}
	
	public AccessLevel getAccessLevel(UserProfileView  UserProfileView){
		AccessLevel accessLevel = AccessLevel.UNKNOWN;
		try{
			if(UserProfileView != null && UserProfileView.getRegistrationInfo() != null){
				String aLevel = UserProfileView.getRegistrationInfo().getAccessLevel();
				if(UserProfileView.getRegistrationInfo() != null && !StringUtils.isEmpty(aLevel)){
					accessLevel= AccessLevel.getByName(aLevel);	
				}
			}
		}catch(Exception e){
			System.err.println("************FAILED TO getAccessLevel**********"+e.getMessage());
			LOG.error("***********FAILED TO getAccessLevel************",e);
		}
		return accessLevel;
	}
	public PartyDA doesPartyHasDA(String partyId){
		/*
		 * Sample INPUT request Payload :
		<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
		<viewUserRolesRequest xmlns="http://www.cisco.com/services/RoleManagement">
		<requestHeader>
                <callingAppName>callingAppName</callingAppName>
                <callingUser>samttest.helenauser023</callingUser>
		</requestHeader>
		<appGroup>DelegatedAdministration</appGroup>
		<userRoleAssocType>DA</userRoleAssocType>//CICA (need to make another call if he has CICA then change the DA to CICA
		<assocPartyId>31760012</assocPartyId>
		<assocStatus>ACTIVE</assocStatus>
		<partyHierarchySearch>PARTY_ONLY</partyHierarchySearch>
		<getTotalCount>true</getTotalCount>
		</viewUserRolesRequest>

		 */
		if(StringUtils.isEmpty(partyId)){
			throw new RuntimeException("Missing Party id");
		}
		ViewUserRolesRequest reqObj = new ViewUserRolesRequest();
		
		AssocPartyIds ids = new AssocPartyIds();
		ids.getAssocPartyId().add(Integer.parseInt(partyId));

		
		reqObj.setAssocPartyIds(ids);
		reqObj.setAssocStatus(RoleAssocStatus.ACTIVE);
		reqObj.setUserRoleAssocType(DA);//CICA
		reqObj.setAppGroup(DELEGATED_ADMIN);
		
		AppNames appNames = new AppNames();
		appNames.getAppName().add(CSAM_APP);
		reqObj.setAppNames(appNames);

		AAAIntegrationClient integrationClient= getAAAClient();
		ViewUserRolesResponse response = null;
		PartyDA partyDA = new PartyDA();
		
		try {
			response = integrationClient.getUserRoles(reqObj);
			
			if(response != null && response.getUserRoles() != null && 
					response.getUserRoles().getUserrole() != null
					&& response.getUserRoles().getUserrole().size() > 0){
				checkDA(partyId, integrationClient, response, partyDA);
			}else{//If not ACTIVE then check PENDING STATUS for DA
				reqObj.setAssocStatus(RoleAssocStatus.PENDING);
				response = integrationClient.getUserRoles(reqObj);
				if(response != null && response.getUserRoles() != null && 
						response.getUserRoles().getUserrole() != null
						&& response.getUserRoles().getUserrole().size() > 0){
					checkDA(partyId, integrationClient, response, partyDA);
				}
			}
			
		} catch (Exception e) {
			LOG.error("***********DA ERROR************",e);
		}
		
		return partyDA;	
	}
	private void checkDA(String partyId,
			AAAIntegrationClient integrationClient,
			ViewUserRolesResponse response, PartyDA partyDA) throws Exception {
		System.out.println("Total Count::" + response.getTotalCount());
		System.out.println("Retrieved Records Count::" + response.getUserRoles().getUserrole().size());
		List<UserRoleType> userRoleTypeLst = response.getUserRoles().getUserrole();
		for(UserRoleType roleRec : userRoleTypeLst){
			String ccoId = roleRec.getUser();
			LOG.info("ccoId of DA :: "+ccoId);
			
			
			String partyName = roleRec.getAssocPartyName();
			String firstName = roleRec.getFirstName();
			String lastName = roleRec.getLastName();
			//String emailAddress = roleRec.getEmailAddress();EF is not maintaining this data
			partyDA.setCompanyName(partyName);
			partyDA.setFirstName(firstName);
			partyDA.setLastName(lastName);
			partyDA.setDA(true);
			String emailAddress = "";
			if(integrationClient.getUserProfileViewByCcoIdGET(ccoId).getRegistrationInfo() != null){
				LOG.info("DA emailaddress is present:: "+ccoId);
				 emailAddress = integrationClient.getUserProfileViewByCcoIdGET(ccoId).getRegistrationInfo().getEmailAddress();
				 partyDA.setEmailAddress(emailAddress);
				 break;
			}
			
			System.out.println("CCOID::" + ccoId + "; PartyID::" + partyId + "; PartyName::" + partyName + "; RoleName::" + roleRec.getRoleName() + "; Assoc Type::" + roleRec.getUserRoleAssocType());
			
		}
	}
	/**
	 * Finds recursively all its parents to find if the party has DA.
	 * @param ccoId
	 * @return
	 */
	public PartyDA doesUserPartyHasDA(String ccoId) {
		PartyDA partyDA = new PartyDA();
		try {
			UserProfileView UserProfileView =  getUserProfile(ccoId);
			String partyId = null;
			if(UserProfileView.getEFValidatedPartyID() != null &&
					UserProfileView.getEFValidatedPartyID().getPartyID() != null){
				partyId = UserProfileView.getEFValidatedPartyID().getPartyID().getIdentifier();
			}
			if(StringUtils.isEmpty(partyId)) {
				return partyDA;
			}
			partyDA = doesPartyHasDA(partyId);
			
			if(partyDA.isDA()) {
				return partyDA;
			}
			//For prod testing not doing recursive check.
			if(ccoId.equals(byPassDACcoId)){
				return partyDA;
			}
			
			while(true){
				AAAIntegrationClient integrationClient= getAAAClient();
				PartyProfileView PartyProfileView = integrationClient.getPartyProfileViewGET(partyId);
				if(PartyProfileView == null || PartyProfileView.getGuPartyID() == null ||
						PartyProfileView.getGuPartyID().getIdentifier().equals(partyId)){
					break;
				}
				partyId = PartyProfileView.getGuPartyID().getIdentifier();
			    partyDA = doesPartyHasDA(partyId);
			    if(partyDA.isDA()) {
					break;
				}
			}
		} catch (Exception e1) {
			LOG.info("***********doesUserPartyHasDA************",e1);
		}
		return partyDA;
		
	}
	public List<PartyInfo> getPartyInfo(String partyId){
		List<PartyInfo> infoList = new ArrayList<PartyInfo>();
		try {
			while(true){
				AAAIntegrationClient integrationClient= getAAAClient();
				PartyInfo partyInfo = new PartyInfo();
				PartyProfileView PartyProfileView = integrationClient.getPartyProfileViewGET(partyId);
				if(PartyProfileView != null){
				  partyInfo.setCompanyName(PartyProfileView.getPartyName().getName());
				  partyInfo.setPartyId(partyId);
				  infoList.add(partyInfo);
				  PartyDA partyDA = doesPartyHasDA(partyId);
				  partyInfo.setDA(partyDA.isDA());
				  partyInfo.setFirstName(partyDA.getFirstName());
				  partyInfo.setLastName(partyDA.getLastName());
				  if(PartyProfileView.getGuPartyID() != null){
					  partyInfo.setParentPartyId(PartyProfileView.getGuPartyID().getIdentifier());
				  }
				}
				if(PartyProfileView == null || PartyProfileView.getGuPartyID() == null ||
						PartyProfileView.getGuPartyID().getIdentifier().equals(partyId)){
					break;
				}
				partyId = PartyProfileView.getGuPartyID().getIdentifier();
			}
		} catch (Exception e1) {
			LOG.info("***********doesUserPartyHasDA************",e1);
		}
		return infoList;
	}
	public CrPartiesByErpPartiesRequest getCrRequest(String siteId) 
	{
		System.out.println("Creating request -getCrRequest(String siteId) " +siteId);
		CrPartiesByErpPartiesRequest request= new CrPartiesByErpPartiesRequest();
		
//		List<String> projectOutputFields = new ArrayList<String>();
//		projectOutputFields.add("");

		EndCustomerSiteUseIdList filterBy = new EndCustomerSiteUseIdList();
		ArrayList<String> endCustomerSiteUseIdList = new ArrayList<String>();
		endCustomerSiteUseIdList.add(siteId);
		filterBy.setEndCustomerSiteUseId(endCustomerSiteUseIdList);
		
		request.setEndCustomerSiteUseIdList(filterBy);
//		request.setProjectOutputFields(projectOutputFields);
		request.setCurrentPage("1");
		request.setPerPage("500");
			
		return request;
	}
	
	
	public String getCrPartyBySiteUseId(long siteUseId,String ccoId){
		AAAIntegrationClient integrationClient= getAAAClient();
		String crPartyId = "";
		
		/*try {
			com.cisco.services.cr.bysiteuseid.EntitlementRequest request = new com.cisco.services.cr.bysiteuseid.EntitlementRequest();
			GetCRPartyByERPParties getCrPartyByErpParties = new GetCRPartyByERPParties();
			ContractSiteUseIdList contractSiteUseIdLst = new ContractSiteUseIdList();
			contractSiteUseIdLst.getContractSiteUseIdIn().add(String.valueOf(siteUseId));

			getCrPartyByErpParties.setContractSiteUseIdList(contractSiteUseIdLst);
			request.setGetCRPartyByERPParties(getCrPartyByErpParties);
			
			GetCRPartyByERPPartyResponse response = integrationClient.getCrPartiesForSiteUseIDs(request);
			
			List<com.cisco.services.cr.bysiteuseid.Record> records = response.getSEARCHRESULTS().getRecord();
			
			if(records != null && !records.isEmpty()){
				crPartyId = records.get(0).getCRPARTYID();
			}
			
		} */
		try {
//			siteUseId = 662574;
			System.out.println("Before calling Eb");
			CrPartiesByErpPartiesRequest request = new CrPartiesByErpPartiesRequest();
			request= getCrRequest(Long.toString(siteUseId));
			CrPartiesByErpPartiesResponse response = integrationClient.getCrPartiesForSiteUseIDs(request);
			List<CrByErpPartyDetail> partyList = response.getPartyDetails();
			if(partyList != null && !partyList.isEmpty()){
				CrByErpPartyDetail party=partyList.get(0);
				if(party.getCrPartyId()!=null)
				crPartyId= party.getCrPartyId(); //getCRPARTYID()
			}
			System.out.println("****Received response from EB****");
			System.out.println("CRPartyId for SiteuseID " + siteUseId+ " is " + crPartyId);
			
		}
			catch (Exception e) {
			LOG.error("Error in getting CR Party by site use id:",e);
			try {
				String errorMessage="Error in getting CR Party by site use id: " + e.getMessage();
				errorMessage=metricsClientUtil.getTruncatedErrorMessage(errorMessage);
				metricsClientUtil.captureMetricDataWithContext(ccoId, OnBoardingEventNotificationType.SSO_Error_In_User_CR_Party_Lookup, "", "", new Date(),errorMessage, "");
			} catch (LoginException e1) {
//				e1.printStackTrace();
			}
		}		
		return crPartyId;
	}
	
	/*public String getCompanies(String crPartyId){
		AAAIntegrationClient integrationClient= getAAAClient();
		
		try {
			PartyProfileView prartyProfile = integrationClient.getPartyProfile(crPartyId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("Error in getting CR Party by site use id:",e);
		}
		return "";
	}*/
	
	/**
	 * 
	 * @param partyId
	 * @return
	 */
	public PartyProfileView getPartyProfile(String partyId) {
		AAAIntegrationClient integrationClient= getAAAClient();
		
		PartyProfileView PartyProfileView = null;
		
		try {
			PartyProfileView = integrationClient.getPartyProfileViewGET(partyId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error("Error fetching the party profile for partyid :"+partyId);
		}
		
		return PartyProfileView;
	}
	
//	/**
//	 * 
//	 * @param partyId
//	 * @return
//	 */
	public PartyHierarchyView getPartyHierarchyType(String crPartyId){
		PartyHierarchyView partyHierarchyType = null;
		AAAIntegrationClient integrationClient= getAAAClient();
		try {
			partyHierarchyType = integrationClient.getPartyHierarchyViewGET(crPartyId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error("Error fetching the PartyHierarchyType for partyid :"+crPartyId);
		}
		return partyHierarchyType;
	}
	
	
	private GetAssetsRequest getContractRequest(String cn,String ccoid)
	{
		GetAssetsRequest request= new GetAssetsRequest();
		String needTotalCount = "Y";
		
		List<String> entitlementMethods = new ArrayList<String>();
		entitlementMethods.add("Right to Technical Support.Open case");
		Asset asset = new Asset();
		String assetType = "contract";
		asset.setAssetType(assetType);
		
		List<User> userList = new ArrayList<User>();
		User userObj =  new User();
		userObj.setId(ccoid);
		userObj.setType("CCO");
		userList.add(userObj);
				
		FilterBy filterBy = new FilterBy();
		List<Object> contractNumberList = new ArrayList<Object>();
		contractNumberList.add(cn);
		filterBy.setContractNumber(contractNumberList);
		List<Object> stsCodeList = new ArrayList<Object>();
		stsCodeList.add("ACTIVE");
		filterBy.setStsCode(stsCodeList);
		asset.setFilterBy(filterBy);
				
		List<Asset> assetList = new ArrayList<Asset>();
		assetList.add(asset);
		
		request.setNeedTotalCount(needTotalCount);
		request.setAsset(assetList);
		request.setLoggedInId(ccoid);
		request.setCurrentPage("1");
		request.setEntitlementMethod(entitlementMethods);
		request.setPerPage("10");
		request.setUser(userList);
			
		return request;
	}
	
	private GetAssetsRequest getContractsForSerialNumberRequest(String sn ,String cn) 
	{
		GetAssetsRequest request= new GetAssetsRequest();
		String needTotalCount = "Y";
		List<String> entitlementMethods = new ArrayList<String>();
		entitlementMethods.add("Right to Technical Support.Open case");
		Asset asset = new Asset();
		String assetType = "contract";
		asset.setAssetType(assetType);
		
		FilterBy filterBy = new FilterBy();
		List<Object> contractNumberList = new ArrayList<Object>();
		contractNumberList.add(cn);
		filterBy.setContractNumber(contractNumberList);
		List<Object> serialNumberList = new ArrayList<Object>();
		serialNumberList.add(sn);
		filterBy.setSerialNumber(serialNumberList);
		asset.setFilterBy(filterBy);
		
		List<Asset> assetList = new ArrayList<Asset>();
		assetList.add(asset);
		request.setNeedTotalCount(needTotalCount);
		request.setAsset(assetList);
		request.setCurrentPage("1");
		request.setEntitlementMethod(entitlementMethods);
		request.setPerPage("10");
			
		return request;
	}
	
}



