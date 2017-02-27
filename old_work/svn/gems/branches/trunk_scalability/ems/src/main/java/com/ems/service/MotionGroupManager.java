package com.ems.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.MotionGroupDao;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.MotionGroup;
import com.ems.model.MotionGroupFixtureDetails;
import com.ems.server.ServerConstants;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.util.ServerUtil;

@Service("motionGroupManager")
@Transactional(propagation = Propagation.REQUIRED)
public class MotionGroupManager {
    private static final Logger logger = Logger.getLogger("EMS");

    @Resource
    GemsGroupManager gemsGroupManager;

    @Resource
	private MotionGroupDao motionGroupDao;
	
	
	public String getNextGroupNo() {
		return motionGroupDao.getNextGroupNo();
	}
	
	public MotionGroup getMotionGroupById(Long motionGroupId) {
		return (MotionGroup) motionGroupDao.getObject(MotionGroup.class, motionGroupId);
	}
	
	public MotionGroup getMotionGroupByGemsGroupId(Long gemsGroupId) {
        return motionGroupDao.getMotionGroupByGemsGroupId(gemsGroupId);
    }
	
	public MotionGroup saveOrUpdateMotionGroup(MotionGroup motionGroup) {
		return (MotionGroup) motionGroupDao.saveObject(motionGroup);
	}
	
    public void deleteMotionGroup(Long id) {
        MotionGroup motionGroup = getMotionGroupById(id);
        gemsGroupManager.deleteGemsGroup(motionGroup.getGemsGroup().getId());
        motionGroupDao.removeObject(MotionGroup.class, id);
    }

    public List<GemsGroup> loadGroupsByCompany(Long companyId) {
        List<GemsGroup> groups = motionGroupDao.loadGroupsByCompany(companyId);
        return groups;
    }

    public List<GemsGroup> loadGroupsByCampus(Long campusId) {
        List<GemsGroup> groups = motionGroupDao.loadGroupsByCampus(campusId);
        return groups;
    }

    public List<GemsGroup> loadGroupsByBuilding(Long buildingId) {
        List<GemsGroup> groups = motionGroupDao.loadGroupsByBuilding(buildingId);
        return groups;
    }

    public List<GemsGroup> loadGroupsByFloor(Long floorId) {
        List<GemsGroup> groups = motionGroupDao.loadGroupsByFloor(floorId);
        return groups;
    }

    public List<GemsGroup> loadObsoleteGroupsByCompany(Long companyId) {
        List<GemsGroup> groups = motionGroupDao.loadObsoleteGroupsByCompany(companyId);
        return groups;
    }
    
    public MotionGroup getMotionGroupByGroupNo(int groupNo) {
    	return motionGroupDao.getMotionGroupByGroupNo(groupNo);
    }
    
    public boolean updateMotionGroupFixtureDetails(Long gemsGroupId, GemsGroupFixture ggfx, MotionGroupFixtureDetails mgfd) {
    	MotionGroupFixtureDetails mgfd_src = motionGroupDao.getMotionGroupFixtureDetails(ggfx.getId());
    	Boolean state = false;
    	if (mgfd_src != null && mgfd != null) {
	    	mgfd_src.copy(mgfd);
	    	gemsGroupManager.saveObject(ggfx);
	    	state = true;
    	}else
    	{
            if (mgfd != null) {
                mgfd_src = new MotionGroupFixtureDetails();
                mgfd_src.copy(mgfd);
                mgfd_src.setGemsGroupFixture(ggfx);
                gemsGroupManager.saveObject(mgfd_src);
                state = true;
            } else {
                state=false;
            }
    	}
    	return state;
    }

    public MotionGroupFixtureDetails getMotionGroupFixtureDetails(Long gemsgroupFixureId) {
    	return motionGroupDao.getMotionGroupFixtureDetails(gemsgroupFixureId);
    }
    
    public void manageGroupMembership(Long gemsGroupId, int groupNo) {
    	List<GemsGroupFixture> currentGroupFixtures = gemsGroupManager.getGemsGroupFixtureByGroup(gemsGroupId);
    	if (currentGroupFixtures != null && currentGroupFixtures.size() > 0) {
	        logger.info(currentGroupFixtures.size() + " send motion configuration to " + groupNo);
	        for (GemsGroupFixture ggf : currentGroupFixtures) {
				sendMotionGroupDetailsToFixture(gemsGroupId, ggf);
	        }
    	}
    }

    /**
     * Send motion group details params in the context of rx/tx motion groups.
     * @param groupId
     * @param ggf
     * @return
     */
	public boolean sendMotionGroupDetailsToFixture(Long groupId,
			GemsGroupFixture ggf) {
		if ((ggf.getNeedSync() & GemsGroupFixture.SYNC_STATUS_GROUP) == GemsGroupFixture.SYNC_STATUS_GROUP) {
			if (ggf.getUserAction() == GemsGroupFixture.USER_ACTION_DEFAULT
					|| ggf.getUserAction() == GemsGroupFixture.USER_ACTION_MOTION_PARAMS_PUSH) {
				MotionGroup motionGroup = getMotionGroupByGemsGroupId(groupId);
				MotionGroupFixtureDetails mgfd = ggf.getMotionGrpFxDetails();
				if (mgfd != null) {
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					try {
						Long fxId = ggf.getFixture().getId();
						int[] fixtureArr = { fxId.intValue() };

						int groupNo = Integer.parseInt(motionGroup.getGroupNo()
								.toString(), 16);
						output.write(ServerUtil.intToByteArray(groupNo));
						output.write(mgfd.getType().byteValue());
						output.write(mgfd.getAmbientType().byteValue());
						output.write(mgfd.getUseEmValue().byteValue());
						output.write(ServerUtil.shortToByteArray(mgfd
								.getLoAmbValue()));
						output.write(ServerUtil.shortToByteArray(mgfd
								.getHiAmbValue()));
						output.write(ServerUtil.intToByteArray(mgfd.getTod()));
						output.write(mgfd.getLightLevel().byteValue());
						DeviceServiceImpl
								.getInstance()
								.sendMotionGroupConfiguration(
										fixtureArr,
										output.toByteArray(),
										ServerConstants.CMD_MOTION_GRP_APPLY_ACTION);
						if (DeviceServiceImpl.getInstance()
								.getSuWirelessGrpConfigChangeAckStatus(fxId)) {
							ggf.setUserAction(GemsGroupFixture.USER_ACTION_DEFAULT);
							ggf.setNeedSync(ggf.getNeedSync()
									| GemsGroupFixture.SYNC_STATUS_GROUP);
							gemsGroupManager.saveGemsGroupFixtures(ggf);
						} else {
							logger.info(fxId
									+ " with gems group "
									+ groupId
									+ ": unable send motion params configuration");
						}
					} catch (IOException ioe) {
						logger.error(ioe.getMessage());
					}
				}
			}
		}
		return true;
	}
}