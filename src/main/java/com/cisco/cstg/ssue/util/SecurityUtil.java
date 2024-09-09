package com.cisco.cstg.ssue.util;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.owasp.esapi.ESAPI;

/**
 * @author suresund
 *
 */
public class SecurityUtil extends Vault {

	private static Logger logger = LogManager.getLogger(SecurityUtil.class
			.getClass());

	/**
	 * Method to sanitize the string content that go into email message and
	 * subject
	 * 
	 * @param value
	 * @return
	 */
	public static String sanitizeforEMail(String value) {
		String jsonRegex = "[<>]";
		Pattern p = Pattern.compile(jsonRegex);
		Matcher m = p.matcher(value);

		if (m.find()) {
			throw new IllegalArgumentException("Invalid title");
		}

		return value;
	}

	private static final String ENCRYPT_INTERNAL_KEY = "extl<):G56bP0H6-{Q#C|z@R85l]?e+7XNrjK*[a}yV!WH:rI.3Bf_S@nt05h_$0c%J";

	public static String sanitizeforHTML(String str) {

		try {
			if (str != null)
				return ESAPI.encoder().encodeForHTML(str);
		} catch (Exception e) {
			logger.error("SecurityUtil:santitize: Exception while encoding "
					+ e.getMessage(), e);
		}
		return str;
	}

	/*
	 * encode value setting in JPA/query
	 */
	public static String sanitize(String str) {

		try {
			if (str != null)
				return ESAPI.encoder().encodeForJavaScript(str);
		} catch (Exception e) {
			logger.error("SecurityUtil:santitize: Exception while encoding "
					+ e.getMessage(), e);
		}
		return str;
	}

	private static final String ENCRYPT_EXTERNAL_KEY = "ssue<):U27tM2o0|)D+O_u&N19g[>z=5ZUubP&]g{rG%ZD/Lk_7Uw~W^r_-*dK#.l*X";

	/**
	 * Method to do a base 64 decoding.
	 * 
	 * @param encodedString
	 * @return
	 */
	public static String decodeBase64(String encodedString) {
		if (encodedString != null) {
			byte[] decodedQuery = Base64.decodeBase64(encodedString.getBytes());
			String decodedString = new String(decodedQuery);
			return decodedString;
		} else {
			return null;
		}
	}

	// TODO check with Suresh if " is allowed while saving a CV
	public static String sanitizeforTitle(String str) {
		String jsonRegex = "[()'<>\"]";
		Pattern p = Pattern.compile(jsonRegex);
		Matcher m = p.matcher(str);

		if (m.find()) {
			throw new IllegalArgumentException("Invalid title");
		}
		return str;
	}

	public static String sanitizeforJSON(String str) {
		String jsonRegex = "[()'<>]";
		Pattern p = Pattern.compile(jsonRegex);
		Matcher m = p.matcher(str);

		if (m.find()) {
			throw new IllegalArgumentException("Invalid JSON input.");
		}

		return str;
	}

	// non-static method to implement secret key
	@Override
	protected String getSecretKey(String source) throws RuntimeException {
		String key = null;

		String responseError = "Unexpected fatal error while processing SecretKey:";
		if (source == null)
			throw new RuntimeException(responseError + "SourceIsNull");
		try {
			Field kField = this.getClass().getDeclaredField(
					"ENCRYPT_" + source.trim().toUpperCase() + "_KEY");
			key = (String) kField.get(null);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(responseError + "NoSuchFieldException",e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(responseError + "IllegalAccessException",e);
		}

		return key;
	}

	// convenience method to access encrypt
	public static final String encrypt(String message, String salt,
			String source) throws RuntimeException {
		return (new SecurityUtil())._encrypt(message, salt, source);
	}

	// convenience method to access decrypt
	public static final String decrypt(String message, String salt,
			String source) throws RuntimeException {
		return (new SecurityUtil())._decrypt(message, salt, source);
	}
}
