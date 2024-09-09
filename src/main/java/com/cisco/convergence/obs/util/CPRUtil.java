package com.cisco.convergence.obs.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cisco.aaa.cpr.jaxb.beans.NominateAdminInfoType;
import com.cisco.aaa.cpr.jaxb.beans.NominateAdminRoleType;
import com.cisco.aaa.cpr.jaxb.beans.NominateSingleAdminRequest;
import com.cisco.aaa.cpr.jaxb.beans.NominateSingleAdminResponse;
import com.cisco.aaa.cpr.jaxb.beans.NominationTypeValues;
import com.cisco.aaa.cpr.jaxb.beans.RequestHeaderGeneric;
import com.cisco.aaa.cpr.jaxb.beans.TOUAValues;
import com.cisco.ata.rest.UserProfileView;
import com.cisco.ca.csp.ef.update.client.EFUpdateClient;
import com.cisco.ca.csp.ef.update.model.StatusResult;
import com.cisco.convergence.obs.exception.LoginException;
import com.cisco.convergence.obs.exception.OnBoardingException;
import com.cisco.convergence.obs.model.DANominationStatus;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

@Component
public class CPRUtil {

	private Logger logger= LogManager.getLogger(CPRUtil.class.getClass());
	@Value("${CPR_DA_NOMINATE_URL}")
	private String daNominateUrl="https://wsgi-stage.cisco.com/cpr/services/v1/nominateadmin";
	
	@Value("${CPR_GEN_ID}")
	private String cprGenId="sntcSelfserv.gen";
	
	@Value("${CPR_PWD}")
	private String cprPwd="titanium";
	
	@Value("${CALLING_APP_NAME}")
	private String callingAppName="SNTC SELF Service OnBoarding";
	
	@Inject
	private AAAClientUtil aaaClientUtil;
	
	@Inject
	private EFUpdateClient efUpdateClient;
	
	public static void main(String[] args){
		CPRUtil cprUtil = new CPRUtil();
		//, "priytam1001@yopmail.com", "100711"
		DANominationStatus daStatus = cprUtil.nominateDA("priytam1001");
		System.out.println(daStatus.getMessage()+daStatus.getErrorCode()+daStatus.getErrorMessage());
	}
	public DANominationStatus nominateDAWithInputs(String ccoId,String partyId){
		//DANominationStatus daNominationStatus = new DANominationStatus();
		//SA - Fix
		DANominationStatus daNominationStatus = callDAService(ccoId,null,partyId);
		return daNominationStatus;
	}
	public StatusResult nominateDA(String ccoId,String partyId) throws OnBoardingException{
		try{
		return efUpdateClient.nominateDA(ccoId, partyId);
		}catch(Exception e){
			throw new OnBoardingException(e);
		}
	}
	public DANominationStatus nominateDA(String ccoId){
		System.out.println("^^^^^^^^^^^^nominateDA^^^^^^^^^^^^^^^^");
		DANominationStatus daNominationStatus = new DANominationStatus();
		try {
			UserProfileView userProfileType =  aaaClientUtil.getUserProfile(ccoId);
			String partyId = null;
			if(userProfileType.getEFValidatedPartyID() != null &&
					userProfileType.getEFValidatedPartyID().getPartyID() != null){
				partyId = userProfileType.getEFValidatedPartyID().getPartyID().getIdentifier();
			}
			if(org.apache.commons.lang.StringUtils.isEmpty(partyId)){
				daNominationStatus.setMessage("FAILURE");
				daNominationStatus.setErrorMessage("Missing EF validated Party ID in the user profile");
				//TODO send email to support team about missing EF validated party id
				return daNominationStatus;
			}
			String email = null;
			if(userProfileType.getRegistrationInfo() != null && 
				userProfileType.getRegistrationInfo().getEmailAddress() != null){
				 email = userProfileType.getEmailAddress();
			}
			daNominationStatus = callDAService(ccoId,email,partyId);

		} catch (Exception e) {
			logger.error("Error in calling EF API", e);
//			daNominationStatus.setErrorCode(String.valueOf(response.getStatus()));
//			daNominationStatus.setErrorMessage("Not able to Parse the response");
			daNominationStatus.setMessage("FAILURE");
		}
		return daNominationStatus;
	}
	
	private DANominationStatus callDAService(String ccoId,String email,String partyId){
		DANominationStatus daNominationStatus = new DANominationStatus();
		try {
			NominateSingleAdminRequest nominateSARequest = createDARequest(ccoId, email, partyId);
			//Send request to EF
			ClientResponse response =  sendEFRequest(NominateSingleAdminRequest.class,nominateSARequest);
			if(response != null && response.getStatus() != 200){
				daNominationStatus.setErrorCode(String.valueOf(response.getStatus()));
				daNominationStatus.setErrorMessage("Couldn't reach server");
				return daNominationStatus;
			}else{
				daNominationStatus.setMessage("OK");
			}
			//Parse response
			String responseXML = getResponseInXML(response);
			
			if(!org.apache.commons.lang.StringUtils.isEmpty(responseXML)){
				StringReader reader = new StringReader(responseXML);
				JAXBContext jaxb = JAXBContext.newInstance(NominateSingleAdminResponse.class);
				Unmarshaller unMarshaller = jaxb.createUnmarshaller();
				NominateSingleAdminResponse res = (NominateSingleAdminResponse)unMarshaller.unmarshal(reader);
				List<com.cisco.aaa.cpr.jaxb.beans.Error> errorList = res.getError();
				if(errorList != null && !errorList.isEmpty()){
					for (com.cisco.aaa.cpr.jaxb.beans.Error error : errorList) {
						daNominationStatus.setErrorCode(error.getErrorCode());
						daNominationStatus.setErrorMessage(error.getErrorMessage());
						System.out.println(error.getErrorCode()+"-Message-"+error.getErrorMessage());
					}
				}else{
					daNominationStatus.setNominated(true);
				}
				
			}else{
				daNominationStatus.setErrorCode(String.valueOf(response.getStatus()));
				daNominationStatus.setErrorMessage("Not able to Parse the response");
				daNominationStatus.setMessage("FAILURE");
			}
			

		} catch (Exception e) {
			logger.error("Error in calling EF API", e);
//			daNominationStatus.setErrorCode(String.valueOf(response.getStatus()));
//			daNominationStatus.setErrorMessage("Not able to Parse the response");
			daNominationStatus.setMessage("FAILURE");
		}
		return daNominationStatus;
	}
	private ClientResponse sendEFRequest(Class requestClass,Object requestObject){
		
		ClientResponse response = null;
		try {
			DefaultClientConfig config = new DefaultClientConfig();
			
			WebResource service = Client.create(config).resource(daNominateUrl);
			HTTPBasicAuthFilter httpBasicAuthFilter = new HTTPBasicAuthFilter(cprGenId,cprPwd);
			
			service.addFilter(httpBasicAuthFilter);

			// POST notification
			JAXBContext jc = JAXBContext.newInstance(requestClass);
			Marshaller marshaller = jc.createMarshaller();
			
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			StringWriter xml = new StringWriter();
			marshaller.marshal(requestObject, xml);
			
			System.out.println("templatedtoToXml():: "+requestClass+" URL: "+daNominateUrl);

			System.out.println("templatedtoToXml():: "+requestClass+" Request to server: "+xml.toString());

			response = service.type("application/xml").accept("application/xml").post(ClientResponse.class,xml.toString());
			
			System.out.println("templatedtoToXml():: "+requestClass+" Response from server: "+ response.getStatus()+"-----:"+response.getClientResponseStatus());
		} catch (Exception e) {
			logger.error("Error in calling EF API sendEFRequest", e);
		}
	
		return response;
	}
	
	private String getResponseInXML(ClientResponse clientResponse)throws Exception{
		String responseXml = null;
		if(clientResponse != null && clientResponse.getEntityInputStream() != null){
			try {
				StringWriter writer = new StringWriter();
				IOUtils.copy(clientResponse.getEntityInputStream(), writer, "UTF-8");
				responseXml = writer.toString();
			} catch (Exception e) {
				logger.error("Error in parsing EF Response", e);
			}
		}
		return responseXml;
	}
	private NominateSingleAdminRequest createDARequest(String ccoId,String email, String partyId) {
		NominateSingleAdminRequest nominateSARequest = new NominateSingleAdminRequest();
		NominateAdminInfoType nominateAdminInfoType = new NominateAdminInfoType();
		nominateAdminInfoType.setAdminRoleName(NominateAdminRoleType.PARTY_DA);
		nominateAdminInfoType.setCcoId(ccoId);
		if(!StringUtils.isEmpty(email)){
			nominateAdminInfoType.setEmailAddress(email);
		}
		nominateAdminInfoType.setPartyID(partyId);
		nominateAdminInfoType.setNominationType(NominationTypeValues.MANUAL);
		nominateAdminInfoType.setTOUA(TOUAValues.N);
		nominateSARequest.getNominateAdminInfoType().add(nominateAdminInfoType);
		
		RequestHeaderGeneric requestHeaderGeneric = new RequestHeaderGeneric();
		requestHeaderGeneric.setCallingAppName(callingAppName);
		requestHeaderGeneric.setCallingUser(cprGenId);
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(Calendar.getInstance().getTime());
		XMLGregorianCalendar date2 = null;
		try {
			date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		requestHeaderGeneric.setRequestDateTime(date2);
		nominateSARequest.setRequestHeader(requestHeaderGeneric);
		return nominateSARequest;
	}	
	public UserProfileView getUserProfileForDPLFlag(String ccoId)throws LoginException{
		try {
			Properties dynaPpropties = new Properties();
			dynaPpropties.setProperty("aaa.credentials.userId",cprGenId);
			dynaPpropties.setProperty("aaa.credentials.password", cprPwd);
			return AAAClientUtil.getAAAClient().getUserProfileViewByCcoIdGET(ccoId);
			
		} catch (Exception e) {
			System.err.println("**********FAILED TO GET USER PROFILE*********"+e.getMessage());
			throw new LoginException("FAILED TO GET USER PROFIL FOR CCO ID:"+ccoId);
		}
	}
}
