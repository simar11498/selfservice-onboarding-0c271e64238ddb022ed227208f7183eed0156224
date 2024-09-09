package com.cisco.convergence.obs.jpa.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cisco.convergence.obs.jpa.model.UserActions;
import com.cisco.convergence.obs.model.AccessLevel;
import com.cisco.convergence.obs.rest.metricsCollection.OnBoardingMetricCollectionEvent;

@Repository
@Transactional("imsApplTransactionManager")
public class MetricDao {

	@PersistenceContext(unitName="hibernatePU")
	private EntityManager entityManager;

	@Value("${hibernate.jdbc.batch_size}")
	private int batchSize;

	private static Logger LOG = LogManager.getLogger(MetricDao.class);

	@Transactional(value="imsApplTransactionManager",readOnly = false, propagation = Propagation.REQUIRED)
	public void insertMetricEvent(
			List<OnBoardingMetricCollectionEvent> eventList) {
		System.out.println("************MetricDao >> Received list of events to insert to the DB*************");
		LOG.info("MetricDao >> Received list of events to insert to the DB");
		try {
			List<UserActions> uactList = getUserActions(eventList);
			bulkSave(uactList);
			LOG.info("MetricDao >> Successfully batch inserted metric events into the database");
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(
					"Exception occured while trying to insert metrics to the Database. Stack trace follows \n",
					e);
		}

	}

	private List<UserActions> getUserActions(
			List<OnBoardingMetricCollectionEvent> eventList) throws Exception {
		List<UserActions> userActionList = new ArrayList<UserActions>();
		for (OnBoardingMetricCollectionEvent event : eventList) {
			UserActions userActions = new UserActions();
			if(event.getAccessLevel() != null  ){
				userActions.setUserRole(event.getAccessLevel().name());
			}else{
				userActions.setUserRole(AccessLevel.UNKNOWN.name());
			}	
			if(StringUtils.isEmpty(event.getCcoId())){
				userActions.setCcoId("UNKNOWN");
			}else{
				userActions.setCcoId(event.getCcoId());
			}	
			if(StringUtils.isEmpty(event.getContext())){
				userActions.setEventDescription("UNKNOWN");
			}else{
				userActions.setEventDescription(event.getContext());
			}			
			if(StringUtils.isEmpty(event.getContractNumber())){
				userActions.setContractNumber("UNKNOWN");
			}else{
				userActions.setContractNumber(event.getContractNumber());
			}
			if(StringUtils.isEmpty(event.getCrpartyId())){
				userActions.setPartyId("UNKNOWN");
			}else{
				userActions.setPartyId(event.getCrpartyId());
			}
			userActions.setSerialNumber(event.getSerialNumber());
			if(event.getEventTimestampAsDate() != null){
				userActions.setCreateTime(new Timestamp(event
						.getEventTimestampAsDate().getTime()));			
			}else{
				userActions.setCreateTime(new Timestamp(Calendar.getInstance().getTime().getTime()));
			}
			userActions.setActionId(event.getEventDescription().getValue());
			userActionList.add(userActions);
		}
		return userActionList;
	}

	public <T extends UserActions> Collection<T> bulkSave(Collection<T> entities) {
		final List<T> savedEntities = new ArrayList<T>(entities.size());
		int i = 0;
		for (T t : entities) {
			savedEntities.add(persistOrMerge(t));
			i++;
			if (i % batchSize == 0) {
				// Flush a batch of inserts and release memory.
				entityManager.flush();
				entityManager.clear();
			}
		}
		return savedEntities;
	}

	private <T extends UserActions> T persistOrMerge(T t) {
		if (t.getId() == null) {
			entityManager.persist(t);
			return t;
		} else {
			return entityManager.merge(t);
		}
	}
}
