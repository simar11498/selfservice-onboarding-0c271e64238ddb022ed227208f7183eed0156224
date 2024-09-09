package com.cisco.convergence.obs.rest.metricsCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class MetricEventDeliveryService {

	// Static variables
	
	@Value("${metrics.delivery.threshold}")
	private int eventAcumulationThreshold;

	private static Logger logger = LogManager.getLogger(MetricEventDeliveryService.class);
	
//	static {
//		_instance = new MetricEventDeliveryService();
//		logger.info("SecondMetricEventDeliveryService instantiated");
//	}
	
	@Inject
	private MetricEventHandlerChainFactory metricEventHandlerChainFactory;
	
	// Instance variables
	
	private Queue<OnBoardingMetricCollectionEvent> queue;
	private final ReentrantLock lock;
	private final Condition deliveryThresholdReached;
	
	private MetricEventHandlerChain eventHandlerChain;
	/*
	 * A separate counter is used as the size() method on a linked list backed queue is O(n)
	 * and not a constant time operation.
	 */
	private final AtomicInteger eventCounter;
	private Thread metricEventDispatcherThread;
	private final ScheduledThreadPoolExecutor backupDispatcherExecutor;
	
	MetricEventDeliveryService() {
		queue = new ConcurrentLinkedQueue<OnBoardingMetricCollectionEvent>();
		lock = new ReentrantLock(true);
		deliveryThresholdReached = lock.newCondition();
		eventCounter = new AtomicInteger(0);
//		eventHandlerChain = metricEventHandlerChainFactory.createMetricEventHandlerChain();
		startMetricEventDispatcherThread();
		backupDispatcherExecutor = new ScheduledThreadPoolExecutor(2, new SimpleThreadFactory());
		backupDispatcherExecutor.scheduleWithFixedDelay(new BackupEventDispatcherTask(), 3600, 3600, TimeUnit.SECONDS);
	}
	
	public void publishMetricEvent(OnBoardingMetricCollectionEvent event) {
		logger.info("SecondMetricEventDeliveryService received event " + eventCounter.get());
		try {
			/*
				No need to protect depositing the message into the queue and the incrementing
				of the counter by a single lock, i.e we do not try to maintain a compound
				invariant that the counter must always accurately reflect the exact number
				of messages in the queue. 
				This is done so as to not introduce any latency on the UI code path
				for this important but non-critical piece of functionality.
				BUt note that despite not having a single lock to protect the 
				invariant, the invariant will still be satisfied, just not at all
				times. There will be brief periods during which the counter will
				go out of sync with the number of elements in the queue but it will
				eventually catch up and will always oscillate within a bounded range. 
			*/
			queue.offer(event);
			int count = eventCounter.incrementAndGet();
			logger.info("Total Queue Count >>><<<" + count);
			if (count >= eventAcumulationThreshold) {
				logger.info("Number of events in queue crossed delivery threshold, signaling the dispatcher");
				if (lock.tryLock()) {
					if (count >= eventAcumulationThreshold) {
						deliveryThresholdReached.signal();	
					}
				}
			}				
		}
		finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();	
			}
			
		}
	}
	
	private void startMetricEventDispatcherThread() {
		metricEventDispatcherThread = new Thread(new EventDispatcher(), "CountBasedMetricEventDispatcherThread");
		metricEventDispatcherThread.setPriority(Thread.MIN_PRIORITY);
		metricEventDispatcherThread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
		metricEventDispatcherThread.start();
		logger.info("MetricEventDeliveryService >> Count based MetricEvent dispatcher thread started");
	}
	
	
	/*
	 * This task is triggered every time the number of metric events in the Queue exceeds the 
	 * delivery threshold which is currently set to 50.
	 * There will be times when this thread will try to run concurrently with 
	 * the backup dispatcher thread. Either this thread will win the race to 
	 * acquire the lock or the backup dispatcher thread will. 
	 */
	private class EventDispatcher implements Runnable {

		public void run() {
			
			try {
				MetricEventDeliveryService.this.lock.lock();
				while (true) {
					logger.info("Main metric event dispatcher thread waiting on threshold condition");
					MetricEventDeliveryService.this.deliveryThresholdReached.await();
					logger.info("Main metric event dispatcher received signal to drain event queue");
					logger.info("There are " + eventCounter.get() + " number of events in the queue.");
					/*
					 *  once control comes here this thread has woken up after getting signaled by the condition.
					 *  No need to check if the condition for which the signal was given is still true, because
					 *  even if the rest of the code below is executed, the email gets sent out only if\
					 *  any event was found in the queue. 
					 *  
					 *  Start draining the queue, one message at a time, decrementing the counter also one message
					 *  at a time, since the queue and the counter are not protected by a single lock but are
					 *  independently thread safe.
					 */
					List<OnBoardingMetricCollectionEvent> eventList = new ArrayList<OnBoardingMetricCollectionEvent>();
					OnBoardingMetricCollectionEvent eventRef = queue.poll();
					while (eventRef != null) {
						eventCounter.decrementAndGet();
						eventList.add(eventRef);
						eventRef = queue.poll(); // retrieve all the events from the queue, not just 50
					}
					if (eventList.size() > 0) {
						if(eventHandlerChain == null){
							eventHandlerChain= metricEventHandlerChainFactory.createMetricEventHandlerChain();
						}
						eventHandlerChain.processEventCollection(eventList);	
					}
				} // go back and wait for the next set of events
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			finally {
				MetricEventDeliveryService.this.lock.unlock();
			}
		}
	}
	
	
	private class DefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

		public void uncaughtException(Thread t, Throwable e) {
			if (t == MetricEventDeliveryService.this.metricEventDispatcherThread) {
				MetricEventDeliveryService.this.startMetricEventDispatcherThread();
			}
		}
		
	}
	
	
	/*
	 * This task will be run every 30 minutes by a scheduled executor.
	 * It will contend with the EventDispatcher which will run every time
	 * the number of events crosses the delivery threshold.
	 */
	private class BackupEventDispatcherTask implements Runnable {

		public void run() {
			logger.info("Backup metric event dispatcher task running. There are " + eventCounter.get() + " number of events to be delivered.");
			try {
				MetricEventDeliveryService.this.lock.lock();
				List<OnBoardingMetricCollectionEvent> eventList = new ArrayList<OnBoardingMetricCollectionEvent>();
				OnBoardingMetricCollectionEvent eventRef = queue.poll();
				while (eventRef != null) {
					eventCounter.decrementAndGet();
					eventList.add(eventRef);
					eventRef = queue.poll(); // retrieve all the events from the queue, not just 50
				}
				if (eventList.size() > 0) {
					if(eventHandlerChain == null){
						eventHandlerChain= metricEventHandlerChainFactory.createMetricEventHandlerChain();
					}
					eventHandlerChain.processEventCollection(eventList);	
				}
			}
			catch (Throwable e) {
				// suppress all exceptions, otherwise this task will not be executed again by the scheduler
			}
			finally {
				MetricEventDeliveryService.this.lock.unlock();
			}
		}
		
	}
	
	
	 private static class SimpleThreadFactory implements ThreadFactory {
		   public Thread newThread(Runnable r) {
		     Thread t = new Thread(r, "TimeBasedMetricEventDispatcherThread");
		     t.setPriority(Thread.MIN_PRIORITY);
		     return t;
		   }
	 }
	
}
