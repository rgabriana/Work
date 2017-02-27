package com.ems.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ems.action.SpringContext;
import com.ems.dao.FirmwareUpgradeDao;
import com.ems.dao.FirmwareUpgradeScheduleDao;
import com.ems.model.FirmwareUpgrade;
import com.ems.model.FirmwareUpgradeSchedule;
import com.ems.model.ImageUpgradeDBJob;
import com.ems.model.ImageUpgradeDeviceStatus;
import com.ems.model.SystemConfiguration;
import com.ems.model.User;
import com.ems.server.ServerConstants;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.ws.util.Response;


/**
 * 
 * @author pankaj kumar chauhan
 *
 */
@Service("firmwareUpgradeManager")
@Transactional(propagation=Propagation.REQUIRED)
public class FirmwareUpgradeManager{

	@Resource
	private FirmwareUpgradeDao firmwareUpgradeDao;
	@Resource
	private FirmwareUpgradeScheduleDao firmwareUpgradeScheduleDao;

	public void save(FirmwareUpgrade firmwareUpgrade){
		firmwareUpgradeDao.saveObject(firmwareUpgrade);
		//ImageUpgradeSO.getInstance().updateFile(true);
	}

	public void update(FirmwareUpgrade firmwareUpgrade){
		firmwareUpgradeDao.saveObject(firmwareUpgrade);
		//ImageUpgradeSO.getInstance().updateFile(true);
	}

	@SuppressWarnings("unchecked")
	public FirmwareUpgrade loadFirmwareUpgrade(){
		List<FirmwareUpgrade> firmwareUpgrades = firmwareUpgradeDao.getFirmWareUpgradeList();
		if(firmwareUpgrades != null && !firmwareUpgrades.isEmpty()){
			return firmwareUpgrades.get(0);
		}
		return null;
	}

	public FirmwareUpgrade loadFirmwareUpgradeByDeviceType(Integer deviceType){
		return firmwareUpgradeDao.loadFirmwareUpgradeByDeviceType(deviceType);
	}
	
	public void uploadImageFile(String path, String fileName, MultipartFile file) throws IOException{
		File imageStore = new File(path);
		if(!imageStore.exists()){
			imageStore.mkdirs();
		}
		
		File image=new File(path, fileName);
		
		byte[] bytes = file.getBytes();
		FileOutputStream fos = new FileOutputStream(image);
		fos.write(bytes);
		fos.flush();
		fos.close();
	}
	
	/*public Response deleteFirmwareImage(String fileName){
		Response response = new Response();
		File image=new File(path, fileName);
		
		if (image.exists()) {
			image.delete();
			response.setStatus(1);
			return response;
		}else{
			response.setStatus(0);
			return response;
		}
	}*/

	public Map<String, String[]> getFirmwareImagesList(String filepath){
		String strSUAppPattern = "su_app";
		String strSUFirmPattern = "su_firm";
		String strGWAppPattern = "gw_app";
		String strGWFirmPattern = "gw_firm";
		String strSUPycPattern = "su_pyc";
		String strGWPycPattern = "gw_pyc";
		String strSU20AppPattern = "su.bin";
		String strGW20AppPattern = "gw.tar";
		String strCU20AppPattern = "cu.bin";
        String strSW20AppPattern = "sw.bin";
        String strSUBLEPattern = "_ble";
        
        String strPLAppPattern = "pl.bin";

		File file=new File(filepath);
		if(!file.exists()){
			file.mkdirs();
		}
		SystemConfigurationManager sysMgr = (SystemConfigurationManager)SpringContext.getBean("systemConfigurationManager");
		SystemConfiguration oConfig = sysMgr.loadConfigByName("upgrade.su_app_pattern");
		if (oConfig != null) {
			strSUAppPattern = oConfig.getValue();
//			logger.debug("SU App pattern: " + strSUAppPattern);
		}

		
		oConfig = sysMgr.loadConfigByName("upgrade.su_ble_pattern");
		if (oConfig != null) {
			strSUBLEPattern = oConfig.getValue();
		}
		
		oConfig = sysMgr.loadConfigByName("upgrade.su_firm_pattern");
		if (oConfig != null) {
			strSUFirmPattern = oConfig.getValue();
//			logger.debug("SU Firmware pattern: " + strSUFirmPattern);
		}		
		
		oConfig = sysMgr.loadConfigByName("upgrade.gw_app_pattern");
		if (oConfig != null) {
			strGWAppPattern = oConfig.getValue();
//			logger.debug("GW App pattern: " + strGWAppPattern);
		}		

		oConfig = sysMgr.loadConfigByName("upgrade.gw_firm_pattern");
		if (oConfig != null) {
			strGWFirmPattern = oConfig.getValue();
//			logger.debug("GW Firm pattern: " + strGWFirmPattern);
		}
		
		oConfig = sysMgr.loadConfigByName("upgrade.su_pyc_pattern");
		if (oConfig != null) {
			strSUPycPattern = oConfig.getValue();
//			logger.debug("SU Radio pattern: " + strSUPycPattern);
		}		
		
		oConfig = sysMgr.loadConfigByName("upgrade.gw_pyc_pattern");
		if (oConfig != null) {
			strGWPycPattern = oConfig.getValue();
//			logger.debug("GW Radio pattern: " + strGWPycPattern);
		}

		// 2.0 Patterns
		oConfig = sysMgr.loadConfigByName("upgrade.su_20_pattern");
		if (oConfig != null) {
			strSU20AppPattern = oConfig.getValue();
//			logger.debug("SU 20 App pattern: " + strSU20AppPattern);
		}

		oConfig = sysMgr.loadConfigByName("upgrade.gw_20_pattern");
		if (oConfig != null) {
			strGW20AppPattern = oConfig.getValue();
//			logger.debug("GW 20 App pattern: " + strGW20AppPattern);
		}

		oConfig = sysMgr.loadConfigByName("upgrade.cu_20_pattern");
		if (oConfig != null) {
			strCU20AppPattern = oConfig.getValue();
//			logger.debug("CU 20 App pattern: " + strCU20AppPattern);
		}
		
	      // 2.0 Patterns
        oConfig = sysMgr.loadConfigByName("upgrade.sw_20_pattern");
        if (oConfig != null) {
            strSW20AppPattern = oConfig.getValue();
//          logger.debug("SW 20 App pattern: " + strSW20AppPattern);
        }

        oConfig = sysMgr.loadConfigByName("upgrade.plugload_pattern");
        if (oConfig != null) {
        	strPLAppPattern = oConfig.getValue();
        }
        
		List<String> oSUImages = new ArrayList<String>();
		List<String> oGWImages = new ArrayList<String>();
		List<String> oCUImages = new ArrayList<String>();
        List<String> oSWImages = new ArrayList<String>();
        List<String> oPLImages = new ArrayList<String>();
		String sName;
		File[] files=new File(filepath).listFiles();
		if(files!=null && files.length>0){
			for(int i=0;i<files.length;i++){
				sName = files[i].getName();
				if ((sName.indexOf(strSUAppPattern) != -1) || (sName.indexOf(strSUFirmPattern) != -1) ||
				    sName.indexOf(strSUPycPattern) != -1 || (sName.indexOf(strSU20AppPattern) != -1)
				    || (sName.indexOf(strSUBLEPattern) != -1) ) {
					oSUImages.add(sName);
				} else if ((sName.indexOf(strGWAppPattern) != -1) || (sName.indexOf(strGWFirmPattern) != -1) || (sName.indexOf(strGW20AppPattern) != -1)) {
					oGWImages.add(sName);
				} else if ((sName.indexOf(strCU20AppPattern) != -1)) {
					oCUImages.add(sName);
                } else if ((sName.indexOf(strSW20AppPattern) != -1)) {
                    oSWImages.add(sName);
                }else if((sName.indexOf(strPLAppPattern) != -1)){
                	oPLImages.add(sName);
                }
                	
			}
		}

		//Arrange items in reverse order to fill the combo
		Collections.reverse(oSUImages);
		Collections.reverse(oCUImages);
		Collections.reverse(oGWImages);
        Collections.reverse(oSWImages);
        Collections.reverse(oPLImages);
		
		String[] firmwareFxUpgradeFileNames=new String[oSUImages.size() + oCUImages.size()];
		String[] firmwareGWUpgradeFileNames=new String[oGWImages.size()];
        String[] firmwareSWUpgradeFileNames=new String[oSWImages.size()];
        String[] firmwarePlugloadUpgradeFileNames=new String[oPLImages.size()];
		int suimagecount = 0;
		// Add SU images
		for (int count = 0; count < oSUImages.size(); count++, suimagecount++) {
			firmwareFxUpgradeFileNames[suimagecount] = oSUImages.get(count);
		}
		// Add CU images
		for (int count = 0; count < oCUImages.size(); count++, suimagecount++) {
			firmwareFxUpgradeFileNames[suimagecount] = oCUImages.get(count);
		}

		// Add GW images
		for (int count = 0; count < oGWImages.size(); count++) {
			firmwareGWUpgradeFileNames[count] = oGWImages.get(count);
		}
		// Add WDS image
        for (int count = 0; count < oSWImages.size(); count++) {
            firmwareSWUpgradeFileNames[count] = oSWImages.get(count);
        }
        // Add Plugload image
        for (int count = 0; count < oPLImages.size(); count++) {
        	firmwarePlugloadUpgradeFileNames[count] = oPLImages.get(count);
        }
		Map<String, String[]> result = new HashMap<String, String[]>();
		result.put("fixtureUpgradeimages", firmwareFxUpgradeFileNames);
		result.put("gatewayUpgradeimages", firmwareGWUpgradeFileNames);
        result.put("wdsUpgradeimages", firmwareSWUpgradeFileNames);
        result.put("plugloadUpgradeimages", firmwarePlugloadUpgradeFileNames);
		return result;
	}
	
	/**
	 * Saves filenames in firmware_upgrade table, which is now deprecated
	 * @param deviceType
	 * @param deviceIds
	 * @param imageFileName
	 * @param user
	 * @return success result
	 */
	@Deprecated
	public int saveImageVersion(int deviceType, Long[] deviceIds, String imageFileName, User user) {
		int result = 0;

		Date date = new Date();
		
		if (deviceType == ServerConstants.DEVICE_FIXTURE) {
			FirmwareUpgrade firmwareUpgrade = loadFirmwareUpgradeByDeviceType(ServerConstants.DEVICE_FIXTURE);
			if (firmwareUpgrade == null) {
				firmwareUpgrade = new FirmwareUpgrade();
			}
			firmwareUpgrade.setFileName(imageFileName);
			firmwareUpgrade.setVersion(null);
			firmwareUpgrade.setUser(user);
			firmwareUpgrade.setUpgradeOn(date);
			firmwareUpgrade.setDeviceType(ServerConstants.DEVICE_FIXTURE);
			save(firmwareUpgrade);
		} else if (deviceType == ServerConstants.DEVICE_GATEWAY) {
			FirmwareUpgrade firmwareGWUpgrade = loadFirmwareUpgradeByDeviceType(ServerConstants.DEVICE_GATEWAY);
			if (firmwareGWUpgrade == null) {
				firmwareGWUpgrade = new FirmwareUpgrade();
			}
			firmwareGWUpgrade.setFileName(imageFileName);
			firmwareGWUpgrade.setVersion(null);
			firmwareGWUpgrade.setUser(user);
			firmwareGWUpgrade.setUpgradeOn(date);
			firmwareGWUpgrade.setDeviceType(ServerConstants.DEVICE_GATEWAY);
			save(firmwareGWUpgrade);
		}
		
		return result;
	}
	
	public ImageUpgradeDBJob save(ImageUpgradeDBJob imageUpgradeJob){
		return (ImageUpgradeDBJob)firmwareUpgradeDao.saveObject(imageUpgradeJob);		
	}
	
	public ImageUpgradeDeviceStatus save(
	    ImageUpgradeDeviceStatus imageUpgradeDeviceStatus){
	  return (ImageUpgradeDeviceStatus)firmwareUpgradeDao.saveObject(
	      imageUpgradeDeviceStatus);
	}

	public void updateDeviceStatus(Long jobId, Long deviceId, String status) {
	  firmwareUpgradeDao.updateDeviceStatus(jobId, deviceId, status);
	}

	public void startDeviceUpgrade(Long jobId, Long[] devicesIds) {
	  firmwareUpgradeDao.startDeviceUpgrade(jobId, devicesIds);
	} //end of method startDeviceUpgrade
	
	public void finishDeviceUpgrade(Long jobId, Long deviceId, String status,
	    int noOfAtts, String desc, String newVersion) {
	  firmwareUpgradeDao.finishDeviceUpgrade(jobId, deviceId, status, noOfAtts, desc, newVersion);
	} //end of method finishDeviceUpgrade
	
	public List<ImageUpgradeDeviceStatus> loadDeviceStatus(Date fromDate,Date toDate) {
		return firmwareUpgradeDao.loadDeviceStatus(fromDate,toDate);
	}
	public List<ImageUpgradeDeviceStatus> loadInprogressDeviceStatus(Date fromDate,Date toDate) {
		return firmwareUpgradeDao.loadInprogressDeviceStatus(fromDate,toDate);
	}
	
	public void scheduleFirmwareUpgradeJob(final ArrayList<ImageUpgradeDBJob> jobList, final Date schedTime) {
		
		new Thread("Device ImageUpgrade") {
			public void run() {  				
				ImageUpgradeSO.getInstance().startDeviceImageUpgrade(jobList, schedTime);
			}
		}.start();
  					
	} //end of method scheduleFirmwareUpgradeJob
	
	public void abortJob(long jobId) {
		
		ImageUpgradeSO.getInstance().abortJob(jobId);
		
	} //end of method cancelFirmwareUpgrade
	
} //end of class FirmwareUpgradeManager