package com.ems.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.FixtureDao;
import com.ems.dao.GemsGroupDao;
import com.ems.dao.GemsGroupPlugloadDao;
import com.ems.dao.PlugloadDao;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.GemsGroupPlugload;
import com.ems.model.MotionGroupFixtureDetails;
import com.ems.model.Plugload;
import com.ems.model.Switch;
import com.ems.server.ServerConstants;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.device.plugload.PlugloadImpl;
import com.ems.types.GGroupType;

/**
 * @author Shilpa Chalasani
 * 
 */
@Service("gemsPlugloadGroupManager")
@Transactional(propagation = Propagation.REQUIRED)
public class GemsPlugloadGroupManager {
    
	static final Logger logger = Logger.getLogger("FixtureLogger");

	@Resource
	private GemsGroupPlugloadDao gemsGroupPlugloadDao;
	
	@Resource
	private PlugloadDao plugloadDao;
	
	@Resource
	EventsAndFaultManager	eventsAndFaultManager;
	
	public List<GemsGroupPlugload> getAllGroupsOfPlugload(Plugload plugload) {
		
		return gemsGroupPlugloadDao.getGemsGroupPlugloadById(plugload.getId());
    	
	} //end of method getAllGroupsOfPlugload
	
	//make sure that this method is called in a separate thread so that it does not stall the receiver thread for packets from sensors
  public int assignPlugloadToGroup(Plugload plugload, int msgType, byte gType, int groupNo, long gemsGrpId) {
  	
  	int[] plugArr = { plugload.getId().intValue() };
  	DeviceServiceImpl.getInstance().sendSUGroupCommand(plugArr, ServerConstants.SU_CMD_JOIN_GRP, gType, groupNo);
		//update the sync state in the database    						
		int iStatus = DeviceServiceImpl.getInstance().getSuWirelessGrpChangeAckStatus(plugload.getId());
		if (iStatus != 0) {
      GemsGroupPlugload groupPlugload = getGemsGroupPlugload(gemsGrpId, plugload.getId());             
      if (groupPlugload != null) {
        if (iStatus == ServerConstants.SU_ACK) {
        	groupPlugload.setNeedSync(groupPlugload.getNeedSync() | GemsGroupFixture.SYNC_STATUS_GROUP);
        } else {
        	groupPlugload.setNeedSync(groupPlugload.getNeedSync() | GemsGroupFixture.SYNC_STATUS_GROUP_NACK);
        }
        gemsGroupPlugloadDao.saveGemsGroupPlugload(groupPlugload);
      } 
		}
		return iStatus;
  	
  } //end of method assignPlugloadToGroup
  
  public GemsGroupPlugload getGemsGroupPlugload(Long groupId, Long plugloadid) {
  
  	return gemsGroupPlugloadDao.getGemsGroupPlugload(groupId, plugloadid);
  
  } //end of method getGemsGroupPlugload
    
  public void deleteGemsGroups(Long groupId) {
  
  	gemsGroupPlugloadDao.deleteGemsGroup(groupId);
  
  }
  
} //end of class GemsPlugloadGroupManager
