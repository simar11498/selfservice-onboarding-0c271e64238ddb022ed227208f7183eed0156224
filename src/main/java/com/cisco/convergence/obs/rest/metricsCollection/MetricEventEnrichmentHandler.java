package com.cisco.convergence.obs.rest.metricsCollection;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.cisco.convergence.obs.model.AccessLevel;
import com.cisco.ata.rest.UserProfileView;
import com.cisco.convergence.obs.util.AAAClientUtil;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class MetricEventEnrichmentHandler implements MetricEventHandlerIfc {
	
	private static Logger logger = Logger.getLogger(MetricEventEnrichmentHandler.class);

	@Inject
	private AAAClientUtil aaaClientUtil;
	
	public void processEventCollection(List<OnBoardingMetricCollectionEvent> eventList) {
		if (eventList != null) {
			for (OnBoardingMetricCollectionEvent event : eventList) {
				populateEvent(event);
			}
		}
		logger.info("MetricEventEnrichmentHandler.processEventCollection() >> Forwarding events to the next handler");
	}
	
	private void populateEvent(OnBoardingMetricCollectionEvent event) {
		try {
			UserProfileView userProfileType = aaaClientUtil.getUserProfile(event.getCcoId());
			AccessLevel accessLevel = aaaClientUtil.getAccessLevel(userProfileType);
			String companyName = "Missing party association on user profile!";
			String partyId = null;
			if(userProfileType != null) {
				partyId = getPartyId(userProfileType);
				if(!StringUtils.isEmpty(partyId)){
					//Setting company name
					companyName = aaaClientUtil.getUserCompany(partyId);
				}
			}
			if (accessLevel == null) {
				event.setAccessLevel(AccessLevel.UNKNOWN);
			}
			else {
				event.setAccessLevel(accessLevel);
			}
			event.setCompanyName(companyName);
			event.setCrpartyId(partyId);
		}
		catch (Exception e) {
			logger.error("MetricEventEnrichmentHandler.processEventCollection() >> Exception occured while fetching data from AAA for cco id " + event.getCcoId());
			e.printStackTrace();
		}
	}
	

	private String getPartyId(UserProfileView userProfileType) {
		String partyId = null;
		if(userProfileType.geteFValidatedCRPartySiteID() == null && userProfileType.getEFValidatedPartyID() != null){
				partyId = userProfileType.getEFValidatedPartyID().getPartyID().getIdentifier();
		}
		else{
			if(userProfileType.geteFValidatedCRPartySiteID() != null && 
					userProfileType.geteFValidatedCRPartySiteID().getPartyID() != null)
			partyId = userProfileType.geteFValidatedCRPartySiteID().getPartyID().getIdentifier();
		}

		 return partyId;
	}
	

}
