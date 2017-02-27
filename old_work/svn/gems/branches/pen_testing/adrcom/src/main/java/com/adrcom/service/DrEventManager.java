package com.adrcom.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.adrcom.dao.DrEventDao;
import com.adrcom.model.DrEvent;
import com.adrcom.model.DrEventSignal;
import com.adrcom.model.DrEventSignalInterval;

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
	
	public List<DrEvent> getAllQueuedDREvents() {
		return drEventDao.getAllQueuedDREvents();
	}
	
	public List<DrEventSignal> loadDrEventSignalsByEventId(Long eventId) {
		return drEventDao.loadDrEventSignalsByEventId(eventId);
	}
	
	public List<DrEventSignalInterval> loadDrEventSignalIntervalsByEventSignalId(Long signalId) {
		return drEventDao.loadDrEventSignalIntervalsByEventSignalId(signalId);
	}

}
