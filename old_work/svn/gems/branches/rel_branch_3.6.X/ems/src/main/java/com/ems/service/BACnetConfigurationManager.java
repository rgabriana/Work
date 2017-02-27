/**
 * 
 */
package com.ems.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.BACnetConfigurationDao;
import com.ems.model.BACnetConfig;
import com.ems.model.BACnetConfiguration;
import com.ems.model.BacnetObjectsCfg;
import com.ems.model.BacnetReportConfiguration;
import com.ems.model.BacnetReportConfigurationList;
import com.ems.model.SystemConfiguration;
import com.ems.server.ServerMain;
import com.ems.util.Constants;
import com.ems.ws.util.Response;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * @author NileshS
 * 
 */
@Service("bacnetConfigurationManager")
@Transactional(propagation = Propagation.REQUIRED)
public class BACnetConfigurationManager {
	
	private static final Logger m_Logger = Logger.getLogger("SysLog");

    @Resource
    private BACnetConfigurationDao bacnetConfigurationDao;
    
    @Resource(name = "bacnetManager")
	private BacnetManager bacnetManager;
    
    @Resource
    private SystemConfigurationManager systemConfigurationManager;
    
    @Resource
    private AreaManager areaManager;

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.BACnetConfigurationManager#delete(java.lang.Long)
     */

    public void delete(Long id) {
    	bacnetConfigurationDao.removeObject(BACnetConfiguration.class, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.BACnetConfigurationManager#loadAllBACnetConfig()
     */

    public List<BACnetConfiguration> loadAllBACnetConfig() {
        return bacnetConfigurationDao.loadAllBACnetConfig();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.BACnetConfigurationManager#loadAllBacnetObjectCfgs()
     */

    public List<BacnetObjectsCfg> loadAllBacnetObjectCfgs() {
        return bacnetConfigurationDao.loadAllBacnetObjectCfgs();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.BACnetConfigurationManager#getAllBacnetObjectCfgsForUI()
     */

    public List<BacnetObjectsCfg> getAllBacnetObjectCfgsForUI() {
        return bacnetConfigurationDao.getAllBacnetObjectCfgsForUI();
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.BACnetConfigurationManager#loadBACnetConfigForUI()
     */

    public List<BACnetConfiguration> loadBACnetConfigForUI() {
        return bacnetConfigurationDao.loadBACnetConfigForUI();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.BACnetConfigurationManager#loadAllBACnetConfigMap()
     */

    public HashMap<String, String> loadAllBACnetConfigMap() {
        return bacnetConfigurationDao.loadAllBACnetConfigMap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.BACnetConfigurationManager#loadBACnetConfigById(java.lang.Long)
     */

    public BACnetConfiguration loadBACnetConfigById(Long id) {
        return bacnetConfigurationDao.loadBACnetConfigById(id);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.BACnetConfigurationManager#loadBACnetConfigById(java.lang.Long)
     */

    public BacnetObjectsCfg loadBacnetObjectsCfgById(Long id) {
        return bacnetConfigurationDao.loadBacnetObjectsCfgById(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.BACnetConfigurationManager#save(com.ems.model.BACnetConfiguration)
     */

    public BACnetConfiguration save(BACnetConfiguration bacnetConfiguration) {
        return (BACnetConfiguration) bacnetConfigurationDao.saveObject(bacnetConfiguration);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.BACnetConfigurationManager#saveBacnetObjectsCfg(com.ems.model.BacnetObjectsCfg)
     */

    public BacnetObjectsCfg saveBacnetObjectsCfg(BacnetObjectsCfg bacnetObjectsCfg) {
        return (BacnetObjectsCfg) bacnetConfigurationDao.saveObject(bacnetObjectsCfg);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.BACnetConfigurationManager#saveBacnetObjectsCfg(com.ems.model.BacnetObjectsCfg)
     */

    public BacnetReportConfiguration saveBacnetReportCfg(BacnetReportConfiguration bacnetReprtCfg) {
        return (BacnetReportConfiguration) bacnetConfigurationDao.saveObject(bacnetReprtCfg);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.BACnetConfigurationManager#update(com.ems.model.BACnetConfiguration)
     */

    public BACnetConfiguration update(BACnetConfiguration bacnetConfiguration) {
        return (BACnetConfiguration) bacnetConfigurationDao.saveObject(bacnetConfiguration);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.BACnetConfigurationManager#loadBACnetConfigByName(java.lang.String)
     */

    public BACnetConfiguration loadBACnetConfigByName(String name) {
        return bacnetConfigurationDao.loadBACnetConfigByName(name);
    }
    
    public List<BACnetConfiguration> getIntialBACnetConfig(){
    	return bacnetConfigurationDao.getIntialBACnetConfig();
    }
    
    public List<BacnetObjectsCfg> getIntialBacnetObjectCfg(){
    	return bacnetConfigurationDao.getIntialBacnetObjectCfg();
    }
    
    public Response saveBACnetConfigurationDetails(List<BACnetConfiguration> bACnetConfigurations) {
		Response resp = new Response();
		final List<String> allowedParamsToSave = Arrays.asList(new String[]{"AreaBaseInstance","EnergyManagerBaseInstance","ListenPort","NetworkId"});
		String emBaseInstanceId = "";
		final List<String> notAllowedParamsToSave = Arrays.asList(new String[]{"DBFile","DeviceNameFormatStringArea","DeviceNameFormatStringEM","DeviceNameFormatStringSwitchGroup","EnergyManagerInstance","EnergyManagerName","GemsIpAddress","LogLevel","ObjectNameFormatStringArea","ObjectNameFormatStringEM","ObjectNameFormatStringFixture","ObjectNameFormatStringPlugload","ObjectNameFormatStringSwitchGroup","ObjectsFile","RestApiKey","RestApiSecret","SwitchGroupBaseInstance","UpdateAreaTimeout","UpdateConfigTimeout","UpdateOccupancyTimeout","Interface","ObjectNameFormatStringAreaFixture","EnableSwitchgroups","ObjectNameFormatStringSwitchSceneDimLevel","ObjectNameFormatStringEMFixture","ObjectNameFormatStringAreaPlugload","ObjectNameFormatStringSwitchScenePlugLevel","InteractiveLogLevel","VendorId","DetailedMode","fixtureOccupancySensor","MaxAPDU","APDUTimeout"});
		try {
			if(bACnetConfigurations !=null){
				for(BACnetConfiguration bacnetConfiguration : bACnetConfigurations){
					if(bacnetConfiguration!=null){
						if (notAllowedParamsToSave.contains(bacnetConfiguration.getName())){
							continue;
						}
						if(bacnetConfiguration.getName()!=null && bacnetConfiguration.getName().equals("EnergyManagerBaseInstance")){
							emBaseInstanceId = bacnetConfiguration.getValue();
						}
						final BACnetConfiguration dbConf = loadBACnetConfigById(bacnetConfiguration.getId());
						if(dbConf!=null){
							dbConf.setName(bacnetConfiguration.getName());
							dbConf.setValue(bacnetConfiguration.getValue());
							save(dbConf);
						} else {
							BACnetConfiguration newDbConf = new BACnetConfiguration();
							newDbConf.setName(bacnetConfiguration.getName());
							newDbConf.setValue(bacnetConfiguration.getValue());
							save(newDbConf);
						}
					} else {
						m_Logger.error("BACnetConfiguration is null");
						throw new IllegalArgumentException("BACnetConfiguration is null");
					}
				}
			}
			final BACnetConfiguration energyMangerNameConf = loadBACnetConfigByName("EnergyManagerName");
			if(emBaseInstanceId!=null && !emBaseInstanceId.equals("") && energyMangerNameConf!=null){
				energyMangerNameConf.setValue("EnergyManager-"+emBaseInstanceId);
				save(energyMangerNameConf);
			}
		} catch (Exception e) {
			m_Logger.error("Error occured saving BacnetConfig", e);
			resp.setMsg("Not able to update bacnetConfig");
			resp.setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		return resp;
	}
    
    public Response saveBacnetObjectCfgs(List<BacnetObjectsCfg> bacObjCfgs){
    	Response resp = new Response();
    	boolean setDetailedMode = false;
    	String result = "SAVE_ERROR";
		try {
			if(bacObjCfgs !=null){
				for(BacnetObjectsCfg boc : bacObjCfgs){
					if(boc!=null){
						final BacnetObjectsCfg dbConf = loadBacnetObjectsCfgById(boc.getId());
						if(dbConf!=null){
							dbConf.setIsvalidobject(boc.getIsvalidobject());
							try {
								saveBacnetObjectsCfg(dbConf);
								m_Logger.error("Updated Bacnet_Objects_Cfg Successfully");
								resp.setMsg("Updated Bacnet_Objects_Cfg Successfully");
								if(Constants.BACNET_FIXSUBHEADER.equals(dbConf.getBacnetpointtype()) || Constants.BACNET_PLSUBHEADER.equals(dbConf.getBacnetpointtype())){
									if("y".equals(dbConf.getIsvalidobject())){
										setDetailedMode = true;
									}
								}
							} catch (Exception e) {
								m_Logger.error("Unable to Update Bacnet_Objects_Cfg", e);
								resp.setMsg("Unable to Update Bacnet_Objects_Cfg");
								throw new IllegalArgumentException("BacnetObjectsCfg is null");
							}
						}
					} else {
						m_Logger.error("BacnetObjectsCfg is null");
						throw new IllegalArgumentException("BacnetObjectsCfg is null");
					}
				}
				try{
            		BACnetConfig bacnetConfig = bacnetManager.getConfig();
            		boolean dbDetailedMode = bacnetConfig.getDetailedMode();
            		result = "SAVE_SUCCESS";
            		if(setDetailedMode && !dbDetailedMode){
            			bacnetConfig.setDetailedMode(true);
            		} if(!setDetailedMode && dbDetailedMode) {
            			bacnetConfig.setDetailedMode(false);
            		}
            		result = bacnetManager.saveConfig(bacnetConfig);
            		if("SAVE_ERROR".equals(result)) {
            			m_Logger.error("Unable to set Detailed Mode,Error while saving Bacent configuration ");
						resp.setMsg("Unable to set Detailed Mode,Error while saving Bacent configuration ");
						throw new IllegalArgumentException("Unable to set Detailed Mode,Error while saving Bacent configuration ");
                	}
        		}catch(Exception e){
    				m_Logger.error("ERROR: While fetching bacnet configuration for detailed mode", e);
    			}
			}
		} catch (Exception e) {
			m_Logger.error("Error occured saving BacnetObjectsCfg", e);
			resp.setMsg("Not able to update BacnetObjectsCfg");
			resp.setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		return resp;
    }
    
    public Response updateBacnetReportConfiguration(List<BacnetReportConfiguration> bacReportConf){
    	Response resp = new Response();
    	try{
    		if(bacReportConf!=null && !bacReportConf.isEmpty()){
    			// truncate the table before saving new values to DB
    			
    			for(BacnetReportConfiguration brc : bacReportConf){
        			if(brc!=null){
        				saveBacnetReportCfg(brc);
        				m_Logger.error("Success");
        				resp.setMsg("Post Success bacnetReport.txt");
        				resp.setStatus(javax.ws.rs.core.Response.Status.OK.getStatusCode());
        			} else {
        				m_Logger.error("BacnetReportConfiguration is null");
        				resp.setMsg("BacnetReportConfiguration is null ");
        				resp.setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        				throw new IllegalArgumentException("BacnetReportConfiguration is null ");
        			}
        		}
    		}
    	} catch(Exception e){
    		m_Logger.error("Error occured saving BacnetReportConfiguration", e);
			resp.setMsg("Not able to save BacnetReportConfiguration");
			resp.setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    	}
    	return resp;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.BACnetConfigurationManager#loadBacnetPointConfigurations()
     */

    public void truncateBacnetReportCfgs() {
       bacnetConfigurationDao.truncateBacnetReportCfgs();
    }
    
    public Boolean getIsAllowedToAccessBacnetPoint(String pointkeyword){
		return bacnetConfigurationDao.getIsAllowedToAccessBacnetPoint(pointkeyword);
	}
	
	public List<BacnetReportConfiguration> loadAllBacnetReportCfgs(){
		return bacnetConfigurationDao.loadAllBacnetReportCfgs();
	}

	public BacnetReportConfigurationList loadAllBacnetReportCfgs(String order,
			String orderway, Boolean bSearch, String searchField,
			String searchString, String searchOper, int offset, int limit){
		return bacnetConfigurationDao.loadAllBacnetReportCfgs(order,orderway,bSearch,searchField,searchString,searchOper, offset, limit);
	}
}
