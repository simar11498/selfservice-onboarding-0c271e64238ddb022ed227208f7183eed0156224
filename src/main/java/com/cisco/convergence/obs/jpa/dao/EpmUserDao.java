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

import com.cisco.convergence.obs.jpa.model.EpmUsers;

@Repository
@Transactional("imsApplTransactionManager")
public class EpmUserDao {
	private static Logger log = LogManager.getLogger(EpmUserDao.class.getName());
	
	@PersistenceContext(unitName="hibernatePU")
	private EntityManager entityManager;
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void insertUserExpiry(String ccoId,String uniqueId) {
		try {
			EpmUsers epmUsers = new EpmUsers();
			epmUsers.setCcoId(ccoId);
			entityManager.persist(epmUsers);
			}catch(Exception e){
				log.error("insertUserExpiry",e);
			}
		
	}
	
	public EpmUsers findEpmUser(String ccoId){
		try {
//			Query query = entityManager.createQuery(" from EpmUsers"
//					+ " where ccoId=?");
//		    query.setParameter(1, ccoId);
//		   	List countList = query.getResultList();
//			if(countList != null && countList.size() > 0){
//				EpmUsers epmUser = (EpmUsers)countList.get(0);
//				return epmUser;
//			}
		}catch(Exception e){
			log.error("findEpmUser",e);
		}
		return null;
	}
	
	public void findUserInAAACache(String ccoid, String partyId){
		
		String sqlQuery="select CCO_ID, APP_NAME, PARTNER_NAME, CUSTOMER_ID, CUSTOMER_NAME, ROLE_NAME, USER_ROLE_ASSOC_TYPE from aaa_admin.C_USER_PARTNER_CUSTOMER_ASSOC  where cco_id= '"+ccoid+"' and customer_id='"+partyId+"'";
		System.out.println(sqlQuery);
		Query q = entityManager.createNativeQuery(sqlQuery);
		List l = q.getResultList();
		System.out.println(l.size());
	}
}
