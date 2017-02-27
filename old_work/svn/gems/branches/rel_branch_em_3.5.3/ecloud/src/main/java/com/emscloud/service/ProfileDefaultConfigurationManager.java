/**
 * 
 */
package com.emscloud.service;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.ProfileDefaultConfigurationDao;
import com.emscloud.model.ProfileDefaultConfiguration;

/**
 * @author yogesh
 * 
 */
@Service("profileDefaultConfigurationManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ProfileDefaultConfigurationManager {

    @Resource
    private ProfileDefaultConfigurationDao profileDefaultConfigurationDao;

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.SystemConfigurationManager#delete(java.lang.Long)
     */

    public void delete(Long id) {
    	profileDefaultConfigurationDao.removeObject(ProfileDefaultConfiguration.class, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.SystemConfigurationManager#loadAllConfig()
     */

    public List<ProfileDefaultConfiguration> loadAllConfig() {
        return profileDefaultConfigurationDao.loadAllConfig();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.SystemConfigurationManager#loadAllConfigMap()
     */

    public HashMap<String, String> loadAllConfigMap() {
        return profileDefaultConfigurationDao.loadAllConfigMap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.SystemConfigurationManager#loadConfigById(java.lang.Long)
     */

    public ProfileDefaultConfiguration loadConfigById(Long id) {
        return profileDefaultConfigurationDao.loadConfigById(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.SystemConfigurationManager#save(com.ems.service. SystemConfigurationManager)
     */

    public ProfileDefaultConfiguration save(ProfileDefaultConfiguration systemConfiguration) {
        return (ProfileDefaultConfiguration) profileDefaultConfigurationDao.saveObject(systemConfiguration);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.SystemConfigurationManager#update(com.ems.service. SystemConfigurationManager)
     */

    public ProfileDefaultConfiguration update(ProfileDefaultConfiguration systemConfiguration) {
        return (ProfileDefaultConfiguration) profileDefaultConfigurationDao.saveObject(systemConfiguration);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.SystemConfigurationManager#loadConfigByName(com.ems. service. SystemConfigurationManager)
     */

    public ProfileDefaultConfiguration loadConfigByName(String name) {
        return profileDefaultConfigurationDao.loadConfigByName(name);
    }
}
