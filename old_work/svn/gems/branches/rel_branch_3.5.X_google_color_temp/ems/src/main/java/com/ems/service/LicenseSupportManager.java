package com.ems.service;

import java.io.IOException;

import javax.annotation.Resource;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.SystemConfiguration;
import com.ems.utils.ArgumentUtils;
import com.enlightedinc.licenseutil.LicenseUtil;
import com.enlightedinc.vo.BacnetLicenseInstance;
import com.enlightedinc.vo.EmLicenseInstance;
import com.enlightedinc.vo.Licenses;
import com.enlightedinc.vo.ZoneSensorsLicenseInstance;

@Service("licenseSupportManager")
@Transactional(propagation = Propagation.REQUIRED)
public class LicenseSupportManager {
	
	@Resource
    private SystemConfigurationManager systemConfigurationManager;
	
	@Resource
    private BacnetSchedulerManager bacnetSchedulerManager;
	
	@Resource
    private BacnetManager bacnetManager;
	
	@Resource
    private NetworkSettingsManager networkSettingsManager;
	
	public String uploadLicenseFile(String jsonNewLicenseEncriptedString){
		
		String uploadStatus = "false";
		
		String jsonNewLicenseString = null;
		
		SystemConfiguration emUUID = systemConfigurationManager.loadConfigByName("em.UUID");
		
		String secretKeyString = LicenseUtil.SECRET_LICENSE_KEY + emUUID.getValue();
		
		SystemConfiguration emLicenseKeyValue = systemConfigurationManager.loadConfigByName("emLicenseKeyValue");
		
		String jsonOldLicenseEncriptedString = null;
		
		if (emLicenseKeyValue != null) {
			jsonOldLicenseEncriptedString = emLicenseKeyValue.getValue();
		}
		
		String jsonOldLicenseString = null;
		
		Licenses oldLicensesObject = null;
		
		Licenses newLicensesObject = null;
		
		try {
			jsonNewLicenseString = LicenseUtil.decrypt(secretKeyString, jsonNewLicenseEncriptedString);
			newLicensesObject = new ObjectMapper().readValue(jsonNewLicenseString, Licenses.class);
				
			jsonOldLicenseString = LicenseUtil.decrypt(secretKeyString, jsonOldLicenseEncriptedString);
			oldLicensesObject = new ObjectMapper().readValue(jsonOldLicenseString, Licenses.class);
			uploadStatus = "true";
			
			
			if(oldLicensesObject.getUuid().equals(newLicensesObject.getUuid())){
				
				for(EmLicenseInstance emLicenseInstance : oldLicensesObject.getEm().getEmLicenseInstanceList()){
					if(emLicenseInstance.getTimeStamp().getTime() == newLicensesObject.getTimeStamp().getTime()){
						uploadStatus = "duplicate";
						break;
					}
				}
				
				if(uploadStatus.equals("duplicate")){
					return uploadStatus;
				}
				
				for(BacnetLicenseInstance bacnetLicenseInstance : oldLicensesObject.getBacnet().getBacnetLicenseInstanceList()){
					if(bacnetLicenseInstance.getTimeStamp().getTime() == newLicensesObject.getTimeStamp().getTime()){
						uploadStatus = "duplicate";
						break;
					}
				}
				
				if(uploadStatus.equals("duplicate")){
					return uploadStatus;
				}
				
				for(ZoneSensorsLicenseInstance zoneSensorsLicenseInstance : oldLicensesObject.getZoneSensors().getZoneSensorsLicenseInstanceList()){
					if(zoneSensorsLicenseInstance.getTimeStamp().getTime() == newLicensesObject.getTimeStamp().getTime()){
						uploadStatus = "duplicate";
						break;
					}
				}
				
				if(uploadStatus.equals("duplicate")){
					return uploadStatus;
				}
				
				if(newLicensesObject.getHVAC().getTimeStamp().getTime() == oldLicensesObject.getHVAC().getTimeStamp().getTime()){
					uploadStatus = "duplicate";
					return uploadStatus;
				}	
				
				
				if(newLicensesObject.getEm() != null){
					if(newLicensesObject.getEm().getEnabled()){
						if(!ArgumentUtils.isNullOrEmpty(newLicensesObject.getEm().getEmLicenseInstanceList())){
							oldLicensesObject.getEm().getEmLicenseInstanceList().add(newLicensesObject.getEm().getEmLicenseInstanceList().get(0));
						}
					}
				}
				
				if(newLicensesObject.getBacnet() != null){
					if(newLicensesObject.getBacnet().getEnabled()){
						oldLicensesObject.getBacnet().setEnabled(true);
						if(!ArgumentUtils.isNullOrEmpty(newLicensesObject.getBacnet().getBacnetLicenseInstanceList())){
							oldLicensesObject.getBacnet().getBacnetLicenseInstanceList().add(newLicensesObject.getBacnet().getBacnetLicenseInstanceList().get(0));
						}
						//System.out.println("In LicenseSupport Management with bacnet enabled");
						if(!bacnetManager.isBacnetServiceRunning()){
							networkSettingsManager.configureBacnetNetworkInterfaceToCorpIfNotExists();
							bacnetManager.enableBacnetPort();
							bacnetManager.startBacnetService();
						}
						bacnetSchedulerManager.startBacnetScheduler();
					}
				}
				
				if(newLicensesObject.getZoneSensors() != null){
					if(newLicensesObject.getZoneSensors().getEnabled()){
						oldLicensesObject.getZoneSensors().setEnabled(true);
						if(!ArgumentUtils.isNullOrEmpty(newLicensesObject.getZoneSensors().getZoneSensorsLicenseInstanceList())){
							oldLicensesObject.getZoneSensors().getZoneSensorsLicenseInstanceList().add(newLicensesObject.getZoneSensors().getZoneSensorsLicenseInstanceList().get(0));
						}
					}
				}
				
				if(newLicensesObject.getHVAC() != null){
					if(newLicensesObject.getHVAC().getEnabled()){
						oldLicensesObject.getHVAC().setEnabled(true);
					}else{
						oldLicensesObject.getHVAC().setEnabled(false);
					}
					oldLicensesObject.getHVAC().setTimeStamp(newLicensesObject.getHVAC().getTimeStamp());
				}
				
				if (emLicenseKeyValue != null) {
					emLicenseKeyValue.setValue(LicenseUtil.getEncryptedJsonLicenseStringFromLicensesObject(oldLicensesObject));
	            	systemConfigurationManager.save(emLicenseKeyValue);
	            }
			}else{
				uploadStatus = "false";
			}
				
		}catch (JsonMappingException e) {
			
		} catch (IOException e) {
			
		} catch (Exception e) {
			
		}
		
		return uploadStatus;
	}
	
	public boolean isZoneSensorsEnabled(){
		boolean isZoneSensorsEnabled = false;
		
		SystemConfiguration emUUID = systemConfigurationManager.loadConfigByName("em.UUID");
		
		String secretKeyString = LicenseUtil.SECRET_LICENSE_KEY + emUUID.getValue();
		
		
		String jsonLicenseEncriptedString = systemConfigurationManager.loadConfigByName("emLicenseKeyValue").getValue();
		
		String jsonLicenseString = null;
		
		try {
			jsonLicenseString = LicenseUtil.decrypt(secretKeyString, jsonLicenseEncriptedString);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
			
		try {
			Licenses licenses = new ObjectMapper().readValue(jsonLicenseString, Licenses.class);
			if(licenses.getZoneSensors() != null){
				isZoneSensorsEnabled = licenses.getZoneSensors().getEnabled();
			}else{
				isZoneSensorsEnabled = false;
			}
			
		} catch (JsonParseException e) {
			//e.printStackTrace();
		} catch (JsonMappingException e) {
			//e.printStackTrace();
		} catch (IOException e) {
			//e.printStackTrace();
		}
		
		return isZoneSensorsEnabled;
	}
	
}
