package com.ems.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.BACnetConfig;
import com.ems.model.SystemConfiguration;
import com.ems.utils.ArgumentUtils;
import com.enlightedinc.licenseutil.LicenseUtil;
import com.enlightedinc.vo.BacnetLicenseInstance;
import com.enlightedinc.vo.EmLicenseInstance;
import com.enlightedinc.vo.Licenses;
import com.enlightedinc.vo.OccupancySensor;
import com.enlightedinc.vo.ZoneSensorsLicenseInstance;
import org.apache.log4j.Logger;


@Service("licenseSupportManager")
@Transactional(propagation = Propagation.REQUIRED)
public class LicenseSupportManager {
	
	private static final Logger m_Logger = Logger.getLogger("SysLog");

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
		
		boolean isFirstLicense = false;
		
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
			uploadStatus = "false";
			
			
			if(oldLicensesObject.getUuid().equals(newLicensesObject.getUuid())){
				
				for(EmLicenseInstance emLicenseInstance : oldLicensesObject.getEm().getEmLicenseInstanceList()){
					if(emLicenseInstance.getTimeStamp().getTime() == newLicensesObject.getTimeStamp().getTime()){
						uploadStatus = "duplicate";
						break;
					}
				}
				
				if(oldLicensesObject!=null && oldLicensesObject.getEm()!=null && oldLicensesObject.getEm().getEmLicenseInstanceList()!=null && oldLicensesObject.getEm().getEmLicenseInstanceList().isEmpty()){
					isFirstLicense = true ;
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
				
				
				Boolean startBacnetService = false;
				
				BACnetConfig bacnetConfig = bacnetManager.getConfig();
				
				if(newLicensesObject.getBacnet() != null){
					if(newLicensesObject.getBacnet().getEnabled()){
						//oldLicensesObject.getBacnet().setEnabled(true);
						if(!ArgumentUtils.isNullOrEmpty(newLicensesObject.getBacnet().getBacnetLicenseInstanceList())){
							oldLicensesObject.getBacnet().getBacnetLicenseInstanceList().add(newLicensesObject.getBacnet().getBacnetLicenseInstanceList().get(0));
						}
						
						if(!oldLicensesObject.getBacnet().getEnabled()){
							oldLicensesObject.getBacnet().setEnabled(true);
							
							bacnetConfig.setEnableBacnet(true);
							
							networkSettingsManager.configureBacnetNetworkInterfaceToCorpIfNotExists();
							bacnetManager.enableBacnetPort();
							startBacnetService = true;
						}
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
				
				if(newLicensesObject.getOccupancySensor() != null){
					if(oldLicensesObject.getOccupancySensor() == null){
						OccupancySensor occupancySensor = new OccupancySensor();
						occupancySensor.setTimeStamp(newLicensesObject.getOccupancySensor().getTimeStamp());
						if(newLicensesObject.getOccupancySensor().getEnabled() ){
							occupancySensor.setEnabled(true);
							bacnetConfig.setFixtureOccupancySensor("enable");
							bacnetConfig.setEnableBacnet(true);
							startBacnetService = true;
						}else{
							occupancySensor.setEnabled(false);
						}
						oldLicensesObject.setOccupancySensor(occupancySensor);
					}else{
						if(newLicensesObject.getOccupancySensor().getEnabled() ){
							oldLicensesObject.getOccupancySensor().setEnabled(true);
							bacnetConfig.setFixtureOccupancySensor("enable");
							bacnetConfig.setEnableBacnet(true);
							startBacnetService = true;
						}
						oldLicensesObject.getOccupancySensor().setTimeStamp(newLicensesObject.getOccupancySensor().getTimeStamp());
					}
					
				}
				
				if (emLicenseKeyValue != null) {
					emLicenseKeyValue.setValue(LicenseUtil.getEncryptedJsonLicenseStringFromLicensesObject(oldLicensesObject));
	            	systemConfigurationManager.save(emLicenseKeyValue);
	            }
				
				if(isFirstLicense){
					bacnetConfig.setEnableBacnet(false);
				}
				if(startBacnetService){
					try {
						bacnetManager.saveConfig(bacnetConfig);
					}catch (Exception e) {
						m_Logger.error("Unable to start bacnet service:"+e.getMessage(), e);
					}
				}
				
				uploadStatus = "true";
				
			}else{
				uploadStatus = "false";
			}
				
		}catch (JsonMappingException e) {
			
		} catch (IOException e) {
			
		} catch (Exception e) {
			m_Logger.error("Unable to start bacnet service:"+e.getMessage(), e);
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
	
	public boolean isFixtureOccupancySensorEnabled(){
		
		boolean isfixtureOccupancySensorEnabled = false;
		
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
			if(licenses.getOccupancySensor() != null){
				isfixtureOccupancySensorEnabled = licenses.getOccupancySensor().getEnabled();
			}else{
				isfixtureOccupancySensorEnabled = false;
			}
			
		} catch (JsonParseException e) {
			//e.printStackTrace();
		} catch (JsonMappingException e) {
			//e.printStackTrace();
		} catch (IOException e) {
			//e.printStackTrace();
		}
		
		return isfixtureOccupancySensorEnabled;
	}
	
}