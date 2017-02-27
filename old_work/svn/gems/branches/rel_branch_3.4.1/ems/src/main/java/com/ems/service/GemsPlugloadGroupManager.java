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
import com.ems.model.MotionGroupPlugloadDetails;
import com.ems.model.Plugload;
import com.ems.model.Switch;
import com.ems.server.ServerConstants;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.device.plugload.PlugloadImpl;
import com.ems.types.DeviceType;
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
  	DeviceServiceImpl.getInstance().sendSUGroupCommand(plugArr, ServerConstants.SU_CMD_JOIN_GRP, gType, groupNo, 
  			DeviceType.Plugload.getName());
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
  
  /**
   * Join group 
   * @param gid
   * @param plugloads
   * @param groupNo
   * @param groupType
   * @return Processing status
   */
  public String asssignPlugloadsToGroup(Long gid, List<GemsGroupPlugload> groupPlugloads, final int groupNo, final int groupType) {
  	
  	List<GemsGroupPlugload> currentPlugloads = gemsGroupPlugloadDao.getGemsGroupPlugloadByGroup(gid);
  	if (currentPlugloads == null) {
  		currentPlugloads = new ArrayList<GemsGroupPlugload>();
  	}

  	grpProcessingUpdate(gid, 0, new Long(groupPlugloads.size()));
  	Iterator<GemsGroupPlugload> itr = groupPlugloads.iterator();
  	while (itr.hasNext()) {
  		final GemsGroupPlugload groupPlugload = (GemsGroupPlugload) itr.next();
  		if (groupPlugload.getUserAction() == GemsGroupPlugload.USER_ACTION_DEFAULT) {
  			if (groupPlugload.getNeedSync() == GemsGroupPlugload.SYNC_STATUS_UNKNOWN) {
  				Thread oGrpUpdateThread = new Thread(String.valueOf(groupNo) + ":GrpJoin") {
  					public void run() {
  						int[] plArr = { groupPlugload.getPlugload().getId().intValue() };
  						if(logger.isDebugEnabled()) {
  							logger.debug(plArr[0] + " joining group " + groupNo);
  						}
  						DeviceServiceImpl.getInstance().sendSUGroupCommand(plArr, ServerConstants.SU_CMD_JOIN_GRP, (byte) groupType, groupNo,
  								DeviceType.Plugload.getName());
  					}
  				};
  				oGrpUpdateThread.start();
  				
  				try {
  					oGrpUpdateThread.join();
  				} catch (InterruptedException ie) {
  					logger.warn(oGrpUpdateThread.getName() + ": interrupted!");
  				}
  				int iStatus = DeviceServiceImpl.getInstance().getSuWirelessGrpChangeAckStatus(groupPlugload.getPlugload().getId());
  				if (iStatus != 0) {
  					grpProcessingUpdate(gid, 1, null);
  					grpProcessingUpdate(gid, 2, null);
  					if (groupPlugload != null) {
  						if (iStatus == ServerConstants.SU_ACK) {
  							groupPlugload.setNeedSync(groupPlugload.getNeedSync() | GemsGroupFixture.SYNC_STATUS_GROUP);
  						} else {
  							groupPlugload.setNeedSync(groupPlugload.getNeedSync() | GemsGroupFixture.SYNC_STATUS_GROUP_NACK);
  							eventsAndFaultManager.addEvent(groupPlugload.getPlugload(), "Join group command returned NACK for the fixture", 
  									EventsAndFault.FIXTURE_GROUP_CHANGE_EVENT);
  						}
  						gemsGroupPlugloadDao.saveGemsGroupPlugload(groupPlugload);
  					}
  				} else {
  					grpProcessingUpdate(gid, 1, null);
  				}
  			}
  		} else if (groupPlugload.getUserAction() == GemsGroupFixture.USER_ACTION_FIXTURE_DELETE
  				|| groupPlugload.getUserAction() == GemsGroupFixture.USER_ACTION_SWITCH_DELETE) {
  			Thread oGrpUpdateThread = new Thread(String.valueOf(groupNo) + ":GrpLeave") {
  				public void run() {
  					int[] plArr = { groupPlugload.getPlugload().getId().intValue() };
  					logger.debug(plArr[0] + " leaving group " + groupNo);
  					DeviceServiceImpl.getInstance().sendSUGroupCommand(plArr, ServerConstants.SU_CMD_LEAVE_GRP,
  							(byte) groupType, groupNo, DeviceType.Plugload.getName());
  				}
  			};
  			oGrpUpdateThread.start();
  
  			try {
  				oGrpUpdateThread.join();
  			} catch (InterruptedException ie) {
  				logger.warn(oGrpUpdateThread.getName() + ": interrupted!");
  			}
  			int iStatus = DeviceServiceImpl.getInstance().getSuWirelessGrpChangeAckStatus(groupPlugload.getPlugload().getId());
  			if (iStatus != 0) {
  				grpProcessingUpdate(gid, 1, null);
  				grpProcessingUpdate(gid, 2, null);
  				if (groupType == GGroupType.MotionGroup.getId()) {
  					gemsGroupPlugloadDao.deleteGemsGroupPlugload(groupPlugload);
  				}
  			} else {
  				grpProcessingUpdate(gid, 1, null);
  			}
  		}
  	}
  	String status = grpProcessingStatus(gid);
  	grpProcessingDone(gid);
  	return status;
      
  } //end of method asssignPlugloadsToGroup

  public int removePlugloadsFromGroup(Long gid, List<GemsGroupPlugload> groupPlugloads, final int groupNo,
          final int groupType, Long forceDelete) {
     //TODO : Plugload : Please change below method w.r.t. Plug load Protocol methods
      List<GemsGroupPlugload> currentPlugloads = gemsGroupPlugloadDao.getGemsGroupPlugloadByGroup(gid);
      if (currentPlugloads == null) {
            currentPlugloads = new ArrayList<GemsGroupPlugload>();
      }
      int iStatus = 0;
      Iterator<GemsGroupPlugload> itr = null;
      grpProcessingUpdate(gid, 0, new Long(groupPlugloads.size()));
      itr = groupPlugloads.iterator();
      while (itr.hasNext()) {
          final GemsGroupPlugload groupPlugload = (GemsGroupPlugload) itr.next();
          if ((groupPlugload.getNeedSync() & GemsGroupPlugload.SYNC_STATUS_GROUP_SYNCD) == GemsGroupPlugload.SYNC_STATUS_GROUP_SYNCD) {
              Thread oGrpUpdateThread = new Thread(String.valueOf(groupNo) + ":GrpLeave") {
                  public void run() {
                      int[] fidArr = { groupPlugload.getPlugload().getId().intValue() };
                      logger.debug(fidArr[0] + " leaving group " + groupNo);
                      DeviceServiceImpl.getInstance().sendSUGroupCommand(fidArr, ServerConstants.SU_CMD_LEAVE_GRP,
                              (byte) groupType, groupNo, DeviceType.Plugload.getName());
                  }
              };
              oGrpUpdateThread.start();
  
              try {
                  oGrpUpdateThread.join();
              } catch (InterruptedException ie) {
                  logger.warn(oGrpUpdateThread.getName() + ": interrupted!");
              }
              int gStatus = DeviceServiceImpl.getInstance().getSuWirelessGrpChangeAckStatus(groupPlugload.getPlugload().getId());
              if (gStatus != 0) {
                  grpProcessingUpdate(gid, 1, null);
                  grpProcessingUpdate(gid, 2, null);
                  gemsGroupPlugloadDao.deleteGemsGroupPlugload(groupPlugload);
                  iStatus |= 0;
              } else {
                   if(forceDelete == 0L) {
                         grpProcessingUpdate(gid, 1, null);
                         iStatus |= 1;
                   }
              }
          }else {
              // Directly delete the plugload from the group.
            gemsGroupPlugloadDao.deleteGemsGroupPlugload(groupPlugload);
          }
      }
      grpProcessingStatus(gid);
      grpProcessingDone(gid);
      return iStatus;
  }
  
  public String grpProcessingStatus(Long gid) {
		StringBuffer status = new StringBuffer("");
		DeviceServiceImpl deviceService = DeviceServiceImpl.getInstance();
		if (deviceService.grpProcessingMap.containsKey(gid)) {
			List<Long> processingList = deviceService.grpProcessingMap.get(gid);
			for (Long a : processingList) {
				status.append(a).append(",");
			}
		}
		return status.toString();
	}
  
  public void grpProcessingDone(Long gid) {
	DeviceServiceImpl.getInstance().grpProcessingMap.remove(gid);
  }
  
  public void grpProcessingUpdate(Long gid, int index, Long value) {
      DeviceServiceImpl deviceService = DeviceServiceImpl.getInstance();
      if(!deviceService.grpProcessingMap.containsKey(gid)) {
             List<Long> newList = new ArrayList<Long>();
             newList.add(new Long(0));
             newList.add(new Long(0));
             newList.add(new Long(0));
             newList.add(new Long(0));
             newList.add(new Long(0));
             newList.add(new Long(0));
             DeviceServiceImpl.getInstance().grpProcessingMap.put(gid, newList);
      }
      List<Long> processingList = deviceService.grpProcessingMap.get(gid);
      if(value == null) {
             processingList.set(index, processingList.get(index) + 1);
      }
      else {
             processingList.set(index, value);
      }
   }
  
  public List<GemsGroupPlugload> getGemsGroupPlugloadByGroup(Long groupId){
	  return gemsGroupPlugloadDao.getGemsGroupPlugloadByGroup(groupId);
  }
  
  public void updateGroupPlugloadSyncPending(Long gemsGroupId, boolean bEnable) {
	  gemsGroupPlugloadDao.updateGroupPlugloadSyncPending(gemsGroupId, bEnable);
  }
  
  public String addGroupPlugload(GemsGroup gemsGroup, Plugload plg, GemsGroupPlugload ggpg) {
	  plg.setGroupsSyncPending(true);
	  plugloadDao.changeGroupsSyncPending(plg);
      // First check if this Plugload is already present but in DELETED state, if present then instead of adding a new entry
      // just change the state to non-deleted
	  	GemsGroupPlugload gemsGroupPlugload = null;
	  	gemsGroupPlugload = gemsGroupPlugloadDao.getGemsGroupPlugload(gemsGroup.getId(), plg.getId());
		if(gemsGroupPlugload == null)
			gemsGroupPlugload = new GemsGroupPlugload();

		if (ggpg.getMotionGrpPlDetails() != null) {
			MotionGroupPlugloadDetails mgpgd = new MotionGroupPlugloadDetails();
			mgpgd.copy(ggpg.getMotionGrpPlDetails());
			mgpgd.setGemsGroupPlugload(gemsGroupPlugload);
			gemsGroupPlugload.setMotionGrpPlDetails(mgpgd);
		}
		gemsGroupPlugload.setGroup(gemsGroup);
		gemsGroupPlugload.setPlugload(plg);
		gemsGroupPlugload.setUserAction(GemsGroupPlugload.USER_ACTION_DEFAULT);
		gemsGroupPlugload.setNeedSync(GemsGroupPlugload.SYNC_STATUS_UNKNOWN);
		gemsGroupPlugloadDao.saveGemsGroupPlugload(gemsGroupPlugload);
      return "S";
  }
  
  public String addGroupPlugload(GemsGroup gemsGroup, Plugload plg) {
	  plg.setGroupsSyncPending(true);
	  plugloadDao.changeGroupsSyncPending(plg);
      // First check if this Plugload is already present but in DELETED state, if present then instead of adding a new entry
      // just change the state to non-deleted
	  GemsGroupPlugload gemsGroupPlugload = null;
	  gemsGroupPlugload = gemsGroupPlugloadDao.getGemsGroupPlugload(gemsGroup.getId(), plg.getId());
		if(gemsGroupPlugload == null)
			gemsGroupPlugload = new GemsGroupPlugload();
		gemsGroupPlugload.setGroup(gemsGroup);
		gemsGroupPlugload.setPlugload(plg);
		gemsGroupPlugload.setUserAction(GemsGroupPlugload.USER_ACTION_DEFAULT);
		gemsGroupPlugload.setNeedSync(GemsGroupPlugload.SYNC_STATUS_UNKNOWN);
		gemsGroupPlugloadDao.saveGemsGroupPlugload(gemsGroupPlugload);
      return "S";
  }
  
  public String addSwitchPlugloads(Switch sw, List<Plugload> plugloads) {
		GemsGroup gemsGroup = sw.getGemsGroup();

		for (Plugload plugload : plugloads) {
			plugload.setGroupsSyncPending(true);
			plugloadDao.changeGroupsSyncPending(plugload);
			GemsGroupPlugload gemsGroupPlugload = new GemsGroupPlugload();
			gemsGroupPlugload.setGroup(gemsGroup);
			gemsGroupPlugload.setPlugload(plugload);
			gemsGroupPlugload
					.setUserAction(GemsGroupFixture.USER_ACTION_DEFAULT);
			gemsGroupPlugload.setNeedSync(GemsGroupFixture.SYNC_STATUS_UNKNOWN);
			gemsGroupPlugloadDao.saveGemsGroupPlugload(gemsGroupPlugload);
		}
		return "S";
	}
  public void updateGemsGroupPlugload(GemsGroupPlugload ggp) {
	  gemsGroupPlugloadDao.updateGemsGroupPlugload(ggp);
  }
} //end of class GemsPlugloadGroupManager
