package com.cisco.convergence.obs.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.inject.Inject;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.stereotype.Service;

import com.cisco.ata.rest.UserProfileView;
import com.cisco.convergence.obs.exception.LoginException;
import com.cisco.convergence.obs.exception.OnBoardingException;
import com.cisco.convergence.obs.jpa.dao.UserContractDao;
import com.cisco.convergence.obs.jpa.dao.UserExpiryDao;
import com.cisco.convergence.obs.jpa.model.UserContract;
import com.cisco.convergence.obs.notification.model.CodeImpl;
import com.cisco.convergence.obs.notification.model.EnumDeliveryChannelExtImpl;
import com.cisco.convergence.obs.notification.model.HeaderDTO;
import com.cisco.convergence.obs.notification.model.NotificationDTO;
import com.cisco.convergence.obs.notification.model.TagMappingInfo;
import com.cisco.convergence.obs.notification.model.TemplateDTO;
import com.cisco.convergence.obs.notification.model.User;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;



@Service
public class NotificationUtil {
	
	private Logger logger= LogManager.getLogger(NotificationUtil.class.getClass());

//	private static final String USER ="Customer";
	private static final String EMAIL="EMAIL";
	private static final String JKS ="JKS";
	private static final String SSL ="SSL";
	private static final String daString = "Dear Administrator"; 
	private static final String dcString = "Dear Customer";
	private static final String sincerely = "Sincerely";
	private static final String csntc= "Cisco Smart Net Total Care";
	private static final String CHINA="CHINA";

	@Inject
	private UserExpiryDao userExpiryDao;

	@Inject
	private UserContractDao userContractDao;
	
	@Inject
	private PropertiesUtil propertiesUtil;
	
	@Inject
	private AAAClientUtil aaaClientUtil;
	
	public static void main(String[] args)throws Exception{
		NotificationUtil util = new NotificationUtil();
		util.sendNotification("vpriyata","en");
	}

	private String decryptPWd(String pwd){
		/*
		 * TODO a work around till we find a way to set ENV_VARIABLE OF THIS USER NAME AND ALGORITHM
		 */
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword("jasypt");    // we HAVE TO set a password
		encryptor.setAlgorithm("PBEWithMD5AndTripleDES");    // optionally set the algorithm
		return encryptor.decrypt(pwd);  // myText.equals(plainText)

	}

	private void initContext(NotificationDTO notificationDTO) throws OnBoardingException {

		SSLContext ctx = null;
		//SA - Fix
		InputStream stream = null;
		InputStream jksStream = null;
		
		try {
			KeyStore keystore = KeyStore.getInstance(JKS);
			String pwd = decryptPWd(propertiesUtil.getCertpassword());

			stream = this.getClass().getClassLoader().
					getResourceAsStream(propertiesUtil.getKeyStoreName());
			keystore.load(stream,pwd.toCharArray());
			
		
			String keyStorePwd = decryptPWd(propertiesUtil.getPassword());
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keystore, keyStorePwd.toCharArray());
	
			KeyStore trustStore = KeyStore.getInstance(JKS);
			jksStream = this.getClass().getClassLoader().getResourceAsStream(propertiesUtil.getCertName());

			trustStore.load(jksStream,pwd.toCharArray());
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(trustStore);
			ctx = SSLContext.getInstance(SSL);
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		} catch (Exception e){
			System.err.println("******Not able to load certificates!**************"+e.getMessage());
			logger.error("******Not able to load certificates!**************",e);
			throw new OnBoardingException("Not able to load certificates!");
		}finally{ //SA - Fix
			try {
				stream.close();
				jksStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		if(ctx != null){
			sendEmail(notificationDTO, ctx);
			/*
			 * COPYING EACH EMAIL THAT GOES OUT TO THE DEBUG EMAIL ALIAS
			 * */

			String bccEmail = propertiesUtil.getDebugEmailAlais();
			setToUsers(bccEmail,notificationDTO);
			sendEmail(notificationDTO, ctx);
		}
		
	}

	private void sendEmail(NotificationDTO notificationDTO, SSLContext ctx) throws OnBoardingException {
		
		System.out.println("^^^^^^^^^^^^CONTEXT INIALIZED AND SENDING REQUEST^^^^^^^^^^^^^^^^");
		ClientResponse response = null;
		try {
			
			DefaultClientConfig config = new DefaultClientConfig();
			config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
					new HTTPSProperties(null, ctx));
			
			WebResource service = Client.create(config).resource(propertiesUtil.getBaseuri());

			String notificationUserName = decryptPWd(propertiesUtil.getUsername());
			String notificationPassword = decryptPWd(propertiesUtil.getPassword());
			
			HTTPBasicAuthFilter httpBasicAuthFilter = new HTTPBasicAuthFilter(notificationUserName,notificationPassword);
			
			service.addFilter(httpBasicAuthFilter);

			// POST notification
			JAXBContext jc = JAXBContext.newInstance(NotificationDTO.class);
			Marshaller marshaller = jc.createMarshaller();
			
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			StringWriter xml = new StringWriter();
			marshaller.marshal(notificationDTO, xml);
			
			logger.info("templatedtoToXml():: Response from server: "+ propertiesUtil.getBaseuri() );
			System.out.println("templatedtoToXml():: Response from server: "+propertiesUtil.getBaseuri());

			System.out.println("templatedtoToXml():: Request to server: "+xml.toString());

			response = service
					.path(propertiesUtil.getServicePath())
					.type("application/xml").accept("application/xml")
					.post(ClientResponse.class,xml.toString());

			logger.info("templatedtoToXml():: Response from server: "
							+ response );
			System.out.println("templatedtoToXml():: Response from server: "
							+ response );


		} catch (Exception e) {
			System.err.println("ERROR in Getting Response From NSCSO"+e.getMessage());
			logger.error("******ERROR in Getting Response From NSCSO**************",e);
			throw new OnBoardingException(e.getMessage());
		}
		//If status is not success tell user to try again!
		if(response != null && response.getStatus() != 200){
			throw new OnBoardingException("Status:"+response.getStatus());
		}
	}

	public void sendMetricsMessage(String email,String message) throws OnBoardingException{
		Properties resourceBundle = propertiesUtil.getResourceBundle("en");
		String subject = resourceBundle.getProperty("notification.metrics.from.subject");
		sendEmail(null, email,false,resourceBundle,message,null,subject,null);
	}
	public void sendNotification(String ccoId,boolean addExpiryLink,String locale,String contractNo) throws OnBoardingException{
		Properties resourceBundle = propertiesUtil.getResourceBundle(locale);
		sendEmail(ccoId, null,false,resourceBundle,null,contractNo,null,null);
	}
	
	public void sendCustomMessage(String email,String message,String subject,String fromUserString) throws OnBoardingException{
		Properties resourceBundle = propertiesUtil.getResourceBundle("en");
		if(!StringUtils.isEmpty(email)){
			sendEmail(null, email,false,resourceBundle,message,null,subject,fromUserString);
		}	
	}
	
	// mail to cin team , mail to user and DA
	public void sendCustomMessage(String ccoId, String email,String message,String subject,String fromUserString) throws OnBoardingException{
		Properties resourceBundle = propertiesUtil.getResourceBundle("en");
		if(!StringUtils.isEmpty(email)){
			// instrumentation for production testing -- prod user mock 
			//overriding EMail for test users on prod				
			if(!StringUtils.isEmpty(ccoId) && propertiesUtil.getTestUserProperties().get("test.user").equals(ccoId)){
					email = "sntc_self_onboard_test@cisco.com";
			}
			
			sendEmail(null, email,false,resourceBundle,message,null,subject,fromUserString);
		}	
	}
	
	public void sendNotification(String ccoId,String locale) throws OnBoardingException{
		System.out.println("sendNotification()::::::::::::::::::::::: Processing started");
		String toEmail =  getUserEmailByCCOId(ccoId);
		System.out.println("toEmail:::::::::::::::::::::::"+toEmail);
		if(StringUtils.isEmpty(toEmail)){
			throw new OnBoardingException("Missing user profile");
		}
		Properties resourceBundle = propertiesUtil.getResourceBundle(locale);
		if(resourceBundle == null){
			resourceBundle = propertiesUtil.getResourceBundle("en");
		}
		logger.info("resourceBundle:::::::::::::::::::"+resourceBundle);
		sendEmail(ccoId, toEmail,true,resourceBundle,null,null,null,null);
	}

	private void sendEmail(String ccoId, String toEmail,boolean addExpiryLink,Properties resourceBundle,
			String message,String contractNo,String subject,String fromUserString)throws OnBoardingException{
		logger.info("sendNotification():: Processing started");
		logger.info("messageBody1 "+message);
		//A back up if for some reason resource bundle comes as null then consider this as English
		if(resourceBundle == null){
			resourceBundle = propertiesUtil.getResourceBundle("en");
		}
		NotificationDTO notificationDTO = new NotificationDTO();
		
		CodeImpl codeImpl = new CodeImpl();
		codeImpl.setType(EMAIL);
		
		EnumDeliveryChannelExtImpl deliveryChannel = new EnumDeliveryChannelExtImpl();
		deliveryChannel.setCodeImpl(codeImpl);
		
		HeaderDTO headerDTO = new HeaderDTO();
		headerDTO.setIsFlexGuiRequest(false);
		headerDTO.setAppId(propertiesUtil.getAppId()); 
		
		// instrumentation for production testing -- prod user mock 
		//overriding EMail for test users on prod				
		if(!StringUtils.isEmpty(ccoId) && propertiesUtil.getTestUserProperties().get("test.user").equals(ccoId)){
				logger.info("instrumentation for production testing : Before : "+toEmail);
				toEmail = "sntc_self_onboard_test@cisco.com";
				logger.info("instrumentation for production testing : After : "+toEmail);
		}
		
		
		if(propertiesUtil.getTurnOffNotificationsToExternal().equalsIgnoreCase("false")){
			logger.info("instrumentation for production testing : Before : "+toEmail);
			toEmail = "sntc_self_onboard_test@cisco.com";
			logger.info("instrumentation for production testing : After : "+toEmail);
			
		}
		
		System.out.println("************TO EMAIL*****************>"+toEmail);
		//Setting to Users
		setToUsers(toEmail,notificationDTO);
		
		TemplateDTO templateDTO = new TemplateDTO();
		templateDTO.setTemplateName(propertiesUtil.getTemplateName());
        
        notificationDTO.setContentType("text/html");	
		notificationDTO.setDelayed(false);
		notificationDTO.setDeliveryChannel(deliveryChannel);
		notificationDTO.setHeader(headerDTO);
		notificationDTO.setTemplate(templateDTO);
	//	notificationDTO.setToUsers(toUserList);

		notificationDTO.setReplaceFromAddressFlag(true);

		User fromUser = new User();
		fromUser.setEmailAddress(propertiesUtil.getFromEmail());
		
		notificationDTO.setSender(fromUser);

		TagMappingInfo info=new TagMappingInfo();

		info.setKey(propertiesUtil.getTaggingKey());
		
		
		
		/*
		 * check if ccoId is null. Metric notification emails don't have a CCO ID
		 * as they go to a generic email alias.
		 */
		String messageBody = null;
		if(StringUtils.isEmpty(fromUserString)){
		 fromUserString = dcString; 
		}
		if (!StringUtils.isEmpty(ccoId)) {
			UserProfileView userProfileType = null;
			try {
				userProfileType = aaaClientUtil.getUserProfile(ccoId);
				System.out.println("******userProfileType**********"+userProfileType);
			} catch (LoginException e) {
				System.err.println("************ERROR IN GETTING USER PROFILE TYPE********"+e.getMessage());
				logger.error("sendEmail", e);
				
			}
			String firstName="", lastName="";
			if(userProfileType != null){ //get message body only when you get user profile from EF
				//Sending notification to the customer with a unique link
				if(addExpiryLink){
						if( userProfileType.getRegistrationInfo() != null){
							firstName = userProfileType.getRegistrationInfo().getFirstName();
							lastName = userProfileType.getRegistrationInfo().getLastName();
							fromUserString=resourceBundle.getProperty("notification.msg.dear")+" "+ firstName+" "+lastName;
						}else{
							fromUserString= resourceBundle.getProperty("notification.msg.dear")+" "+resourceBundle.getProperty("notification.msg.customer");							
						}					

					messageBody = setCustomerMessage(ccoId, resourceBundle,templateDTO,notificationDTO);
				}
				else{//Send confirmation email after verifying the email
					// Its prior code - this is based on old work flow. potentially remove.
					fromUserString =  daString;
					messageBody = setEFMessage(ccoId, resourceBundle, templateDTO,userProfileType,notificationDTO,contractNo);
				}
			}
		}else{
			//Sending metrics report
			setCustomSubject(resourceBundle,notificationDTO,subject);
			messageBody = message;
		}
		//If no message body then return error.
		if(StringUtils.isEmpty(messageBody)){
			System.out.println("Missing message body");
//			throw new OnBoardingException("Missing message body");
			messageBody ="Missing message body";
		}
		info.setValue(messageBody);

		if(notificationDTO.getListTagInputs() == null){
			List<TagMappingInfo> tagMappingList=new ArrayList<TagMappingInfo>();
			notificationDTO.setListTagInputs(tagMappingList);
			
		}
		
		notificationDTO.getListTagInputs().add(info);

		TagMappingInfo tagMappingInfo=new TagMappingInfo();
		tagMappingInfo.setKey(propertiesUtil.getTaggingName());
		tagMappingInfo.setValue(fromUserString);
	
		notificationDTO.getListTagInputs().add(tagMappingInfo);
	
		notificationDTO.setToBeNotifiedSecurely(false);
	
		initContext(notificationDTO);
	}

	private void setCustomSubject(Properties resourceBundle,
			NotificationDTO notificationDTO,String subject) {
			TagMappingInfo subjecTagInfo=new TagMappingInfo();
		subjecTagInfo.setKey(propertiesUtil.getTagginsSubjectKey());
		subjecTagInfo.setValue(subject);
		
		if(notificationDTO.getListTagInputs() == null){
			List<TagMappingInfo> tagMappingList=new ArrayList<TagMappingInfo>();
			notificationDTO.setListTagInputs(tagMappingList);
		}
		notificationDTO.getListTagInputs().add(subjecTagInfo);
	}

	private String getContract(String ccoId){
		try {
			UserContract userContract = userContractDao.findByCcoid(ccoId);
			if(userContract != null && userContract.getContract() != null){
				return userContract.getContract();
			}
		} catch (Exception e) {
			logger.info("--getContract---",e);
		}
		return "UNKNOWN";
	}
	private String setEFMessage(String ccoId, Properties resourceBundle,
			TemplateDTO templateDTO,UserProfileView userProfileType,
			NotificationDTO notificationDTO,String contractNo){
		StringBuffer message = new StringBuffer();
		if(userProfileType != null && userProfileType.getRegistrationInfo() != null){
			try {
				String firstName = (userProfileType.getRegistrationInfo().getFirstName());
				String lastName = (userProfileType.getRegistrationInfo().getLastName());
				String email  = (userProfileType.getRegistrationInfo().getEmailAddress());
				String contract = null;
				if(StringUtils.isEmpty(contractNo)){
					contract = getContract(ccoId);
				}else{
					contract = contractNo;
				}
				String companyName = getCompanyName(userProfileType);
				
				message.append("CCO ID:").append(ccoId).append("<br/>");
				message.append("Contract #:").append(contract).append("<br/>");
				message.append("Name of User:").append(firstName).append(" ").append(lastName).append("<br/>");
				message.append("Company Name:").append(companyName).append("<br/>");
				message.append("Email:").append(email).append("<br/>");
				
				message.append("<br/><br/>").append(sincerely).append(",");
				message.append("<br/>").append(csntc);
				
				setCINEmailSubject(resourceBundle, notificationDTO,firstName, lastName);
			
				String toEmail = getToEmail(userProfileType);
				String bccEmail = propertiesUtil.getDebugEmailAlais();
				
				if(!StringUtils.isEmpty(ccoId) && propertiesUtil.getTestUserProperties().get("test.user").equals(ccoId)){
						logger.info("instrumentation for production testing : Before : "+toEmail);
						toEmail = "sntc_self_onboard_test@cisco.com";
						bccEmail ="sntc_self_onboard_test@cisco.com";						
						logger.info("instrumentation for production testing : After : "+toEmail);
				}
				
				if(propertiesUtil.getTurnOffNotificationsToExternal().equalsIgnoreCase("false")){
					logger.info("instrumentation for production testing : Before : "+toEmail);
					toEmail = "sntc_self_onboard_test@cisco.com";
					bccEmail ="sntc_self_onboard_test@cisco.com";
					logger.info("instrumentation for production testing : After : "+toEmail);
					
				}
				//Setting to Users list
				setToUsers(toEmail,notificationDTO);
				setBccUsers(bccEmail,notificationDTO);
			} catch (Exception e) {
				System.err.println("**********FAILED TO GET DATA FOR EF MESSAGE"+e.getMessage());
				logger.error("******FAILED TO GET DATA FOR EF MESSAGE**************",e);
//				e.printStackTrace();
			}
		}
		return message.toString();
	}

	private String getToEmail(UserProfileView userProfileType) {
		String country = null;
		if(userProfileType.getRegistrationInfo().getLocations() != null && 
				userProfileType.getRegistrationInfo().getLocations().getPostalAddresses() != null && 
					userProfileType.getRegistrationInfo().getLocations().
						getPostalAddresses().getPrimaryPostalAddress() != null){
			country = userProfileType.getRegistrationInfo().getLocations().getPostalAddresses().getPrimaryPostalAddress().getCountry();
		}
		String toEmail = propertiesUtil.getEfOtherEmailAlias();
		if(!StringUtils.isEmpty(country) && country.equals(CHINA)){
			toEmail = propertiesUtil.getZhEmailAlias();
		}
		return toEmail;
	}

	private void setCINEmailSubject(Properties resourceBundle,
			NotificationDTO notificationDTO, String firstName, String lastName) {
//		String subject = resourceBundle.getProperty("notification.ef.from.subject");
//		subject = firstName+" "+lastName+" "+subject;
		String subject = resourceBundle.getProperty("notification.msg.generic.subject");
		TagMappingInfo subjecTagInfo=new TagMappingInfo();
		subjecTagInfo.setKey(propertiesUtil.getTagginsSubjectKey());
		subjecTagInfo.setValue(subject);
		
		if(notificationDTO.getListTagInputs() == null){
			List<TagMappingInfo> tagMappingList=new ArrayList<TagMappingInfo>();
			notificationDTO.setListTagInputs(tagMappingList);
		}
		notificationDTO.getListTagInputs().add(subjecTagInfo);
	}

	private String getCompanyName(UserProfileView userProfileType) {
		String companyName = "Missing company name in user profile";
		try {
			String partyId = AAAClientUtil.getPartyId(userProfileType);
			if(!StringUtils.isEmpty(partyId)){
				companyName= aaaClientUtil.getUserCompany(partyId);
			}
		} catch (Exception e) {
			logger.error("getCompanyName",e);
		}
		return companyName;
	}
	
	private void setToUsers(String toEmail,NotificationDTO notificationDTO){
		if(!StringUtils.isEmpty(toEmail)){
			String[] emailArray = toEmail.split(",");
			List<User> toUserList = new ArrayList<User>();
			if(emailArray != null && emailArray.length > 0){
				for (int i = 0; i < emailArray.length; i++) {
					User toUser = new User();
					toUser.setEmailAddress(emailArray[i]);
					toUserList.add(toUser);	
				}
			}else{
				//back up condition. It never reach here
				User toUser = new User();
				toUser.setEmailAddress(toEmail);
			}
			notificationDTO.setToUsers(toUserList);
		}

	}

	private void setBccUsers(String toEmail,NotificationDTO notificationDTO){
		if(!StringUtils.isEmpty(toEmail)){
			String[] emailArray = toEmail.split(",");
			List<User> bccUserList = new ArrayList<User>();
			if(emailArray != null && emailArray.length > 0){
				for (int i = 0; i < emailArray.length; i++) {
					User toUser = new User();
					toUser.setEmailAddress(emailArray[i]);
					bccUserList.add(toUser);	
				}
			}else{
				//back up condition. It never reach here
				User bccUser = new User();
				bccUser.setEmailAddress(toEmail);
			}
			notificationDTO.setBccUsers(bccUserList);
		}

	}
	private String setCustomerMessage(String ccoId, Properties resourceBundle,
			TemplateDTO templateDTO,NotificationDTO notificationDTO) {
		StringBuffer bodyWithLinkBuffer = new StringBuffer();
		
		String subject = resourceBundle.getProperty("notification.customer.from.subject") ;
		TagMappingInfo subjecTagInfo=new TagMappingInfo();
		subjecTagInfo.setKey(propertiesUtil.getTagginsSubjectKey());
		
		subjecTagInfo.setValue(subject);
		
		if(notificationDTO.getListTagInputs() == null){
			List<TagMappingInfo> tagMappingList=new ArrayList<TagMappingInfo>();
			notificationDTO.setListTagInputs(tagMappingList);
		}
		notificationDTO.getListTagInputs().add(subjecTagInfo);
		String uniqueLink = getUniqueLink(ccoId);
		//If we can't insert the link DB then we dont send notification
		if(!StringUtils.isEmpty(uniqueLink)){
			bodyWithLinkBuffer.append(resourceBundle.getProperty("notification.msg.customer.body"));
			bodyWithLinkBuffer.append("<br/><br/><br/><a href="+uniqueLink+">"+uniqueLink);
			bodyWithLinkBuffer.append("</a>").append("<br/><br/>");
			bodyWithLinkBuffer.append(resourceBundle.getProperty("notification.msg.end"));
			bodyWithLinkBuffer.append("<br/>").append(resourceBundle.getProperty("notification.from.footer"));
			bodyWithLinkBuffer.append("<br/><br/><br/>").append(resourceBundle.getProperty("notification.msg.footer1"));
			bodyWithLinkBuffer.append("<a href='").append(resourceBundle.getProperty("notification.smartnet.community.url")).append("'>");
			bodyWithLinkBuffer.append(resourceBundle.getProperty("notification.msg.footer3")).append("</a>");
			//bodyWithLinkBuffer.append("<br/><br/>").append(resourceBundle.getProperty("notification.msg.footer2"));
		}
		return bodyWithLinkBuffer.toString();
	}
	

	private String getUserEmailByCCOId(String ccoId){
		String toEmail ="";
		try {
			UserProfileView userProfileType = aaaClientUtil.getUserProfile(ccoId);
			if(userProfileType != null && userProfileType.getRegistrationInfo() != null){
				toEmail = userProfileType.getRegistrationInfo().getEmailAddress();
			}
		} catch (LoginException e) {
			logger.error("getUserEmailByCCOId---",e);
		}
		return toEmail;
	}

	private String getUniqueLink(String ccoId){
		System.out.println("**********GETTING UNIQUE LINK*********");
		String verifyEmailLink = "";
		try {
			String uniqueID = UUID.randomUUID().toString();
			//generating unique id and inserting into table
			userExpiryDao.insertUserExpiry(ccoId,uniqueID);
			verifyEmailLink = propertiesUtil.getVerificationLink()+"?uId="+uniqueID;
		} catch (Exception e) {
			logger.error("Failed to GEt Unique Link--->",e);
		}
		System.out.println("**********UNIQUE LINK*********"+verifyEmailLink);
		return verifyEmailLink;
	}
	
	public boolean verifyEmail(String ccoId,String uniqueId){
		return userExpiryDao.isLinkExpired(ccoId, uniqueId);
	}
	public boolean isVisited(String ccoId,String uniqueId){
		return userExpiryDao.isPageVisited(ccoId, uniqueId);
	}
}