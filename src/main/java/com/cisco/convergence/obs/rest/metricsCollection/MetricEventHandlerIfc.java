package com.cisco.convergence.obs.rest.metricsCollection;

import java.util.List;

public interface MetricEventHandlerIfc {

	public void processEventCollection(List<OnBoardingMetricCollectionEvent> eventList);
}
