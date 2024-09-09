package com.cisco.convergence.obs.util;

import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.cisco.convergence.obs.exception.LoginException;
import com.cisco.convergence.obs.rest.metricsCollection.MetricEventDeliveryService;
import com.cisco.convergence.obs.rest.metricsCollection.OnBoardingEventNotificationType;
import com.cisco.convergence.obs.rest.metricsCollection.OnBoardingMetricCollectionEvent;

@Component
public class MetricsClientUtil {
	
	@Inject
	private PropertiesUtil propertiesUtil;

	@Inject
	private MetricEventDeliveryService metricEventDeliveryService;
	
	public void captureMetricData(String ccoId, OnBoardingEventNotificationType evenType, String contract, String sn,
			Date timestamp, String crPartyId, String companyName){
		OnBoardingMetricCollectionEvent event = new OnBoardingMetricCollectionEvent(ccoId, timestamp, "", contract, sn, evenType, null, null);
		fireMetricCollectionEvent(event,ccoId);
	}
	public void captureMetricData(String ccoId, OnBoardingEventNotificationType evenType, String contract, String sn, Date timestamp, String crPartyId) throws LoginException {
		// (String cco, String time, String cpnyNm, String ctrctNum, String srlNum, OnBoardingEventNotificationType evntDes, AccessLevel acsLvl)
		OnBoardingMetricCollectionEvent event = new OnBoardingMetricCollectionEvent(ccoId, timestamp, "", contract, sn, evenType, null, null);
		fireMetricCollectionEvent(event,ccoId);
	}
	
	public void captureMetricDataWithContext(String ccoId, OnBoardingEventNotificationType evenType, String contract, String sn, Date timestamp, String context, String crPartyId) throws LoginException {
		// (String cco, String time, String cpnyNm, String ctrctNum, String srlNum, OnBoardingEventNotificationType evntDes, AccessLevel acsLvl, String context)
		OnBoardingMetricCollectionEvent event = new OnBoardingMetricCollectionEvent(ccoId, timestamp, "", contract, sn, evenType, null, null, context);
		fireMetricCollectionEvent(event,ccoId);
	}
	
	private void fireMetricCollectionEvent(OnBoardingMetricCollectionEvent event,String ccoId) {
		//Mocked for testing Metrics persistence -uncomment after testing
		if(!propertiesUtil.getTestUserProperties().get("test.user").equals(ccoId)) {
			metricEventDeliveryService.publishMetricEvent(event);
		}
	}
	
	/**
	 * Truncate the lengthy error message to of size 1750
	 * 
	 * @param errorMessage
	 * @return truncated error message
	 */


	public String getTruncatedErrorMessage(String errorMessage)
	{
		String truncatedErrorMessage=errorMessage;
		if(!StringUtils.isEmpty(errorMessage) && errorMessage.length()>1750)
		{
			truncatedErrorMessage=StringUtils.substring(errorMessage, 0,1749);

		}
		return truncatedErrorMessage;

	}
	
}
