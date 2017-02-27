package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.FixtureDistancesDao;
import com.ems.model.FixtureDistances;

@Service("fixtureDistanceManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureDistanceManager {
	
	@Resource
    FixtureDistancesDao fdDao;
	
	public void addFixtureDistance(FixtureDistances fd) {
		fdDao.addFixtureDistance(fd);
	}
	
	public List<FixtureDistances> loadAllFixtureDistances() {
		return fdDao.loadAllFixtureDistances();
	}
	
	public List<FixtureDistances> getFixtureDistances(String snap) {
		return fdDao.getFixtureDistances(snap);
	}
	
	public void removeAllFixtureDistances() {
		fdDao.removeAllFixtureDistances();
	}

}
