package KeygeneratorUtils.core;

import java.security.MessageDigest;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EnlightedKeyGenerator {
	
	private static EnlightedKeyGenerator enlightedKeyGenerator = null ;
	private String apiKey ;
	private byte[] seceretKey ;
	private byte[] encrptedApiKey ;
	private static final String  encryptionAlgorithm = "AES" ;
	private static SecretKeySpec skeySpec  ;
	private EnlightedKeyGenerator()
	{
		
		
	}
	
	public static synchronized EnlightedKeyGenerator getInstance()
	{
		if (enlightedKeyGenerator==null)
		{
			enlightedKeyGenerator = new EnlightedKeyGenerator() ;
		}
		return enlightedKeyGenerator ;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public byte[] getSeceretKey() {
		return seceretKey;
	}

	public void setSeceretKey(byte[] seceretKey) {
		this.seceretKey = seceretKey;
		skeySpec = secret() ;
	}

	
	
	private SecretKeySpec secret()
	{  
		  SecretKeySpec skeySpec = null ;
		try {
			skeySpec = CreateKeySpec("admin" , encryptionAlgorithm ,128);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		  
		    return skeySpec ;
	}
	private SecretKeySpec CreateKeySpec(String pass,  
            String algo, int bit) throws Exception {  
      
		byte[] salt =  seceretKey ;
        int iteration = 1024;// Iteration count  
  
        KeySpec kSpec = new PBEKeySpec(pass.toCharArray(), salt,  
                iteration,bit);  
  
       SecretKey secKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES")  
                .generateSecret(kSpec);  
        
       
  
        MessageDigest med = MessageDigest.getInstance("MD5");  
        med.update(secKey.getEncoded());  
        med.update(salt);  
        for (int i = 1; i < iteration; i++)  
            med.update(med.digest());  
  
        byte[] keyBytz = med.digest();  
        SecretKeySpec skeyspec = new SecretKeySpec(keyBytz, algo);  
       
        return skeyspec;  
    }  
	public byte[] doEncryption()
	{	
		byte[] key = null ;
		KeyEncryptor keyEncryptor =KeyEncryptor.getInstance() ;
		keyEncryptor.setapiKey(apiKey) ;
		
		keyEncryptor.setEncriptionAlgorithm(encryptionAlgorithm) ;
		 if(skeySpec!=null)
		 {
			 key =keyEncryptor.encrypt(skeySpec) ;
		 }
		return key ;
	}
	public String doDecryption()
	{
		String orignalKey = null ;
		KeyDecryptor keyDecryptor = KeyDecryptor.getInstance() ;
		keyDecryptor.setEncriptionAlgorithm(encryptionAlgorithm) ;
		keyDecryptor.setApiKey(encrptedApiKey);
		 if(skeySpec!=null)
		 {
			 orignalKey = keyDecryptor.decrypt(skeySpec) ;
		 }
		return orignalKey ;
		
	}

	public byte[] getEncrptedApiKey() {
		return encrptedApiKey;
	}

	public void setEncrptedApiKey(byte[] encrptedApiKey) {
		this.encrptedApiKey = encrptedApiKey;
	}

	
	

	
	
	

}
