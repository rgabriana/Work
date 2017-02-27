package com.enlightedinc.adr.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enlightedinc.adr.dao.DrEventDao;
import com.enlightedinc.adr.model.DrEvent;
import com.enlightedinc.adr.model.DrEventSignal;
import com.enlightedinc.adr.model.DrEventSignalInterval;

/**
 * @author Kushal
 */
@Service("drEventManager")
@Transactional(propagation=Propagation.REQUIRED)
public class DrEventManager {
	
	@Resource
	private DrEventDao drEventDao;
	
	public DrEvent saveOrUpdateEvent(DrEvent drEvent) {
		return (DrEvent)drEventDao.saveObject(drEvent);
	}
	
	public DrEventSignal saveOrUpdateEventSignal(DrEventSignal drEventSignal) {
		return (DrEventSignal) drEventDao.saveObject(drEventSignal);
	}
	
	public DrEventSignalInterval saveOrUpdateEventSignalInterval(DrEventSignalInterval drEventSignalInterval) {
		return (DrEventSignalInterval) drEventDao.saveObject(drEventSignalInterval);
	}
	
	public DrEvent loadDrEventByEventId(String id) {
		return drEventDao.loadDrEventByEventId(id);
	}
	
	public DrEventSignal loadDrEventSignalByEventIdAndSignalId(Long eventId, String signalId) {
		return drEventDao.loadDrEventSignalByEventIdAndSignalId(eventId, signalId);
	}
	
	public void deleteSignalsAndIntervals(Long eventId) {
		drEventDao.deleteSignalsAndIntervals(eventId);
	}

}
