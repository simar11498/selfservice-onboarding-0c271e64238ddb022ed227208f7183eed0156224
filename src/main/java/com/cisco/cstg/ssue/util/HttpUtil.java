package com.cisco.cstg.ssue.util;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//IMS Change 26-Jun-2012: Added new class
/**
 * Utility class to execute various HTTP functionalities
 */
public class HttpUtil {

	public static final String OBSSOCOOKIE_PARAM = "ObSSOCookie";
	
	private static Logger logger = LogManager.getLogger(HttpUtil.class.getClass());


	public static String runGet(String urlString, Map<String, String> queryParamMap, Map<String, String> headerMap,
			int connectionTimeOutinMS, int socketTimeOutinMS) throws HttpUtilException {
		return HttpUtil.runGet(urlString, queryParamMap, headerMap, connectionTimeOutinMS, socketTimeOutinMS, null);
	}

	/**
	 * 
	 * @param urlString
	 *            : The URL to be used for the http call
	 * @param queryParamMap
	 *            : Map containing the url parameters
	 * @param headerMap
	 *            : Map containing the header values that needs to be sent as part of http call.
	 * @param connectionTimeOutinMS
	 *            - connection timeout
	 * @param socketTimeOutinMS
	 *            - socket timeout
	 * @param customUserAgentString
	 *            - a custom value for User-Agent header (optional)
	 * @return response: String containing the response of the http call. Returns null if no response
	 * @throws HttpUtilException
	 */
	public static String runGet(String urlString, Map<String, String> queryParamMap, Map<String, String> headerMap,
			int connectionTimeOutinMS, int socketTimeOutinMS, String customUserAgentString) throws HttpUtilException {

		String response = null;

		long startTime = 0;
		long endTime = 0;

		try {
			System.out.println("$$$$$$$$$$$$$$$$URL STRING*******************"+urlString);
			startTime = System.currentTimeMillis();
			HttpClient client = new HttpClient();

			HttpConnectionManager connectionManager = client.getHttpConnectionManager();
			HttpConnectionParams connectionParams = connectionManager.getParams();

			// Setting the time out values
			connectionParams.setConnectionTimeout(connectionTimeOutinMS);
			connectionParams.setSoTimeout(socketTimeOutinMS);

			// Setting the url
			HttpMethod method = new GetMethod(urlString);

			// set a custom User-Agent string if available
			if (customUserAgentString != null) {
				method.setRequestHeader("User-Agent", customUserAgentString);
			}

			// Setting the query string
			if (queryParamMap != null) {
				for (String paramName : queryParamMap.keySet()) {
					method.setQueryString(new NameValuePair[] { new NameValuePair(paramName, queryParamMap.get(paramName)) });
				}
			}

			// Setting the header. cookies, authentication details etc.
			if (headerMap != null) {
				for (String headerName : headerMap.keySet()) {
					if (headerName.equalsIgnoreCase(OBSSOCOOKIE_PARAM)) {
						method.addRequestHeader("Cookie", headerMap.get(headerName));
					}

					method.addRequestHeader(headerName, headerMap.get(headerName));
				}
			}

			// Invoking the url and getting the response
			client.executeMethod(method);

			int statusCode = method.getStatusCode();
			String statusText = method.getStatusText();

			logger.info("GET URL invoked:" + urlString + ": Status Code:" + statusCode + ": Status Text:" + statusText);

			// throw error for non-success scenarios
			if (statusCode >= 200 && statusCode <= 299) {
				response = method.getResponseBodyAsString();
			} else {
				throw new HttpUtilException("HTTP Error while invoking GET Call to URL " + urlString + ", Server returned Status Code "
						+ statusCode + ": " + statusText);
			}

		} catch (HttpException e) {
			throw new HttpUtilException("HttpException: Error occurred while making the GET call to URL: " + urlString, e);
		} catch (IOException e) {
			throw new HttpUtilException("IOException: Error occurred while making the GET call to URL: " + urlString, e);
		} catch (RuntimeException e) {
			throw new HttpUtilException("Runtime Error occurred while making the GET call to URL: " + urlString, e);
		} finally {
			endTime = System.currentTimeMillis();
			logger.info("Time Taken by HttpUtil.runGet() in ms:" + (endTime - startTime) + " for URL: " + urlString);
		}

		return response;
	}

	public static String runPost(String urlString, Map<String, String> queryParamMap, Map<String, String> headerMap,
			int connectionTimeOutinMS, int socketTimeOutinMS) throws HttpUtilException {
		return HttpUtil.runPost(urlString, queryParamMap, headerMap, connectionTimeOutinMS, socketTimeOutinMS, null);
	}

	/**
	 * Method to run HTTP POST method for given set of parameters
	 * 
	 * @param urlString
	 *            : The URL to be used for the http call
	 * @param queryParamMap
	 *            : Map containing the url parameters
	 * @param headerMap
	 *            : Map containing the header values that needs to be sent as part of http call.
	 * @param connectionTimeOutinMS
	 *            - connection timeout
	 * @param socketTimeOutinMS
	 *            - socket timeout
	 * @param customUserAgentString
	 *            - a custom value for User-Agent header (optional)
	 * @return response: String containing the response of the http call. Returns null if no response
	 * @throws HttpUtilException
	 */
	public static String runPost(String urlString, Map<String, String> queryParamMap, Map<String, String> headerMap,
			int connectionTimeOutinMS, int socketTimeOutinMS, String customUserAgentString) throws HttpUtilException {

		String response = null;

		long startTime = 0;
		long endTime = 0;

		try {
			startTime = System.currentTimeMillis();
			HttpClient client = new HttpClient();

			HttpConnectionManager connectionManager = client.getHttpConnectionManager();
			HttpConnectionParams connectionParams = connectionManager.getParams();

			// Setting the time out values
			connectionParams.setConnectionTimeout(connectionTimeOutinMS);
			connectionParams.setSoTimeout(socketTimeOutinMS);

			// Setting the url
			HttpMethod method = new PostMethod(urlString);

			// set a custom User-Agent string if available
			if (customUserAgentString != null) {
				method.setRequestHeader("User-Agent", customUserAgentString);
			}

			// Setting the query string
			if (queryParamMap != null) {
				for (String paramName : queryParamMap.keySet()) {
					logger.debug(paramName + "," + queryParamMap.get(paramName));
					method.setQueryString(new NameValuePair[] { new NameValuePair(paramName, (String) queryParamMap.get(paramName)) });
				}
			}

			// Setting the header. cookies, authentication details etc.
			if (headerMap != null) {
				for (String headerName : headerMap.keySet()) {
					if (headerName.equalsIgnoreCase(OBSSOCOOKIE_PARAM)) {
						method.addRequestHeader("Cookie", OBSSOCOOKIE_PARAM + "=" + headerMap.get(headerName));
					}
					method.addRequestHeader(headerName, headerMap.get(headerName));
				}
			}

			// Invoking the url and getting the response
			client.executeMethod(method);

			int statusCode = method.getStatusCode();
			String statusText = method.getStatusText();

			logger.info("POST URL invoked:" + urlString + ": Status Code:" + statusCode + ": Status Text:" + statusText);

			// throw error for non-success scenarios
			if (statusCode >= 200 && statusCode <= 299) {
				response = method.getResponseBodyAsString();
			} else {
				throw new HttpUtilException("HTTP Error while invoking POST Call to URL " + urlString + ", Server returned Status Code "
						+ statusCode + ": " + statusText);
			}

		} catch (HttpException e) {
			throw new HttpUtilException("HttpException: Error occurred while making the POST call to URL: " + urlString, e);
		} catch (IOException e) {
			throw new HttpUtilException("IOException: Error occurred while making the POST call to URL: " + urlString, e);
		} catch (RuntimeException e) {
			throw new HttpUtilException("Runtime Error occurred while making the POST call to URL: " + urlString, e);
		} finally {
			endTime = System.currentTimeMillis();
			logger.info("Time Taken by HttpUtil.runPost() in ms:" + (endTime - startTime) + " for URL: " + urlString);
		}

		return response;
	}
}
