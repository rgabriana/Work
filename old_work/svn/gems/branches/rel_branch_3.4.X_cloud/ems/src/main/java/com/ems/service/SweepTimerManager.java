package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.SweepTimerDao;
import com.ems.model.SweepTimer;

@Service("sweepTimerManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SweepTimerManager {
	
	@Resource
	private SweepTimerDao sweepTimerDao;
	
	public SweepTimer saveSweepTimer(SweepTimer sweepTimer) {
		return (SweepTimer)sweepTimerDao.saveObject(sweepTimer);
	}
	
	public SweepTimer loadSweepTimerById(Long id) {
		return (SweepTimer)sweepTimerDao.loadSweepTimerById(id);
	}
	
	public void deleteSweepTimerbyId(Long id) {
		sweepTimerDao.removeObject(SweepTimer.class, id);
	}
	
	public List<SweepTimer> loadAllSweepTimer() {
		return sweepTimerDao.loadAllSweepTimer();
	}

	public SweepTimer loadSweepTimerByName(String sweeptimername) {
		return sweepTimerDao.loadSweepTimerByName(sweeptimername);
	}
	
}
