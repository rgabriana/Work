package com.enlightedinc.adr.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enlightedinc.adr.dao.ADRTargetDao;
import com.enlightedinc.adr.model.ADRTarget;

/**
 * @author Kushal
 */
@Service("adrTargetManager")
@Transactional(propagation=Propagation.REQUIRED)
public class ADRTargetManager {

	@Resource
	private ADRTargetDao adrTargetDao;

		/**
	 * Gets all ADRTarget objects
	 * 
	 * @return list of ADRTarget objects
	 */
	public List<ADRTarget> getAllADRTargets() {
		return adrTargetDao.getAllADRTargets();
	}

	/**
	 * Get ADRTarget object by id
	 * 
	 * @param id, the unique identifier
	 * @return the ADRTarget object
	 */
	public ADRTarget getADRTargetById(Long id) {
		return adrTargetDao.getADRTargetById(id);
	}

	/**
	 * Saves or updates DRTarget object
	 * 
	 * @param drTarget, the DRTarget object
	 */
	public void saveOrUpdateADRTarget(ADRTarget adrTarget) {
		adrTargetDao.saveOrUpdateADRTarget(adrTarget);
	}

	/**
	 * Get ADRTarget object by event identifier
	 * 
	 * @param event identifier
	 * @return the ADRTarget object
	 */
	public ADRTarget getADRTargetByIdentifier(String identifier) {
		return adrTargetDao.getADRTargetByIdentifier(identifier);
	}
	
    /**
     * Get all ADRTarget objects with event status = NEAR, FAR, ACTIVE.
     * 
     * @return list of all ADRTarget objects in the database
     */
	public List<ADRTarget> getAllQueuedADRTargets() {
		return adrTargetDao.getAllQueuedADRTargets();
	}

}