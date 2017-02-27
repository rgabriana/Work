package com.communication.utils;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.sun.jersey.core.util.Base64;

public class EmTasksUUIDUtil {
	
	public static final String SECRET_LICENSE_KEY = "enLighted-SaveEnergy"; 	
	
	public static String encrypt(String secretKeyString,String Data) throws Exception {
	    Key key = generateKey(secretKeyString);
	    Cipher c = Cipher.getInstance("AES");
	    c.init(Cipher.ENCRYPT_MODE, key);
	    byte[] encVal = c.doFinal(Data.getBytes());
	    byte[] encryptedValue = Base64.encode(encVal);
	    return new String (encryptedValue);
	}	

	public static String decrypt(String secretKeyString,String encryptedString) throws Exception {
	    Key key = generateKey(secretKeyString);
	    Cipher c = Cipher.getInstance("AES");
	    c.init(Cipher.DECRYPT_MODE, key);
	    byte[] decordedValue = Base64.decode(encryptedString.getBytes());
	    byte[] decValue = c.doFinal(decordedValue);
	    String decryptedValue = new String(decValue);
	    return decryptedValue;
	}
	
	private static Key generateKey(String secretKeyString) throws Exception {
		
		byte[] key = secretKeyString.getBytes("UTF-8");
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		key = sha.digest(key);
		key = Arrays.copyOf(key, 16); // use only first 128 bit

		SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
		return secretKeySpec;
	}	
	
}