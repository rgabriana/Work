package com.ems.hvac.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.Provider;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;

import com.ems.util.Constants;
import org.apache.commons.codec.binary.Base64;

/**
 * Generate the Key pair using openssl as following on unix and from using them ran the following java program
 * openssl genrsa -out supp_private.key 1024  						---- To generate private key
 * openssl rsa -pubout -in supp_private.key -out em_public.key		----- To generate public key
 * 
 * sudo openssl genrsa -out em_private.key 1024
 * sudo openssl rsa -pubout -in em_private.key -out supp_public.key
 * 
 * 
 * scp /media/sf_enlighted/em_private.key  enlighted@192.168.137.106:/home/enlighted/.
 * scp /media/sf_enlighted/em_public.key  enlighted@192.168.137.106:/home/enlighted/.
 * scp /media/sf_enlighted/supp_private.key  enlighted@192.168.137.106:/home/enlighted/.
 * scp /media/sf_enlighted/supp_public.key  enlighted@192.168.137.106:/home/enlighted/.
 * 
 * sudo cp em_private.key /var/lib/tomcat6/Enlighted
 * sudo cp em_public.key /var/lib/tomcat6/Enlighted
 * sudo cp supp_private.key /var/lib/tomcat6/Enlighted
 * sudo cp supp_public.key /var/lib/tomcat6/Enlighted
 * 
 * EM_PUB_FILE pairs with SUPP_PVT_FILE and vice versa
 * @author admin
 *
 */
public class CryptographyUtil {

	public static void main(String[] args) throws IOException {
		try {
			//First get the encrypted key from EM for the time..
			final Date d = new Date();
			final String data = String.valueOf(d.getTime());
			final String supportKey = getEncryptedString(Constants.EM_PUB_FILE, data);
			final String supportKeyAtSUPP = getDecryptedString(Constants.SUPP_PVT_FILE, supportKey);
			final String tempPassAtSUPP = getEncryptedString(Constants.SUPP_PUB_FILE, supportKeyAtSUPP);
			final String tempPass = getDecryptedString(Constants.EM_PVT_FILE, tempPassAtSUPP);
			System.out.println("INPUT:"+data+":OUTPUT:"+tempPass);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      
	}

	public static String getDecryptedString(final String keyFilePath, final String encryptedStr ) throws Exception{
		final Provider provider = new BouncyCastleProvider(); 
		Security.addProvider(provider);
		final KeyPair keyPair = readKeyPair(new File(keyFilePath), null); 
		final String str = decrypt(keyPair.getPrivate(), Base64.decodeBase64(encryptedStr));
		Security.removeProvider(provider.getName());
		return str;
	}
	public static String getEncryptedString(final String keyFilePath, final String input ) throws Exception{
		final Provider provider = new BouncyCastleProvider(); 
		Security.addProvider(provider);
		final Key key= readPublicKey(new File(keyFilePath), null); 
		final String str = Base64.encodeBase64String(encrypt(key, input));
		Security.removeProvider(provider.getName());
		return str;
	}
	
	private static byte[] encrypt(Key pubkey, String text) {
	    try {
	        Cipher rsa;
	        rsa = Cipher.getInstance("RSA");
	        rsa.init(Cipher.ENCRYPT_MODE, pubkey);
	        return rsa.doFinal(text.getBytes());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}


	private static String decrypt(Key decryptionKey, byte[] buffer) {
	    try {
	        Cipher rsa;
	        rsa = Cipher.getInstance("RSA");
	        rsa.init(Cipher.DECRYPT_MODE, decryptionKey);
	        byte[] utf8 = rsa.doFinal(buffer);
	        return new String(utf8, "UTF8");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	private static KeyPair readKeyPair(File privateKey, String keyPassword) throws IOException {
	    FileReader fileReader = new FileReader(privateKey);
	    PEMReader r = new PEMReader(fileReader);
	    try {
	    	Object o = r.readObject();
	    	
	        return (KeyPair) o;
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        r.close();
	        fileReader.close();
	    }
	}

	private static Key readPublicKey(File privateKey, String keyPassword) throws IOException {
	    FileReader fileReader = new FileReader(privateKey);
	    PEMReader r = new PEMReader(fileReader);
	    try {
	        return (RSAPublicKey) r.readObject();
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        r.close();
	        fileReader.close();
	    }
	}
}
