package com.ems.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.GemsGroupDao;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.MotionGroupFixtureDetails;
import com.ems.model.Switch;
import com.ems.server.ServerConstants;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.types.GGroupType;

/**
 * @author Shilpa Chalasani
 * 
 */
@Service("gemsGroupManager")
@Transactional(propagation = Propagation.REQUIRED)
public class GemsGroupManager {
    static final Logger logger = Logger.getLogger("FixtureLogger");

    @Resource
    private GemsGroupDao gemsGroupDao;

    @Resource
    private FixtureManager fixtureManager;
    
    @Resource
    EventsAndFaultManager	eventsAndFaultManager;
    
    public List<GemsGroup> loadGroupsByCompany(Long companyId) {
        List<GemsGroup> groups = gemsGroupDao.loadGroupsByCompany(companyId);
        return groups;
    }
    
    public List<GemsGroup> loadObsoleteGroupsByCompany(Long companyId) {
        List<GemsGroup> groups = gemsGroupDao.loadObsoleteGroupsByCompany(companyId);
        return groups;
    }
    
    public List<GemsGroup> loadGroupsByBuilding(Long buildingId) {
        List<GemsGroup> groups = gemsGroupDao.loadGroupsByBuilding(buildingId);
        return groups;
    }
    
    public List<GemsGroup> loadGroupsByCampus(Long campusId) {
        List<GemsGroup> groups = gemsGroupDao.loadGroupsByCampus(campusId);
        return groups;
    }
    
    public List<GemsGroup> loadGroupsByFloor(Long floorId) {
        List<GemsGroup> groups = gemsGroupDao.loadGroupsByFloor(floorId);
        return groups;
    }

    public GemsGroup loadGroupsByGroupNameAndFloor(String groupName, Long floorId) {
        return gemsGroupDao.loadGroupsByGroupNameAndFloor(groupName, floorId);
    }

    public GemsGroup loadGemsGroup(Long id) {
        return gemsGroupDao.loadGemsGroup(id);
    }

    public GemsGroup createNewGroup(GemsGroup gemsGroup) {
        return gemsGroupDao.createNewGroup(gemsGroup);
    }

    public GemsGroup editGroup(GemsGroup gemsGroup) {
        return (GemsGroup)gemsGroupDao.saveObject(gemsGroup);
    }
    
    public void deleteGroup(GemsGroup gemsGroup) {
        gemsGroupDao.deleteGroup(gemsGroup);
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
    
    public void grpProcessingDone(Long gid) {
    	DeviceServiceImpl.getInstance().grpProcessingMap.remove(gid);
    }
    
    public String grpProcessingStatus(Long gid) {
    	StringBuffer status = new StringBuffer("");
    	DeviceServiceImpl deviceService = DeviceServiceImpl.getInstance();
    	if(deviceService.grpProcessingMap.containsKey(gid)) {
    		List<Long> processingList = deviceService.grpProcessingMap.get(gid);
    		for(Long a: processingList) {
    			status.append(a).append(",");
    		}
    	}
    	return status.toString();
    }

    public void saveGemsGroupFixtures(GemsGroupFixture groupFixture) {
       gemsGroupDao.saveGemsGroupFixtures(groupFixture);
    }

    public void deleteGemsGroup(Long groupId) {
        gemsGroupDao.deleteGemsGroup(groupId);
    }
    
    public void saveObject(GemsGroupFixture ggfx) {
    	gemsGroupDao.saveObject(ggfx);
    }
    
    public void saveObject(MotionGroupFixtureDetails mgfd_src) {
    	gemsGroupDao.saveObject(mgfd_src);
    }

    //make sure that this method is called in a separate thread so that it does not stall the receiver thread for packets from sensors
    public int assignFixtureToGroup(Fixture fixture, int msgType, byte gType, int groupNo, long gemsGrpId) {
    	
    	int[] fixArr = { fixture.getId().intValue() };
    	DeviceServiceImpl.getInstance().sendSUGroupCommand(fixArr, ServerConstants.SU_CMD_JOIN_GRP, gType, groupNo);
			//update the sync state in the database    						
			int iStatus = DeviceServiceImpl.getInstance().getSuWirelessGrpChangeAckStatus(fixture.getId());
			if (iStatus != 0) {
        GemsGroupFixture groupFixture = getGemsGroupFixture(gemsGrpId, fixture.getId());             
        if (groupFixture != null) {
          if (iStatus == ServerConstants.SU_ACK) {
          	groupFixture.setNeedSync(groupFixture.getNeedSync() | GemsGroupFixture.SYNC_STATUS_GROUP);
          } else {
          	groupFixture.setNeedSync(groupFixture.getNeedSync() | GemsGroupFixture.SYNC_STATUS_GROUP_NACK);
          }
          gemsGroupDao.saveGemsGroupFixtures(groupFixture);
        } 
			}
			return iStatus;
    	
    } //end of method assignFixtureToGroup
    
    /**
     * Join group 
     * @param gid
     * @param fixtures
     * @param groupNo
     * @param groupType
     * @return Processing status
     */
    public String asssignFixturesToGroup(Long gid, List<GemsGroupFixture> groupfixtures, final int groupNo,
            final int groupType) {
        List<GemsGroupFixture> currentFixtures = gemsGroupDao.getAllGemsGroupFixtureByGroup(gid);
        if (currentFixtures == null) {
            currentFixtures = new ArrayList<GemsGroupFixture>();
        }

        Iterator<GemsGroupFixture> itr = null;
        grpProcessingUpdate(gid, 0, new Long(groupfixtures.size()));
        itr = groupfixtures.iterator();
        while (itr.hasNext()) {
            final GemsGroupFixture groupFixture = (GemsGroupFixture) itr.next();
            if (groupFixture.getUserAction() == GemsGroupFixture.USER_ACTION_DEFAULT) {
                if (groupFixture.getNeedSync() == GemsGroupFixture.SYNC_STATUS_UNKNOWN) {
                    Thread oGrpUpdateThread = new Thread(String.valueOf(groupNo) + ":GrpJoin") {
                        public void run() {
                            int[] fidArr = { groupFixture.getFixture().getId().intValue() };
                            logger.debug(fidArr[0] + " joining group " + groupNo);
                            DeviceServiceImpl.getInstance().sendSUGroupCommand(fidArr, ServerConstants.SU_CMD_JOIN_GRP,
                                    (byte) groupType, groupNo);
                        }
                    };
                    oGrpUpdateThread.start();
        
                    try {
                        oGrpUpdateThread.join();
                    } catch (InterruptedException ie) {
                        logger.warn(oGrpUpdateThread.getName() + ": interrupted!");
                    }
                    int iStatus = DeviceServiceImpl.getInstance().getSuWirelessGrpChangeAckStatus(groupFixture.getFixture().getId());
                    if (iStatus != 0) {
                        grpProcessingUpdate(gid, 1, null);
                        grpProcessingUpdate(gid, 2, null);
                        if (groupFixture != null) {
                            if (iStatus == ServerConstants.SU_ACK)
                                groupFixture.setNeedSync(groupFixture.getNeedSync() | GemsGroupFixture.SYNC_STATUS_GROUP);
                            else
                            {
                                groupFixture.setNeedSync(groupFixture.getNeedSync() | GemsGroupFixture.SYNC_STATUS_GROUP_NACK);
                                eventsAndFaultManager.addEvent(groupFixture.getFixture(), "Join group command returned NACK for the fixture", EventsAndFault.FIXTURE_GROUP_CHANGE_EVENT);
                            }
                            gemsGroupDao.saveGemsGroupFixtures(groupFixture);
                        }
                    } else {
                        grpProcessingUpdate(gid, 1, null);
                    }
                }
            } else if (groupFixture.getUserAction() == GemsGroupFixture.USER_ACTION_FIXTURE_DELETE
                    || groupFixture.getUserAction() == GemsGroupFixture.USER_ACTION_SWITCH_DELETE) {
                Thread oGrpUpdateThread = new Thread(String.valueOf(groupNo) + ":GrpLeave") {
                    public void run() {
                        int[] fidArr = { groupFixture.getFixture().getId().intValue() };
                        logger.debug(fidArr[0] + " leaving group " + groupNo);
                        DeviceServiceImpl.getInstance().sendSUGroupCommand(fidArr, ServerConstants.SU_CMD_LEAVE_GRP,
                                (byte) groupType, groupNo);
                    }
                };
                oGrpUpdateThread.start();
    
                try {
                    oGrpUpdateThread.join();
                } catch (InterruptedException ie) {
                    logger.warn(oGrpUpdateThread.getName() + ": interrupted!");
                }
                int iStatus = DeviceServiceImpl.getInstance().getSuWirelessGrpChangeAckStatus(groupFixture.getFixture().getId());
                if (iStatus != 0) {
                    grpProcessingUpdate(gid, 1, null);
                    grpProcessingUpdate(gid, 2, null);
                    if (groupType == GGroupType.MotionGroup.getId()) {
                        gemsGroupDao.deleteGemsGroupFixtures(groupFixture);
                    }
                } else {
                    grpProcessingUpdate(gid, 1, null);
                }
            }
        }
        String status = grpProcessingStatus(gid);
        grpProcessingDone(gid);
        return status;
    }

    public int removeFixturesFromGroup(Long gid, List<GemsGroupFixture> groupfixtures, final int groupNo,
            final int groupType, Long forceDelete) {
        List<GemsGroupFixture> currentFixtures = gemsGroupDao.getGemsGroupFixtureByGroup(gid);
        if (currentFixtures == null) {
            currentFixtures = new ArrayList<GemsGroupFixture>();
        }
        int iStatus = 0;
        Iterator<GemsGroupFixture> itr = null;
        grpProcessingUpdate(gid, 0, new Long(groupfixtures.size()));
        itr = groupfixtures.iterator();
        while (itr.hasNext()) {
            final GemsGroupFixture groupFixture = (GemsGroupFixture) itr.next();
            if ((groupFixture.getNeedSync() & GemsGroupFixture.SYNC_STATUS_GROUP_SYNCD) == GemsGroupFixture.SYNC_STATUS_GROUP_SYNCD) {
                Thread oGrpUpdateThread = new Thread(String.valueOf(groupNo) + ":GrpLeave") {
                    public void run() {
                        int[] fidArr = { groupFixture.getFixture().getId().intValue() };
                        logger.debug(fidArr[0] + " leaving group " + groupNo);
                        DeviceServiceImpl.getInstance().sendSUGroupCommand(fidArr, ServerConstants.SU_CMD_LEAVE_GRP,
                                (byte) groupType, groupNo);
                    }
                };
                oGrpUpdateThread.start();
    
                try {
                    oGrpUpdateThread.join();
                } catch (InterruptedException ie) {
                    logger.warn(oGrpUpdateThread.getName() + ": interrupted!");
                }
                int gStatus = DeviceServiceImpl.getInstance().getSuWirelessGrpChangeAckStatus(groupFixture.getFixture().getId());
                if (gStatus != 0) {
                    grpProcessingUpdate(gid, 1, null);
                    grpProcessingUpdate(gid, 2, null);
                    gemsGroupDao.deleteGemsGroupFixtures(groupFixture);
                    iStatus |= 0;
                } else {
                	if(forceDelete == 0L) {
	                    grpProcessingUpdate(gid, 1, null);
	                    iStatus |= 1;
                	}
                }
            }else {
                // Directly delete the fixture from the group.
                gemsGroupDao.deleteGemsGroupFixtures(groupFixture);
            }
        }
        grpProcessingStatus(gid);
        grpProcessingDone(gid);
        return iStatus;
    }
    
    public List<GemsGroupFixture> getAllGroupsOfFixture(Fixture fixture) {
    
    	return gemsGroupDao.getGemsGroupFixtureByFixture(fixture.getId());
    	
    } //end of method getAllGroupsOfFixture
    
    public String resetAllGroupOnFixtures(List<Fixture> fixtures) {
        Iterator<Fixture> itr = fixtures.iterator();
        
        final int type = GGroupType.MotionGroup.getId();
        
        grpProcessingUpdate(0L, 3, new Long(fixtures.size()));
        while (itr.hasNext()) {
            final Fixture fixture = (Fixture) itr.next();

            List<GemsGroupFixture> groupFixtureList = gemsGroupDao.getGemsGroupFixtureByFixture(fixture.getId());
            if (groupFixtureList != null && groupFixtureList.size() > 0) {
            	
            	Thread oGrpUpdateThread = new Thread(fixture.getFixtureName() + ":GrpUpdate") {
                    public void run() {
                    	int[] fidArr = {fixture.getId().intValue()};
                    	DeviceServiceImpl.getInstance().sendSUGroupCommand(fidArr, ServerConstants.SU_CMD_REQ_REST_GRP,
                                (byte) type, 0);
                    }
                };
                oGrpUpdateThread.start();

                try {
                	oGrpUpdateThread.join();
                } catch (InterruptedException ie) {
                    logger.warn(oGrpUpdateThread.getName() + ": interrupted!");
                }
                int iStatus = DeviceServiceImpl.getInstance().getSuWirelessGrpChangeAckStatus(fixture.getId());
                if (iStatus != 0) {
                	grpProcessingUpdate(0L, 4, null);
                	grpProcessingUpdate(0L, 5, null);
                    Iterator<GemsGroupFixture> oItr = groupFixtureList.iterator();
                    while(oItr.hasNext()) {
                        gemsGroupDao.deleteGemsGroupFixtures(oItr.next());
                    }
                }
                else {
                	grpProcessingUpdate(0L, 4, null);
                }
            }
            else {
            	grpProcessingUpdate(0L, 4, null);
            	grpProcessingUpdate(0L, 5, null);
            }
        }
        String status = grpProcessingStatus(0L);
        grpProcessingDone(0L);
        return status;
    }

    public GemsGroupFixture getGemsGroupFixture(Long groupId, Long fixtureid) {
        return gemsGroupDao.getGemsGroupFixture(groupId, fixtureid);
    }

    public List<GemsGroupFixture> getGemsGroupFixtureByGroup(Long groupId) {
        return gemsGroupDao.getGemsGroupFixtureByGroup(groupId);
    }

    public List<GemsGroupFixture> getAllGemsGroupFixtureByGroup(Long groupId) {
        return gemsGroupDao.getAllGemsGroupFixtureByGroup(groupId);
    }

    public void deleteGemsGroupsFromFixture(Long fixtureId) {
    	gemsGroupDao.deleteGemsGroupsFromFixture(fixtureId);
    }
    
    
    public String addSwitchFixtures(Switch sw, List<Fixture> fixtures) {
     	GemsGroup gemsGroup = sw.getGemsGroup();

     	for (Fixture fixt: fixtures) {
            fixt.setGroupsSyncPending(true);
            fixtureManager.changeGroupsSyncPending(fixt);
    		GemsGroupFixture gemsGroupFixture = new GemsGroupFixture();
    		gemsGroupFixture.setGroup(gemsGroup);
    		gemsGroupFixture.setFixture(fixt);
            gemsGroupFixture.setUserAction(GemsGroupFixture.USER_ACTION_DEFAULT);
    		gemsGroupFixture.setNeedSync(GemsGroupFixture.SYNC_STATUS_UNKNOWN);
    		gemsGroupDao.saveGemsGroupFixtures(gemsGroupFixture);
    	}
        return "S";
    }
    
    public String addGroupFixture(GemsGroup gemsGroup, Fixture fixt) {
        fixt.setGroupsSyncPending(true);
        fixtureManager.changeGroupsSyncPending(fixt);
        // First check if this SU is already present but in DELETED state, if present then instead of adding a new entry
        // just change the state to non-deleted
		GemsGroupFixture gemsGroupFixture = null;
		gemsGroupFixture = gemsGroupDao.getGemsGroupFixture(gemsGroup.getId(), fixt.getId());
		if(gemsGroupFixture == null)
			gemsGroupFixture = new GemsGroupFixture();
		gemsGroupFixture.setGroup(gemsGroup);
		gemsGroupFixture.setFixture(fixt);
        gemsGroupFixture.setUserAction(GemsGroupFixture.USER_ACTION_DEFAULT);
		gemsGroupFixture.setNeedSync(GemsGroupFixture.SYNC_STATUS_UNKNOWN);
		gemsGroupDao.saveGemsGroupFixtures(gemsGroupFixture);
        return "S";
    }
    
    public String addGroupFixture(GemsGroup gemsGroup, Fixture fixt, GemsGroupFixture ggf) {
        fixt.setGroupsSyncPending(true);
        fixtureManager.changeGroupsSyncPending(fixt);
        // First check if this SU is already present but in DELETED state, if present then instead of adding a new entry
        // just change the state to non-deleted
		GemsGroupFixture gemsGroupFixture = null;
		gemsGroupFixture = gemsGroupDao.getGemsGroupFixture(gemsGroup.getId(), fixt.getId());
		if(gemsGroupFixture == null)
			gemsGroupFixture = new GemsGroupFixture();

		if (ggf.getMotionGrpFxDetails() != null) {
			MotionGroupFixtureDetails mgfd = new MotionGroupFixtureDetails();
			mgfd.copy(ggf.getMotionGrpFxDetails());
			mgfd.setGemsGroupFixture(gemsGroupFixture);
			gemsGroupFixture.setMotionGrpFxDetails(mgfd);
		}
		gemsGroupFixture.setGroup(gemsGroup);
		gemsGroupFixture.setFixture(fixt);
        gemsGroupFixture.setUserAction(GemsGroupFixture.USER_ACTION_DEFAULT);
		gemsGroupFixture.setNeedSync(GemsGroupFixture.SYNC_STATUS_UNKNOWN);
		gemsGroupDao.saveGemsGroupFixtures(gemsGroupFixture);
        return "S";
    }
    
    public String removeSwitchFixtures(Long groupId, Long fixtureId) {
    	fixtureManager.changeGroupsSyncPending(fixtureManager.getFixtureById(fixtureId), true);
		GemsGroupFixture gemsGroupFixture = gemsGroupDao.getGemsGroupFixture(groupId, fixtureId);
		gemsGroupDao.deleteGemsGroupFixtures(gemsGroupFixture);
        return "S";
    }
    
	public String removeGroupFixture(Long groupId, Long fixtureId) {
		fixtureManager.changeGroupsSyncPending(fixtureManager.getFixtureById(fixtureId), true);
		GemsGroupFixture gemsGroupFixture = gemsGroupDao.getGemsGroupFixture(groupId, fixtureId);
//		gemsGroupFixture.setUserAction(GemsGroupFixture.USER_ACTION_FIXTURE_DELETE);
		gemsGroupDao.deleteGemsGroupFixtures(gemsGroupFixture);
        return "S";
    }
    
    public void deleteGemsGroups(Long groupId) {
    	gemsGroupDao.deleteGemsGroup(groupId);
    }
    
    public void updateGemsGroup(GemsGroupFixture ggf) {
        gemsGroupDao.saveGemsGroupFixtures(ggf);
    }
    
    public List<GemsGroupFixture> getGemsGroupFixtureByFixture(Long fixtureId) {
    	return gemsGroupDao.getGemsGroupFixtureByFixture(fixtureId);
    }
    
    @Deprecated
    public String asssignFixturesToGroup(Long gid, List<Fixture> fixtures) {
        String status = grpProcessingStatus(gid);
        grpProcessingDone(gid);
        return status;
    }

    @Deprecated
    public String saveGemsGroupFixtures(Long gid, List<Fixture> fixtures) {
        String status = grpProcessingStatus(gid);
        grpProcessingDone(gid);
        return status;
    }

    @Deprecated
    public void deleteGemsGroup(Long gid, List<Fixture> fixtures) {
    }
}
