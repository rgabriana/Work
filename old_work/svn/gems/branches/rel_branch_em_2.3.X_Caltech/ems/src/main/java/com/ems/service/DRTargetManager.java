package com.ems.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.DRTargetDao;
import com.ems.model.DRTarget;
import com.ems.model.EventsAndFault;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.types.DRStatusType;
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
	public List<DRTarget> getAllDRTargets() {
		return drTargetDao.getAllDRTargets();
	}

	public List<DRTarget> getEnabledDRTargets() {
		return drTargetDao.getEnabledDRTargets();
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
	public void saveOrUpdateDRTarget(DRTarget drTarget) {
		drTargetDao.saveOrUpdateDRTarget(drTarget);
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
	public void executeDR(DRTarget drTarget) throws InterruptedException {
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
	    				
	    	        	List<DRTarget> enabledlist = getEnabledDRTargets();
	    	        	if(enabledlist != null)
	    	        	{
	    		    		for(DRTarget enbldr: enabledlist) {
	    		    			if(enbldr.getId() != dr.getId()) {
	    		    				if(enbldr.getDuration() != 0 && (new Date(enbldr.getStartTime().getTime() + enbldr.getJitter() + 1000*enbldr.getDuration() - 2000)).after(new Date())) {
	    		    					cancelDR();
	    		    				}
	    		    				// First cancel already enabled one
	    		    				enbldr.setEnabled(DRTarget.DISABLED);
	    		    				DrLevel level = DrLevel.valueOf(enbldr.getPriceLevel().toUpperCase());
	    	    					eventsAndFaultManager.addEvent("DR(" +  level.getName() + ") cancelled", EventsAndFault.DR_EVENT_STR, EventsAndFault.INFO_SEV_STR);
	    		    				saveOrUpdateDRTarget(enbldr);
	    		    			}
	    		    		}
	    	        	}
	    				
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
	    				dr.setDrStatus(DRStatusType.Active.getName());
	    				if(!(DRTarget.ENABLED).equals(dr.getEnabled())) { 
	    					eventsAndFaultManager.addEvent("DR(" +  drLevel.getName() + ") initiated", EventsAndFault.DR_EVENT_STR, EventsAndFault.INFO_SEV_STR);
	    					dr.setEnabled(DRTarget.ENABLED);
	    					if(dr.getDuration() != 0)
	    						executeDR(dr);
	    				}
	    				saveOrUpdateDRTarget(dr);
	    			}
	    			else if(drTimeRem<0) {
    					eventsAndFaultManager.addEvent("DR(" +  drLevel.getName() + ") completed. Duration:" + dr.getDuration() + " seconds", EventsAndFault.DR_EVENT_STR, EventsAndFault.INFO_SEV_STR);
	    				dr.setEnabled(DRTarget.DISABLED);
	    				dr.setDrStatus(DRStatusType.Completed.getName());
	    				saveOrUpdateDRTarget(dr);
    				}
	    		}
        	}
    	}
    	catch (Exception e) {
			e.printStackTrace();
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
	
	public DRTarget getEnabledDRTargetByDrEvent(Long id) {
		return drTargetDao.getEnabledDRTargetByDrEvent(id);
	}
	
	public DRTarget getFirstDRTargetByIdentifier(String drIdentifier) {
		return drTargetDao.getFirstDRTargetByIdentifier(drIdentifier);
	}
	
}