package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.PlacedFixtureDao;
import com.ems.model.Fixture;
import com.ems.model.PlacedFixture;


@Service("placedFixtureManager")
@Transactional(propagation = Propagation.REQUIRED)
public class PlacedFixtureManager {

	@Resource
	PlacedFixtureDao placedFixtureDao;
	
	public void addPlacedFixture(PlacedFixture fx) {
		placedFixtureDao.addPlacedFixture(fx);		
    }
	
	public PlacedFixture getPlacedFixtureById(Long id)
	{
		return placedFixtureDao.getPlacedFixtureById(id);
	}

	public void editPlacedFixture(PlacedFixture fx) {
		// TODO Auto-generated method stub
		placedFixtureDao.editPlacedFixture(fx);
	}
    
	public PlacedFixture save(PlacedFixture fixture) {
        return (PlacedFixture) placedFixtureDao.saveObject(fixture);
    }

	public void deletePlacedFixtureById(Long id) {
		// TODO Auto-generated method stub
		placedFixtureDao.deletePlacedFixtureById(id);		
	}

	public PlacedFixture getPlacedFixtureByMacAddr(String macAddr) {
		return placedFixtureDao.getPlacedFixtureByMacAddr(macAddr);
	}
	
	public List<PlacedFixture> getAllPlacedFixtures() {
		return placedFixtureDao.getAllPlacedFixtures();
	}
    
	public List<PlacedFixture> loadFixtureByFloorId(Long id) {
        List<PlacedFixture> fixtures = placedFixtureDao.loadPlacedFixturesByFloorId(id);

        return fixtures;
    }	
}
