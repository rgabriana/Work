/**
 *
 */
package com.ems.server.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.model.DRTarget;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.server.ServerConstants;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureManager;
import com.ems.types.DrLevel;

/**
 * @author sreedhar.kamishetti
 *
 */
public class DRService {

	private static DRService instance = null;

	private EventsAndFaultManager eventMgr = null;
	private FixtureManager fixtureMgr = null;

	private static Logger logger = Logger.getLogger("DemandResponse");

	class DRData {

		private Date drStartTime = null;
    private int drLevel = 0;
    private int drDuration = 0; //in seconds
    private boolean overrideChanged = false;
    private Double price = 0.0;


	}

  //hash map to hold the gateways to active DRs on them
  private HashMap<Long, DRData> gwDrMap = new HashMap<Long, DRData>();

  /**
	 *
	 */
	private DRService() {
		// TODO Auto-generated constructor stub

  	eventMgr = (EventsAndFaultManager) SpringContext.getBean("eventsAndFaultManager");
  	fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureManager");

  }

	public static DRService getInstance() {

		if(instance == null) {
			synchronized(DRService.class) {
				if(instance == null) {
					instance = new DRService();
				}
			}
		}
		return instance;

	} //end of method getInstance

	public DRData getDrData(Long gwId) {

		return gwDrMap.get(gwId);

	}

	public double getPrice(Long gwId) {

		if(gwDrMap.containsKey(gwId)) {
			return gwDrMap.get(gwId).price;
		}
		return 0.0;

	} //end of method getPrice

	public int getDRLevel(Long gwId) {

		int level = 0;
		if(gwDrMap.containsKey(gwId)) {
			level = gwDrMap.get(gwId).drLevel;
		}
		if(logger.isDebugEnabled()) {
			logger.debug(gwId + ": dr level - " + level);
		}
		return level;

	} //end of method getDRLevel

	public boolean isOverrideChanged(Long gwId) {

		boolean override = false;
		if(gwDrMap.containsKey(gwId)) {
			override = gwDrMap.get(gwId).overrideChanged;
		}
		if(logger.isDebugEnabled()) {
			logger.debug(gwId + ": override - " + override);
		}
		return override;

	} //end of method isOverrideChanged

	//level should be one of DrLevel enumeration
  //public void startDR(DrLevel level, int duration, Date drCurrentJobStartTime) {
  public void startDR(Long gwId, DRTarget target) {

  	if(target == null) {
  		//there are no pending DRs. So, reset the dr data for gateway if not reset previously
  		DRData data = gwDrMap.get(gwId);
  		data.overrideChanged = false;
  		if(data.drLevel > 0) {
  			//dr ended now
  			if(logger.isDebugEnabled()) {
    			logger.debug(gwId + ": dr ended for the gateway");
    		}
  			data.drLevel = 0;
  			data.drDuration = 0;
  			data.overrideChanged = true;
  		}
  		return;
  	}

  	//dr is active
  	DRData data = gwDrMap.get(gwId);
  	if(data == null) {
  		//dr data is not entered for this gateway so far
  		data = new DRData();
  		gwDrMap.put(gwId, data);
  	}
  	data.drStartTime = new Date(target.getStartTime().getTime() + target.getJitter());
  	int oldLevel = data.drLevel;
  	DrLevel level = DrLevel.valueOf(target.getPriceLevel().toUpperCase());
  	if(level == null) {
  		data.drLevel = 0;
  	} else {
  		data.drLevel = level.value();
  	}
  	if(target.getDuration() == 0) {
			data.drDuration = target.getDuration()+(10*60);
  	} else {
			data.drDuration = target.getDuration();
  	}
  	data.overrideChanged = false;
  	if(oldLevel != data.drLevel) {
  		data.overrideChanged = true;
  	}
  	data.price = target.getPricing();

  } //end of method startDR

  public void cancelDR(DRTarget target) {

  	cancelDR();

  }

  public int getDRTimeRemaining(Long gwId) {

  	DRData data = gwDrMap.get(gwId);
  	return getDRTimeRemaining(data);

	}

  public int getDRTimeRemaining(DRData data) {

  	if(data == null) {
  		//dr must be cancelled
  		return 0;
  	}
		int drTimeRem = 0;
		if(data.drStartTime != null) {
			drTimeRem = (int)(data.drDuration - (System.currentTimeMillis() - data.drStartTime.getTime())/1000);
			if(drTimeRem < 0) {
				drTimeRem = 0;
			}
		}
		return drTimeRem;

  }

  public void cancelDR() {

  	//drDuration = 0;
  	eventMgr.addEvent("DR Condition cancelled", EventsAndFault.DR_EVENT_STR);
  	new Thread() {
  		public void run() {
  			List<Fixture> fixtureList = fixtureMgr.get1_0CommissionedDrReactiveFixtureList();
  			if(fixtureList!=null) {
  				int noOfFixtures = fixtureList.size();
  				int[] fixtArr = new int[noOfFixtures];
  				for (int i = 0; i < noOfFixtures; i++) {
  					int fixtureId = fixtureList.get(i).getId().intValue();
  					fixtArr[i] = fixtureId;
  					// setFixtureState(fixtureId, ServerConstants.AUTO_STATE_ENUM);
  				}
  				DeviceServiceImpl.getInstance().setFixtureState(fixtArr, ServerConstants.AUTO_STATE_ENUM);
  			}
  		}
  	}.start();

  } // end of method cancelDR

  public void executeDR(DRTarget target) {

  	int duration = target.getDuration() / 60;
  	int percentage = target.getTargetReduction();
  	List<Fixture> fixtureList = fixtureMgr.get1_0CommissionedDrReactiveFixtureList();
  	if(fixtureList!=null && fixtureList.size() > 0 && duration > 1)  {
  		int noOfFixtures = fixtureList.size();
  		int[] fixtureIdArr = new int[noOfFixtures];
  		for (int i = 0; i < noOfFixtures; i++) {
  			int fixtureId = fixtureList.get(i).getId().intValue();
  			fixtureIdArr[i] = fixtureId;
  		}
  		DeviceServiceImpl.getInstance().dimFixtures(fixtureIdArr, -1 * percentage, duration - 1);
  	}

  } // end of method executeDR

}
