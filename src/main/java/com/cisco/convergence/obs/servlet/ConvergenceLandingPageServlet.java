package com.cisco.convergence.obs.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@WebServlet(name="ConvergenceLandingPageServlet", urlPatterns="/ConvergenceLandingPageServlet", loadOnStartup=1)
public class ConvergenceLandingPageServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Map<String, String> urlContentMap = new HashMap<String, String>();
	// cache settings
	private Long lastServerCacheLoadedMs = 0L;
	private static final long SERVER_CACHE_LIMIT_MS = 1000 * 60 * 60 * 1; // 1 Hour
	private static final long BROWSER_CACHE_LIMIT_MS = 60 * 30; // 30 seconds
	

	private static Logger LOG = LogManager.getLogger(ConvergenceLandingPageServlet.class.getName());

	private String readURL(String url) {
		Long currentTimeMs = System.currentTimeMillis();
		String indexPageContents = urlContentMap.get(url);
		//SA - Fix
		BufferedReader in=null;
		
		if (StringUtils.isBlank(indexPageContents) || (currentTimeMs - lastServerCacheLoadedMs) > SERVER_CACHE_LIMIT_MS) {
			StringBuilder response = new StringBuilder();
			LOG.info("Reading contents of URL @ " + url);
			

			try {
				URL resource = new URL(url);
				InputStream inStream =  validateInputStream(resource.openStream());
				if(inStream != null){
					 in = new BufferedReader(new InputStreamReader(inStream));
	
					String inputLine = null;
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
	
					indexPageContents = response.toString();
	
					// put the url contents into the cache map
					urlContentMap.put(url, indexPageContents);
	
					lastServerCacheLoadedMs = currentTimeMs;
	
					in.close();
				}
			} catch (Exception e) {
				LOG.error("Error while reading URL contents @ " + url, e);
			}finally{ //SA - Fix
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return indexPageContents;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		PrintWriter out = resp.getWriter();

		String lifecycle = validateStringType(System.getProperty("cisco.life"));
		
		if(StringUtils.isEmpty(lifecycle)){
			lifecycle ="prod";
		}
		
		System.out.println("***********LOADING ENVIROMENT************"+lifecycle);
		
		String indexUrl = getServletConfig().getInitParameter("indexPage");
		
		String webAppName = getServletConfig().getInitParameter("webAppName");
		System.out.println("webAppName ::"+webAppName);
		
		if (StringUtils.isBlank(indexUrl)) {
			indexUrl = "https://www.cisco.com/web/fw/tools/ssue/self-onboard/" + lifecycle + "/index.html";
		}

		indexUrl = indexUrl.replace("#lifecycle#", lifecycle);

		String urlContents = readURL(indexUrl);
		resp.setContentType("text/html");
		if (StringUtils.isNotBlank(urlContents)) {
			LOG.info("Rendering Landing Page contents ..");
			// set cache headers to response
			resp.setDateHeader("Expires", System.currentTimeMillis() + BROWSER_CACHE_LIMIT_MS * 1000);
			resp.addHeader("Cache-Control", "max-age=" + BROWSER_CACHE_LIMIT_MS);
			// print the contents to response writer
			out.print(urlContents);
		} else {
			out.print("Could not load Landing page contents @ " + indexUrl);
		}
	}
	private String validateStringType(String s) {
	       if(s instanceof String){
	            return s;
	       }
	    return null;
	}
	
	private InputStream validateInputStream(InputStream stream){
		 if(stream instanceof InputStream){
	            return stream;
	       }
	    return null;
	}
}
