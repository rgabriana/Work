package com.ems.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.quartz.JobDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.BACnetConfig;
import com.ems.model.BACnetConfiguration;
import com.ems.model.EventsAndFault;
import com.ems.model.SystemConfiguration;
import com.ems.server.ServerMain;
import com.ems.types.NetworkType;
import com.ems.utils.AdminUtil;
import com.enlightedinc.licenseutil.LicenseUtil;
import com.enlightedinc.vo.Licenses;

@Service("bacnetManager")
@Transactional(propagation = Propagation.REQUIRED)
public class BacnetManager {
	
	@Resource
    private SystemConfigurationManager systemConfigurationManager;
	
	@Resource(name = "bacnetConfigurationManager")
    private BACnetConfigurationManager bacnetConfigurationManager;
	
	@Resource
    private BacnetSchedulerManager bacnetSchedulerManager;
	
	@Resource
    private EventsAndFaultManager eventsAndFaultManager;
	
	@Resource
    private NetworkSettingsManager networkSettingsManager;
	
	public static final Logger logger = Logger.getLogger("SysLog");
	
	JobDetail bacnetSchedulerJob;
	
	private boolean isRunning = false;

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public boolean isRunning() {
		return isRunning;
	}

    public BACnetConfig getConfig() {
        Properties props = getBacnetConfiguration();
        BACnetConfig bacnetConfig = new BACnetConfig();
        if (props != null) {

            bacnetConfig.setApduLength(Integer.parseInt(props.getProperty("MaxAPDU","1476")));
            bacnetConfig.setApduTimeout(Integer.parseInt(props.getProperty("APDUTimeout","10000")));
            bacnetConfig.setEnergymanagerBaseInstance(Long.parseLong(props.getProperty("EnergyManagerBaseInstance","300001")));
            bacnetConfig.setEnergyManagerName("EnergyManager-"+props.getProperty("EnergyManagerBaseInstance","300001"));
            bacnetConfig.setSwitchgroupBaseInstance(Long.parseLong(props.getProperty("SwitchGroupBaseInstance","400001")));
            bacnetConfig.setAreaBaseInstance(Long.parseLong(props.getProperty("AreaBaseInstance","500001")));
            bacnetConfig.setServerPort(Integer.parseInt(props.getProperty("ListenPort","47809")));
            bacnetConfig.setNetworkId(Integer.parseInt(props.getProperty("NetworkId","9999")));
            bacnetConfig.setVendorId(props.getProperty("VendorId","516"));
            bacnetConfig.setRestApiKey(props.getProperty("RestApiKey","bacnet"));
            bacnetConfig.setRestApiSecret(props.getProperty("RestApiSecret","890fa8601b70a152b778eac7fdfb613720a8fc19"));
            bacnetConfig.setDetailedMode(Boolean.parseBoolean(props.getProperty("DetailedMode","false")));
            bacnetConfig.setFixtureOccupancySensor(props.getProperty("fixtureOccupancySensor","disable"));

            
            SystemConfiguration bacnetConfigEnableConfiguration = systemConfigurationManager.loadConfigByName("bacnet_config_enable");
            
            if(bacnetConfigEnableConfiguration !=null){
            	bacnetConfig.setEnableBacnet(Boolean.valueOf(bacnetConfigEnableConfiguration.getValue()));
            }
            

        } else {
            return null;
        }
        return bacnetConfig;
    }

    /**
     * From UI if network interface is disabled for bacnet then call this method
     * @return
     */
    public boolean diableBacnetFromNetworkSettings(){
    	SystemConfiguration bacnetConfigEnableConfiguration = systemConfigurationManager.loadConfigByName("bacnet_config_enable");
    	if(bacnetConfigEnableConfiguration !=null){
    		bacnetConfigEnableConfiguration.setValue("false");
    		systemConfigurationManager.save(bacnetConfigEnableConfiguration);
    	}
    	stopBacnetService();
    	bacnetSchedulerManager.stopBacnetScheduler();
    	
    	return true;
    }
    
    public boolean isBacnetServiceEnabled(){
    	boolean bReturn = false;
    	SystemConfiguration bacnetConfigEnableConfiguration = systemConfigurationManager.loadConfigByName("bacnet_config_enable");
    	if(bacnetConfigEnableConfiguration !=null){
    		bReturn = Boolean.valueOf(bacnetConfigEnableConfiguration.getValue());
    	}
    	return bReturn;
    }
    
    public String saveConfig(BACnetConfig config) {
    	
    	//System.out.println("In saveConfig() start");
    	SystemConfiguration bacnetConfigEnableConfiguration = systemConfigurationManager.loadConfigByName("bacnet_config_enable");
    	Boolean isStopped = false;
    	if(bacnetConfigEnableConfiguration !=null){
    		if(config.getEnableBacnet()){
    			bacnetConfigEnableConfiguration.setValue("true");
    			systemConfigurationManager.save(bacnetConfigEnableConfiguration);
    		}else{
    			bacnetConfigEnableConfiguration.setValue("false");
    			systemConfigurationManager.save(bacnetConfigEnableConfiguration);
    			stopBacnetService();
                bacnetSchedulerManager.stopBacnetScheduler();
                isStopped = true;
                //return "SAVE_SUCCESS";
    		}
    	}
    	
    	String bacnetFile = ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/bacnet/config/bacnet.conf";
//            	
//    	Properties props = new Properties();
//        try {
//			props.load(new FileInputStream(bacnetFile));
//			logProperties(props,"***PREVIOUS BACNET CONFIGURATION VALUES ARE****************");
//		} catch (FileNotFoundException e1) {
//			logger.error("ERROR: FileNotFoundException ", e1);
//		} catch (IOException e1) {
//			logger.error("ERROR: IOException ", e1);
//		}
        
        Properties props = getBacnetConfiguration();
        logProperties(props,"***PREVIOUS BACNET CONFIGURATION VALUES ARE****************");
        
        props.setProperty("EnergyManagerBaseInstance", "" + config.getEnergymanagerBaseInstance());
        props.setProperty("EnergyManagerName", "" + "EnergyManager-" + config.getEnergymanagerBaseInstance());
        props.setProperty("AreaBaseInstance", "" + config.getAreaBaseInstance());
        props.setProperty("NetworkId", "" + config.getNetworkId());
        //props.setProperty("VendorId", "" + config.getVendorId());
        //props.setProperty("APDUTimeout", "" + config.getApduTimeout());
        //props.setProperty("SwitchGroupBaseInstance", "" + config.getSwitchgroupBaseInstance());
        ////props.setProperty("ListenPort", "" + config.getServerPort());
        //props.setProperty("MaxAPDU", "" + config.getApduLength());
        
        try {
			BACnetConfiguration embi = bacnetConfigurationManager.loadBACnetConfigByName("EnergyManagerBaseInstance");
			if(embi!=null){
				embi.setValue(""+config.getEnergymanagerBaseInstance());
				bacnetConfigurationManager.save(embi);
			} else {
				embi = new BACnetConfiguration();
				embi.setName("EnergyManagerBaseInstance");
				embi.setValue("" + config.getEnergymanagerBaseInstance());
				embi.setIsallowedtoshow(true);
				bacnetConfigurationManager.save(embi);
			}
			
			BACnetConfiguration emname = bacnetConfigurationManager.loadBACnetConfigByName("EnergyManagerName");
			if(emname!=null){
				emname.setValue("EnergyManager-" + config.getEnergymanagerBaseInstance());
				bacnetConfigurationManager.save(emname);
			} else {
				emname = new BACnetConfiguration();
				emname.setName("EnergyManagerName");
				emname.setValue("EnergyManager-" + config.getEnergymanagerBaseInstance());
				bacnetConfigurationManager.save(emname);
			}
			
			
			BACnetConfiguration abi = bacnetConfigurationManager.loadBACnetConfigByName("AreaBaseInstance");
			if(abi!=null){
				abi.setValue(""+config.getAreaBaseInstance());
				bacnetConfigurationManager.save(abi);
			} else {
				abi = new BACnetConfiguration();
				abi.setName("AreaBaseInstance");
				abi.setValue(""+config.getAreaBaseInstance());
				abi.setIsallowedtoshow(true);
				bacnetConfigurationManager.save(abi);
			}
			
			BACnetConfiguration networkId = bacnetConfigurationManager.loadBACnetConfigByName("NetworkId");
			if(networkId!=null){
				networkId.setValue(""+config.getNetworkId());
				bacnetConfigurationManager.save(networkId);
			} else {
				networkId = new BACnetConfiguration();
				networkId.setName("NetworkId");
				networkId.setValue(""+config.getNetworkId());
				networkId.setIsallowedtoshow(true);
				bacnetConfigurationManager.save(networkId);
			}
			
			
			BACnetConfiguration vendorId = bacnetConfigurationManager.loadBACnetConfigByName("VendorId");
			
			if(config.getVendorId() != null && config.getVendorId() != "" ){
				//props.setProperty("VendorId", "" + config.getVendorId());
				if(vendorId!=null){
					vendorId.setValue(config.getVendorId());
					bacnetConfigurationManager.save(vendorId);
				} else {
					vendorId = new BACnetConfiguration();
	        		vendorId.setName("VendorId");
	        		vendorId.setValue(config.getVendorId());
	        		vendorId.setIsallowedtoshow(true);
	        		bacnetConfigurationManager.save(vendorId);
	        	}
			}
			
			BACnetConfiguration maxAPDU = bacnetConfigurationManager.loadBACnetConfigByName("MaxAPDU");
			
			if(config.getApduLength() > 0 ){
				//props.setProperty("MaxAPDU", "" + config.getApduLength());
				if(maxAPDU!=null){
					maxAPDU.setValue(""+config.getApduLength());
					bacnetConfigurationManager.save(maxAPDU);
				} else {
					maxAPDU = new BACnetConfiguration();
					maxAPDU.setName("MaxAPDU");
	        		maxAPDU.setValue(""+config.getApduLength());
	        		bacnetConfigurationManager.save(maxAPDU);
	        	}
			}
			
			BACnetConfiguration restApiKey = bacnetConfigurationManager.loadBACnetConfigByName("RestApiKey");
			
			BACnetConfiguration resrApiSecret = bacnetConfigurationManager.loadBACnetConfigByName("RestApiSecret");
			
			//BACnetConfiguration sgbi = bacnetConfigurationManager.loadBACnetConfigByName("SwitchGroupBaseInstance");
			
			BACnetConfiguration fixOccupancy = bacnetConfigurationManager.loadBACnetConfigByName("fixtureOccupancySensor");
			if(config.getFixtureOccupancySensor()!=null && config.getFixtureOccupancySensor()!=""){
				props.setProperty("fixtureOccupancySensor", "" + config.getFixtureOccupancySensor());
				if(fixOccupancy!=null){
					fixOccupancy.setValue(""+config.getFixtureOccupancySensor());
					bacnetConfigurationManager.save(fixOccupancy);
			    } else {
			    	fixOccupancy = new BACnetConfiguration();
			    	fixOccupancy.setName("fixtureOccupancySensor");
			    	fixOccupancy.setValue(""+config.getFixtureOccupancySensor());
					bacnetConfigurationManager.save(fixOccupancy);
			    }
			}
			if(config.getRestApiKey() != null && config.getRestApiKey() != "" ){
				props.setProperty("RestApiKey", "" + config.getRestApiKey());
				if(restApiKey!=null){
			    	restApiKey.setValue(config.getRestApiKey());
			    	bacnetConfigurationManager.save(restApiKey);
			    } else {
			    	restApiKey = new BACnetConfiguration();
			    	restApiKey.setName("RestApiKey");
			    	restApiKey.setValue(config.getRestApiKey());
			    	bacnetConfigurationManager.save(restApiKey);
			    }
			}
			if(config.getRestApiSecret() != null && config.getRestApiSecret() != ""){
			 	props.setProperty("RestApiSecret", "" + config.getRestApiSecret());
			   	if(resrApiSecret!=null){
			   		resrApiSecret.setValue(config.getRestApiSecret());
			    	bacnetConfigurationManager.save(resrApiSecret);
			    } else {
			    	resrApiSecret = new BACnetConfiguration();
			     	resrApiSecret.setName("RestApiSecret");
			     	resrApiSecret.setValue(config.getRestApiSecret());
			 		bacnetConfigurationManager.save(resrApiSecret);
			    }
			}
			
			
			BACnetConfiguration detailedMode = bacnetConfigurationManager.loadBACnetConfigByName("DetailedMode");
			
			if(config.getDetailedMode() != null){
				props.setProperty("DetailedMode", "" +config.getDetailedMode());
				if(detailedMode!=null){
					detailedMode.setValue(""+config.getDetailedMode());
			    	bacnetConfigurationManager.save(detailedMode);
				} else {
			    	detailedMode = new BACnetConfiguration();
			    	detailedMode.setName("DetailedMode");
			    	detailedMode.setValue(""+config.getDetailedMode());
					bacnetConfigurationManager.save(detailedMode);
			    }
			} 
			String listenport = props.getProperty("ListenPort");
			if(listenport == null){
				listenport = "47808";
			}
			BACnetConfiguration listenPortConf = bacnetConfigurationManager.loadBACnetConfigByName("ListenPort");
			if(listenPortConf!=null){
				listenport = listenPortConf.getValue();
			}
			if(String.valueOf(config.getServerPort()) != "" && !listenport.equals(String.valueOf(config.getServerPort()))){
//			if(String.valueOf(config.getServerPort()) != "" && !props.getProperty("ListenPort").equals(String.valueOf(config.getServerPort()))){
				String bacnetInterface = networkSettingsManager.loadCurrentMappingByNetworkType(NetworkType.BACnet.getName());
				if(bacnetInterface != null){
					//Call the shell and execute it.
			     	try {
			     		String[] cmdArr = { "bash", ServerMain.getInstance().getTomcatLocation() + "adminscripts/enablePort.sh", String.valueOf(config.getServerPort()), listenport, bacnetInterface };
						final Process pr = Runtime.getRuntime().exec(cmdArr);
						AdminUtil.readStreamOfProcess(pr);
						pr.waitFor();
					} catch (Exception e) {
						logger.error("ERROR: Not able to enable the port "+ config.getServerPort() +" on BACnet Interface "+bacnetInterface, e);
					}
				}else{
					//Call the shell and execute it.
			     	try {
			     		String[] cmdArr = { "bash", ServerMain.getInstance().getTomcatLocation() + "adminscripts/enablePort.sh", String.valueOf(config.getServerPort()), listenport };
						final Process pr = Runtime.getRuntime().exec(cmdArr);
						AdminUtil.readStreamOfProcess(pr);
						pr.waitFor();
					} catch (Exception e) {
						logger.error("ERROR: Not able to enable the port "+ config.getServerPort(), e);
					}
				}
				props.setProperty("ListenPort", "" + config.getServerPort());
				if(listenPortConf!=null){
					listenPortConf.setValue(String.valueOf(config.getServerPort()));
			    	bacnetConfigurationManager.save(listenPortConf);
			    } else {
			    	listenPortConf = new BACnetConfiguration();
			    	listenPortConf.setName("ListenPort");
			    	listenPortConf.setValue(String.valueOf(config.getServerPort()));
			    	listenPortConf.setIsallowedtoshow(true);
					bacnetConfigurationManager.save(listenPortConf);
			    }
			}
		} catch (Exception e1) {
			logger.error("ERROR: Not able to set config values to DB ", e1);
			return "SAVE_ERROR";
		}
        
        try {
            props.store(new FileOutputStream(bacnetFile), null);
            logProperties(props,"***UPDATED--BACNET CONFIGURATION VALUES ARE****************");
            String desc = "Bacnet configuration changed - ";
            
            eventsAndFaultManager.addEvent(desc, EventsAndFault.BACNET_EVENT_STR, EventsAndFault.INFO_SEV_STR);
            if(!isStopped){
            	if(isBacnetEnabled()){
                	stopBacnetService();
                	bacnetSchedulerManager.stopBacnetScheduler();
                	if(!isBacnetServiceRunning()){
                		startBacnetService();
                	}
                	bacnetSchedulerManager.startBacnetScheduler();
                }else{
                	stopBacnetService();
                	bacnetSchedulerManager.stopBacnetScheduler();
                }
            }
        } catch (Exception e) {
            logger.error("ERROR: while setting properties to DB and bacnet.conf ", e);
            return "SAVE_ERROR";
        }
        logger.debug("properties set to DB and bacnet.conf ");
        return "SAVE_SUCCESS";
    }

	private String logProperties(Properties props, final String s) {
		String bufStr;
		//Print the previous bacnet properties 
		final Set set  = props.keySet();
		final StringBuffer buf = new StringBuffer("");
		if (set != null){
			for (final Object o : set){
				if (o != null){
					buf.append(o.toString()+":"+props.getProperty(o.toString())+";");
				}
			}
		}
		bufStr = buf.toString();
		
		logger.error(s+"::"+bufStr);
		return bufStr;
	}
    
    
    public Properties getBacnetConfiguration() {

//        Properties bacnetProp = new Properties();
        Properties bacnetConfProp = new Properties();
        
//        String bacnetFile = ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/bacnet/config/bacnet.conf";
        
        final List<BACnetConfiguration> list = bacnetConfigurationManager.loadAllBACnetConfig();
        if (list != null && !list.isEmpty()) {
        	for(BACnetConfiguration bConf : list){
        		bacnetConfProp.setProperty(bConf.getName(), bConf.getValue());
        	}
        }
        
//        try {
//        	bacnetProp.load(new FileInputStream(bacnetFile));
//        } catch (Exception ex) {
//        	logger.error("Exception Occured while loading bacnet.conf file"+ex);
//        }
        
        return bacnetConfProp;
    } 
    
    public boolean isBacnetEnabled(){
    	boolean result =false ;
    	
    	SystemConfiguration emUUID = systemConfigurationManager.loadConfigByName("em.UUID");
		
		if(emUUID != null){
			String secretKeyString = LicenseUtil.SECRET_LICENSE_KEY + emUUID.getValue();
			
			SystemConfiguration emLicenseKeyValue = systemConfigurationManager.loadConfigByName("emLicenseKeyValue");
			
			if(emLicenseKeyValue != null){
				
				String jsonLicenseString = null;
				
				try {
					jsonLicenseString = LicenseUtil.decrypt(secretKeyString, emLicenseKeyValue.getValue());
				} catch (Exception e1) {
					//e1.printStackTrace();
					logger.debug("Unable to decrypt secretKeyString ",e1);
				}
					
				try {
					Licenses licenses = new ObjectMapper().readValue(jsonLicenseString, Licenses.class);
					if(licenses.getBacnet() != null){
						result = licenses.getBacnet().getEnabled();
					}
				} catch (Exception e2){
					logger.debug("Unable to read value from jsonLicenseString to the licenses ",e2);
				}
			}
		}
		//System.out.println("In isBacnetEnabled():"+result);
    	return result;
    }
    
    public void startBacnetService() {
    	//System.out.println("In startBacnetService() start");
        try {
        	setRunning(true);
        	String tomcatLocation = ServerMain.getInstance().getTomcatLocation();
        	String bacnetLightingFile = tomcatLocation + "../../Enlighted/bacnet/bacnetLighting";
        	String bacnetFile = tomcatLocation + "../../Enlighted/bacnet/config/bacnet.conf";
        	Process bacnetProcess =  Runtime.getRuntime().exec( "sudo " + bacnetLightingFile+" -3 -c -f " + bacnetFile );
        	//AdminUtil.readStreamOfProcess(bacnetProcess);
        	//if(bacnetProcess.waitFor() == 0){
        		//if (bacnetProcess.exitValue() == 0){
            		String desc = "Bacnet service started";
                    if(logger.isInfoEnabled()) {
                      logger.info(desc);
                    }
                    eventsAndFaultManager.addEvent(desc, EventsAndFault.BACNET_EVENT_STR, EventsAndFault.INFO_SEV_STR);
            	/*}else{
            		String desc = "Bacnet service could not be started";
                    logger.error(desc);
                    eventsAndFaultManager.addEvent(desc, EventsAndFault.BACNET_EVENT_STR, EventsAndFault.CRITICAL_SEV_STR);
            		logger.error(desc);
            	}*/
        	//}
        } catch (Exception e) {
        	String desc = "Bacnet service could not be started";
            logger.error(desc);
            eventsAndFaultManager.addEvent(desc, EventsAndFault.BACNET_EVENT_STR, EventsAndFault.CRITICAL_SEV_STR);
    		logger.error(desc + e.getMessage());
    		//System.out.println("In startBacnetService() exception");
        } finally {
			setRunning(false);
		}
        //System.out.println("In startBacnetService() end");
    }
    
    public void stopBacnetService() {
    	//System.out.println("In stopBacnetService() start");
        try {
            Process bacnetProcess = Runtime.getRuntime().exec("sudo pidof bacnetLighting");
            
            //AdminUtil.readStreamOfProcess(bacnetProcess);
            
            if(bacnetProcess.waitFor() == 0){
            	if (bacnetProcess.exitValue() != 0) return;
                BufferedReader outReader = new BufferedReader(new InputStreamReader(bacnetProcess.getInputStream()));
                /*for (String pid : outReader.readLine().trim().split(" ")) {
                	logger.info("Killing bacnetLighting pid: "+pid);
                	Process killBacnet = Runtime.getRuntime().exec("sudo kill " + pid);
                	int status = killBacnet.waitFor();
                	String desc = "Bacnet service stopped: Status  " + status;
                    if(logger.isInfoEnabled()) {
                      logger.info(desc);
                    }
                    eventsAndFaultManager.addEvent(desc, EventsAndFault.BACNET_EVENT_STR, EventsAndFault.INFO_SEV_STR);
                }*/
                
                String pid = null;
                while ((pid = outReader.readLine()) != null) {
                	logger.info("Killing bacnetLighting pid: "+pid);
                	Process killBacnet = Runtime.getRuntime().exec("sudo kill " + pid);
                	int status = killBacnet.waitFor();
                	String desc = "Bacnet service stopped: Status  " + status;
                    if(logger.isInfoEnabled()) {
                      logger.info(desc);
                    }
                    eventsAndFaultManager.addEvent(desc, EventsAndFault.BACNET_EVENT_STR, EventsAndFault.INFO_SEV_STR);
                }
                //AdminUtil.readStreamOfProcess(bacnetProcess);
           }
        } catch (Exception e) {
            logger.error("Bacnet process could not be stopped " + e.getMessage());
            //System.out.println("In stopBacnetService() exception");
        }
        //System.out.println("In stopBacnetService() end");
    } 
    
    public boolean isBacnetServiceRunning() {
    	
    	boolean isBacnetServiceRunning = false;
    	
    	try {
            Process bacnetProcess = Runtime.getRuntime().exec("sudo pidof bacnetLighting");
            
            //AdminUtil.readStreamOfProcess(bacnetProcess);
            
            if (bacnetProcess.waitFor() == 0) {
            	
            	if(bacnetProcess.exitValue() == 0){
            		isBacnetServiceRunning = true;
            	}
            	else{
            		isBacnetServiceRunning = false;
            	}
            }
        } catch (Exception e) {
            logger.error("Bacnet process is not found " + e.getMessage());
        }
        //System.out.println("In isBacnetServiceRunning():"+isBacnetServiceRunning);
        return isBacnetServiceRunning;
    }
    
    public void enableBacnetPort(){
    	String port = getBacnetConfiguration().getProperty("ListenPort");
    	if(port != "" && port != null){
    		String bacnetInterface = networkSettingsManager.loadCurrentMappingByNetworkType(NetworkType.BACnet.getName());
        	if(bacnetInterface != null){
        		try {
             		String[] cmdArr = { "bash", ServerMain.getInstance().getTomcatLocation() + "adminscripts/enablePort.sh", port, "", bacnetInterface };
        			final Process pr = Runtime.getRuntime().exec(cmdArr);
        			AdminUtil.readStreamOfProcess(pr);
        			pr.waitFor();
        		} catch (Exception e) {
        			logger.error("ERROR: Not able to enable the port "+ port +" on BACnet Interface "+bacnetInterface, e);
        		}
        	}else{
        		try {
             		String[] cmdArr = { "bash", ServerMain.getInstance().getTomcatLocation() + "adminscripts/enablePort.sh", port, ""};
        			final Process pr = Runtime.getRuntime().exec(cmdArr);
        			AdminUtil.readStreamOfProcess(pr);
        			pr.waitFor();
        		} catch (Exception e) {
        			logger.error("ERROR: Not able to enable the port "+ port, e);
        		}
        	}
    	}
    }
    
}