/**
 * 
 */
package com.ems.service;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.CannedProfileConfigurationDao;
import com.ems.dao.SystemConfigurationDao;
import com.ems.model.CannedProfileConfiguration;
import com.ems.model.SystemConfiguration;

/**
 * @author yogesh
 * 
 */
@Service("cannedProfileConfigurationManager")
@Transactional(propagation = Propagation.REQUIRED)
public class CannedProfileConfigurationManager {

    @Resource
    private CannedProfileConfigurationDao customProfileConfigurationDao;

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.SystemConfigurationManager#delete(java.lang.Long)
     */

    public void delete(Long id) {
        customProfileConfigurationDao.removeObject(SystemConfiguration.class, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.SystemConfigurationManager#loadAllConfig()
     */

    public List<CannedProfileConfiguration> loadAllConfig() {
        return customProfileConfigurationDao.loadAllConfig();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.SystemConfigurationManager#loadAllConfigMap()
     */

    public HashMap<String, Boolean> loadAllConfigMap() {
        return customProfileConfigurationDao.loadAllConfigMap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.SystemConfigurationManager#loadConfigById(java.lang.Long)
     */

    public List<CannedProfileConfiguration> loadConfigById(Long id) {
        return customProfileConfigurationDao.loadConfigById(id);
    }
    
    public List<CannedProfileConfiguration> loadConfigByProfileId(Integer id) {
        return customProfileConfigurationDao.loadConfigByProfileId(id);
    }
    
    public CannedProfileConfiguration loadConfigByName(String name)
    {
    	return customProfileConfigurationDao.loadConfigByName(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.SystemConfigurationManager#save(com.ems.service. SystemConfigurationManager)
     */

    public CannedProfileConfiguration save(CannedProfileConfiguration customProfileConfiguration) {
        return (CannedProfileConfiguration) customProfileConfigurationDao.saveObject(customProfileConfiguration);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.SystemConfigurationManager#update(com.ems.service. SystemConfigurationManager)
     */

    public CannedProfileConfiguration update(CannedProfileConfiguration customProfileConfiguration) {
        return (CannedProfileConfiguration) customProfileConfigurationDao.saveObject(customProfileConfiguration);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.SystemConfigurationManager#loadConfigByName(com.ems. service. SystemConfigurationManager)
     */

    
}
