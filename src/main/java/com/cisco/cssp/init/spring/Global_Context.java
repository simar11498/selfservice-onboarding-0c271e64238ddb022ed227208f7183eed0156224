package com.cisco.cssp.init.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.web.SpringServletContainerInitializer;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * Spring Global Context Definition - loaded via Bootstrap - each major service
 * class is imported, rather then a single mega config - beans defined here will
 * be Globally accessible across all MVC contexts
 * 
 * Consideration - if deployment unit is only a single mvc context, you might
 * decide to only define context there.
 * 
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

// Everything is now loaded via java config
// @ImportResource({ "classpath:springGlobalContext.xml" })

@Aspect
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import({ Global_PropertyFilesConfig.class, Global_JpaConfig.class,Global_JpaConfig_OV.class})
@ComponentScan({ "com.cisco.convergence","com.cisco.aaa","com.cisco.ca.csp.ef"})

public class Global_Context {

	final private Logger logger = LogManager.getLogger(getClass());

	@Inject
	Environment env;

	Global_Context() {

		logger.info("\n\n****************** Loading Spring Global Context *************************");
	}

	/**
	 * 
	 * - Ref: http://static.springsource.org/spring/docs/3.0.7.RELEASE/spring-
	 * framework-reference/html/jmx.html Note: replaceExisting is used in tomcat
	 * hot deployments ref.
	 * http://www.javacodegeeks.com/2011/06/zero-downtime-deployment
	 * -and-rollback.html
	 * 
	 * @return
	 */
	@Bean
	public AnnotationMBeanExporter annotationMBeanExporter() {
		AnnotationMBeanExporter mbeanExporter = new AnnotationMBeanExporter();

		// logger.info("******************* Init code" +
		// env.getProperty("db.url"));

		// mbeanExporter
		// .setRegistrationBehavior(AnnotationMBeanExporter.REGISTRATION_REPLACE_EXISTING);
		mbeanExporter
				.setRegistrationPolicy(RegistrationPolicy.REPLACE_EXISTING);
		return mbeanExporter;
	}

	/**
	 * Bean name can be specified using name attribute, if none specified then
	 * method name becomes bean name. Multiple beans of same type can be
	 * specified using coma separated value in name.
	 * 
	 * @return
	 */
//	@Bean(name = "aTrivialRestSampleId")
//	public RestTemplate getRestTemplate() {
//		RestTemplate restTemplate = new RestTemplate();
//		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
//		messageConverters.add(new SimpleConverter());
//		restTemplate.setMessageConverters(messageConverters);
//		return restTemplate;
//	}

	@Bean(name = "csAgentRestTemplate")
	public RestTemplate csAgentRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();

		MappingJacksonHttpMessageConverter jacksonConverter = new MappingJacksonHttpMessageConverter();
		jacksonConverter.setPrefixJson(false);
		jacksonConverter.setSupportedMediaTypes(new ArrayList<MediaType>(Arrays.asList(MediaType.APPLICATION_XML)));

		messageConverters.add(jacksonConverter);

		restTemplate.setMessageConverters(messageConverters);
		return restTemplate;
	}
//	@Bean(name = "corsFilter")
//	public SimpleCORSFilter getCustomFilter(){
//		SimpleCORSFilter filter = new SimpleCORSFilter();
//	    return filter;
//	}
//	
}
