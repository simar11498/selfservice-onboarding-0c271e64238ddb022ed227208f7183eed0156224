package com.cisco.convergence.obs.rest.metricsCollection;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.convergence.obs.jpa.dao.MetricDao;

@Component
public class MetricEventDBHandler implements MetricEventHandlerIfc {
	
//	private static Logger logger = Logger.getLogger(MetricEventDBHandler.class);
	
	@Autowired
	private MetricDao metricDao;

	public void processEventCollection(List<OnBoardingMetricCollectionEvent> eventList) {
		if (metricDao != null) {
			if ((eventList != null) && (eventList.size() > 0)) {
				metricDao.insertMetricEvent(eventList);
			}
		}
	}

}
