package com.cisco.convergence.obs.jpa.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cisco.convergence.obs.jpa.model.UserExpiry;

@Repository
@Transactional("imsApplTransactionManager")
public class UserExpiryDao {
	private static Logger log = LogManager.getLogger(UserExpiryDao.class.getName());
	
	@PersistenceContext(unitName="hibernatePU")
	private EntityManager entityManager;
	
	@Transactional(value="imsApplTransactionManager",readOnly = false, propagation = Propagation.REQUIRED)
	public boolean isLinkExpired(String ccoId,String uniqueId) {
		boolean isEmailLinkExpired = false;
			try {
			Query query = entityManager.createQuery(" from UserExpiry"
					+ " where ccoId=? and unique_id=?");
		    query.setParameter(1, ccoId);
		    query.setParameter(2, uniqueId);
			List countList = query.getResultList();
			if(countList != null && countList.size() > 0){
				UserExpiry userExpiry = (UserExpiry)countList.get(0);
				if( Integer.valueOf(userExpiry.getVisited())  > 0){
					isEmailLinkExpired = true;
				}else{
					userExpiry.setCcoId(ccoId);
					userExpiry.setUnique_id(uniqueId);
					userExpiry.setVisited("1");
					updateUserExpiry(userExpiry,entityManager);
				}
			}else{
				//User entry should present in DB, otherwise it is treated as expiry
				isEmailLinkExpired = true;
			}
			}catch(Exception e){
			log.error("isLinkExpired",e);
		}
		return isEmailLinkExpired;
	}
	
	
	//
	@Transactional(value="imsApplTransactionManager",readOnly = false, propagation = Propagation.REQUIRED)
	public boolean isPageVisited(String ccoId,String uniqueId) {
		boolean isPageVisited = false;
			try {
			Query query = entityManager.createQuery(" from UserExpiry"
					+ " where ccoId=? and unique_id=? and visited = 1");
		    query.setParameter(1, ccoId);
		    query.setParameter(2, uniqueId);
			List countList = query.getResultList();
			if(countList != null && countList.size() > 0){
				 isPageVisited = true;
			}
			}catch(Exception e){
			log.error("isLinkExpired",e);
		}
		return isPageVisited;
	}
	
	
	
	@Transactional(value="imsApplTransactionManager",readOnly = false, propagation = Propagation.REQUIRED)
	private void updateUserExpiry(UserExpiry userExpiry,EntityManager entityManager) {
		try {
			entityManager.merge(userExpiry);
		  }catch(Exception e){
			log.error("insertUserExpiry",e);
		}
		
	}
	
	@Transactional(value="imsApplTransactionManager", readOnly = false, propagation = Propagation.REQUIRED)	
	public void insertUserExpiry(String ccoId,String uniqueId) {
		try {
			UserExpiry existinguserExpiry = findUserExpiry(ccoId);
			if(existinguserExpiry == null){
				insertExpiry(ccoId, uniqueId);
			}else{
				existinguserExpiry.setVisited("1");
				updateUserExpiry(existinguserExpiry,entityManager);	
				insertExpiry(ccoId, uniqueId);
			}
		}catch(Exception e){
			log.error("insertUserExpiry",e);
			System.err.println("EEEEEEERROR---------------->"+e.getMessage());
		}
	}

	private void insertExpiry(String ccoId, String uniqueId) {
		UserExpiry userExpiry = new UserExpiry();
		userExpiry.setCcoId(ccoId);
		userExpiry.setUnique_id(uniqueId);
		userExpiry.setVisited("0");
		entityManager.persist(userExpiry);
	}
	
	private UserExpiry findUserExpiry(String ccoId){
		try {
			Query query = entityManager.createQuery(" from UserExpiry"
					+ " where ccoId=?  order by  id desc");
		    query.setParameter(1, ccoId);
		    //query.setParameter(2, uniqueId);
			List countList = query.getResultList();
			if(countList != null && !countList.isEmpty()){
				return (UserExpiry) countList.get(0);
			}
		}catch(Exception e){
			log.error("getUserExpirty",e);
			System.err.println("EEEEEEERROR---------------->"+e.getMessage());
		}	
		return null;
	}
}
