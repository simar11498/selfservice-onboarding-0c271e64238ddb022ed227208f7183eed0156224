package com.cisco.cssp.init.spring;

import java.lang.reflect.Method;
import java.net.URL;

import javax.inject.Inject;

import org.apache.log4j.PropertyConfigurator;
//import org.apache.log4j.helpers.Loader;
import org.apache.logging.log4j.core.util.Loader; //replacement for above Loader class(migrating 1.x to 2.x)
//import org.apache.logging.log4j.core.config.properties;
import org.springframework.web.SpringServletContainerInitializer;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import org.apache.logging.log4j.core.LoggerContext;


/**
 * 
 * Bootstrap leverages Spring initialization, which itself uses a Servlet 3.0 ServletContainerInitializer. Spring jar include a meta-inf entry
 * which causes the Spring Container to be invoked by JEE startup, and then Spring looks for any class in class-loader that 
 * implements WebApplicationInitializer. 
 * 
 * Note - this mechanism has a dependency on Jar Scanning - which can increase bootup time. While there are some alternatives to a
 * web.xml based approach, for reference app we will stick with the standard model.
 * 
 *      
 * @see <a
 *      href="http://wikicentral.cisco.com/display/SFAECSAP/Runtime+Support">
 *      CSSP Wiki </a>
 *      
 * @see <a
 *      href="http://static.springsource.org/spring/docs/3.2.x/spring-framework-reference/html/new-in-3.0.html#new-java-configuration">
 *      Spring Java Config</a>
 *      
 * @see SpringServletContainerInitializer
 * @see javax.servlet.ServletContainerInitializer
 * 
 * 
 * <p>     
 *      
 * Container Initialization:
 * <p><IMG SRC="doc-files/init.jpg">
 * 
 * Spring Overview
 * <p><IMG SRC="doc-files/spring.jpg">
 * 
 */
public class Bootstrap extends AbstractAnnotationConfigDispatcherServletInitializer {

	private static String validateStringType(String s) {
	       if(s instanceof String){
	            return s;
	       }
	    return null;
	}
	
	public Bootstrap() {
		
		// Nice place to load log4j. 
		URL configFile = null;
		String resource = "log4j.properties" ;
		// configFile = getClass().getResource(resource);
		
		// tomcat context is where log4j will be found. Here is some magic for getting that context
		configFile = getThreadContextLoader().getResource(resource);
		

		StringBuffer sbuf = new StringBuffer();
		sbuf.append("\n\t JVM Classpath is: "
				+ validateStringType(System.getProperty("java.class.path")).replaceAll(";", validateStringType(System.getProperty("line.separator"))));

		sbuf.append( "\n\n" +  this.getClass().getName() + " invoked by extending WebApplicationContext") ;
		sbuf.append("\n==== Stage 1: LOG4J INIT from static initialize probably already done. Add log4j.debug to verify.");
		sbuf.append("\n======== Stage 2: LOG4J INIT from Servlet container ThreadContext:");
		sbuf.append("\n==============  Resource " + resource );
		sbuf.append("\n==============  Location:" + configFile + "\n\n");
		

		System.out.println(sbuf);

		if (configFile == null) {
			System.out
					.println("\n\t*** ERROR ==> Cannot find config file in class path: "
							+ configFile);
		} else {
			
			PropertyConfigurator.configure(configFile) ;
			//Tomcat 7 is not liking config and watch. We can use JMX to control instead
//			PropertyConfigurator.configureAndWatch(configFile.getPath(),
//					5000);
			logger.warn("Log4j initialized from " + configFile.getPath() );
		}
	}

	/**
	 * 
	 * Stolen from log4j. We use the servlet container classpath as this will pick up property file from web-inf/classes
	 * Otherwise the root tomcat classpath is used - which does not include servlet container.
	 * 
	 * @return
	 * 
	 * @see Loader
	 */
	public static ClassLoader getThreadContextLoader() {

		// Are we running on a JDK 1.2 or later system?
		ClassLoader loader = null ;
		try {

			Method method  = Thread.class.getMethod("getContextClassLoader", null);
			loader = (ClassLoader) method.invoke(Thread.currentThread(), null) ;
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
			// We are running on JDK 1.1
			return null;
		}

		return loader;
	}
	
	
	
	/** 
	 * 
	 * Global beans available in all contexts. Note that security and jaxws are loaded here
	 * too allow testing of Global context exclusive of we container
	 * 
	 */
    @Override
    protected Class<?>[] getRootConfigClasses() {
         return new Class[] { Global_Context.class, SSO_Schedular_Config.class};
    	// return null ;
    }

    /**
     * Note these contexts hide from each other
     * 
     * 
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {
       //  return new Class[] { };
       return new Class[] { MvcContext.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/helloSvc/*" };
    }
    
}

