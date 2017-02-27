package com.emscloud.service;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.SystemConfigurationDao;
import com.emscloud.model.SystemConfiguration;


@Service("systemConfigurationManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SystemConfigurationManager {

    @Resource
    private SystemConfigurationDao systemConfigurationDao;

 
    public void delete(Long id) {
        systemConfigurationDao.removeObject(SystemConfiguration.class, id);
    }

 

    public List<SystemConfiguration> loadAllConfig() {
        return systemConfigurationDao.loadAllConfig();
    }

    public HashMap<String, String> loadAllConfigMap() {
        return systemConfigurationDao.loadAllConfigMap();
    }


    public SystemConfiguration loadConfigById(Long id) {
        return systemConfigurationDao.loadConfigById(id);
    }


    public SystemConfiguration save(SystemConfiguration systemConfiguration) {
        return (SystemConfiguration) systemConfigurationDao.saveObject(systemConfiguration);
    }



    public SystemConfiguration update(SystemConfiguration systemConfiguration) {
        return (SystemConfiguration) systemConfigurationDao.saveObject(systemConfiguration);
    }


    public SystemConfiguration loadConfigByName(String name) {
        return systemConfigurationDao.loadConfigByName(name);
    }
}
