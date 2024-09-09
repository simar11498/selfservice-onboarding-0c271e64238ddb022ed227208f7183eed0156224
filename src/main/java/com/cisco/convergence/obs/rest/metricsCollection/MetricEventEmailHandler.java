package com.cisco.convergence.obs.rest.metricsCollection;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.cisco.convergence.obs.util.NotificationUtil;
import com.cisco.convergence.obs.util.PropertiesUtil;

@Component
public class MetricEventEmailHandler implements MetricEventHandlerIfc {
	
	private static Logger logger = Logger.getLogger(MetricEventEmailHandler.class);
	@Inject
	private NotificationUtil notificationUtil;
	
	@Inject
	private PropertiesUtil propertiesUtil;

	public void processEventCollection(List<OnBoardingMetricCollectionEvent> eventList) {
		logger.info("MetricEventEmailHandler received " + eventList.size() + " events for emailing.");
		if (eventList != null) {
			StringBuffer buffer = new StringBuffer();
			for(OnBoardingMetricCollectionEvent e : eventList) {
				buffer.append(e.toString()).append("\n");
			}
			//sendEmail(buffer.toString());
		}
	}
	
	public void sendEmail(String messageBodyPart) {
		logger.info("MetricEventEmailHandler sending the following events as email \n" + messageBodyPart);
		try {
			//notificationUtil.sendMetricsMessage(propertiesUtil.getMetricsEmailAlias(), messageBodyPart);
			logger.info("MetricEventEmailHandler successfully sent metric email");
		}
		catch (Exception e) {
			logger.error("Unable to send metrics notification email due to the following exception\n");
			e.printStackTrace();
			logger.info(messageBodyPart);
		}
	}

}
