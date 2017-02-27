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

import com.ems.dao.SystemConfigurationDao;
import com.ems.model.SystemConfiguration;
import com.ems.model.User;
import com.ems.types.AuthenticationType;

/**
 * @author yogesh
 * 
 */
@Service("systemConfigurationManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SystemConfigurationManager {

    @Resource
    private SystemConfigurationDao systemConfigurationDao;

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.SystemConfigurationManager#delete(java.lang.Long)
     */

    public void delete(Long id) {
        systemConfigurationDao.removeObject(SystemConfiguration.class, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.SystemConfigurationManager#loadAllConfig()
     */

    public List<SystemConfiguration> loadAllConfig() {
        return systemConfigurationDao.loadAllConfig();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.SystemConfigurationManager#loadAllConfigMap()
     */

    public HashMap<String, String> loadAllConfigMap() {
        return systemConfigurationDao.loadAllConfigMap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.SystemConfigurationManager#loadConfigById(java.lang.Long)
     */

    public SystemConfiguration loadConfigById(Long id) {
        return systemConfigurationDao.loadConfigById(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.SystemConfigurationManager#save(com.ems.service. SystemConfigurationManager)
     */

    public SystemConfiguration save(SystemConfiguration systemConfiguration) {
        return (SystemConfiguration) systemConfigurationDao.saveObject(systemConfiguration);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.SystemConfigurationManager#update(com.ems.service. SystemConfigurationManager)
     */

    public SystemConfiguration update(SystemConfiguration systemConfiguration) {
        return (SystemConfiguration) systemConfigurationDao.saveObject(systemConfiguration);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.SystemConfigurationManager#loadConfigByName(com.ems. service. SystemConfigurationManager)
     */

    public SystemConfiguration loadConfigByName(String name) {
        return systemConfigurationDao.loadConfigByName(name);
    }
    
    public boolean isExternalUser(User user){
	 	SystemConfiguration authTypeConfig = this
				.loadConfigByName("auth.auth_type");
	 	AuthenticationType authenticationType = AuthenticationType.DATABASE;
		if (authTypeConfig != null) {
			authenticationType = AuthenticationType.valueOf(authTypeConfig
					.getValue());
			
		}
		if (user != null && ("admin".equals(user.getEmail()) )){
			return false;
		}
		return authenticationType != AuthenticationType.DATABASE;
		
    }
}
