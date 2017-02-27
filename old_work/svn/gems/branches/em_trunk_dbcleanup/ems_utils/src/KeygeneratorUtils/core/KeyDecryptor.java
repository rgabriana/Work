package KeygeneratorUtils.core;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

 class KeyDecryptor {

	private static KeyDecryptor KeyDecryptor = null ;
	private byte[] apiKey = null ;
	private String encriptionAlgorithm ;
	
	private KeyDecryptor()
	{
		
	}
	public static synchronized KeyDecryptor getInstance()
	{
		if (KeyDecryptor==null)
		{
			KeyDecryptor = new KeyDecryptor() ;
		}
		return KeyDecryptor ;
	}
	
	
	public String decrypt(SecretKeySpec skeySpec)
	{
		String decryptedKey = null ;
		  byte[] decrypted = null ;
		if(this.apiKey !=null)
		{
			  Cipher cipher;
			 
			try {
				 decrypted = this.apiKey;
			
				cipher = Cipher.getInstance(encriptionAlgorithm);
				 cipher.init(Cipher.DECRYPT_MODE, skeySpec);
				 byte[] Key =
					      cipher.doFinal(decrypted);
					     decryptedKey = new String(Key);
					     return decryptedKey ;
			    
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {		
				e.printStackTrace();
			} catch (BadPaddingException e) {		
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			}
			
			    
			    	
		}
		return null ;
	}
	
	public String getEncriptionAlgorithm() {
		return encriptionAlgorithm;
	}
	public void setEncriptionAlgorithm(String encriptionAlgorithm) {
		this.encriptionAlgorithm = encriptionAlgorithm;
	}
	public byte[] getApiKey() {
		return apiKey;
	}
	public void setApiKey(byte[] apiKey) {
		this.apiKey = apiKey;
	}
}
