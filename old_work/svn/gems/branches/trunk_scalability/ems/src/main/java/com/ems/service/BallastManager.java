package com.ems.service;

import java.math.BigInteger;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.BallastDao;
import com.ems.model.Ballast;
import com.ems.model.BallastList;

@Service("ballastManager")
@Transactional(propagation = Propagation.REQUIRED)
public class BallastManager {
	
	@Resource
	BallastDao ballastDao;
	
	@Resource
	FixtureManager fixtureManager;

	@CacheEvict(value = "ballast", key="#ballast.id")
	public Ballast addBallast(Ballast ballast) {
		return ballastDao.addBallast(ballast);
    }
	
	@CacheEvict(value = "ballast", key="#ballast.id")
	public void editBallast(Ballast ballast,List<BigInteger> fixturesIdList) {
		fixtureManager.editFixtureBallast(ballast, fixturesIdList);
		ballastDao.editBallast(ballast);
	}
	
	
	public BallastList loadBallastList(String order,String orderway,Boolean bSearch, String searchField, String searchString, String searchOper, int offset, int limit) {
		return ballastDao.loadBallastList(order,orderway,bSearch,searchField,searchString,searchOper, offset, limit);
	}
	
	@CacheEvict(value = "ballast", key="#id")
	public void deleteBallastById (Long id) {
		ballastDao.deleteBallastById(id);
	}
	
	@Cacheable(value = "ballast", key="#id")
	public Ballast getBallastById(Long id) {
		return ballastDao.getBallastById(id);
	}
	
	@CacheEvict(value = "ballast", key="#ballastId")
	public void updateBallastVoltPowerMapId(Long ballastId, Long voltPwrMapId) {
		ballastDao.updateBallastVoltPowerMapId(ballastId, voltPwrMapId);
	}
	
	public Ballast getBallastByDisplayLabel(String displayLabel) {
		return ballastDao.getBallastByDisplayLabel(displayLabel);
	}
	
	public Ballast getBallastByName(String name) {
		return ballastDao.getBallastByName(name);
	}

	public List<Ballast> getAllBallasts() {
		return ballastDao.getAllBallasts();
	}

	public BallastList loadBallastListByUsage(String order,String orderway,Boolean bSearch, String searchField, String searchString, String searchOper, int offset, int limit) {
		return ballastDao.loadBallastListByUsage(order,orderway,bSearch,searchField,searchString,searchOper, offset, limit);
	}
	
	public List<Object[]> getBallastCountByBallastName() {
       return  ballastDao.getBallastCountByBallastName();
	}
}
