package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.cache.FixtureCache;
import com.ems.dao.FixtureClassDao;
import com.ems.model.Ballast;
import com.ems.model.Bulb;
import com.ems.model.EventsAndFault;
import com.ems.model.FixtureClass;
import com.ems.model.FixtureClassList;
import com.ems.ws.util.Response;

@Service("fixtureClassManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureClassManager {

	@Resource
	FixtureClassDao fixtureClassDao;

	@Resource(name = "bulbManager")
    private BulbManager bulbManager;

	@Resource(name = "ballastManager")
    private BallastManager ballastManager;

	@Resource
	private EventsAndFaultManager eventManager;

	private static final Logger m_Logger = Logger.getLogger("WSLogger");

	public Response uploadFixtureClass(List<FixtureClass> fixtureClassList){
		Integer status = 200;
		Response resp = new Response();

		try
		{
			for(FixtureClass fxClass : fixtureClassList)
			{
				FixtureClass fxClassExisting = getFixtureClassByName(fxClass.getName());

				if(fxClassExisting != null)
				{
					m_Logger.error("Upload fixture type error: Fixture class with the same name already exists: " + fxClass.getName());
					eventManager.addEvent("Upload fixture type error: Fixture class with the same name already exists: " + fxClass.getName(), EventsAndFault.FIXTURE_CONFIGURATION_UPLOAD);
					status = 300;
					continue;
				}

				Bulb bulb = bulbManager.getBulbByName(fxClass.getBulb().getBulbName());

				if(bulb == null)
				{
					m_Logger.error("Upload fixture type error: Bulb associated with the fixture type does not exist, name = " + fxClass.getBulb().getBulbName());
					eventManager.addEvent("Upload fixture type error: Bulb associated with the fixture type does not exist, name = " + fxClass.getBulb().getBulbName(), EventsAndFault.FIXTURE_CONFIGURATION_UPLOAD);
					status = 300;
					continue;
				}

				Ballast ballast = ballastManager.getBallastByDisplayLabel(fxClass.getBallast().getDisplayLabel());

				if(ballast == null)
				{
					m_Logger.error("Upload fixture type error: Ballast associated with the fixture type does not exist, name = " + fxClass.getBallast().getBallastName());
					eventManager.addEvent("Upload fixture type error: Ballast associated with the fixture type does not exist, name = " + fxClass.getBallast().getBallastName(), EventsAndFault.FIXTURE_CONFIGURATION_UPLOAD);
					status = 300;
					continue;
				}

				addFixtureClass(fxClass.getName(), fxClass.getNoOfBallasts().toString(), fxClass.getVoltage().toString(),
						ballast.getId().toString(), bulb.getId().toString());
			}
		resp.setStatus(status);		//Successful uploading of fixture types from EmConfig ,  keep this statement at last
		}
		catch(Exception e)
		{
			e.printStackTrace();
			resp.setStatus(500);
		}

		return resp;
	}

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
