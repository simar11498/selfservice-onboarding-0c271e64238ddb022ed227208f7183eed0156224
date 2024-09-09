package com.cisco.cstg.ssue.util;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

public abstract class Vault {
	
	protected abstract String getSecretKey(String source);

	//santnair: Implementation to decrypt password strings used in application (Zeus US165)
	public final String _encrypt(String message, String salt, String source) throws RuntimeException {
		String encryptedValue = null;
		String messageToEncrypt = null;
		if (salt != null && salt.length() > 0) {
			messageToEncrypt = message + salt;
		} else {
			messageToEncrypt = message;
		}

		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		//decryptor.setAlgorithm("PBEWithMD5AndTripleDES");
		encryptor.setPassword(getSecretKey(source));

		encryptedValue = encryptor.encrypt(messageToEncrypt);

		return encryptedValue;
	}

	//santnair: Implementation to decrypt password strings used in application (Zeus US165)
	public final String _decrypt(String encryptedValue, String salt, String source) throws RuntimeException {
		String decryptedValue = null;

		StandardPBEStringEncryptor decryptor = new StandardPBEStringEncryptor();
		//decryptor.setAlgorithm("PBEWithMD5AndTripleDES");
		decryptor.setPassword(getSecretKey(source));
		String decryptedString = decryptor.decrypt(encryptedValue);

		if (salt != null && salt.length() > 0) {
			int saltIndex = decryptedString.length()-salt.length();
			String extractedSalt = decryptedString.substring(saltIndex);
			if (salt.equals(extractedSalt)) {
				decryptedValue = decryptedString.substring(0, saltIndex);
			} else {
				throw new EncryptionOperationNotPossibleException();
			}
		} else {
			decryptedValue = decryptedString;
		}

		return decryptedValue;
	}

}
