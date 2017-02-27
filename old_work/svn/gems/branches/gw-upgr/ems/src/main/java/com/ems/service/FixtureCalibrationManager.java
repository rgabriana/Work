/**
 * 
 */
package com.ems.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.FixtureCalibrationDao;
import com.ems.model.BallastVoltPower;
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

    public FixtureLampCalibration save(FixtureLampCalibration flc) throws SQLException, IOException {
        return (FixtureLampCalibration) fixtureCalibrationDao.saveObject(flc);
    }
    

    public void update(FixtureLampCalibration flc) throws SQLException, IOException {
        fixtureCalibrationDao.update(flc);
    }

    public FixtureCalibrationMap save(FixtureCalibrationMap fcm) throws SQLException, IOException {
        return (FixtureCalibrationMap) fixtureCalibrationDao.saveObject(fcm);
    }

    public void update(FixtureCalibrationMap fcm) throws SQLException, IOException {
        fixtureCalibrationDao.update(fcm);
    }

    public FixtureLampCalibration getFixtureCalibrationMapByFixtureId(Long fixtureId) {
        return fixtureCalibrationDao.getFixtureCalibrationMapByFixtureId(fixtureId);
    }
    
    public LampCalibrationConfiguration getCalibrationConfiguration() {
        return fixtureCalibrationDao.getCalibrationConfiguration();
    }
    
    public void updateLampCalibrationConfiguration(LampCalibrationConfiguration lcm) throws SQLException, IOException {
        fixtureCalibrationDao.updateLampCalibrationConfiguration(lcm);
    }
    
    public void updateFixtureCalibrationMap(List<FixtureCalibrationMap> fixtureCalibrationMap,Long fixtureId) {
    	fixtureCalibrationDao.updateFixtureCalibrationMap(fixtureCalibrationMap,fixtureId);
    }

	public List<FixtureCalibrationMap> getAllFixtureVoltPowersFromId(Long fixtureId) {
		return fixtureCalibrationDao.getAllFixtureVoltPowersFromId(fixtureId);
	}
	public void deleteFixtureCurve(Long fixtureId) {
		fixtureCalibrationDao.deleteFixtureCurve(fixtureId);
	}
    
}
