package com.cisco.convergence.obs.rest.metricsCollection;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

@Component
public class MetricEventHandlerChainFactory {
	
	@Inject 
	private MetricEventEnrichmentHandler metricEventEnrichmentHandler;
	
	@Inject 
	private MetricEventEmailHandler metricEventEmailHandler;
	
	@Inject
	private MetricEventDBHandler metricEventDBHandler;
	
	public MetricEventHandlerChain createMetricEventHandlerChain() {
		return new MetricEventHandlerChain(metricEventEnrichmentHandler,
				metricEventEmailHandler, metricEventDBHandler);
	}

}
