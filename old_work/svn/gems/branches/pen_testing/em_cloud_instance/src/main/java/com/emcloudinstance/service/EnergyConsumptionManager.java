package com.emcloudinstance.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emcloudinstance.dao.EnergyConsumptionDao;
import com.emcloudinstance.vo.EcSyncVo;

@Service("energyConsumptionManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EnergyConsumptionManager {
	    static final Logger logger = Logger.getLogger(EnergyConsumptionManager.class.getName());
	    @Resource
	    private EnergyConsumptionDao energyConsumptionDao;
	    
	    public List<EcSyncVo> load15minFloorEnergyConsumptionForAllFloorWithZb(Date oFDate, String mac,String emTimeZone) {
			return energyConsumptionDao.load15minFloorEnergyConsumptionForAllFloorWithZb(oFDate,mac,emTimeZone);
		}
	    public Date loadLatestEnergyConsumptionDate(String mac ,String emTimeZone) {
	    	return energyConsumptionDao.loadLatestEnergyConsumptionDate(mac , emTimeZone);
	    }
	    public Date loadFirstEnergyConsumptionDate(String mac,String emTimeZone) {
	    	return energyConsumptionDao.loadFirstEnergyConsumptionDate(mac, emTimeZone);
	    }    

}
