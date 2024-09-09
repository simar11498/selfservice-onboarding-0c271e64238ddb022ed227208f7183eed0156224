package com.cisco.cssp.init.jee;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

/**
 * Note that the init param allows for NON-spring injected resources to be
 * managed as well
 * 
 * @author pnightin
 * 
 * @see <a
 *      href="http://static.springsource.org/spring/docs/3.1.0.M1/spring-framework-reference/html/remoting.html#rest-resttemplate">
 *      Spring REST Template </a>
 * 
 */
@WebServlet(name = "jerseySpringModule", urlPatterns = { "/rest/*" }, loadOnStartup = 2, 
	initParams = { @WebInitParam(name = "com.sun.jersey.config.property.packages", 
	value = "com.cisco.convergence.obs.rest,com.cisco.convergence.obs.rest.mapper") })
public class SpringJerseyServlet extends SpringServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7945330137433274427L;
	final private Logger logger = LogManager.getLogger(getClass());

	/**
	 * Simple hook to avoid touching web.xml to do Spring MVC
	 */

	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);

		logger.warn("\n\n =============== Spring Jersey Wrapper To Avoid web.xml: initialized ============\n"
				+ "Any method annotated with jaxRS  is exposed over http\n\n");
	}

}
