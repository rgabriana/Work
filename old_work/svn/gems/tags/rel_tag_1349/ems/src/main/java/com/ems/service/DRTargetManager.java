package com.ems.service;

import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.DRTargetDao;
import com.ems.model.DRTarget;
import com.ems.server.device.DRThread;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.service.DRTargetManager;

/**
 * DRTargetManagerImpl, class implementing DRTargetManager interface
 * @author Shiv Mohan
 */
@Service("drTargetManager")
@Transactional(propagation=Propagation.REQUIRED)
public class DRTargetManager {

	@Resource
	private DRTargetDao drTargetDao;

		/**
	 * Gets all DRTarget objects
	 * 
	 * @return list of DRTarget objects
	 */
	public List<DRTarget> getAllDRTargets() {
		return drTargetDao.getAllDRTargets();
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

	public void executeDR(DRTarget drTarget) throws InterruptedException {
		
	  try {
	    System.out.println("inside the executeDR");
	    DeviceServiceImpl.getInstance().executeDR(drTarget.getTargetReduction(), 
	    	drTarget.getDuration());
	  } catch (Exception e) {
	    // e.printStackTrace();
	  }
	  
	} //end of method executeDR

	public void cancelDR(DRTarget drTarget) {

	  System.out.println("inside the cancel DR target");
	  DeviceServiceImpl.getInstance().cancelDR();
	  
	}
	
	/**
	 * Updates the non null DRTarget attributes
	 * 
	 * @param the drTarget to update
	 */
	public void updateAttributes(DRTarget drTarget){
		drTargetDao.updateAttributes(drTarget);
	}
	
    public void instantiateDRThread() {
    	List<DRTarget> drlist = getAllDRTargets();
		try {
			if(!DRThread.instanceExists()) {
				DRThread drThread = DRThread.getInstance();
				if(drThread.getDrTarget() == null) {
					for(DRTarget dr: drlist) {
						if(dr.getEnabled().equals("Yes")) {
							drThread.setDrTarget(dr);
							drThread.setDrTargetManager(this);
							int interval = (dr.getDuration() * 60) - Integer.parseInt((new Long((Calendar.getInstance().getTime().getTime() - dr.getStartTime().getTime())/1000L)).toString());
							if(interval > 0) {
								drThread.setInterval(interval);
							}
							else {
								drThread.setInterval(-1);
							}
							drThread.start();
							break;
						}
					}
				}
			}
		}
		 catch (Exception e) {
				e.printStackTrace();
		}
    }

}