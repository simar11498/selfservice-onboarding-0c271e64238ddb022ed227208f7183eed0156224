package com.cisco.cssp.init.jee;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cisco.convergence.obs.util.ValidateMethodUtil;


@WebFilter(filterName = "ShowParamsFilter", urlPatterns = { "/*" }, description = "CS-AP Test Filter", initParams = { @WebInitParam(name = "placeHolder", value = "HelloWorld") })
public class ShowParamsFilter implements Filter {

	private static String validateStringType(String s) {
	       if(s instanceof String){
	            return s;
	       }
	    return null;
	}
	
	protected final Log logger = LogFactory.getLog(getClass());
	private FilterConfig _filterConfig;


	public void init(FilterConfig filterConfig) throws ServletException {
		_filterConfig = filterConfig;

		logger.warn("\n\n =============== CS_AP Params Filter: initialized ============\n"
				+ "Urls matching will have params logged. \n\n");

	}

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws ServletException, IOException {

		if(req == null) return;
		
		HttpServletRequest httpRequest = (HttpServletRequest) req;
		StringBuilder builder = new StringBuilder("Headers found: ");

		if(httpRequest != null) {
			@SuppressWarnings("unchecked")
			Enumeration<String> headerNames =  validateEnumeration(httpRequest.getHeaderNames());
			
			if(headerNames != null){
				for (String name : Collections.list(headerNames)) {
					builder.append("\n\t" + name + "\t\t" + validateStringType(httpRequest.getHeader(name)));
				}
			}
			
			@SuppressWarnings("unchecked")
			Enumeration<String> paramNames =  validateEnumeration(httpRequest.getParameterNames());
			if(paramNames != null){
				builder.append("\n\n Params found: ");
				for (String name : Collections.list(paramNames)) {
					builder.append("\n\t" + name + "\t\t"
							+ validateStringType(httpRequest.getParameter(name)));
				}
			}
		}
		logger.debug(builder.toString());

		chain.doFilter(req, res);

		logger.debug("Complete");
		// PrintWriter out = res.getWriter();
		// out.print( _filterConfig.getInitParameter("placeHolder"));
	}

	public void destroy() {
		// destroy
	}
	@SuppressWarnings("rawtypes")
	private Enumeration validateEnumeration(Enumeration s) {
	       if(s instanceof Enumeration){
	            return s;
	       }
	    return null;
	}
}
