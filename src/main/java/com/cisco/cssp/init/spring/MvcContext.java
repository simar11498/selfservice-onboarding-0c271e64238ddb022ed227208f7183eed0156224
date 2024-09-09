package com.cisco.cssp.init.spring;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.web.SpringServletContainerInitializer;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;


/**
 * 
 * Spring MVC Context Definition - loaded via Bootstrap - each major service
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
 * 
 */
@Aspect
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import({ Global_PropertyFilesConfig.class } )
@EnableWebMvc
@ComponentScan( {"com.cisco.cssp.sample.springmvc"})
public class MvcContext extends WebMvcConfigurerAdapter {
	final private Logger logger = LogManager.getLogger(getClass());
	
	MvcContext() {
		logger.info("\n\n****************** Loading Spring Module Context *************************") ;
	}
	
	  /**
     * ViewResolver configuration to determine convention for views in @controller classes
     */
    @Bean
    public ViewResolver viewResolver() {
    	InternalResourceViewResolver viewResolver = new InternalResourceViewResolver() ;
    	viewResolver.setPrefix("/svcHello/") ;
    	viewResolver.setSuffix(".jsp") ;
    	
        return viewResolver;
    }

	@Pointcut("within(@org.springframework.stereotype.Controller *)")
    private void mvcPC() {} ;
	
//	@Around("mvcPC()")
//	public Object mvcAdvice(ProceedingJoinPoint pjp)
//			throws Throwable {
//
//
//		Object obj = Jmx_SimonRegistration.executeSimon(pjp, "java.springmvc.");
//
//		return obj;
//	}
	


}
