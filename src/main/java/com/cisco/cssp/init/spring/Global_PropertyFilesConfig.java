package com.cisco.cssp.init.spring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 *
 * Property file loading with optional override properties loaded from $HOME/csapOverRide.properties.
 * <br><br>
 * Includes support for reading encrypted properties Note: property files may be
 * mixed with both encrypted and non encrypted entries. eg. <br>
 * <br><br>
 * 
 * factorySample.madeup.user=theSampleUser<br>
 * factorySample.madeup.password=ENC(
 * IlmCW3t72xMelXziSZQRWjXsMbXderPo70SxOFvgyuRBCk+1xpwZGg==)<br>
 * <br><br>
 * 
 * Encrypted entries can be generated via the CS-AP console. Desktop testing
 * can use the example in SpringJavaConfig_GlobalContextTest.java to encrypt.
 * 
 * @author pnightin
 * 
 * @see EncryptablePropertySourcesPlaceholderConfigurer
 * @see PropertySourcesPlaceholderConfigurer
 * 
 * 
 * 
 * @see <a href="http://www.jasypt.org/spring31.html"> Jascrypt </a>
 * 
 * @see <a
 *      href="http://static.springsource.org/spring/docs/3.2.x/spring-framework-reference/html/beans.html#beans-factory-extension-bpp">
 *      Spring Unified Properties </a>
 * 
 * @see <a
 *      href="http://blog.springsource.com/2011/02/15/spring-3-1-m1-unified-property-management/">
 *      Spring Property docs </a>
 * 
 * 
 */
@Configuration
public class Global_PropertyFilesConfig {

	static final private Logger logger = LogManager.getLogger(Global_PropertyFilesConfig.class);

	public final static String ENV_VARIABLE = "dev";

	// Note the spring injection of environment - which includes env vars.
	// This can be used to overwrit

	@Bean
    static public PropertySourcesPlaceholderConfigurer globalPropertySources(Environment env) {
    	
        PropertySourcesPlaceholderConfigurer p = new PropertySourcesPlaceholderConfigurer();
		List<Resource> locations = new ArrayList<Resource>() ;
        locations.add( new ClassPathResource("_tuning.properties") ) ;
        locations.add( new ClassPathResource("obs.properties") ) ;
        locations.add( new ClassPathResource("jdbc.properties") ) ;
        locations.add( new ClassPathResource("notification.properties") ) ;
        locations.add( new ClassPathResource("metricPersistence.properties") ) ;
//        locations.add( new ClassPathResource("cprEndPointUrls.properties") ) ;
        locations.add( new ClassPathResource("datestusers.properties") ) ;
        locations.add( new ClassPathResource("efEndPointUrls.properties"));
      
        
        
        p.setLocations( locations.toArray(new Resource[0]) );
        p.setIgnoreResourceNotFound( false );
        
		StringBuilder builder = new StringBuilder();
		builder.append("\n\n ==========================");
		builder.append("\n Loading Properties from : ");
		try {
			for (Resource r : locations) {
                System.out.println("\n==> Iterator Example..." + r.getFile().getAbsolutePath());
				builder.append("\n Resource: " + r + " location: "  + r.getFile().getAbsolutePath() );
			}
		} catch (IOException e) {
		    
			logger.error("globalPropertySources", e);
		}


		builder.append("\n Properties are code injected using the default ${}. Property values may optionally be encrypted" );
		builder.append("\n==========================\n\n");

		logger.warn(builder.toString());
		
        return p;
    }
    

}

