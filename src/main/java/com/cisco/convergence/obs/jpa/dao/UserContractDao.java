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

import com.cisco.convergence.obs.jpa.model.UserContract;

@Repository
@Transactional("imsApplTransactionManager")
public class UserContractDao {
	private static Logger log = LogManager.getLogger(UserContractDao.class.getName());
	
	@PersistenceContext(unitName="hibernatePU")
	private EntityManager entityManager;
	
	public UserContract findByCcoid(String ccoId){
		try {
			
			System.out.println(entityManager.getEntityManagerFactory().getProperties().get("hibernate.ejb.persistenceUnitName"));
			
			Query query = entityManager.createQuery(" from UserContract"
					+ " where ccoId=?");
		    query.setParameter(1, ccoId);
		   	List countList = query.getResultList();
			if(countList != null && countList.size() > 0){
				UserContract contract = (UserContract)countList.get(0);
				return contract;
			}
		}catch(Exception e){
			log.error("findByCcoid",e);
		} 
		return null;
	}
	private UserContract findByCcoid(String ccoId,String contract,EntityManager entityManager){
		try {
			
			Query query = entityManager.createQuery(" from UserContract"
					//+ " where ccoId=? and contract=?");
					+ " where ccoId=?");
		    query.setParameter(1, ccoId);
		    //query.setParameter(2, contract);
			List countList = query.getResultList();
			if(countList != null && countList.size() > 0){
				UserContract userContract = (UserContract)countList.get(0);
				return userContract;
			}
			
		}catch(Exception e){
			log.error("findByCcoid--",e);
		}
		return null;
	}
	
	//MYCODECHANGES starts
	@Transactional(value="imsApplTransactionManager", propagation = Propagation.REQUIRED)
	public String checkAAAcache(String ccoId, String customerId){
		
		try{
			
		System.out.println("Cco_id: "+ccoId+" customerid :"+customerId);
		String sqlquery="SELECT CCO_ID,APP_NAME,CUSTOMER_ID,CUSTOMER_NAME,ASSIGNEE_PARTYID FROM C_USER_PARTNER_CUSTOMER_ASSOC WHERE CCO_ID = ? and CUSTOMER_ID = ?";
		Query query = entityManager.createNativeQuery(sqlquery);
		
	    query.setParameter(1, ccoId);
	    query.setParameter(2, customerId);
	    //query.setParameter(2, contract);
		List countList = query.getResultList();
		if(countList != null && countList.size() > 0){
			System.out.println("CCOID Exists in AAACache");
			return "true";
		}
		else 
		{
			System.out.println("CCOID Doesn't Exists in AAACache");
			return "false";
			}
		}
		catch(Exception e){
			log.error("checkAAAcacheExists for CCOID--",e);
		}
		return "null";
	}
	
	//MYCODECHANGES starts
	@Transactional(value="imsApplTransactionManager", propagation = Propagation.REQUIRED)
	public List<String> getIgnoredEmailLinks(String fromDate, String toDate){
		
		try{
			
		System.out.println("Fromdate: "+fromDate+" toDate :"+toDate);
		
		String sqlquery="select distinct(cco_id) from ims_appl.user_actions where action_id=106 and create_time< sysdate-"+fromDate+" and create_time > sysdate-"+toDate+" minus select distinct(cco_id) from ims_appl.user_actions where action_id=108 and cco_id in (select distinct(cco_id) from ims_appl.user_actions where action_id=106 and create_time < sysdate-"+fromDate+" and create_time >sysdate-"+toDate+")";
		System.out.println("sqlqueryforEmail "+sqlquery);
		Query query = entityManager.createNativeQuery(sqlquery);
		
	    // query.setParameter(1, fromDate);
	    //query.setParameter(2, toDate);
	    //query.setParameter(2, contract);
		List<String> countList = query.getResultList();
		if(countList != null && countList.size() > 0){
			System.out.println("NOT_CLICKED_ON_EMAILLINK count "+countList.size());
			return countList;
		}
		else 
		{
			System.out.println("No such users NOT_CLICKED_ON_EMAILLINK");
			return countList;
		}
		}
		catch(Exception e){
			log.error("Exception while executing query ",e);
			return null;
		}
		
	}
	@Transactional(value="imsApplTransactionManager", propagation = Propagation.REQUIRED)
	public String checkPartyInAAAcache(String ccoId, String customerId){
		
		try{
			
		System.out.println("Cco_id: "+ccoId+" customerid :"+customerId);
		String sqlquery="SELECT CCO_ID,APP_NAME,CUSTOMER_ID,CUSTOMER_NAME,ASSIGNEE_PARTYID FROM C_USER_PARTNER_CUSTOMER_ASSOC WHERE CUSTOMER_ID = ?";
		Query query = entityManager.createNativeQuery(sqlquery);
	    query.setParameter(1, customerId);
	    //query.setParameter(2, contract);
		List countList = query.getResultList();
		if(countList != null && countList.size() > 0){
			System.out.println("PartyId Exists in AAACache");
			return "true";
		}
		else 
		{
			System.out.println("PartyId Doesn't Exists in AAACache");
			return "false";
			}
		}
		catch(Exception e){
			log.error("checkAAAcacheExists for PartyID--",e);
		}
		return "null";
	}
	
	
	//MYCODECHANGES ends
	/**
	 * 
	 * @param ccoId
	 * @param contract
	 */
	@Transactional(value="imsApplTransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public void insertUserContract(String ccoId,String contract) {
			try {
			UserContract uContract = findByCcoid(ccoId,contract,entityManager);
			//log.info("uContract find : "+ uContract.getCcoId()+ " - "+ uContract.getContract());
			//If cco id with same contract exists then no need to do anything
			if(uContract == null){
				UserContract userContract = new UserContract();
				userContract.setCcoId(ccoId);
				userContract.setContract(contract);
				entityManager.persist(userContract);
			}else{
				uContract.setCcoId(ccoId);
				uContract.setContract(contract);				
				entityManager.merge(uContract);								
			}
		}catch(Exception e){
			e.printStackTrace();
			log.error("insertUserContract",e);
			}
	}
	
		/**
		 * 
		 * @param serviceLines
		 * @return
		 */
		@Transactional(value="imsApplTransactionManager")
		public String checkGSPEntitlementEnable(List<String> serviceLines){	
			String gspEntitlementStatus="NO";
			StringBuffer svsLineBuffer = new StringBuffer();
			
			for(String serviceLine : serviceLines ){
				svsLineBuffer.append("'"+serviceLine+"'").append(",");
			}
			String svcLineFinale = svsLineBuffer.toString();
			svcLineFinale = svcLineFinale.substring(0,svcLineFinale.lastIndexOf(","));
			try{
				//SELECT SERVICELINE  FROM OV_GSP_SMT_ENTL WHERE SERVICELINE IN ('SMARTNET',"SNTC')
				String entitlementQuery = "SELECT SERVICELINE  FROM OV_GSP_SMT_ENTL WHERE ltrim(rtrim(SERVICELINE)) IN (" +svcLineFinale+")";
				Query query = entityManager.createNativeQuery(entitlementQuery);
				List data = query.getResultList();
				if(data.size() > 0){
					gspEntitlementStatus = "YES";			
				}
				
				String entitlementQuery1 = "SELECT SERVICELINE  FROM OV_GSP_SMT_ENTL WHERE SERVICELINE IN (" +svcLineFinale+")";
				Query query1 = entityManager.createNativeQuery(entitlementQuery1);
				List data1 = query1.getResultList();
				if(data1.size() > 0){
					gspEntitlementStatus = "YES";			
				}
				
				
				
			}catch(Exception e){
				log.error(" Error: While fetching data from onview for GSP Service Line. ");									
			}		
			return gspEntitlementStatus;
		}
}
