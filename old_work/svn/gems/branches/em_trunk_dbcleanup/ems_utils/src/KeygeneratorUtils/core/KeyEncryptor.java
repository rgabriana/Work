package KeygeneratorUtils.core;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;



/**
 * @author Sameer Surjikar
 * This class encrypt the key that has been given to it. 
 *
 */
 class KeyEncryptor {

	private static KeyEncryptor keyEncryptor = null ;
	private String apiKey  = null ;
	private String encriptionAlgorithm ;
	
	
	private KeyEncryptor()
	{
		
	}
	public static synchronized KeyEncryptor getInstance()
	{
		if (keyEncryptor==null)
		{
			keyEncryptor = new KeyEncryptor() ;
		}
		return keyEncryptor ;
	}
	public String getapiKey () {
		return apiKey ;
	}
	public void setapiKey (String apiKey ) {
		this.apiKey  = apiKey ;
	}
	
	public  byte[] encrypt(SecretKeySpec skeySpec)
	{
		  byte[] encrypted = null ;
		if(this.apiKey !=null)
		{
			  Cipher cipher;
			try {
				cipher = Cipher.getInstance(encriptionAlgorithm );
			

			    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

			    encrypted =
			      cipher.doFinal(this.apiKey.getBytes());
				return encrypted ;
				
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {	
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {		
				e.printStackTrace();
			} catch (BadPaddingException e) {		
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
	
	
}
