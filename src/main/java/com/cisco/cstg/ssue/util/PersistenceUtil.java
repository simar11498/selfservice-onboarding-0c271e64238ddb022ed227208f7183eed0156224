package com.cisco.cstg.ssue.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class PersistenceUtil {
		
	private static Logger log = LogManager.getLogger(PersistenceUtil.class.getName());
	private static final String PROPERTY = "ssueap.model.PersistenceUnit";
	
	private static final String OBS_PROPERTY="ssueap.model.OBS.PersistenceUnit";
	
	private static Properties ssueAppProperties = null;

	// Cache
	//private static EntityManagerFactory emf = null;
	//SA - Fix
	private static String hbm2ddl = null;

	// using initialize-on-demand holder class pattern for initializing the singleton emf object
	private static class InstanceHolder{
		private static final EntityManagerFactory emf = createEntityManagerFactory(); 
	}

	public static EntityManagerFactory getEntityManagerFactory(){
		return InstanceHolder.emf; //line1
	}
	
	public static EntityManagerFactory getOBSEntityManagerFactory(){
		return createOBSEntityManagerFactory(); //line1
	}

	private static String validateMethod(String s) {
	       if(s instanceof String){
	            return s;
	       }
	    return null;
	}
	
	private static synchronized EntityManagerFactory createOBSEntityManagerFactory() {
		// Allow override from system properties
		//String persistenceUnit = System.getProperty(OBS_PROPERTY);
		// Security related Fix - SA Report
		String persistenceUnit = validateMethod(System.getProperty(OBS_PROPERTY)); 
		InputStream is =null;
		if (persistenceUnit == null) {
			// get it from the property file
			if(ssueAppProperties == null){
				ssueAppProperties = new Properties();
				is = PersistenceUtil.class.getResourceAsStream("/ssueap.properties");
				try{
					ssueAppProperties.load(is);
				} catch (Exception exception) {
					// throw error
					log.fatal("Caught Exception in PersistenceUtil.getEntityManagerFactory - " + exception);
					log.error (exception.getMessage(), exception);
				} finally {
					if(is != null){
						try {
							is.close();
						} catch (IOException e) {
							log.error (e.getMessage(), e);
						}
					}
				}
			}
			persistenceUnit = ssueAppProperties.getProperty(OBS_PROPERTY);
			
		}

		Map<String, Object> configOverrides = new HashMap<String, Object>();
		configOverrides.put("hibernate.hbm2ddl.auto", hbm2ddl);
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, configOverrides);

		return emf;
	}
	
	private static synchronized EntityManagerFactory createEntityManagerFactory() {
		// Allow override from system properties
		//String persistenceUnit = System.getProperty(PROPERTY);
		// Security related Fix - SA Report
		String persistenceUnit = validateMethod(System.getProperty(PROPERTY)); 
		if (persistenceUnit == null) {
			// get it from the property file
			Properties properties = new Properties();
			InputStream is = PersistenceUtil.class.getResourceAsStream("/ssueap.properties");
			try {

				properties.load(is);
				persistenceUnit = properties.getProperty(PROPERTY);
			} catch (Exception exception) {
				// throw error
				log.fatal("Caught Exception in PersistenceUtil.getEntityManagerFactory - " + exception);
				log.error (exception.getMessage(), exception);
			} finally {
				if(is != null){
					try {
						is.close();
					} catch (IOException e) {
						log.error (e.getMessage(), e);
					}
				}
			}
		}

		Map<String, Object> configOverrides = new HashMap<String, Object>();
		configOverrides.put("hibernate.hbm2ddl.auto", hbm2ddl);
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, configOverrides);

		return emf;
	}
}
