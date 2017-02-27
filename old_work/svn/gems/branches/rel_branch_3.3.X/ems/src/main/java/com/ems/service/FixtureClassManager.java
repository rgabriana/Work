package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.cache.FixtureCache;
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
		// TODO Auto-generated method stub
		return fixtureClassDao.getFixtureClassCountByBulbId(id);
	}

	public Integer getFixtureClassCountByBallastId(Long id) {
		// TODO Auto-generated method stub
		return fixtureClassDao.getFixtureClassCountByBallastId(id);
	}
	
	public Integer getFixtureCountByFixtureClassId(Long id) {
		return fixtureClassDao.getFixtureCountByFixtureClassId(id);
	}

	public FixtureClass getFixtureClassByName(String fixtureType) {
		// TODO Auto-generated method stub
		return fixtureClassDao.getFixtureClassByName(fixtureType);
	}

	public Long bulkFixtureTypessignToFixture(String fixtureIdsList,
			Long currentFixturetypeId) {
		Long totalRecordUpdated = null;
        synchronized (this) {
            totalRecordUpdated = fixtureClassDao.bulkFixtureTypessignToFixture(fixtureIdsList, currentFixturetypeId);
            //Invalidate the Fixture Cache for all fixtures
            String fixtureArray[];
            fixtureArray = fixtureIdsList.split(",");
            if (fixtureArray != null) {
            	for (String s: fixtureArray)
                {
            		 Long fixtureId = Long.parseLong(s);
            		 FixtureCache.getInstance().invalidateDeviceCache(fixtureId);
                }
            }
        }
		return totalRecordUpdated;
	}
	
	public Long bulkFixtureTypeAssignToLocatorDevices(String locatorDeviceIdsList,
			Long currentFixturetypeId) {
		Long totalRecordUpdated = null;
        totalRecordUpdated = fixtureClassDao.bulkFixtureTypeAssignToLocatorDevices(locatorDeviceIdsList, currentFixturetypeId);
        return totalRecordUpdated;
	}
	
	
	public List<Object[]> getCommissionedFxTypeCount()
	{
	    return fixtureClassDao.getCommissionedFxTypeCount();
	}
}
