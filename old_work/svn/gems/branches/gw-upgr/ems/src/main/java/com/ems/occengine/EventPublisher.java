/**
 * 
 */
package com.ems.occengine;

import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

public class EventPublisher {
	public static final Logger logger = Logger.getLogger(EventPublisher.class
			.getName());
	private final static EventPublisher m_instance = new EventPublisher();
	
	private CacheManager oCacheMgr = CacheManager.getInstance();

	private EventPublisher() {
	}

	public static EventPublisher getInstance() {
		return m_instance;
	}

	public void processGroupLevelOccStatus(List<Long> zoneList) {
		for (int zoneCount = 0; zoneCount < zoneList.size(); zoneCount++) {
			Long groupId = zoneList.get(zoneCount);
			ZoneEventVO oZone = oCacheMgr.getZone(groupId);
			if (oZone != null) {
				logger.debug("\nStart zone level processing. Name = " + oZone.getName() + ", occ_status = " + oZone.getOccStatus()
						+ ", last_communication " + oZone.getLastCommunication() + ", statechangedate = " + oZone.getStateChangeDate()
						+ " isFailure = " + oZone.isZoneFailure());
				byte newStatus = oZone.getOccStatus();
				List<String> zoneSuList = oZone.getSensorList();
				
				if (zoneSuList != null) {
					if(oZone.getOccStatus() == CommandsConstants.UNOCCUPIED) {
						int requiredOnBits = oZone.getoRule().getSensorOnTriggerDelay()/5;
						int autoPercent = oZone.getoRule().getLingerToAutoPercent();
						if(requiredOnBits == 0) {
							requiredOnBits = 1;
							autoPercent = 100;
						}
						int onBits = ZoneProcessor.getOnBitsCnt(oZone, requiredOnBits);
						if(onBits >= requiredOnBits * autoPercent/100) {
							newStatus = 1;
						}
						logger.info("Zone Name = " + oZone.getName() + ", onBits = " + onBits 
								+ ", requiredOnBits = " + requiredOnBits + ", density = " + oZone.getoRule().getLingerToAutoPercent()
								+ ", newStatus = " + newStatus + ", isFailure = " + oZone.isZoneFailure());
							
					}
					else if (oZone.getOccStatus() == CommandsConstants.OCCUPIED) {
						if(oZone.isOverride()) {
							oZone.setOverride(false);
						}
						else {
							int requiredOffBits = oZone.getoRule().getSensorOffTriggerDelay()/5;
							int offBits = ZoneProcessor.getOffBitsCnt(oZone, requiredOffBits);
							if(offBits >= requiredOffBits * oZone.getoRule().getLingerToOffPercent() / 100 ) {
								newStatus = 0;
							}
							logger.info("Zone Name = " + oZone.getName() + ", offBits = " + offBits 
									+ ", requiredOffBits = " + requiredOffBits + ", density = " + oZone.getoRule().getLingerToOffPercent()
									+ ", newStatus = " + newStatus + ", isFailure = " + oZone.isZoneFailure());
						}
					}
				}
				
				if(oZone.getOccStatus() != newStatus) {
					oZone.setOccStatus((byte) newStatus);
					oZone.setStateChangeDate(new Date());
				}
				
				oZone.setLastCommunication(new Date());
				
				logger.debug("Publish Zone " + oZone.getName());
			}
			logger.debug("End zone level processing. Name = " + oZone.getName() + ", occ_status = " + oZone.getOccStatus()
					+ ", last_communication " + oZone.getLastCommunication() + ", statechangedate" + oZone.getStateChangeDate()
					+ " isFailure = " + oZone.isZoneFailure());
		}
	}


	
	
}
