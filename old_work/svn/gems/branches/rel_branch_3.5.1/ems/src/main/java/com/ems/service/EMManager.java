package com.ems.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.DashboardRecord;
import com.ems.vo.EMPowerConsumption;
import com.ems.vo.FixturePower;
import com.ems.vo.PlugloadPower;

@Service("emManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EMManager {

	@Resource(name = "companyManager")
	CompanyManager companyManager;
	@Resource(name = "energyConsumptionManager")
	EnergyConsumptionManager energyConsumptionManager;
	@Resource(name = "areaManager")
	AreaManager areaManager;

	public EMPowerConsumption getEMEnergyConsumption(Long pid,String property) {
		EMPowerConsumption res = new EMPowerConsumption();
        Calendar oCalendar = Calendar.getInstance();
        Date oFDate = null;
        oFDate = new Date(System.currentTimeMillis());
        oCalendar.setTime(oFDate);
      
		List<DashboardRecord> lightRecords = energyConsumptionManager.load15minRecentSummary(pid, property, oFDate);
		
		if (lightRecords != null && !lightRecords.isEmpty()) {
			Double powerused = (double) 0;
			if(lightRecords.size()>0)
			{
				powerused = lightRecords.get(0).getPowerused();
			}
			res.setEnergyUsedLighting(powerused);
		}
		List<DashboardRecord> plugloadRecords = energyConsumptionManager.load15minRecentSummaryForAllPlugload(pid, property, oFDate);
		if (plugloadRecords != null && !plugloadRecords.isEmpty()) {
			Double plpowerused = (double) 0;
			if(plugloadRecords.size()>0)
			{
				DashboardRecord object = plugloadRecords.get(0);
				if (object != null && object.getPowerused()!=null) {
					plpowerused += object.getPowerused();
				}
			}
			res.setEnergyUsedPlugload(plpowerused);
		}
		return res;
	}

	public FixturePower getFixtureEnergyConsumption(Long pid,String property) {
		FixturePower res = new FixturePower();
		Calendar oCalendar = Calendar.getInstance();
        Date oFDate = null;
        oFDate = new Date(System.currentTimeMillis());
        oCalendar.setTime(oFDate);
		List<DashboardRecord> lightRecords = energyConsumptionManager.load15minRecentSummary(pid, property, oFDate);
		if (lightRecords != null && !lightRecords.isEmpty()) {
			Double powerused = (double) 0;
			if(lightRecords.size()>0)
			{
				powerused = lightRecords.get(0).getPowerused();
			}
			res.setEnergy(powerused);
		}
		return res;
	}

	public PlugloadPower getPlugloadEnergyConsumption(Long pid,String property) {
		PlugloadPower res = new PlugloadPower();
		Calendar oCalendar = Calendar.getInstance();
        Date oFDate = null;
        oFDate = new Date(System.currentTimeMillis());
        oCalendar.setTime(oFDate);
        
        List<DashboardRecord> plugloadRecords = energyConsumptionManager.load15minRecentSummaryForAllPlugload(pid, property, oFDate);
		if (plugloadRecords != null && !plugloadRecords.isEmpty()) {
			Double plManagedpowerused = (double) 0;
			Double plUnManagedpowerused = (double) 0;
			if(plugloadRecords.size()>0)
			{
				DashboardRecord object = plugloadRecords.get(0);
				if (object != null && object.getPowerused()!=null) {
					plManagedpowerused += object.getPowerused();
					plUnManagedpowerused+=object.getUnmanagedPowerUsed();
				}
			}
			res.setManagedEnergy(plManagedpowerused);
			res.setUnmanagedEnergy(plUnManagedpowerused);
		}
		return res;
	}
}
