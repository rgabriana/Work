package com.ems.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.util.Date;
import java.util.List;

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
import com.ems.model.EventsAndFault;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.SchedulerManager;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.types.DRStatusType;
import com.ems.types.DRType;
import com.ems.types.DrLevel;
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
    
    public DRStatus getCurrentDRProcessRunning()
    {
    	DRStatus drStatus = new DRStatus();
    	drStatus.setStatus(false);
    	try {
        	List<DRTarget> adrlist = getAllDRTargets();
        	if(adrlist != null)
        	{
	    		for(DRTarget dr: adrlist) {
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
	
}