package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

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

	public void addBallast(Ballast ballast) {
			
		ballastDao.addBallast(ballast);
    }
	
	public void editBallast(Ballast ballast) {
				
		ballastDao.editBallast(ballast);
	}
	
	public BallastList loadBallastList(String order,String orderway,Boolean bSearch, String searchField, String searchString, String searchOper, int offset, int limit) {
		return ballastDao.loadBallastList(order,orderway,bSearch,searchField,searchString,searchOper, offset, limit);
	}
	
	public void deleteBallastById (Long id) {
		ballastDao.deleteBallastById(id);
		
	}
	
	public Ballast getBallastById(Long id) {
		return ballastDao.getBallastById(id);
	}
	
	public Ballast getBallastByDisplayLabel(String displayLabel) {
		return ballastDao.getBallastByDisplayLabel(displayLabel);
	}
	
	public List<Ballast> getAllBallasts() {
		return ballastDao.getAllBallasts();
	}

	public BallastList loadBallastListByUsage(String order,String orderway,Boolean bSearch, String searchField, String searchString, String searchOper, int offset,
			int limit) {
		// TODO Auto-generated method stub
		return ballastDao.loadBallastListByUsage(order,orderway,bSearch,searchField,searchString,searchOper, offset, limit);
	}
}
