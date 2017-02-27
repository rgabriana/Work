package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.ApplicationConfigurationDao;
import com.ems.model.ApplicationConfiguration;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Service("applicationConfigurationManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ApplicationConfigurationManager {

    @Resource
    private ApplicationConfigurationDao applicationConfigurationDao;

    /**
     * save applicationConfiguration details.
     * 
     * @param applicationConfiguration
     *            com.ems.model.ApplicationConfiguration object.
     */
    public ApplicationConfiguration save(ApplicationConfiguration applicationConfiguration) {
        return (ApplicationConfiguration) applicationConfigurationDao.saveObject(applicationConfiguration);
    }

    /**
     * update applicationConfiguration details.
     * 
     * @param applicationConfiguration
     *            com.ems.model.ApplicationConfiguration object
     */
    public ApplicationConfiguration update(ApplicationConfiguration applicationConfiguration) {
        return (ApplicationConfiguration) applicationConfigurationDao.saveObject(applicationConfiguration);
    }

    /**
     * load all applicationConfiguration objects. 1. Self login config 2. Valid domain list
     * 
     * @return com.ems.model.ApplicationConfiguration collection
     */
    public List<ApplicationConfiguration> loadAllConfig() {
        return applicationConfigurationDao.loadAllConfig();
    }

    /**
     * load applicationConfiguration object.
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.ApplicationConfiguration
     */
    public ApplicationConfiguration loadConfigById(Long id) {
        return applicationConfigurationDao.loadConfigById(id);
    }

    /**
     * Delete ApplicationConfiguration details
     * 
     * @param id
     *            database id(primary key)
     */
    public void delete(Long id) {
        applicationConfigurationDao.removeObject(ApplicationConfiguration.class, id);
    }
}
