package com.ems.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.dao.BallastVoltPowerDao;
import com.ems.model.Ballast;
import com.ems.model.BallastVoltPower;
import com.ems.vo.model.VoltPowerCurveValue;

@Service("ballastVoltPowerManager")
@Transactional(propagation = Propagation.REQUIRED)
public class BallastVoltPowerManager {
	
	@Resource
	BallastVoltPowerDao ballastVoltPowerDao;
	
	@Resource
	BallastManager ballastManager;
	
	@Cacheable(value = "bvp_ballast_inputvolt", key="#ballastId.toString() + #inputVoltage.toString()")
	public List<BallastVoltPower> getBallastVoltPowerByBallastIdInputVoltage(Long ballastId, Double inputVoltage) {
		return ballastVoltPowerDao.getAllBallastVoltPowersFromId(ballastId, inputVoltage);
	}

	@CacheEvict(value = {"bvp_ballast_inputvolt", "bvp_vpm"}, allEntries = true)
	public void add(Long ballastId, Double voltage, Double volt, Double power,Long voltPowerMapid,Boolean enableFlag) {
		ballastVoltPowerDao.add(ballastId,voltage,volt,power,voltPowerMapid,enableFlag);		
	}

	@CacheEvict(value = {"bvp_ballast_inputvolt", "bvp_vpm"}, allEntries = true)
	public void updatePower(Long ballastId, Double voltage, Double volt, Double power) {
		ballastVoltPowerDao.updatePower(ballastId,voltage,volt,power);
		
	}
	
	@CacheEvict(value = {"bvp_ballast_inputvolt", "bvp_vpm"}, allEntries = true)
	public void updateEnable(Long ballastId, Double voltage, Double volt,Boolean enableFlag) {
		ballastVoltPowerDao.updateEnable(ballastId,voltage,volt,enableFlag);
		
	}	
	
	
	public List<Long> getAllDistinctBallastVoltPowers() {
		return ballastVoltPowerDao.getAllBallastVoltPowers();
		
	}	

	public List<Double> getVoltageLevelsByBallastId(Long ballastId) {
		return ballastVoltPowerDao.getVoltageLevelsByBallastId(ballastId);
		
	}
	
	@CacheEvict(value = {"bvp_ballast_inputvolt", "bvp_vpm"}, allEntries = true)
	public void updateBallastVoltPowerMap(List<BallastVoltPower> ballastVoltPowerMap) {
		ballastVoltPowerDao.updateBallastVoltPowerMap(ballastVoltPowerMap);
	}
	
	@CacheEvict(value = {"bvp_ballast_inputvolt", "bvp_vpm"}, allEntries = true)
	public void deleteBallastCurve(Long ballastId,Double inputVoltage) {
		ballastVoltPowerDao.deleteBallastCurve(ballastId,inputVoltage);
	}	
	
	public long getMaxVoltPowerMapId() {
		return ballastVoltPowerDao.getMaxVoltPowerMapId();
	}
	
	@CacheEvict(value = {"bvp_ballast_inputvolt", "bvp_vpm"}, allEntries = true)
	public void cleanCache() {
		return;
	}
	
	@Cacheable(value = "bvp_vpm", key="#voltPowerMapId")
	public List<BallastVoltPower> getBallastVoltPowerByVoltPowerMapId(Long voltPowerMapId) {
		return ballastVoltPowerDao.getBallastVoltPowerByVoltPowerMapId(voltPowerMapId);
	}
	
	@CacheEvict(value = {"bvp_ballast_inputvolt", "bvp_vpm"}, allEntries = true)
	public void addVoltPowerCurveMap(Ballast ballast, Collection<VoltPowerCurveValue> curveValues) {
		Long voltPwrMapId = 1L;
	     
	    ballast = ballastManager.getBallastById(ballast.getId());
	    if(ballast.getVoltPowerMapId() != null) {
	    	voltPwrMapId = ballast.getVoltPowerMapId();
	    }
	    else {
	    	voltPwrMapId = getMaxVoltPowerMapId() + 1;
	    }
	    HashMap<Double, Double> curveMap = new HashMap<Double, Double>();
	    Iterator<VoltPowerCurveValue> iter = curveValues.iterator();
	    VoltPowerCurveValue curveVal = null;
	    while(iter.hasNext()) {
	      curveVal = iter.next();
	      curveMap.put(curveVal.getVolts(), curveVal.getCurveValue());
	    }
	    ballastVoltPowerDao.addVoltPowerMap(voltPwrMapId, curveMap);
	    ballastManager.updateBallastVoltPowerMapId(ballast.getId(), voltPwrMapId);
	}
	
	public Double getBallastVoltPowerFactor(Ballast ballast, int volt) {
		BallastVoltPowerManager bvpm = (BallastVoltPowerManager) SpringContext.getBean("ballastVoltPowerManager");
		ballast = ballastManager.getBallastById(ballast.getId());
		List<BallastVoltPower> bvp = bvpm.getBallastVoltPowerByVoltPowerMapId(ballast.getId());
		Double output = 0D;
		if(bvp != null) {
			Map<Double, Double> map = new HashMap<Double, Double>(); 
			for(BallastVoltPower each : bvp) {
				map.put(each.getVolt(), each.getPower());
			}
			output = getInterpolatedValue(map, volt);
		} 
		return output;
		
	}
	
	private Double getInterpolatedValue(Map<Double, Double> map, double volt) {
	      
	    volt = volt/10;
	    Double value = map.get(volt);
	    if(value != null) { //corresponding curve value already exists in the map
	      return value;
	    }
	    double rem = volt % .5;   
	    double upperVolt = volt + (.5 - rem);    
	    double lowerVolt = upperVolt - 0.5;
	    
	    double upperValue = 100.0;
	    while(upperVolt <= 10) {
	      if(map.containsKey(upperVolt)) {
		upperValue = map.get(upperVolt);
		break;
	      }
	      upperVolt += 0.5;
	    }
	    
	    double lowerValue = 0.0;
	    while(lowerVolt >= 0) {
	      if(map.containsKey(lowerVolt)) {
		lowerValue = map.get(lowerVolt);
		break;
	      }
	      lowerVolt -= 0.5;
	    }
	    double curveVal = lowerValue + (((volt - lowerVolt) * upperValue) - 
		((volt - lowerVolt) * lowerValue)) / (upperVolt - lowerVolt);    
	    return curveVal;
	    
	  } //end of method getInterpolatedValue
}
