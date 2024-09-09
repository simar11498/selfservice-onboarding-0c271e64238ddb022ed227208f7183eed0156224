package com.cisco.convergence.obs.jpa.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cisco.convergence.obs.jpa.model.UserDACompany;

@Repository
@Transactional("imsApplTransactionManager")
public class OBSDao {
	
private static Logger log = LogManager.getLogger(OBSDao.class.getName());
	
	@PersistenceContext(unitName="hibernatePU")
	private EntityManager entityManager1;
	
	
	/**
	 * 
	 * @param ccoId
	 * @return
	 */
	public UserDACompany findByCcoid(String ccoId) throws Exception{
		
			
			//System.out.println(entityManager1.getEntityManagerFactory().getProperties().get("hibernate.ejb.persistenceUnitName"));
			
			Query query = entityManager1.createQuery(" from UserDACompany"
					+ " where ccoId=? order by id desc");
		    query.setParameter(1, ccoId);
		   	List countList = query.getResultList();
			if(countList != null && countList.size() > 0){
				UserDACompany userSelDAPartyId = (UserDACompany)countList.get(0);
				return userSelDAPartyId;
			}
		
		return null;
	}
	
	/**
	 * 
	 * @param ccoId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserDACompany> findUserDAByCcoid(String ccoId) throws Exception{
		
			
			//System.out.println(entityManager1.getEntityManagerFactory().getProperties().get("hibernate.ejb.persistenceUnitName"));
			
			Query query = entityManager1.createQuery(" from UserDACompany"
					+ " where ccoId=? order by id desc");
		    query.setParameter(1, ccoId);
		   	List<UserDACompany> countList = query.getResultList();
		   	log.info("findUserDAByCcoid : countList size : "+ countList);
			if(countList != null && countList.size() > 0){
				List<UserDACompany> userSelDAPartyId = (List<UserDACompany>)countList;
				return userSelDAPartyId;
			}
		
		return null;
	}
	/**
	 * 
	 * @param ccoId
	 * @param partyId
	 * @return
	 */
	public UserDACompany findByCcoid(String ccoId, String partyId){
		try {
			
			//System.out.println(entityManager1.getEntityManagerFactory().getProperties().get("hibernate.ejb.persistenceUnitName"));
			
			Query query = entityManager1.createQuery(" from UserDACompany"
					+ " where ccoId=? and partyId=?");
		    query.setParameter(1, ccoId);
		    query.setParameter(2, partyId);
		   	List countList = query.getResultList();
			if(countList != null && countList.size() > 0){
				UserDACompany userSelDAPartyId = (UserDACompany)countList.get(0);
				return userSelDAPartyId;
			}
		}catch(Exception e){
			log.error("findByCcoid",e);
		}
		return null;
	}
	
	
	/**
	 * 
	 * @param ccoId
	 * @param partyId
	 */
	@Transactional(value="imsApplTransactionManager",readOnly = false, propagation = Propagation.REQUIRED)
	public void insertUserDACompany(String ccoId,String partyId, boolean guidCheck) throws Exception{
			//find if user exists in DB
			List<UserDACompany> userDACompanyList = findUserDAByCcoid(ccoId);
			log.info("Before removing data, userDACompanyList size :"+userDACompanyList);
			//If cco id with same partyid exists then remove then and insert new record
			if(userDACompanyList != null && !userDACompanyList.isEmpty()){
				for (UserDACompany userDACompany2 : userDACompanyList) {
					entityManager1.remove(userDACompany2);
					entityManager1.flush();
				}				
			}
			
			UserDACompany userSelDAComp = new UserDACompany();
			userSelDAComp.setCcoId(ccoId);
			userSelDAComp.setPartyId(partyId);
			userSelDAComp.setGuidCheck(String.valueOf(guidCheck));
			entityManager1.persist(userSelDAComp);
	
	}
	
	@Transactional(value="imsApplTransactionManager",readOnly = false, propagation = Propagation.REQUIRED)
	public void update(String guidCheck, String ccoId,String partyId) throws Exception{
		
		//String sql = "update USER_DA_OPTION set GUID_CHECK="+guidCheck+" where ccoid="+ccoId+" and partyId ="+partyId;
		
		String updateSql = "Update UserDACompany set guidCheck=:guidCheck where ccoId= :ccoId and partyId =:partyId";
		
		Query updateQuery = entityManager1.createQuery(updateSql);
		updateQuery.setParameter(1, guidCheck);
		updateQuery.setParameter(2, ccoId);
		updateQuery.setParameter(3, partyId);
		updateQuery.executeUpdate();
	}
	
	
	

}
