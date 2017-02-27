package com.ems.service;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.GemsGroupDao;
import com.ems.dao.SwitchDao;
import com.ems.model.DashboardRecord;
import com.ems.model.Fixture;
import com.ems.model.GemsGroupFixture;
import com.ems.model.GemsGroupPlugload;
import com.ems.model.Plugload;
import com.ems.model.Switch;
import com.ems.model.SwitchGroup;
import com.ems.vo.EMPowerConsumption;
import com.ems.vo.FixturePower;
import com.ems.vo.PlugloadPower;
import com.ems.ws.util.Response;

@Service("emManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EMManager {

	@Resource(name = "companyManager")
	CompanyManager companyManager;
	@Resource(name = "energyConsumptionManager")
	EnergyConsumptionManager energyConsumptionManager;
	@Resource(name = "areaManager")
	AreaManager areaManager;
	@Resource(name = "fixtureManager")
	FixtureManager fixtureManager;
	@Resource(name = "plugloadManager")
	private PlugloadManager plugloadManager;
	
	@Resource
    private SwitchDao switchDao;
	
	@Resource
    private GemsGroupDao gemsGroupDao;

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
	
	public Response setEmergencyOnByPercentage(Integer time, Integer percentage) {
		Response res = new Response();
		if (time == null) {
			time = 60;
		}
		List<Fixture> fixtures = fixtureManager.getAllCommissionedFixtureList();
		int[] fixtureList = new int[fixtures.size()];
		int count = 0;
		Iterator<Fixture> itr = fixtures.iterator();
		while (itr.hasNext()) {
			Fixture fixture = (Fixture) itr.next();
			fixtureList[count++] = fixture.getId().intValue();
		}
		fixtureManager.absoluteDimFixtures(fixtureList, percentage, time);

		List<Plugload> plugloadArr = plugloadManager
				.getAllCommissionedPlugloads();
		if (plugloadArr != null && plugloadArr.size() > 0) {
			int[] plugloadList = new int[plugloadArr.size()];
			int pcount = 0;
			Iterator<Plugload> pitr = plugloadArr.iterator();
			while (pitr.hasNext()) {
				Plugload plugload = (Plugload) pitr.next();
				plugloadList[pcount++] = plugload.getId().intValue();
			}
			plugloadManager.turnOnOffPlugloads(plugloadList, percentage, time);
		}

		return res;
	}
	
	public Response setEmergencyOn(Integer time) {
		Response res = new Response();
		if (time == null) {
			time = 60;
		}
		List<Fixture> fixtures = fixtureManager.getAllCommissionedFixtureList();
		int[] fixtureList = new int[fixtures.size()];
		int count = 0;
		Iterator<Fixture> itr = fixtures.iterator();
		while (itr.hasNext()) {
			Fixture fixture = (Fixture) itr.next();
			fixtureList[count++] = fixture.getId().intValue();
		}
		fixtureManager.absoluteDimFixtures(fixtureList, 100, time);

		List<Plugload> plugloadArr = plugloadManager
				.getAllCommissionedPlugloads();
		if (plugloadArr != null && plugloadArr.size() > 0) {
			int[] plugloadList = new int[plugloadArr.size()];
			int pcount = 0;
			Iterator<Plugload> pitr = plugloadArr.iterator();
			while (pitr.hasNext()) {
				Plugload plugload = (Plugload) pitr.next();
				plugloadList[pcount++] = plugload.getId().intValue();
			}
			plugloadManager.turnOnOffPlugloads(plugloadList, 100, time);
		}

		return res;
	}
	
	public void setEmergencyOnBySwitchId(Long switchId, Integer time, Integer percentage) {
		
		if (time == null) {
			time = 60;
		}
		
		Switch oSwitch = switchDao.getSwitchById(switchId);
        if (oSwitch == null) {
            return;
        }
        SwitchGroup oSwitchGroup = switchDao.loadSwitchGroupByGemsGroupId(oSwitch.getGemsGroup().getId());
        if (oSwitchGroup == null) {
            return;
        }
        List<GemsGroupFixture> oGGFxList = gemsGroupDao.getGemsGroupFixtureByGroup(oSwitchGroup.getGemsGroup().getId());
        
        List<GemsGroupPlugload> oGGPgList = gemsGroupDao.getGemsGroupPlugloadByGroup(oSwitchGroup.getGemsGroup().getId());
        
        if (oGGFxList == null && oGGPgList == null) {
            return;
        }
        if ((oGGFxList != null && oGGFxList.size() == 0) && (oGGPgList!= null && oGGPgList.size() == 0)) {
            return;
        }
        
        int[] fixtureArr=null,plugloadArr=null;
        
        if(oGGFxList != null && oGGFxList.size() > 0){
        	fixtureArr = new int[oGGFxList.size()];
            for (int count = 0; count < oGGFxList.size(); count++) {            
                fixtureArr[count] = oGGFxList.get(count).getFixture().getId().intValue();                
            }
            
            fixtureManager.absoluteDimFixtures(fixtureArr, percentage, time);
        }        
        
        if(oGGPgList != null && oGGPgList.size() > 0){
        	plugloadArr = new int[oGGPgList.size()];
            for (int count = 0; count < oGGPgList.size(); count++) {            
            	plugloadArr[count] = oGGPgList.get(count).getPlugload().getId().intValue();            	
            }
            
            plugloadManager.turnOnOffPlugloads(plugloadArr, percentage, time);
        }
		
	}
	
	public Response setAllAuto() {
		Response res = new Response();
		List<Fixture> fixtures = fixtureManager.getAllCommissionedFixtureList();
		int[] fixtureList = new int[fixtures.size()];
		int count = 0;
		Iterator<Fixture> itr = fixtures.iterator();
		while (itr.hasNext()) {
			Fixture fixture = (Fixture) itr.next();
			fixtureList[count++] = fixture.getId().intValue();
		}
		fixtureManager.auto(fixtureList);

		List<Plugload> plugloadArr = plugloadManager
				.getAllCommissionedPlugloads();
		if (plugloadArr != null && plugloadArr.size() > 0) {
			int[] plugloadList = new int[plugloadArr.size()];
			int pcount = 0;
			Iterator<Plugload> pitr = plugloadArr.iterator();
			while (pitr.hasNext()) {
				Plugload plugload = (Plugload) pitr.next();
				plugloadList[pcount++] = plugload.getId().intValue();
			}
			plugloadManager.auto(plugloadList);
		}

		return res;
	}
	
	public Response setAutoBySwitchId(Long switchId) {
		Response res = new Response();
		
		Switch oSwitch = switchDao.getSwitchById(switchId);
        if (oSwitch == null) {
            return res;
        }
        SwitchGroup oSwitchGroup = switchDao.loadSwitchGroupByGemsGroupId(oSwitch.getGemsGroup().getId());
        if (oSwitchGroup == null) {
            return res;
        }
        List<GemsGroupFixture> oGGFxList = gemsGroupDao.getGemsGroupFixtureByGroup(oSwitchGroup.getGemsGroup().getId());
        
        List<GemsGroupPlugload> oGGPgList = gemsGroupDao.getGemsGroupPlugloadByGroup(oSwitchGroup.getGemsGroup().getId());
        
        if (oGGFxList == null && oGGPgList == null) {
            return res;
        }
        if ((oGGFxList != null && oGGFxList.size() == 0) && (oGGPgList!= null && oGGPgList.size() == 0)) {
            return res;
        }
        
        int[] fixtureArr=null,plugloadArr=null;
        
        if(oGGFxList != null && oGGFxList.size() > 0){
        	fixtureArr = new int[oGGFxList.size()];
            for (int count = 0; count < oGGFxList.size(); count++) {            
                fixtureArr[count] = oGGFxList.get(count).getFixture().getId().intValue();                
            }
            
            fixtureManager.auto(fixtureArr);
        }
		
        if(oGGPgList != null && oGGPgList.size() > 0){
        	plugloadArr = new int[oGGPgList.size()];
            for (int count = 0; count < oGGPgList.size(); count++) {            
            	plugloadArr[count] = oGGPgList.get(count).getPlugload().getId().intValue();            	
            }
            
            plugloadManager.auto(plugloadArr);
        }
		
		return res;
	}
}
