/**
 * 
 */
package com.ems.service;

import javax.annotation.Resource;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.SystemConfigurationDao;
import com.ems.model.SystemConfiguration;

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
     * @seecom.ems.service.SystemConfigurationManager#save(com.ems.service. SystemConfigurationManager)
     */
    @CacheEvict(value="system_configuration", key="#systemConfiguration.name")
    public SystemConfiguration save(SystemConfiguration systemConfiguration) {
        return (SystemConfiguration) systemConfigurationDao.saveObject(systemConfiguration);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.SystemConfigurationManager#update(com.ems.service. SystemConfigurationManager)
     */
    @CacheEvict(value="system_configuration", key="#systemConfiguration.name")
    public SystemConfiguration update(SystemConfiguration systemConfiguration) {
        return (SystemConfiguration) systemConfigurationDao.saveObject(systemConfiguration);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.SystemConfigurationManager#loadConfigByName(com.ems. service. SystemConfigurationManager)
     */
    @Cacheable(value="system_configuration", key="#name")
    public SystemConfiguration loadConfigByName(String name) {
        return systemConfigurationDao.loadConfigByName(name);
    }
}
