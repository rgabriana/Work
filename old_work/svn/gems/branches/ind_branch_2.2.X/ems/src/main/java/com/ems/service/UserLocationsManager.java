package com.ems.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.UserLocationsDao;
import com.ems.model.UserLocations;
import com.ems.types.FacilityType;

@Service("userLocationsManager")
@Transactional(propagation = Propagation.REQUIRED)
public class UserLocationsManager {
	
	@Resource(name = "userLocationsDao")
	private UserLocationsDao userLocationsDao;
	
	public boolean isFloorAssignedToUser(Long id) {
		return userLocationsDao.isFloorAssignedToUser(id);
	}
	
	public boolean isAreaAssignedToUser(Long id) {
		return userLocationsDao.isAreaAssignedToUser(id);
	}

	public UserLocations loadUserLocation(long userId, FacilityType type,long locationId) {
		return userLocationsDao.loadUserLocation(userId, type,locationId) ;
		
	}
	

}
