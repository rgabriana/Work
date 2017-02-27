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
import com.ems.model.Fixture;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.GemsGroupType;
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
    static final Logger logger = Logger.getLogger("GemsGroupLogger");

    @Resource
    private GemsGroupDao gemsGroupDao;

    public List<GemsGroupType> getGroupTypeList() {
        return gemsGroupDao.getGroupTypeList();
    }

    public void saveGemsGroupType(GemsGroupType gemsGroupType) {
        gemsGroupDao.saveGemsGroupType(gemsGroupType);
    }

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
        return gemsGroupDao.saveGroup(gemsGroup);
    }
    
    public void deleteGroup(GemsGroup gemsGroup) {
        gemsGroupDao.deleteGroup(gemsGroup);
    }
    
    public void deleteGroupType(GemsGroupType gemsGroupType) {
        gemsGroupDao.deleteGroupType(gemsGroupType);
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
    
    public String asssignFixturesToGroup(Long gid, List<Fixture> fixtures) {
    	
        GemsGroup group = gemsGroupDao.loadGemsGroup(gid);
        final int groupNo = group.getType().getGroupNo();
        // Currently only motion group is supported
        final int type = GGroupType.MotionGroup.getId();
        
        List<GemsGroupFixture> currentFixtures = gemsGroupDao.getGemsGroupFixtureByGroup(gid);
        if(currentFixtures == null) {
        	currentFixtures = new ArrayList<GemsGroupFixture>();
        }
        grpProcessingUpdate(gid, 3, 0L);
        Iterator<Fixture> itr = null;
        for(GemsGroupFixture ggf: currentFixtures) {
        	itr = fixtures.iterator();
        	boolean found = false;
        	while (itr.hasNext()) {
        		Fixture fixture = (Fixture) itr.next();
        		if(fixture.getId().compareTo(ggf.getFixture().getId()) == 0) {
        				found = true;
        		}
        	}
       		if(!found) {
       			final Fixture fixture = ggf.getFixture();
       			Thread oGrpUpdateThread = new Thread(fixture.getFixtureName() + ":GrpUpdate") {
                    public void run() {
                    	int[] fidArr = {fixture.getId().intValue()};
                    	DeviceServiceImpl.getInstance().sendSUGroupCommand(fidArr, ServerConstants.SU_CMD_LEAVE_GRP,
                                (byte) type, groupNo);
                    }
                };
                oGrpUpdateThread.start();

                try {
                	oGrpUpdateThread.join();
                } catch (InterruptedException ie) {
                    logger.warn(oGrpUpdateThread.getName() + ": interrupted!");
                }
                if (DeviceServiceImpl.getInstance().getSuWirelessGrpChangeAckStatus(fixture)) {
                	grpProcessingUpdate(gid, 4, null);
                	grpProcessingUpdate(gid, 5, null);
                	gemsGroupDao.deleteGemsGroupFixtures(ggf);
                }
                else {
                	grpProcessingUpdate(gid, 4, null);
                }
            }
        }
        
        grpProcessingUpdate(gid, 0, new Long(fixtures.size()));
        itr = fixtures.iterator();
        while (itr.hasNext()) {
            final Fixture fixture = (Fixture) itr.next();

            GemsGroupFixture groupFixture = gemsGroupDao.getGemsGroupFixture(gid, fixture.getId());
            if (groupFixture == null) {
       			Thread oGrpUpdateThread = new Thread(fixture.getFixtureName() + ":GrpUpdate") {
                    public void run() {
                    	int[] fidArr = {fixture.getId().intValue()};
                    	DeviceServiceImpl.getInstance().sendSUGroupCommand(fidArr, ServerConstants.SU_CMD_JOIN_GRP,
                                (byte) type, groupNo);
                    }
                };
                oGrpUpdateThread.start();

                try {
                	oGrpUpdateThread.join();
                } catch (InterruptedException ie) {
                    logger.warn(oGrpUpdateThread.getName() + ": interrupted!");
                }
                if (DeviceServiceImpl.getInstance().getSuWirelessGrpChangeAckStatus(fixture)) {
                	grpProcessingUpdate(gid, 1, null);
                	grpProcessingUpdate(gid, 2, null);
                    groupFixture = new GemsGroupFixture();
                    groupFixture.setFixture(fixture);
                    groupFixture.setGroup(group);
                    gemsGroupDao.saveGemsGroupFixtures(groupFixture);
                }
                else {
                	grpProcessingUpdate(gid, 1, null);
                }

            }
            else {
            	grpProcessingUpdate(gid, 1, null);
            	grpProcessingUpdate(gid, 2, null);
            }
        }
        String status = grpProcessingStatus(gid);
        grpProcessingDone(gid);
        return status;
    }

    public String saveGemsGroupFixtures(Long gid, List<Fixture> fixtures) {
        GemsGroup group = gemsGroupDao.loadGemsGroup(gid);
        final int groupNo = group.getType().getGroupNo();
        // Currently only motion group is supported
        final int type = GGroupType.MotionGroup.getId();
        
        grpProcessingUpdate(gid, 0, new Long(fixtures.size()));
        Iterator<Fixture> itr = fixtures.iterator();
        while (itr.hasNext()) {
            final Fixture fixture = (Fixture) itr.next();

            GemsGroupFixture groupFixture = gemsGroupDao.getGemsGroupFixture(gid, fixture.getId());
            if (groupFixture == null) {
            	Thread oGrpUpdateThread = new Thread(fixture.getFixtureName() + ":GrpUpdate") {
                    public void run() {
                    	int[] fidArr = {fixture.getId().intValue()};
                    	DeviceServiceImpl.getInstance().sendSUGroupCommand(fidArr, ServerConstants.SU_CMD_JOIN_GRP,
                                (byte) type, groupNo);
                    }
                };
                oGrpUpdateThread.start();

                try {
                	oGrpUpdateThread.join();
                } catch (InterruptedException ie) {
                    logger.warn(oGrpUpdateThread.getName() + ": interrupted!");
                }
                if (DeviceServiceImpl.getInstance().getSuWirelessGrpChangeAckStatus(fixture)) {
                	grpProcessingUpdate(gid, 1, null);
                	grpProcessingUpdate(gid, 2, null);
                    groupFixture = new GemsGroupFixture();
                    groupFixture.setFixture(fixture);
                    groupFixture.setGroup(group);
                    gemsGroupDao.saveGemsGroupFixtures(groupFixture);
                }
                else {
                	grpProcessingUpdate(gid, 1, null);
                }
            }
            else {
            	grpProcessingUpdate(gid, 1, null);
            	grpProcessingUpdate(gid, 2, null);
            }
        }
        String status = grpProcessingStatus(gid);
        grpProcessingDone(gid);
        return status;
    }

    public void deleteGemsGroup(Long gid, List<Fixture> fixtures) {
        GemsGroup group = gemsGroupDao.loadGemsGroup(gid);
        final int groupNo = group.getType().getGroupNo();
        final int type = GGroupType.MotionGroup.getId();

        Iterator<Fixture> itr = fixtures.iterator();
        boolean deleteGroup = true;
        while (itr.hasNext()) {
            final Fixture fixture = (Fixture) itr.next();
            GemsGroupFixture groupFixture = gemsGroupDao.getGemsGroupFixture(gid, fixture.getId());
            if (groupFixture != null) {
            	Thread oGrpUpdateThread = new Thread(fixture.getFixtureName() + ":GrpUpdate") {
                    public void run() {
                    	int[] fidArr = {fixture.getId().intValue()};
                    	DeviceServiceImpl.getInstance().sendSUGroupCommand(fidArr, ServerConstants.SU_CMD_LEAVE_GRP,
                                (byte) type, groupNo);
                    }
                };
                oGrpUpdateThread.start();

                try {
                	oGrpUpdateThread.join();
                } catch (InterruptedException ie) {
                    logger.warn(oGrpUpdateThread.getName() + ": interrupted!");
                }
                if (DeviceServiceImpl.getInstance().getSuWirelessGrpChangeAckStatus(fixture)) {
                	gemsGroupDao.deleteGemsGroupFixtures(groupFixture);
                }
                else {
                	deleteGroup = false;
                }
            }
        }
        if(deleteGroup) {
            deleteGroup(group);
            deleteGroupType(group.getType());
        }
    }

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
                if (DeviceServiceImpl.getInstance().getSuWirelessGrpChangeAckStatus(fixture)) {
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
    
    public void deleteGemsGroupsFromFixture(Long fixtureId) {
    	gemsGroupDao.deleteGemsGroupsFromFixture(fixtureId);
    }
}
