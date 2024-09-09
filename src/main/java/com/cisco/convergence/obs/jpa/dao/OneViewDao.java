package com.cisco.convergence.obs.jpa.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cisco.convergence.obs.jpa.model.ContractInfo;
import com.cisco.convergence.obs.jpa.model.ContractGSPEntitlementStatus;

@Repository
@Transactional("OVTransactionManager")
public class OneViewDao {
	
	@PersistenceContext(unitName="hibernateOVPU")
	private EntityManager entityManager;

	private static Logger log = LogManager.getLogger(OneViewDao.class.getName());

	public ContractInfo findServiceLine(String contractNumber) {
		ContractInfo contractInfo = new ContractInfo();
		List<String> serviceLineList = new ArrayList<String>();

		try {
			/*
			 * CAFI2DEV NRT_ADMIN NRTADTX!9999 
			 */
			String serviceLineQuery = "select distinct SERVICE_LINE_NAME from XXCCS_DS_SAHDR_CORE core,"
					+ "XXCCS_DS_CVDPRDLINE_DETAIL prddetal, XXCCS_DS_INSTANCE_DETAIL detail"
					+ " where core.contract_id=prddetal.contract_id and "
					+ "prddetal.instance_id=detail.INSTANCE_ID and core.CONTRACT_NUMBER='"
					+ contractNumber + "'";
			Query query = entityManager.createNativeQuery(serviceLineQuery);

			List sLineList = query.getResultList();
			
			if (sLineList != null && !sLineList.isEmpty()) {
				log.info("----SLineList----"+sLineList.size());
				for (Object object : sLineList) {
					serviceLineList.add(object.toString());
				}
			}
			contractInfo.setServiceLine(serviceLineList);
		} catch (Exception e) {
			log.error("findServiceLine", e);
		}
		return contractInfo;
	}
	public long findBillToSiteUseId(String contractNumber,String serialNumber){
		long siteUseId = -1;
		try {
			String partyQuery = "SELECT DISTINCT CORE.BILL_TO_SITE_USE_ID"
					+ " FROM XXCCS_DS_SAHDR_CORE CORE,XXCCS_DS_CVDPRDLINE_DETAIL PRDDETAL,"
					+ "XXCCS_DS_INSTANCE_DETAIL DETAIL WHERE "
					+ "CORE.CONTRACT_ID = PRDDETAL.CONTRACT_ID "
					+ "AND PRDDETAL.INSTANCE_ID = DETAIL.INSTANCE_ID "
					+ "AND CORE.CONTRACT_NUMBER ='"+contractNumber+"' "
					+ "AND SERIAL_NUMBER ='"+serialNumber+"'";
			Query query = entityManager.createNativeQuery(partyQuery);

			List siteUseIdList = query.getResultList();
			
			if (siteUseIdList != null && !siteUseIdList.isEmpty() && siteUseIdList.size() ==1) {
				log.info("----SLineList----"+siteUseIdList.size());
				siteUseId = Long.valueOf(siteUseIdList.get(0).toString());
			}else{
				log.error("No SiteUse Found for this customer");
			}
		} catch (Exception e) {
			log.error("No SiteUse Found for this customer",e);
		}
		return siteUseId;
	}
	
	
	
	// Contract status
	public String checkContractStatus(String contractNumber,String serialNumber){	
		String contractStatus="OTHER_THAN_ACTIVE";
		try {
			String contractStatusQuery = "SELECT CONTRACT_STATUS"
					+ " FROM XXCCS_DS_SAHDR_CORE CORE,XXCCS_DS_CVDPRDLINE_DETAIL PRDDETAL,"
					+ " XXCCS_DS_INSTANCE_DETAIL DETAIL WHERE  "
					+ "CORE.CONTRACT_ID = PRDDETAL.CONTRACT_ID "
					+ "AND PRDDETAL.INSTANCE_ID = DETAIL.INSTANCE_ID "
					+ "AND CORE.CONTRACT_NUMBER ='"+contractNumber+"' "
					+ "AND SERIAL_NUMBER ='"+serialNumber+"'"
					+" and CONTRACT_STATUS='ACTIVE' ";
			Query query = entityManager.createNativeQuery(contractStatusQuery);

			List contractStatusList = query.getResultList();
			
			if (contractStatusList != null && !contractStatusList.isEmpty() && contractStatusList.size() > 0) {
				log.info("---- contractStatusList ----"+contractStatusList.size());
				 contractStatus= contractStatusList.get(0).toString();
			}else{
				log.error(" Contract Status Other than Active");
			}
		} catch (Exception e) {
			log.error("Check Contract Status Failed with Exception:  ",e);
		}
		return contractStatus;
	}
	
	
}
