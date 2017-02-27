/**
 * 
 */
package com.ems.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.FixtureCalibrationDao;
import com.ems.model.FixtureCalibrationMap;
import com.ems.model.FixtureLampCalibration;
import com.ems.model.LampCalibrationConfiguration;

/**
 * @author yogesh
 * 
 */
@Service("fixtureCalibrationManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureCalibrationManager {

    @Resource
    FixtureCalibrationDao fixtureCalibrationDao;

    @CacheEvict(value = {"fixt_callib_map", "fixt_lamp_config"}, allEntries = true)
    public FixtureLampCalibration save(FixtureLampCalibration flc) throws SQLException, IOException {
        return (FixtureLampCalibration) fixtureCalibrationDao.saveObject(flc);
    }
    
    @CacheEvict(value = {"fixt_callib_map", "fixt_lamp_config"}, allEntries = true)
    public void update(FixtureLampCalibration flc) throws SQLException, IOException {
        fixtureCalibrationDao.update(flc);
    }

    @CacheEvict(value = {"fixt_callib_map", "fixt_lamp_config"}, allEntries = true)
    public FixtureCalibrationMap save(FixtureCalibrationMap fcm) throws SQLException, IOException {
        return (FixtureCalibrationMap) fixtureCalibrationDao.saveObject(fcm);
    }

    @CacheEvict(value = {"fixt_callib_map", "fixt_lamp_config"}, allEntries = true)
    public void update(FixtureCalibrationMap fcm) throws SQLException, IOException {
        fixtureCalibrationDao.update(fcm);
    }

    @Cacheable(value = {"fixt_lamp_config"}, key = "#fixtureId")
    public FixtureLampCalibration getFixtureCalibrationMapByFixtureId(Long fixtureId) {
        return fixtureCalibrationDao.getFixtureCalibrationMapByFixtureId(fixtureId);
    }
    
    @Cacheable(value = "lamp_calib_config", key="#root.methodName")
    public LampCalibrationConfiguration getCalibrationConfiguration() {
        return fixtureCalibrationDao.getCalibrationConfiguration();
    }
    
    @CacheEvict(value = "lamp_calib_config", allEntries = true)
    public void updateLampCalibrationConfiguration(LampCalibrationConfiguration lcm) throws SQLException, IOException {
        fixtureCalibrationDao.updateLampCalibrationConfiguration(lcm);
    }
    
    @CacheEvict(value = {"fixt_callib_map", "fixt_lamp_config"}, key = "#fixtureId")
    public void updateFixtureCalibrationMap(List<FixtureCalibrationMap> fixtureCalibrationMap,Long fixtureId) {
    	fixtureCalibrationDao.updateFixtureCalibrationMap(fixtureCalibrationMap,fixtureId);
    }

    @Cacheable(value = {"fixt_callib_map"}, key = "#fixtureId")
	public List<FixtureCalibrationMap> getAllFixtureVoltPowersFromId(Long fixtureId) {
		return fixtureCalibrationDao.getAllFixtureVoltPowersFromId(fixtureId);
	}
	
	@CacheEvict(value = {"fixt_callib_map", "fixt_lamp_config"}, key = "#fixtureId")
	public void deleteFixtureCurve(Long fixtureId) {
		fixtureCalibrationDao.deleteFixtureCurve(fixtureId);
	}
    
}
