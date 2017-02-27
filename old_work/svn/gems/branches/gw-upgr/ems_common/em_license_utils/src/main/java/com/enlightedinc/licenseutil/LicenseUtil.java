package com.enlightedinc.licenseutil;

import java.io.IOException;
import java.security.Key;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.enlightedinc.vo.Bacnet;
import com.enlightedinc.vo.BacnetLicenseInstance;
import com.enlightedinc.vo.Em;
import com.enlightedinc.vo.EmLicenseInstance;
import com.enlightedinc.vo.Hvac;
import com.enlightedinc.vo.Licenses;
import com.enlightedinc.vo.ZoneSensors;
import com.enlightedinc.vo.ZoneSensorsLicenseInstance;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.sun.jersey.core.util.Base64;

public class LicenseUtil {
	
	public static final String SECRET_LICENSE_KEY = "enLighted-SaveEnergy"; 
	
	public static String getDefaultEncryptedJsonLicenseString(String uuid){
		
		Licenses licenses = new Licenses();
		
		licenses.setUuid(uuid);
		
		Date currentTime = new Date();
		
		licenses.setTimeStamp(currentTime);
		
		Em em = new Em();
		
		em.setEnabled(true);
		
		List<EmLicenseInstance> emLicenseInstanceList = new ArrayList<EmLicenseInstance>();
		
		/*EmLicenseInstance emLicenseInstance = new EmLicenseInstance();
		
		emLicenseInstance.setNoofdevices(1000);
		emLicenseInstance.setTimeStamp(currentTime);
		
		emLicenseInstanceList.add(emLicenseInstance);*/
		
		em.setEmLicenseInstanceList(emLicenseInstanceList);
		
		licenses.setEm(em);
		
		Bacnet bacnet = new Bacnet();
		
		bacnet.setEnabled(false);
		
		List<BacnetLicenseInstance> bacnetLicenseInstanceList = new ArrayList<BacnetLicenseInstance>();
		
		bacnet.setBacnetLicenseInstanceList(bacnetLicenseInstanceList);
		
		licenses.setBacnet(bacnet);
		
		Hvac hvac = new Hvac();
		
		hvac.setEnabled(false);
		
		hvac.setTimeStamp(currentTime);
		
		licenses.setHVAC(hvac);
		
		ZoneSensors zoneSensors = new ZoneSensors();
		
		zoneSensors.setEnabled(false);
		
		List<ZoneSensorsLicenseInstance> zoneSensorsLicenseInstanceList = new ArrayList<ZoneSensorsLicenseInstance>();
		
		zoneSensors.setZoneSensorsLicenseInstanceList(zoneSensorsLicenseInstanceList);
		
		licenses.setZoneSensors(zoneSensors);
		
		String jsonLicenseString = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			jsonLicenseString = mapper.writeValueAsString(licenses);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String secretKeyString = SECRET_LICENSE_KEY + uuid;
	 	
		//byte[] dataEnc = null;
		String dataEncString = null; 
		try {
			dataEncString = encrypt(secretKeyString,jsonLicenseString);
			//dataEncString = new String(dataEnc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
		return dataEncString;	
	}
	
	
	public static String getEncryptedJsonLicenseStringFromLicensesObject(Licenses licenses){
		
		String jsonLicenseString = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			jsonLicenseString = mapper.writeValueAsString(licenses);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String secretKeyString = SECRET_LICENSE_KEY + licenses.getUuid();
	 	
		//byte[] dataEnc = null;
		String dataEncString = null; 
		try {
			dataEncString = encrypt(secretKeyString,jsonLicenseString);
			//dataEncString = new String(dataEnc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
		return dataEncString;	
	}
	
	public static String getEncryptedJsonLicenseString(String uuid,int noOfEmDevices,Boolean bacnetEnabled,int noOfBacnetDevices,Boolean zoneSensorsEnabled,int noOfZoneSensors,Boolean hvacEnabled,String emProductId,String bacnetProductId,String zoneSensorsProductId){
		
		Licenses licenses = new Licenses();
		
		licenses.setUuid(uuid);
		
		Date currentTime = new Date();
		
		licenses.setTimeStamp(currentTime);
		
		Em em = new Em();
		
		em.setEnabled(true);
		
		List<EmLicenseInstance> emLicenseInstanceList = new ArrayList<EmLicenseInstance>();
		
		if(noOfEmDevices > 0){
			EmLicenseInstance emLicenseInstance = new EmLicenseInstance();
			
			emLicenseInstance.setNoofdevices(noOfEmDevices);
			emLicenseInstance.setTimeStamp(currentTime);
			emLicenseInstance.setProductId(emProductId);
			
			emLicenseInstanceList.add(emLicenseInstance);
		}
		
		em.setEmLicenseInstanceList(emLicenseInstanceList);
		
		licenses.setEm(em);
		
		Bacnet bacnet = new Bacnet();
		
		List<BacnetLicenseInstance> bacnetLicenseInstanceList = new ArrayList<BacnetLicenseInstance>();
		
		if(bacnetEnabled){
			bacnet.setEnabled(true);
			
			BacnetLicenseInstance bacnetLicenseInstance = new BacnetLicenseInstance();
						
			if(noOfBacnetDevices > 0){
				bacnetLicenseInstance.setNoofdevices(noOfBacnetDevices);
				bacnetLicenseInstance.setTimeStamp(currentTime);
				bacnetLicenseInstance.setProductId(bacnetProductId);
				bacnetLicenseInstanceList.add(bacnetLicenseInstance);
			}
			
		}else{
			bacnet.setEnabled(false);
		}
		
		bacnet.setBacnetLicenseInstanceList(bacnetLicenseInstanceList);
		
		
		licenses.setBacnet(bacnet);
		
		
		ZoneSensors zoneSensors = new ZoneSensors();
		
		List<ZoneSensorsLicenseInstance> zoneSensorsLicenseInstanceList = new ArrayList<ZoneSensorsLicenseInstance>();
		
		if(zoneSensorsEnabled){
			zoneSensors.setEnabled(true);
			
			ZoneSensorsLicenseInstance zoneSensorsLicenseInstance = new ZoneSensorsLicenseInstance();
						
			if(noOfZoneSensors > 0){
				zoneSensorsLicenseInstance.setNoofdevices(noOfZoneSensors);
				zoneSensorsLicenseInstance.setTimeStamp(currentTime);
				zoneSensorsLicenseInstance.setProductId(zoneSensorsProductId);
				zoneSensorsLicenseInstanceList.add(zoneSensorsLicenseInstance);
			}
			
		}else{
			zoneSensors.setEnabled(false);
		}
		
		zoneSensors.setZoneSensorsLicenseInstanceList(zoneSensorsLicenseInstanceList);
		
		
		licenses.setZoneSensors(zoneSensors);
		
		
		Hvac hvac = new Hvac();
		
		if(hvacEnabled){
			hvac.setEnabled(true);
		}else{
			hvac.setEnabled(false);
		}
		
		hvac.setTimeStamp(currentTime);
		
		licenses.setHVAC(hvac);
		
		String jsonLicenseString = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			jsonLicenseString = mapper.writeValueAsString(licenses);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String secretKeyString = LicenseUtil.SECRET_LICENSE_KEY + uuid;
	 	
		//byte[] dataEnc = null;
		String dataEncString = null; 
		try {
			dataEncString = LicenseUtil.encrypt(secretKeyString,jsonLicenseString);
			//dataEncString = new String(dataEnc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return dataEncString;	
	}
	
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String EncryptedJsonLicenseString = null;	
		String uuid = null;
		int noOfEmDevices = 0;
		Boolean bacnetEnabled = false;
		int noOfBacnetDevices = 0;
		Boolean zoneSensorsEnabled = false;
		int noOfZoneSensors = 0;
		Boolean hvacEnabled = false;
		String emProductId = null;
		String bacnetProductId = "NB-LG-01";
		String zoneSensorsProductId = "EM-ZONE-1";
		
		if (args.length != 8) {
			System.err.println("Program requires 8 parameters.");
            return;
        }
		
		if( "".equals(args[0]) || args[0].indexOf("=") < 0 || args[0].indexOf("uuid") < 0){
			System.err.println("Argument 1 is not proper");
			return;
		}else{
			String[] str = args[0].split("=", 2);
			if(!"uuid".equals(str[0]) || "".equals(str[1])){
				System.err.println("Argument 1 is not proper");
				return;
			}

			uuid = str[1];
		}
		
		if( "".equals(args[1]) || args[1].indexOf("=") < 0 || args[1].indexOf("em_devices") < 0){
			System.err.println("Argument 2 is not proper");
			return;
		}else{
			String[] str = args[1].split("=", 2);
			if(!"em_devices".equals(str[0]) || "".equals(str[1])){
				System.err.println("Argument 2 is not proper");
				return;
			}
			
			try {
				noOfEmDevices = Integer.parseInt(str[1]);
			} catch (NumberFormatException e) {
				System.err.println("Argument 2 is not proper");
			    return;
			}
			
		}
		
		if( "".equals(args[2]) || args[2].indexOf("=") < 0 || args[2].indexOf("bacnet") < 0){
			System.err.println("Argument 3 is not proper");
			return;
		}else{
			String[] str = args[2].split("=", 2);
			if(!"bacnet".equals(str[0]) || "".equals(str[1])){
				System.err.println("Argument 3 is not proper");
				return;
			}
			if(!("true".equals(str[1]) || "false".equals(str[1]))){
				System.err.println("Argument 3 is not proper");
				return;
			}
			bacnetEnabled = Boolean.parseBoolean(str[1]);
		}
		
		if( "".equals(args[3]) || args[3].indexOf("=") < 0 || args[3].indexOf("bacnet_devices") < 0){
			System.err.println("Argument 4 is not proper");
			return;
		}else{
			String[] str = args[3].split("=", 2);
			if(!"bacnet_devices".equals(str[0]) || "".equals(str[1])){
				System.err.println("Argument 4 is not proper");
				return;
			}
			
			try {
				noOfBacnetDevices = Integer.parseInt(str[1]);
			} catch (NumberFormatException e) {
				System.err.println("Argument 4 is not proper");
			    return;
			}
			
		}
		
		if( "".equals(args[4]) || args[4].indexOf("=") < 0 || args[4].indexOf("zoneSensorsEnabled") < 0){
			System.err.println("Argument 5 is not proper");
			return;
		}else{
			String[] str = args[4].split("=", 2);
			if(!"zoneSensorsEnabled".equals(str[0]) || "".equals(str[1])){
				System.err.println("Argument 5 is not proper");
				return;
			}
			if(!("true".equals(str[1]) || "false".equals(str[1]))){
				System.err.println("Argument 5 is not proper");
				return;
			}
			zoneSensorsEnabled = Boolean.parseBoolean(str[1]);
		}
		
		if( "".equals(args[5]) || args[5].indexOf("=") < 0 || args[5].indexOf("noOfZoneSensors") < 0){
			System.err.println("Argument 6 is not proper");
			return;
		}else{
			String[] str = args[5].split("=", 2);
			if(!"noOfZoneSensors".equals(str[0]) || "".equals(str[1])){
				System.err.println("Argument 6 is not proper");
				return;
			}
			
			try {
				noOfZoneSensors = Integer.parseInt(str[1]);
			} catch (NumberFormatException e) {
				System.err.println("Argument 6 is not proper");
			    return;
			}
			
		}
		
		if( "".equals(args[6]) || args[6].indexOf("=") < 0 || args[6].indexOf("hvac") < 0){
			System.err.println("Argument 7 is not proper");
			return;
		}else{
			String[] str = args[6].split("=", 2);
			if(!"hvac".equals(str[0]) || "".equals(str[1])){
				System.err.println("Argument 7 is not proper");
				return;
			}
			if(!("true".equals(str[1]) || "false".equals(str[1]))){
				System.err.println("Argument 7 is not proper");
				return;
			}
			
			hvacEnabled = Boolean.parseBoolean(str[1]);
		}
		
		if( "".equals(args[7]) || args[7].indexOf("=") < 0 || args[7].indexOf("emProductId") < 0){
			System.err.println("Argument 8 is not proper");
			return;
		}else{
			String[] str = args[7].split("=", 2);
			if("".equals(str[0]) || "".equals(str[1]) || !"emProductId".equals(str[0])){
				System.err.println("Argument 8 is not proper");
				return;
			}
			if(!(str[1].equals("EM-SW-1000") || str[1].equals("EM-NWS-1"))){
				System.err.println("Argument 8 is not proper. emProductId should be either EM-SW-1000 or EM-NWS-1");
				return;
			}
			emProductId = str[1];
		}
		
		EncryptedJsonLicenseString = getEncryptedJsonLicenseString(uuid,noOfEmDevices,bacnetEnabled,noOfBacnetDevices,zoneSensorsEnabled,noOfZoneSensors,hvacEnabled,emProductId,bacnetProductId,zoneSensorsProductId);
		System.out.print(EncryptedJsonLicenseString);
   	
	}
	
}
