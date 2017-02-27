package com.ems.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.DRTargetDao;
import com.ems.model.DRTarget;
import com.ems.model.Device;
import com.ems.model.EventsAndFault;
import com.ems.model.Gateway;
import com.ems.model.OverrideSchedulesFacility;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.SchedulerManager;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.types.DRStatusType;
import com.ems.types.DRType;
import com.ems.types.DrLevel;
import com.ems.types.FacilityType;
import com.ems.utils.ArgumentUtils;
import com.ems.vo.DRStatus;
import com.ems.vo.model.DRTargetList;

/**
 * DRTargetManagerImpl, class implementing DRTargetManager interface
 * @author Shiv Mohan
 */
@Service("drTargetManager")
@Transactional(propagation=Propagation.REQUIRED)
public class DRTargetManager {

	@Resource
	private DRTargetDao drTargetDao;
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	@Resource
	EventsAndFaultManager eventsAndFaultManager;
	

	
	/**
	 * Gets all DRTarget objects
	 * 
	 * @return list of DRTarget objects
	 */
	private List<DRTarget> getAllDRTargets() {
		return drTargetDao.getAllDRTargets();
	}

	@SuppressWarnings({ "rawtypes" })
	public List getAllDRTargets(Integer offset,Integer limit,DRTargetList mList) {
		return drTargetDao.getAllDRTargets(offset, limit,mList);
	}
	
	/**
	 * Gets all DRTarget objects
	 * 
	 * @return list of DRTarget objects
	 */
	public List<DRTarget> getAllManualDRTargets() {
		return drTargetDao.getAllManualDRTargets();
	}
	
	/**
	 * Gets all DRTarget objects (pagination)
	 * 
	 * @return list of DRTarget objects
	 */
	@SuppressWarnings("rawtypes")
	public List getAllManualDRTargets(Integer offset,Integer limit,DRTargetList mList, Boolean showAllChecked, String orderby, String orderway) {
		return drTargetDao.getAllManualDRTargets(offset, limit, mList, showAllChecked, orderby, orderway);
	}

	/**
	 * Gets all ADRTarget objects
	 * 
	 * @return list of ADRTarget objects
	 */
	public List<DRTarget> getAllScheduledADRTargets() {
		return drTargetDao.getAllScheduledADRTargets();
	}

	/**
	 * Get DRTarget object by id
	 * 
	 * @param id, the unique identifier
	 * @return the DRTarget object
	 */
	public DRTarget getDRTargetById(Long id) {
		return drTargetDao.getDRTargetById(id);
	}

	/**
	 * Saves or updates DRTarget object
	 * 
	 * @param drTarget, the DRTarget object
	 */
	public DRTarget saveOrUpdateDRTarget(DRTarget drTarget) {
		return drTargetDao.saveOrUpdateDRTarget(drTarget);
	}
	
	/**
	 * Deletes DRTarget object by id
	 * 
	 * @param id, the unique identifier
	 * @return delete status
	 */
	public int deleteDRTarget(Long id) {
		return drTargetDao.deleteDRTarget(id);
	}
	
	public void optOutOfDrByIdentifier(String drIdentifier) {
		drTargetDao.optOutOfDrByIdentifier(drIdentifier);
	}
	
	
	/**
	 * Cancel DR on 1.0 SUs.
	 * @param drTarget
	 */
	public void cancelDR() {
		DeviceServiceImpl.getInstance().cancelDR();
	}

	/**
	 * Execute DR for 1.0 SUs
	 * 
	 * @param drTarget
	 * @throws InterruptedException
	 */
	private void executeDR(DRTarget drTarget) throws InterruptedException {
		DeviceServiceImpl.getInstance().executeDR(drTarget.getTargetReduction(), 
		    	drTarget.getDuration()/60);
	} //end of method executeDR
	
	private boolean isOverrideActiveOnFacility(DRTarget dr, Long facilityId, String type) {
		
		if(drTargetDao.isOverrideActiveOnFacility(dr.getId(), facilityId, type) ) {
			return true;
		}
		return false;
		
	} //end of method isOverrideActiveOnArea
    
    public DRStatus getCurrentDRProcessRunning(Device device)
    {
    	DRStatus drStatus = new DRStatus();
    	drStatus.setStatus(false);
    	try {
        	List<DRTarget> adrlist = getAllDRTargets();
        	if(adrlist != null)
        	{
	    		for(DRTarget dr: adrlist) {
	    			if(device != null) {
	    				//if dr active for this device 
	    				if(device.getAreaId() != null) {
	    					if(!isOverrideActiveOnFacility(dr, device.getAreaId(), "AREA")) {
	    						continue;
	    					}
	    				} else {
	    					if(!isOverrideActiveOnFacility(dr, device.getFloorId(), "FLOOR")) {
	    						continue;
	    					}
	    				}	
	    			}
	    			int drTimeRem = ArgumentUtils.isDurationInPeriod(dr.getDuration(),new Date(dr.getStartTime().getTime() + dr.getJitter()));
    				DrLevel drLevel = DrLevel.valueOf(dr.getPriceLevel().toUpperCase());
	    			if(drTimeRem > 0) {
	    				if(drStatus.getStatus() == true)
	    					continue;
	    				
	    				drStatus.setTimeremaning(drTimeRem);
	    				//Converting Duration into Seconds as StartDR() method consumes duration in seconds
	    				if(dr.getDuration() == 0)
		    				drStatus.setDuration(dr.getDuration()+(10*60));
	    				else
	    					drStatus.setDuration(dr.getDuration());
	    				drStatus.setLevel(drLevel);
	    				drStatus.setType(dr.getDrType());
	    				drStatus.setStatus(true);
	    				drStatus.setStartTime(dr.getStartTime());
	    				drStatus.setPrice(dr.getPricing());
	    				drStatus.setJitter(dr.getJitter());
	    				if(!dr.getDrStatus().equals(DRStatusType.Active.getName()) && !dr.getDrType().equals(DRType.HOLIDAY.getName())) {
	    					if(dr.getDuration() != 0)
	    						executeDR(dr);
	    				}
	    				dr.setDrStatus(DRStatusType.Active.getName());
	    				saveOrUpdateDRTarget(dr);
	    				DeviceServiceImpl.getInstance().updateDRStatusinHeartBeatFile(dr.getDrType(), drLevel,  new Date(dr.getStartTime().getTime() + dr.getJitter()), dr.getDuration());
	    			}
	    			else if(drTimeRem<0) {
	    				cancelDR();
    					eventsAndFaultManager.addEvent("DR(" +  drLevel.getName() + ") completed. Duration:" + dr.getDuration() + " seconds", EventsAndFault.DR_EVENT_STR, EventsAndFault.INFO_SEV_STR);
	    				dr.setDrStatus(DRStatusType.Completed.getName());
	    				saveOrUpdateDRTarget(dr);
    				}
	    		}
        	}
    	}
    	catch (Exception e) {
			e.printStackTrace();
    	}
    	if(!drStatus.getStatus()) {
    		try {
				DeviceServiceImpl.getInstance().updateDRStatusinHeartBeatFile("NA", DrLevel.HIGH,  new Date(), -1);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	return drStatus;
    }
   
    /**
	 * Get DRTarget object by price_level and dr_type
	 * 
	 * @param price_level (Low/High/Moderate/Special)
	 * @param dr_type (Manual/AutoDR)
	 * @return the DRTarget object
	 */
    public DRTarget getDRTargetByPriceLevelAndDrType(DrLevel priceLevel, String drType) {
		return drTargetDao.getDRTargetByPriceLevelAndDrType(priceLevel,drType);
	}
    
    /**
	 * Get DRTarget object by price_level and dr_type
	 * 
	 * @param price_level (Low/High/Moderate/Special)
	 * @param dr_type (Manual/AutoDR)
	 * @return the DRTarget object
	 */
    public DRTarget getDRTargetByDRIdentifierAndUid(String drIdentifier, Integer uid) {
		return drTargetDao.getDRTargetByDRIdentifierAndUid(drIdentifier, uid);
	}

	@SuppressWarnings("rawtypes")
	public List getAllDRTargetsCanceledOrCompleted(Integer offset,Integer limit,DRTargetList mList) {
		return drTargetDao.getAllDRTargetsCanceledOrCompleted(offset, limit, mList);
	}
	
	public DRTarget getFirstDRTargetByIdentifier(String drIdentifier) {
		return drTargetDao.getFirstDRTargetByIdentifier(drIdentifier);
	}
	
	/*
	 * if startDate is null, start now
	 */
	public void scheduleOverrideJobs(String jobName, String group, String triggerName, Date startDate) throws SchedulerException {
        JobDetail endJob = newJob(OverrideSchedulerJob.class)
                .withIdentity(jobName, group)
                .build();
        SimpleTrigger endTrigger = null;
        if (startDate == null) {
            endTrigger = (SimpleTrigger) newTrigger() 
                    .withIdentity(triggerName)
                    .startNow()
                    .withSchedule(simpleSchedule()
                    		.withRepeatCount(0))
                    .build();
        }
        else {
        	 endTrigger = (SimpleTrigger) newTrigger() 
                     .withIdentity(triggerName, group)
                     .startAt(startDate)
                     .withSchedule(simpleSchedule()
                     		.withRepeatCount(0))
                     .build();
        }
        
        SchedulerManager.getInstance().getScheduler().scheduleJob(endJob, endTrigger);
	}
	
	public void deleteScheduledJob(String jobName, String group) throws SchedulerException{
		if(SchedulerManager.getInstance().getScheduler().checkExists(new JobKey(jobName, group))) {
			if(SchedulerManager.getInstance().getScheduler().deleteJob(new JobKey(jobName, group)) == false)
				System.out.println("Failed to delete Quartz job" + jobName);
		}
	}
	
	public int getPercentRedByDrLevel(String drLevel) {
		drLevel = drLevel.toUpperCase();
		if ("LOW".equals(drLevel)) {
			return 10;
		}
		if ("MODERATE".equals(drLevel)) {
			return 25;
		}
		if ("HIGH".equals(drLevel)) {
			return 50;
		}
		if ("SPECIAL".equals(drLevel)) {
			return 40;
		}
		return 0;
	}
	
	public DRTarget getDRTargetByDrIdentifier(String drIdentifier) {
		return drTargetDao.getDRTargetByDrIdentifier(drIdentifier);
	}
	
	public List<DRTarget> getAllScheduledManualDRTargets() {
		return drTargetDao.getAllScheduledManualDRTargets();
	}
	
	public void setDRFacilities(String[] assignedFacilities, Long scheduleId) {

		DRTarget drTarget = (DRTarget) drTargetDao.getObject(DRTarget.class, scheduleId);

		// Let's get dr locations
		//Set<OverrideSchedulesFacility> overrideSchedulesFacility = drTarget.getOverrideSchedulesFacility();
		List<OverrideSchedulesFacility> overrideSchedulesFacility = getFacilitiesOfDRTarget(scheduleId);

		// Let put the location in a set so that we can easily figure out
		// assignments
		Map<String, OverrideSchedulesFacility> overrideSchedulesFacilityMap = new HashMap<String, OverrideSchedulesFacility>();

		if(!ArgumentUtils.isNullOrEmpty(overrideSchedulesFacility)){
			for (OverrideSchedulesFacility osf : overrideSchedulesFacility) {
				overrideSchedulesFacilityMap
						.put(osf.getFacilityType().getName()
								+ osf.getFacilityId(), osf);
			}
		}

		if (assignedFacilities != null && assignedFacilities.length > 0) {
			for (String facility : assignedFacilities) {

				if (facility.length() == 0) {
					continue;
				}

				String[] facilityDetail = facility.split("_");
				String facilityType = facilityDetail[0];
				Long facilityId = Long.parseLong(facilityDetail[1]);

				OverrideSchedulesFacility contains = overrideSchedulesFacilityMap.remove(FacilityType
						.valueOf(facilityType.toUpperCase()).getName()
						+ facilityId);
				// Add if dr locations does not exists.
				if (contains == null) {
					OverrideSchedulesFacility location = new OverrideSchedulesFacility();
					location.setFacilityId(facilityId);
					location.setDrTarget(drTarget);

					if ("company".equalsIgnoreCase(facilityType)) {
						location.setFacilityType(FacilityType.COMPANY);
					} else if ("campus".equalsIgnoreCase(facilityType)) {
						location.setFacilityType(FacilityType.CAMPUS);
					} else if ("building".equalsIgnoreCase(facilityType)) {
						location.setFacilityType(FacilityType.BUILDING);
					} else if ("floor".equalsIgnoreCase(facilityType)) {
						location.setFacilityType(FacilityType.FLOOR);
					} else if ("area".equalsIgnoreCase(facilityType)) {
						location.setFacilityType(FacilityType.AREA);
					}
					drTargetDao.saveObject(location);
				}
			}
		}
		
		Iterator itr = overrideSchedulesFacilityMap.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, OverrideSchedulesFacility> pairs = (Map.Entry<String, OverrideSchedulesFacility>) itr.next();
			drTargetDao.removeObject(OverrideSchedulesFacility.class, pairs.getValue().getId());
		}
	

	}
	
	public List<OverrideSchedulesFacility> getFacilitiesOfDRTarget(Long drScheduleId) {
		return drTargetDao.getFacilitiesOfDRTarget(drScheduleId);
	}
	
}