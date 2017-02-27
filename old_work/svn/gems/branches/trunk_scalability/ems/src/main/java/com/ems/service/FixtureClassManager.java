package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.FixtureClassDao;
import com.ems.model.FixtureClass;
import com.ems.model.FixtureClassList;

@Service("fixtureClassManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureClassManager {
	
	@Resource
	FixtureClassDao fixtureClassDao;
	
	public FixtureClass addFixtureClass(String name,String noOfBallasts,String voltage ,String ballastId,String bulbId) {
		
		return fixtureClassDao.addFixtureClass(name, noOfBallasts, voltage,ballastId,bulbId);
    }
	
	public void editFixtureClass(String id,String name,String noOfBallasts,String voltage ,String ballastId,String bulbId) {
		
		fixtureClassDao.editFixtureClass(id,name, noOfBallasts, voltage,ballastId,bulbId);
    }
	
	public void deleteFixtureClassById(Long id){
		fixtureClassDao.deleteFixtureClassById(id);
	}
	
	public FixtureClassList loadFixtureClassList(String orderway, int offset, int limit) {
		return fixtureClassDao.loadFixtureClassList(orderway, offset, limit);
	}
	
	public List<FixtureClass> loadAllFixtureClasses() {
		return fixtureClassDao.loadAllFixtureClasses();
	}

	public FixtureClass getFixtureClassById(Long Id){
		return fixtureClassDao.getFixtureClassById(Id);
	}
	
	public FixtureClass getFixtureClass(String name,String noOfBallasts,String voltage ,String ballastId,String bulbId){
		return fixtureClassDao.getFixtureClass(name, noOfBallasts, voltage,ballastId,bulbId);
		
	}
	
	public Integer getFixtureClassCountByBulbId(Long id) {
		return fixtureClassDao.getFixtureClassCountByBulbId(id);
	}

	public Integer getFixtureClassCountByBallastId(Long id) {
		return fixtureClassDao.getFixtureClassCountByBallastId(id);
	}

	public FixtureClass getFixtureClassByName(String fixtureType) {
		return fixtureClassDao.getFixtureClassByName(fixtureType);
	}

	public List<Object[]> getCommissionedFxTypeCount()
	{
	    return fixtureClassDao.getCommissionedFxTypeCount();
	}
}
