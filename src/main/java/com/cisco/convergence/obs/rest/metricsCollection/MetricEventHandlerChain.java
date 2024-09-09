package com.cisco.convergence.obs.rest.metricsCollection;

import java.util.ArrayList;
import java.util.List;

public class MetricEventHandlerChain implements MetricEventHandlerIfc {

	private List<MetricEventHandlerIfc> handlers;
	
	public MetricEventHandlerChain() {
		this.handlers = new ArrayList<MetricEventHandlerIfc>();
	}
	
	public MetricEventHandlerChain(MetricEventHandlerIfc... eventHandlerIfcs) {
		this();
		for (MetricEventHandlerIfc h : eventHandlerIfcs) {
			this.handlers.add(h);
		}
	}
	
	public void addHandler(MetricEventHandlerIfc h) {
		handlers.add(h);
	}
	
	
	public void removeHandler(MetricEventHandlerIfc h) {
		handlers.remove(h);
	}
	
	public void processEventCollection(List<OnBoardingMetricCollectionEvent> eventList) {
		for (MetricEventHandlerIfc h : handlers) {
			try {
				h.processEventCollection(eventList);	
			}
			catch (Exception e) {
			}
		}
	}

}
