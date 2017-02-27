package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.BallastVoltPowerDao;
import com.ems.model.BallastVoltPower;

@Service("ballastVoltPowerManager")
@Transactional(propagation = Propagation.REQUIRED)
public class BallastVoltPowerManager {
	
	@Resource
	BallastVoltPowerDao ballastVoltPowerDao;
	
	public List<BallastVoltPower> getBallastVoltPowerByBallastIdInputVoltage(Long ballastId, Double inputVoltage) {
		// TODO Auto-generated method stub
		return ballastVoltPowerDao.getAllBallastVoltPowersFromId(ballastId, inputVoltage);
	}

	public void add(Long ballastId, Double voltage, Double volt, Double power,Long voltPowerMapid,Boolean enableFlag) {
		// TODO Auto-generated method stub
		ballastVoltPowerDao.add(ballastId,voltage,volt,power,voltPowerMapid,enableFlag);		
	}

	public void updatePower(Long ballastId, Double voltage, Double volt, Double power) {
		// TODO Auto-generated method stub
		ballastVoltPowerDao.updatePower(ballastId,voltage,volt,power);
		
	}
	
	public void updateEnable(Long ballastId, Double voltage, Double volt,Boolean enableFlag) {
		// TODO Auto-generated method stub
		ballastVoltPowerDao.updateEnable(ballastId,voltage,volt,enableFlag);
		
	}	
	
	public List<Long> getAllDistinctBallastVoltPowers() {
		return ballastVoltPowerDao.getAllBallastVoltPowers();
		
	}	

	public List<Double> getVoltageLevelsByBallastId(Long ballastId) {
		// TODO Auto-generated method stub
		return ballastVoltPowerDao.getVoltageLevelsByBallastId(ballastId);
		
	}
	
	public void updateBallastVoltPowerMap(List<BallastVoltPower> ballastVoltPowerMap) {
		ballastVoltPowerDao.updateBallastVoltPowerMap(ballastVoltPowerMap);
	}
	
	public void deleteBallastCurve(Long ballastId,Double inputVoltage) {
		ballastVoltPowerDao.deleteBallastCurve(ballastId,inputVoltage);
	}	
}
