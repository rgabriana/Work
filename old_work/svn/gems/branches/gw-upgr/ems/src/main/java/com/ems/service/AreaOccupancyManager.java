package com.ems.service;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Area;
import com.ems.occengine.OccupancyEngine;

@Service("areaOccupancyManager")
@Transactional(propagation = Propagation.REQUIRED)
public class AreaOccupancyManager {
	@Resource(name = "areaManager")
	AreaManager areaManager;
	
	@Resource(name = "floorManager")
	FloorManager floorManager;
	
	@Resource(name = "fixtureManager")
	FixtureManager fixtureManager;
	
	public void addAreaInOccupancyEngine() {
	  List<Area>  areaList = areaManager.getAllZoneEnableAreas();
	  if(areaList!= null && areaList.size()>0) {
		Iterator<Area> areaListItr = areaList.iterator();
		while(areaListItr.hasNext()) {
		  Area area = areaListItr.next();
		  List<String> sensors = fixtureManager.getSensorsByAreaId(area.getId());
		  if(!sensors.isEmpty())
			  OccupancyEngine.getInstance().addZone(area.getId(), area.getName(), sensors);
		}
	  }
	}
}
