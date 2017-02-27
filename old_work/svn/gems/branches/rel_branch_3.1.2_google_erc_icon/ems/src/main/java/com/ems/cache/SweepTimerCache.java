/**
 * 
 */
package com.ems.cache;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.ems.action.SpringContext;
import com.ems.model.Area;
import com.ems.model.Fixture;
import com.ems.model.SweepTimer;
import com.ems.service.FacilityTreeManager;
import com.ems.service.SweepTimerManager;

/**
 * @author Sreedhar
 *
 */
public class SweepTimerCache {
  
  private static SweepTimerCache instance = null;
  
  private ConcurrentHashMap<Long, Long> areaSweepCache = 
      new ConcurrentHashMap<Long, Long>();
  
  private ConcurrentHashMap<Long, Long> floorSweepCache = 
      new ConcurrentHashMap<Long, Long>();
  
  private ConcurrentHashMap<Long, SweepTimer> sweepCache = 
      new ConcurrentHashMap<Long, SweepTimer>();
  
  private ConcurrentHashMap<String, String> sweepAssLevelMap = 
      new ConcurrentHashMap<String, String>();  
  
  private SweepTimerManager sweepTimerManager=null;
	
  /**
   * 
   */
  private SweepTimerCache() {
  
    // TODO Auto-generated constructor stub   
    
  } //end of constructor
  
  public static SweepTimerCache getInstance() {
    
    if(instance == null) {
      synchronized(SweepTimerCache.class) {
	if(instance == null) {
	  instance = new SweepTimerCache();
	  instance.initializeSweepTimerCache();
	}
      }      
    }
    return instance;
    
  } //end of class getInstance
  
  private void initializeSweepTimerCache() {
  
    if (sweepTimerManager == null) {
      sweepTimerManager = (SweepTimerManager) SpringContext.getBean("sweepTimerManager");
    }
    //Create cache for all sweep Timer
    List<SweepTimer> sweepTimerList= sweepTimerManager.loadAllSweepTimer();
    if(sweepTimerList!=null && sweepTimerList.size()>0){
	    Iterator<SweepTimer> sweepTimerItr = sweepTimerList.iterator();
	    while(sweepTimerItr.hasNext()) {
	      SweepTimer sweepTimer = sweepTimerItr.next();
	      updateSweepTimer(sweepTimer);
	    }
    }
    FacilityTreeManager facilityMgr = 
	(FacilityTreeManager) SpringContext.getBean("facilityTreeManager");
    facilityMgr.loadCompanyHierarchy();
    
  } //end of method initializeSweepTimerCache
  
  public SweepTimer getAreaSweepTimer(Long areaId) {
    
    if(!areaSweepCache.containsKey(areaId)) {
      return null;
    }
    long sweepId = areaSweepCache.get(areaId);
    if(!sweepCache.containsKey(sweepId)) {
      return null;
    }
    return sweepCache.get(sweepId);
    
  } //end of method getAreaSweepTimer
  
  public SweepTimer getFloorSweepTimer(Long floorId) {
    
    if(!floorSweepCache.containsKey(floorId)) {      
      return null;
    }
    long sweepId = floorSweepCache.get(floorId);
    if(!sweepCache.containsKey(sweepId)) {      
      return null;
    }
    return sweepCache.get(sweepId);
    
  } //end of method getFloorSweepTimer
  
  public SweepTimer getFixureSweepTimer(Fixture fixture) {
    
    Area area = fixture.getArea();
    SweepTimer sweepTimer = null;
    if(area != null) {
      sweepTimer = getAreaSweepTimer(area.getId());
    }
    if(sweepTimer == null) {
      sweepTimer = getFloorSweepTimer(fixture.getFloor().getId());
    }
    return sweepTimer;
    
  } //end of method getFixtureSweepTimer

  public void invalidateTreeCache() {
    
    areaSweepCache.clear();
    floorSweepCache.clear();
    sweepAssLevelMap.clear();
    
  } //end of method invalidateTreeCache
  
  public void addAreaSweepTimer(Long areaId, Long sweepTimerId) {
    
	if(sweepTimerId==null)
		  sweepTimerId =0L;
    areaSweepCache.put(areaId, sweepTimerId);
    
  } //end of method addAreaSweepTimer
    
  public void addFloorSweepTimer(Long floorId, Long sweepTimerId) {
	if(sweepTimerId==null)
	  sweepTimerId =0L;
    floorSweepCache.put(floorId, sweepTimerId);
    
  } //end of method addFloorSweepTimer
  
  public String getFixtureAssocLevel(Fixture fixture) {
    
    Area area = fixture.getArea();
    if(area != null) {
      if(areaSweepCache.containsKey(area.getId())) {
	return "area_" + area.getId();
      }
    }
    String assocKey = "floor_" + fixture.getFloor().getId();
    while(sweepAssLevelMap.contains(assocKey)) {
      assocKey = sweepAssLevelMap.get(assocKey);
    }
    return assocKey;
    
  } //end of method getFixtureAssocLevel
  
  public void addSweepTimerAssociation(String currLevel, String assLevel) {
    
    sweepAssLevelMap.put(currLevel, assLevel);
    
  } //end of method addSweeptimerAssociation
  
  public void updateSweepTimer(SweepTimer st) {
    
    sweepCache.put(st.getId(), st);
    
  } //end of method updateSweepTimer
    
} //end of class SweepTimerCache
