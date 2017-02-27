package com.ems.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.cache.FixtureCache;
import com.ems.dao.FixtureDao;
import com.ems.model.Ballast;
import com.ems.model.Fixture;
import com.ems.model.FixtureCustomGroupsProfile;
import com.ems.model.FixtureLampCalibration;
import com.ems.model.Gateway;
import com.ems.model.GemsGroupFixture;
import com.ems.model.Groups;
import com.ems.model.LampCalibrationConfiguration;
import com.ems.model.OutageBasePower;
import com.ems.model.ProfileHandler;
import com.ems.model.Switch;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.RemoteDebugging;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.device.DeviceService;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.discovery.DiscoverySO;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.server.util.ServerUtil;
import com.ems.types.UserAuditActionType;
import com.ems.vo.model.FixtureLampStatusVO;
import com.ems.vo.model.FixtureList;
import com.ems.vo.model.FixtureOutageVO;
import com.ems.ws.util.Response;
import com.enlightedinc.hvac.model.Sensor;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Service("fixtureManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureManager {
    static final Logger logger = Logger.getLogger("FixtureLogger");

    @Resource
    private FixtureDao fixtureDao;

    @Resource
    private GroupManager groupManager;

    @Resource
    private ProfileManager profileManager;

    @Resource
    private GatewayManager gatewayManager;

    @Resource
    private GemsGroupManager gemsGroupManager;

    @Resource
    private SwitchManager switchManager;

    @Resource
    private FixtureCalibrationManager fixtureCalibrationManager;
    @Resource
    private UserAuditLoggerUtil userAuditLoggerUtil;

    @Resource(name = "cacheManager")
	EhCacheCacheManager ehCacheCacheManager;
    
    
    public void setRealTimeStats(Fixture fixture) {
    	Ehcache cache = (Ehcache)ehCacheCacheManager.getCache("fixture_snap").getNativeCache();
    	if(cache != null && cache.getKeys().size() > 0) {
    		Element element = cache.get(fixture.getSnapAddress());
    		if(element != null && element.getObjectValue() != null) {
    			fixture.setLastConnectivityAt(((Fixture) element.getObjectValue()).getLastConnectivityAt());
    			fixture.setLastStatsRcvdTime((((Fixture) element.getObjectValue()).getLastStatsRcvdTime()));
    			fixture.setDimmerControl((((Fixture) element.getObjectValue()).getDimmerControl()));
    			fixture.setLastOccupancySeen((((Fixture) element.getObjectValue()).getLastOccupancySeen()));
    			fixture.setLightLevel((((Fixture) element.getObjectValue()).getLightLevel()));
    			fixture.setWattage((((Fixture) element.getObjectValue()).getWattage()));
    			fixture.setAvgTemperature((((Fixture) element.getObjectValue()).getAvgTemperature()));
    		}
    	}
    }

    /**
     * save Fixture details.
     * 
     * @param floor
     *            com.ems.model.Fixture
     */
    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
    			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public Fixture save(Fixture fixture) {
        return (Fixture) fixtureDao.saveObject(fixture);
    }

    /**
     * update Fixture details.
     */
    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public Fixture updateFixture(Fixture fixture, boolean duringCommissioning) {
    	
    	if(!duringCommissioning) {
	        try {
	            if (fixture.getIsHopper() != null) {
	            	if (fixture.getIsHopper() == 0) {
	            		enableHopper(fixture.getId(), false);
	            	} else {
	                enableHopper(fixture.getId(), true);
	            	}
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
    	}
    	fixture.setBaselinePower(null);
        FixtureCache.getInstance().invalidateDeviceCache(fixture.getId());
        return fixtureDao.update(fixture);
    }

    /**
     * Load fixture details.load all fixtures of given group
     * 
     * @param id
     *            group id
     * @return com.ems.model.Fixture collection load only id,sensorId,floor id,area id,subArea id, x axis,y axis, group
     *         id details of fixture other details loads as null.
     */
    public List<Fixture> loadFixtureByGroupId(Long id) {
        logger.info("Entering loadFixtureByGroupId");
        List<Fixture> fixtures = fixtureDao.loadFixtureByGroupId(id);
        logger.info("Exiting loadFixtureByGroupId");
        return fixtures;
    }

    /**
     * Load fixture details.load all fixtures of given floor
     * 
     * @param id
     *            floor id
     * @return com.ems.model.Fixture collection load only id,sensorId,floor id,area id,subArea id, x axis,y axis details
     *         of fixture other details loads as null.
     */
    public List<Fixture> loadFixtureByFloorId(Long id) {
        logger.info("Entering loadFixtureByFloorId");
        List<Fixture> fixtures = fixtureDao.loadFixtureByFloorId(id);
        if (fixtures != null)
            logger.info("Exiting loadFixtureByFloorId -- " + fixtures.size());
        else
            logger.info("Exiting loadFixtureByFloorId -- ");
        return fixtures;
    }
    
    /**
     * Load all fixture Id's of given ballast id
     * 
     * @param id
     *            ballast id
     * @return com.ems.model.Fixture Id's collection 
     */
    public List<BigInteger> loadFixturesIdListByBallastId(Long id) {
        logger.info("Entering loadFixtureByBallastId");
        List<BigInteger> fixturesIdList = fixtureDao.loadFixturesIdListByBallastId(id);
        if (fixturesIdList != null)
            logger.info("Exiting loadFixtureByBallastId -- " + fixturesIdList.size());
        else
            logger.info("Exiting loadFixtureByBallastId -- ");
        return fixturesIdList;
    }

    public List<Fixture> loadPlacedAndCommissionedFixtureByFloorId(Long id) {
        logger.info("Entering loadPlacedAndCommissionedFixtureByFloorId");
        List<Fixture> fixtures = fixtureDao.loadPlacedAndCommissionedFixtureByFloorId(id);
        if (fixtures != null)
            logger.info("Exiting loadPlacedAndCommissionedFixtureByFloorId -- " + fixtures.size());
        else
            logger.info("Exiting loadPlacedAndCommissionedFixtureByFloorId -- ");
        return fixtures;
    }
    
    /**
     * Load fixture details.load all placed fixtures of given floor
     * 
     * @param id
     *            floor id
     * @return com.ems.model.Fixture collection load only id,sensorId,floor id,area id,subArea id, x axis,y axis details
     *         of fixture other details loads as null.
     */
    public List<Fixture> loadPlacedFixtureByFloorId(Long id) {
        logger.info("Entering loadPlacedFixtureByFloorId");
        List<Fixture> fixtures = fixtureDao.loadPlacedFixtureByFloorId(id);
        if (fixtures != null)
            logger.info("Exiting loadPlacedFixtureByFloorId -- " + fixtures.size());
        else
            logger.info("Exiting loadPlacedFixtureByFloorId -- ");
        return fixtures;
    }

    /**
     * get the fixture by state
     * 
     * @param state
     *            fixture state
     */
    public List<Fixture> loadFixtureByState(String state) {
        String fixtureState = null;
        if (state.equalsIgnoreCase(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR)) {
            fixtureState = ServerConstants.FIXTURE_STATE_COMMISSIONED_STR;
        } else if (state.equalsIgnoreCase(ServerConstants.FIXTURE_STATE_DELETED_STR)) {
            fixtureState = ServerConstants.FIXTURE_STATE_DELETED_STR;
        } else if (state.equalsIgnoreCase(ServerConstants.FIXTURE_STATE_DISCOVER_STR)) {
            fixtureState = ServerConstants.FIXTURE_STATE_DISCOVER_STR;
        } else if (state.equalsIgnoreCase(ServerConstants.FIXTURE_STATE_PLACED_STR)) {
            fixtureState = ServerConstants.FIXTURE_STATE_PLACED_STR;
        } else if (state.equalsIgnoreCase(ServerConstants.FIXTURE_STATE_VALIDATED_STR)) {
            fixtureState = ServerConstants.FIXTURE_STATE_VALIDATED_STR;
        } else {
            return null;
        }

        logger.info("Entering loadFixtureBystate");
        List<Fixture> fixtures = fixtureDao.loadFixtureByState(fixtureState);
        if (fixtures != null)
            logger.info("Exiting loadFixtureBystate -- " + fixtures.size());
        else
            logger.info("Exiting loadFixtureBystate -- ");
        return fixtures;

    }

    /**
     * Load fixture locations .load all fixtures of given floor
     * 
     * @param id
     *            floor id
     * @return com.ems.model.Fixture collection load only id,sensorId,floor id,area id,subArea id, x axis,y axis details
     *         of fixture other details loads as null.
     */
    @SuppressWarnings("rawtypes")
	public List loadFixtureLocationsByFloorId(Long id) {

        logger.info("Entering loadFixtureLocationsByFloorId");
        List fixtures = fixtureDao.loadFixtureLocationsByFloorId(id);
        if (fixtures != null) {
            logger.info("Exiting loadFixtureByFloorId -- " + fixtures.size());
        } else {
            logger.info("Exiting loadFixtureByFloorId -- ");
        }
        return fixtures;

    } // end of method loadFixtureLocationsByFloorId

    /**
     * Load fixture details.load all fixtures of given building
     * 
     * @param id
     *            building id
     * @return com.ems.model.Fixture collection load only id,sensorId,floor id,area id,subArea id, x axis,y axis details
     *         of fixture other details loads as null.
     */
    public List<Fixture> loadFixtureByBuildingId(Long id) {
        logger.info("Entering loadFixtureByBuildingId");
        List<Fixture> fixtures = fixtureDao.loadFixtureByBuildingId(id);
        logger.info("Exiting loadFixtureByBuildingId");
        return fixtures;
    }

    /**
     * Load fixture details.load all fixtures of given campus
     * 
     * @param id
     *            campus id
     * @return com.ems.model.Fixture collection load only id,sensorId,floor id,area id,subArea id, x axis,y axis details
     *         of fixture other details loads as null.
     */
    public List<Fixture> loadFixtureByCampusId(Long id) {
        logger.info("Entering loadFixtureByCampusId");
        List<Fixture> fixtures = fixtureDao.loadFixtureByCampusId(id);
        logger.info("Exiting loadFixtureByCampusId");
        return fixtures;
    }

    private void deleteGroupAndLightLevelsForFixture(Long id) {
        // First delete the light levels associated with this fixture for the associated switches
        List<GemsGroupFixture> listGemsGroupFixture = gemsGroupManager.getGemsGroupFixtureByFixture(id);

        if (listGemsGroupFixture != null) {
            for (GemsGroupFixture grpFixture : listGemsGroupFixture) {
                Switch sw = switchManager.getSwitchByGemsGroupId(grpFixture.getGroup().getId());

                if (sw == null)
                    continue;

                // Now delete the lightlevel by switch id and fixture id
                switchManager.deleteSceneLevelsForSwitch(sw.getId(), id);
            }
        }
        gemsGroupManager.deleteGemsGroupsFromFixture(id);
    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public int deleteFixtureWithoutAck(Fixture fixture) {
        int iStatus = 1;
        try {
            if (fixture == null) {
                return 3;
            }
            if (fixture.getState().equals(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR)) {
                // fixture is already commissioned so first change it to factory default
                // before deleting it
                if (logger.isDebugEnabled()) {
                    logger.debug(fixture.getId() + ": sending wireless factory because of force delete");
                }
                DeviceServiceImpl.getInstance().setWirelessFactoryDefaults(fixture, true);
            }
            deleteGroupAndLightLevelsForFixture(fixture.getId());
            FixtureCache.getInstance().invalidateDeviceCache(fixture.getId());
            fixture.setState(ServerConstants.FIXTURE_STATE_DELETED_STR);
            fixtureDao.updateState(fixture);
            Gateway gateway = fixture.getGateway();
            if (gateway != null) {
            	gateway = gatewayManager.loadGateway(gateway.getId());
                if (gateway.getNoOfSensors() > 0) {
                    gateway.setNoOfSensors(gateway.getNoOfSensors() - 1);
                    gatewayManager.updateFields(gateway);
                }
            }
        } catch (ObjectRetrievalFailureException orfe) {
            iStatus = 3;
            orfe.printStackTrace();
        }
        return iStatus;
    }

    /**
     * Delete Fixture details
     * 
     * @param id
     *            database id(primary key)
     */
    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public int deleteFixture(final Fixture fixture) {
        int iStatus = 1;
        try {
            if (fixture == null) {
                return 3;
            }
            if (fixture.getState().equals(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR)) {
                // fixture is already commissioned so first change it to factory default
                // before deleting it
                if (logger.isDebugEnabled()) {
                    logger.debug(fixture.getId() + ": sending wireless factory because of delete");
                }
                Thread oSWFDThread = new Thread(fixture.getFixtureName() + "SWFD") {
                    public void run() {
                        DeviceServiceImpl.getInstance().setWirelessFactoryDefaults(fixture, true);
                    }
                };
                oSWFDThread.start();

                try {
                    oSWFDThread.join();
                } catch (InterruptedException ie) {
                    logger.warn(fixture.getId() + ": interrupted!");
                }
                if (DeviceServiceImpl.getInstance().getSuWirelessChangeAckStatus(fixture) == false) {
                    iStatus = 2;
                }
            }
            if (iStatus == 1) {
                // switchFixtureDao.deleteSwitchFixturesForFixture(id);
                deleteGroupAndLightLevelsForFixture(fixture.getId());
                FixtureCache.getInstance().invalidateDeviceCache(fixture.getId());
                fixture.setState(ServerConstants.FIXTURE_STATE_DELETED_STR);
                fixtureDao.updateState(fixture);
                Gateway gateway = fixture.getGateway();
                if (gateway != null) {
                	gateway = gatewayManager.loadGateway(gateway.getId());
                    if (gateway.getNoOfSensors() > 0) {
                        gateway.setNoOfSensors(gateway.getNoOfSensors() - 1);
                        gatewayManager.updateFields(gateway);
                    }
                }
            }
        } catch (ObjectRetrievalFailureException orfe) {
            iStatus = 3;
            orfe.printStackTrace();
        }
        return iStatus;
    }

    /**
     * Load all fixtures within an area
     * 
     * @param id
     * @return com.ems.model.Fixture collection
     */
    public List<Fixture> loadFixtureByAreaId(Long id) {
        logger.info("Entering loadFixtureByAreaId");
        List<Fixture> fixtures = fixtureDao.loadFixtureByAreaId(id);
        logger.info("Entering loadFixtureByAreaId");
        return fixtures;
    }

    /**
     * Load all fixtures within a sub-area
     * 
     * @param id
     * @return com.ems.model.Fixture collection
     */
    public List<Fixture> loadFixtureBySubareaId(Long id) {
        logger.info("Entering loadFixtureBySubareaId");
        List<Fixture> fixtures = fixtureDao.loadFixtureBySubareaId(id);
        logger.info("Entering loadFixtureBySubareaId");
        return fixtures;
    }

    /**
     * Called during upgrade to 2.3, fixture map isn't created.
     * @param group
     */
    @CacheEvict(value = {"fixture_id", "fixture_snap"}, allEntries=true)
    public void enablePushProfileForGroup(Groups group) {
        fixtureDao.enablePushProfileForGroup(group.getId());
    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void enablePushProfileForFixture(Fixture fixture) {
        fixtureDao.enablePushProfileForFixture(fixture.getId());
    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void enablePushGlobalProfileForFixture(Fixture fixture) {
        fixtureDao.enablePushGlobalProfileForFixture(fixture.getId());
    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void resetPushProfileForFixture(Fixture fixture) {
        fixtureDao.resetPushProfileForFixture(fixture.getId());     
    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void resetPushGlobalProfileForFixture(Fixture fixture) {
        fixtureDao.resetPushGlobalProfileForFixture(fixture.getId());
    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void enablePushProfileAndGlobalPushProfile(Fixture fixture, boolean pushProfileStatus,
            boolean globalPushProfileStatus) {
        fixtureDao.enablePushProfileAndGlobalPushProfile(fixture.getId(), pushProfileStatus, globalPushProfileStatus);
    }

    public void pushProfileToFixtureNow(Long fixtureId) {
        // PUSHING REAL TIME PROFILE TO SU
        Fixture oFixture = getFixtureById(fixtureId);
        Groups group = groupManager.getGroupById(oFixture.getGroupId());
        DeviceServiceImpl.getInstance().sendCurrentProfile(group.getProfileHandler(), fixtureId.intValue());
        DeviceServiceImpl.getInstance().setGlobalProfile(fixtureId);
    }
    
    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void pushProfileToFixtureNow(Fixture fixture, int groupId) {
    	
    	assignGroupProfileToFixtureProfile(fixture, (long)groupId);
      // PUSHING REAL TIME PROFILE TO SU      
      Groups group = groupManager.getGroupById((long)groupId);
      DeviceServiceImpl.getInstance().sendCurrentProfile(group.getProfileHandler(), Integer.parseInt(fixture.getId().toString()));
      DeviceServiceImpl.getInstance().setGroupGlobalProfile(fixture.getId(), groupId);
      
    } //end of method pushProfileToFixtureNow

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public Fixture updatePosition(Fixture fixture, Integer x, Integer y, String state) {
        FixtureCache.getInstance().invalidateDeviceCache(fixture.getId());
        return fixtureDao.updatePosition(fixture.getId(), x, y, state);
    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void changeFixtureProfile(Fixture fixture, Long groupId, String currentProfile, String originalProfileFrom) {
        synchronized (this) {
            FixtureCache.getInstance().invalidateDeviceCache(fixture.getId());
            Long globalProfileHandlerId = profileManager.getGlobalProfileHandlerId();
            if (groupId.equals(0L)) {
                fixtureDao.updateProfileHandlerIdForFixture(fixture.getId(), globalProfileHandlerId, currentProfile,
                        originalProfileFrom);
            } else {
                fixtureDao.changeFixtureProfile(fixture.getId(), groupId, globalProfileHandlerId, currentProfile,
                        originalProfileFrom);
                DeviceServiceImpl.getInstance().updateGroupProfile(groupId, fixture.getId());
            }
        }
    }
    
    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void assignProfileToFixture(Fixture fixture, Long groupId) {
      synchronized (this) {
          FixtureCache.getInstance().invalidateDeviceCache(fixture.getId());       
          fixtureDao.assignProfileToFixture(fixture.getId(), groupId);
          DeviceServiceImpl.getInstance().updateGroupProfile(groupId, fixture.getId());        
      }
      
    } //end of method assignProfileToFixture
    
    @CacheEvict(value = {"fixture_id", "fixture_snap"}, allEntries=true)
    public Long bulkProfileAssignToFixture(String fixtureIdsList, Long groupId, String currentProfile) {
    	Long totalRecordUpdated = null;
        synchronized (this) {
            totalRecordUpdated = fixtureDao.bulkProfileAssignToFixture(fixtureIdsList, groupId, currentProfile);
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

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void assignGroupProfileToFixtureProfile(Fixture fixture, Long groupId) {
        fixtureDao.assignGroupProfileToFixtureProfile(fixture.getId(), groupId);
        FixtureCache.getInstance().invalidateDeviceCache(fixture.getId());
    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void upgradeFixtureProfile(ProfileHandler newPFH, Fixture fixture) {
        fixtureDao.upgradeFixtureProfile(newPFH, fixture.getId());
    }

    public void absoluteDimFixtures(int[] fixtureArr, int percentage, int time) {

        if (time == 1777) {
            // enable hopper
            enableHopper(fixtureArr[0], true);
            return;
        }
        if (time == 1778) {
            enableHopper(fixtureArr[0], false);
            return;
        }
        fixtureArr = sortFixtures(fixtureArr);
        if (logger.isDebugEnabled()) {
            logger.debug("called absolute dimfixtures with percentage -- " + percentage);
        }
        DeviceServiceImpl.getInstance().absoluteDimFixtures(fixtureArr, percentage, time);

    } // end of method absoluteDimFixtures

    public void dimFixtures(int[] fixtureArr, int percentage, int time) {
        fixtureArr = sortFixtures(fixtureArr);
        DeviceService ds = DeviceServiceImpl.getInstance();
        if (logger.isDebugEnabled()) {
            logger.debug("called dimfixtures with percentage -- " + percentage);
        }
        if (time == 37) { // temporary code to start image upgrade. if time is 37, it will start
            // image upgrade
            ImageUpgradeSO.getInstance().testStartImageUpload(fixtureArr);
        } else if (time == 73) {
            // image upgrade in loop
            ImageUpgradeSO.getInstance().startImageUploadLoop(fixtureArr);
        } else if (time == 77) {
            DeviceServiceImpl.getInstance().getCurrentVersion(fixtureArr[0]);
        } else if (time == 97) { // temporary for RDB
            DeviceServiceImpl.getInstance().startRemoteDebug(fixtureArr[0], 3, 0);
        } else if (time == 2000) {
            // 1 bit, every 1 min
            triggerMotionBits(fixtureArr, (byte) 1, (byte) 1, (byte) 1, new Date(System.currentTimeMillis()), new Date(
                    System.currentTimeMillis() + 5 * 1000 * 60));
        } else if (time == 2001) {
            // 2 bit, every 1 min
            triggerMotionBits(fixtureArr, (byte) 2, (byte) 1, (byte) 1, new Date(System.currentTimeMillis()), new Date(
                    System.currentTimeMillis() + 5 * 1000 * 60));
        } else if (time == 2002) {
            // 1 bit, every 5 min
            triggerMotionBits(fixtureArr, (byte) 1, (byte) 5, (byte) 1, new Date(System.currentTimeMillis()), new Date(
                    System.currentTimeMillis() + 5 * 1000 * 60));
        } else if (time == 2003) {
            // 2 bit, every 5 min
            triggerMotionBits(fixtureArr, (byte) 2, (byte) 5, (byte) 1, new Date(System.currentTimeMillis()), new Date(
                    System.currentTimeMillis() + 5 * 1000 * 60));
        } else if (time == 2004) {
            // stop
            triggerMotionBits(fixtureArr, (byte) 0, (byte) 5, (byte) 0, new Date(System.currentTimeMillis()), new Date(
                    System.currentTimeMillis() + 5 * 1000 * 60));
        } else {
            ds.dimFixtures(fixtureArr, percentage, time);
        }
    }

	public void dimFixture(int fixtureId, int percentage, int time) {

        if (time == 1777) {
            // enable hopper
            enableHopper(fixtureId, true);
            return;
        }
        if (time == 1778) {
            enableHopper(fixtureId, false);
            return;
        }
        DeviceServiceImpl.getInstance().absoluteDimFixture(fixtureId, percentage, time);
    }

    /**
     * return list of Gateway object associated with gateway
     * 
     * @param gatewayId
     * @return list of Gateway object associated with gateway
     */
    public List<Fixture> loadAllFixtureByGatewayId(Long gatewayId) {
        List<Fixture> fixtureList = fixtureDao.loadAllFixtureByGatewayId(gatewayId);
        if (fixtureList != null && !fixtureList.isEmpty())
            return fixtureList;
        return new ArrayList<Fixture>();
    }

    /**
     * return list of Gateway object associated with secondary gateway
     * 
     * @param secGwId
     * @return list of Gateway object associated with secondary gateway
     */
    public List<Fixture> getAllCommissionedFixturesBySecGwId(Long secGwId) {

        List<Fixture> fixtureList = fixtureDao.getAllCommissionedFixturesBySecGwId(secGwId);
        if (fixtureList != null && !fixtureList.isEmpty())
            return fixtureList;
        return new ArrayList<Fixture>();

    }

    /**
     * return list of Gateway object associated with secondary gateway
     * 
     * @param secGwId
     * @return list of Gateway object associated with secondary gateway
     */
    public List<Fixture> loadAllFixtureBySecondaryGatewayId(Long secGwId) {
        List<Fixture> fixtureList = fixtureDao.loadAllFixtureBySecondaryGatewayId(secGwId);
        if (fixtureList != null && !fixtureList.isEmpty())
            return fixtureList;
        return new ArrayList<Fixture>();
    }

    /**
     * Load fixtures available in fixture table
     * 
     * @return
     */
    public List<Fixture> loadAllFixtures() {
        return fixtureDao.loadAllFixtures();
    }

    public List<Long> loadFixturesIdWithGroupSynchFlagTrue() {
        return fixtureDao.loadFixturesIdWithGroupSynchFlagTrue();
    }

    /**
     * Load fixtures available in fixture table minus the deleted ones
     * 
     * @return
     */
    public List<Fixture> loadAllOrgFixtures() {
        return fixtureDao.loadAllOrgFixtures();
    }

    /**
     * Load fixtures who custom profile_handler points to default profile_handler.
     * 
     * @return
     */
    public List<Fixture> loadAllFixturesWithDefaultPFID() {
        return fixtureDao.loadAllFixturesWithDefaultPFID();
    }

    /**
     * get the fixture by snap address
     * 
     * @param snapAddr
     * @return the fixture by snap address
     */
    @Cacheable(value = "fixture_snap", key="#snapAddr")
    public Fixture getFixtureBySnapAddr(String snapAddr) {
        return (Fixture) fixtureDao.getFixtureBySnapAddr(snapAddr);
    } // end of method getFixtureBySnapAddr

    /**
     * get the deleted fixture by snap address
     * 
     * @param snapAddr
     * @return the deleted fixture by snap address
     */
    public Fixture getDeletedFixtureBySnapAddr(String snapAddr) {
        return (Fixture) fixtureDao.getDeletedFixtureBySnapAddr(snapAddr);
    } // end of method getDeletedFixtureBySnapAddr

    /**
     * get the fixture by ip
     * 
     * @param snapAddr
     * @return the fixture by i[
     */
    public Fixture getFixtureByIp(String ip) {
        return (Fixture) fixtureDao.getFixtureByIp(ip);
    } // end of method getFixtureByIp
    
    /**
     * get the fixture count by ballast id
     * 
     * @param ballastId
     * @return the fixture count
     */
    
    public Integer getFixtureCountByBallastId(Long ballastId) {
        return fixtureDao.getFixtureCountByBallastId(ballastId);
    } // end of method getFixtureCountByBallastId

    /**
     * get the fixture by macAddr
     * 
     * @param macAddr
     * @return the fixture by macAddr
     */
    public Fixture getFixtureByMacAddr(String macAddr) {
        return (Fixture) fixtureDao.getFixtureByMacAddr(macAddr);
    } // end of method getFixtureByIp

    public int commissionFixture(long fixtureId, int type) {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling commissionFixture API");
        }
        // DiscoverySO.getInstance().addFixtures(fixtureIds);
        int ret = DiscoverySO.getInstance().startValidation(fixtureId, type);
        if (logger.isDebugEnabled()) {
            logger.debug("Done with commissionFixture API");
        }
        FixtureCache.getInstance().invalidateDeviceCache(fixtureId);
        return ret;
    }

    public int commissionFixtures(long gatewayId, long floorId, int type) {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling bulk commissionFixtures API -- " + gatewayId);
        }
        // TODO: Needs optimization...
        List<Fixture> fixtureList = getUnCommissionedFixtureList(gatewayId);
        int ret = DiscoverySO.getInstance().startValidation(gatewayId, fixtureList, type);
        if (logger.isDebugEnabled()) {
            logger.debug("Done with bulk commissionFixtures API");
        }
        return ret;
    }
    
    
    public int commissionPlacedFixtures(Long gatewayId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling Commission PlacedFixtures API -- " + gatewayId);
        }
        int ret = DiscoverySO.getInstance().startPlacedFixturesCommission(gatewayId);
        if (logger.isDebugEnabled()) {
            logger.debug("Done with Commission PlacedFixtures API");
        }
        return ret;
    }

    // this is called when "commission and place fixture is called
    public boolean validateFixture(Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling validateFixture API");
        }
        return DiscoverySO.getInstance().commissionFixture(id);
    }

    /**
     * Will be used when called via bulk commission screen.
     */
    public boolean validateFixture(long fixtureId, long gatewayId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling validateFixture API via gateway");
        }
        return DiscoverySO.getInstance().commissionFixture(fixtureId, gatewayId);
    }

    public void startDistanceDiscovery(Long floorId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling startDistanceDiscovery API");
        }
        DiscoverySO.getInstance().startDistanceDiscovery(floorId.intValue());
        if (logger.isDebugEnabled()) {
            logger.debug("Done with startDistanceDiscovery API");
        }
    }

    public void startValidation(int floorId, long gatewayId, int type) {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling startValidation API");
        }
        // DiscoverySO.getInstance().startValidation(floorId, gatewayIdList, type);
        if (logger.isDebugEnabled()) {
            logger.debug("Done with startValidation API");
        }
    }

    public void startIdentification(int fixtureId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling startIdentification API");
        }
        DiscoverySO.getInstance().startIdentification(fixtureId);
        if (logger.isDebugEnabled()) {
            logger.debug("Done with startIdentification API");
        }
    }

    public void validationFinished(int fixtureId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling validationFinished API");
        }
        DiscoverySO.getInstance().validationFinished(fixtureId);
        if (logger.isDebugEnabled()) {
            logger.debug("Done with validationFinished API");
        }
    }

    public Boolean isDistanceDiscoveryInProgress() {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling isDistanceDiscoveryInProgress....");
        }
        boolean progress = DiscoverySO.getInstance().isDistanceDiscoveryInProgress();
        if (logger.isDebugEnabled()) {
            logger.debug("Distance discovery in progress....." + progress);
        }
        return progress;
    }

    public int getDiscoveryStatus() {
        int dStatus = DiscoverySO.getInstance().getDiscoveryStatus();
        if (logger.isDebugEnabled()) {
            logger.debug("Discovery Status: " + dStatus);
        }
        return dStatus;

    } // end of method getDiscoveryStatus

    public List<Fixture> getSortedFixtures() {

        return fixtureDao.getSortedFixtures();

    } // end of method getSortedFixtures

    @SuppressWarnings("rawtypes")
	public List getCurrentValPresenceList(int floorId) {

        List<String> list = DiscoverySO.getInstance().getCurrentValPresenceList(floorId);
        List<Fixture> fixtures = new ArrayList<Fixture>();
        for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
            String object = (String) iterator.next();
            Fixture fixture = fixtureDao.getFixtureName(object);
            if (fixture != null) {
                fixtures.add(fixture);
            }
        }
        return fixtures;

    } // end of method getCurrentValPresenceList

    @SuppressWarnings("rawtypes")
	public List getUnValidatedFixtureList(int floorId) {

        return fixtureDao.getUnValidatedFixtureList(floorId);

    }

    public List<Fixture> getUnCommissionedFixtureList(long gatewayId) {

        return fixtureDao.getUnCommissionedFixtureList(gatewayId);

    }

    public List<Fixture> getPlacedFixtureList(long gatewayId) {

        return fixtureDao.getPlacedFixtureList(gatewayId);

    }

    public String getCommissionStatus(long fixtureId) {
        return fixtureDao.getCommissionStatus(fixtureId);
    }
    
    public Integer getFixtureHopperStatus(long fixtureId) {
    	return fixtureDao.getFixtureHopperStatus(fixtureId);
    }

    /**
     * get the fixture by name
     * 
     * @param snapAddr
     * @return the fixture by name
     */
    public Fixture getFixtureName(String name) {
        return fixtureDao.getFixtureName(name);
    }

    public void auto(int[] fixtureArr) {
        if (logger.isDebugEnabled()) {
            logger.debug("inside auto...");
        }
        DeviceServiceImpl.getInstance().setAutoState(fixtureArr);

    } // end of method auto

    public void baseline(int[] fixtureArr) {
        if (logger.isDebugEnabled()) {
            logger.debug("inside baseline...");
        }
        DeviceServiceImpl.getInstance().setBaselineState(fixtureArr);

    } // end of method baseline

    public void bypass(int[] fixtureArr) {

    } // end of method bypass

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void updateStats(Fixture fixture) {
        try {
            fixtureDao.updateStats(fixture);
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end of method updateStats

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void updateState(Fixture fixture) {

        try {
            fixtureDao.updateState(fixture);
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end of method updateState

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void updateStateAndLastConnectivityTime(Fixture fixture) {

        try {
            fixtureDao.updateStateAndLastConnectivityTime(fixture);
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end of method updateState

    public int getMaxNoOfAllowedSensors() {
        return DiscoverySO.getInstance().getMaxNoOfAllowedSensors();
    }

    public boolean isDiscoveryInProgress() {
        return DiscoverySO.getInstance().isDiscoveryInProgress();
    }

    public void startNetworkDiscovery(long floorId) {

        if (logger.isDebugEnabled()) {
            logger.debug("startNetworkDiscovery for - " + floorId);
        }
        DiscoverySO.getInstance().startNetworkDiscovery(floorId, 0, ServerConstants.DEVICE_FIXTURE);

    }

    public int startNetworkDiscovery(long floorId, long gatewayId) {
        if (logger.isDebugEnabled()) {
            logger.debug("startNetworkDiscovery for - " + floorId + " via gateway: " + gatewayId);
        }
        int ret = DiscoverySO.getInstance().startNetworkDiscovery(floorId, gatewayId, ServerConstants.DEVICE_FIXTURE);
        if (logger.isDebugEnabled()) {
            logger.debug("Start Network Discovery Status: " + ret);
        }
        return ret;
    }

    public void cancelNetworkDiscovery() {

        DiscoverySO.getInstance().cancelNetworkDiscovery();

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void exitCommissioning(List gatewayIdList, List fixtureIdList) {
        int iUnCommissionedFixtures = 0;
        if (gatewayIdList != null) {
            if (fixtureIdList != null)
                iUnCommissionedFixtures = fixtureIdList.size();
            if (logger.isDebugEnabled()) {
                logger.debug("Exiting fixture commissioning process... GW (" + gatewayIdList.size()
                        + "), UnCommissioned Fixtures (" + iUnCommissionedFixtures + ")");
            }
            Integer gwId = (Integer) gatewayIdList.get(0);
            if (logger.isDebugEnabled()) {
                logger.debug("gwid == " + gwId.longValue());
            }
            DiscoverySO.getInstance().finishCommissioning(gwId, fixtureIdList);
        }
        return;
    }

    public int exitCommissioning(Long gatewayId) {
        int iUnCommissionedFixtures = 0;
        List<Fixture> fixtureList = getUnCommissionedFixtureList(gatewayId);
        if (fixtureList != null)
            iUnCommissionedFixtures = fixtureList.size();
        if (logger.isDebugEnabled()) {
            logger.debug("Exiting fixture commissioning process... GW (" + gatewayId + "), UnCommissioned Fixtures ("
                    + iUnCommissionedFixtures + ")");
            logger.debug("gwid == " + gatewayId);
        }
        return DiscoverySO.getInstance().finishCommissioning(gatewayId, fixtureList);
    }
    
    
    public int exitPlacedFixtureCommissioning(Long gatewayId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Exiting Placed fixture commissioning process... GW (" + gatewayId + ")");
            logger.debug("gwid == " + gatewayId);
        }
        return DiscoverySO.getInstance().finishPlacedFixturesCommissioning(gatewayId);
    }


    public void autoPlaceFinished(long floorId) {
        DiscoverySO.getInstance().autoPlaceFinished(floorId);

    }

    public Integer getTotalFixtureCountInSensor(Long floorId) {
        return fixtureDao.getTotalFixtureCountInSensor(floorId);
    }

    public List<String> getRDBCategoryList() {
        return RemoteDebugging.getRootList();
    }

    public List<String> getRDBSubCategoryList(String sRootCategory) {
        return RemoteDebugging.getSubSectionList(sRootCategory);
    }

    public void startRDBOnFixture(int fixtureId, int rootcategory, int subcategory) {
        if (logger.isDebugEnabled()) {
            logger.debug("inside startRDBOnFixture...");
        }
        DeviceServiceImpl.getInstance().startRemoteDebug(fixtureId, rootcategory, subcategory);
    }

    @Cacheable(value = "fixture_id", key="#id")
    public Fixture getFixtureById(Long id) {
        return fixtureDao.getFixtureById(id);
    }

    public Integer getCommType(Long fixtureId) {
        return fixtureDao.getCommType(fixtureId);
    }

    public String getIpAddress(Long fixtureId) {
        return fixtureDao.getIpAddress(fixtureId);
    }

    public void getCurrentDetails(Long fixtureId) {
        DeviceServiceImpl.getInstance().getFixtureCurrentStatus(fixtureId);
    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void updateVersion(String version, Fixture fixture, long gwId) {
        fixtureDao.updateVersion(version, fixture.getId(), gwId);
    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void updateRealtimeStats(Fixture fixture) {
        fixtureDao.updateRealtimeStats(fixture);
    }

    @CacheEvict(value = {"fixture_id", "fixture_snap"}, allEntries=true)
    public void adjustLastOccupancyTime(long floorId, int sec) {
        fixtureDao.adjustLastOccupancyTime(floorId, sec);
    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void updateBootInfo(Fixture fixture, String upgrStatus) {
        fixtureDao.updateBootInfo(fixture, upgrStatus);
    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void setImageUpgradeStatus(Fixture fixture, String status) {
        fixtureDao.setImageUpgradeStatus(fixture.getId(), status);
    } // end of method setImageUpgradeState

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void updateCurrentState(Fixture fixture, String state) {
        fixtureDao.updateCurrentState(fixture.getId(), state);
    } // end of method updateCurrentState

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void updateFirmwareVersion(String version, Fixture fixture, long gwId) {
        fixtureDao.updateFirmwareVersion(version, fixture.getId(), gwId);
    } // end of method updateFirmwareVersion

    /**
     * This function will be useful in replace a dead fixture with a new fixture at the time of Commission Process
     * 
     * @param fromFixtureId
     * @param toFixtureId
     * @return True if successfull, false otherwise.
     */
    @CacheEvict(value = {"fixture_id", "fixture_snap"}, allEntries=true)
    public boolean rmaFixture(Long fromFixtureId, Long toFixtureId) {
        Fixture oldFixture = getFixtureById(fromFixtureId);
        Fixture newFixture = getFixtureById(toFixtureId);
        String fixtureName = oldFixture.getFixtureName();
        String newFixtureName = newFixture.getFixtureName();
        String newSnapAddr = newFixture.getSnapAddress();
        String newSensorId = newFixture.getSensorId();
        String newMacAddr = newFixture.getMacAddress();
        Integer commissionStatus = newFixture.getCommissionStatus();
        if (logger.isDebugEnabled()) {
            logger.debug("RMA: From: " + fromFixtureId + "(" + fixtureName + ") => " + " To: " + toFixtureId + "("
                    + newFixtureName + ")");
        }
        // update the new node snap address/ mac address so that it is unique. this is required
        // because delete is not happening before update
        fixtureDao.replaceFixture(toFixtureId, newFixtureName, newMacAddr + "_rma", newSensorId, newSnapAddr + "_rma",
                commissionStatus, ServerConstants.FIXTURE_STATE_DELETED_STR);

        if (fixtureName.equals("Sensor" + ServerUtil.generateName(oldFixture.getSnapAddress()))) {
            // old fixture is with default name. so update with new fixture's default name
            fixtureName = newFixtureName;
        }
        // update the old fixture
        fixtureDao.replaceFixture(fromFixtureId, fixtureName, newMacAddr, newSensorId, newSnapAddr, commissionStatus,
                ServerConstants.FIXTURE_STATE_DISCOVER_STR);
        if (logger.isDebugEnabled()) {
            logger.debug("saving the old fixture with new attributes");
        }
        // delete the new fixture only if it has no data.
        if (newFixture.getLatestEnergyConsumption() == null)
            fixtureDao.removeObject(Fixture.class, newFixture.getId());
        
        //invalidate the cache for these fixture objects
        FixtureCache.getInstance().invalidateDeviceCache(toFixtureId);
        FixtureCache.getInstance().invalidateDeviceCache(fromFixtureId);
        
        
        // Validate will be called by the UI in a two step process, since the CommandScheduler picks up the old fixture
        // details
        // since the transcation isn't complete until this call is finished.
        /*
         * if(validateFixture(fromFixtureId)) { //commission of new fixture is successful with the old fixture object
         * return true; }
         */
        return true;
    } // end of method rmaFixture

    public Integer getCommissionStatus(Long fixtureId) {
        return fixtureDao.getCommissionStatus(fixtureId);
    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void updateCommissionStatus(Fixture fixture, int status) {
        fixtureDao.updateCommissionStatus(fixture.getId(), status);
    }

    @CacheEvict(value = {"fixture_id", "fixture_snap"}, allEntries=true)
    public void updateCommissionStatus(int[] fixtureIds, int status) {
        fixtureDao.updateCommissionStatus(fixtureIds, status);
    }

    /**
     * Sort Fixtures based on X & Y co-ordinates
     * 
     * @param fixtureArr
     *            input fixtureid array
     * @return sorted fixtureid array
     */
    public int[] sortFixtures(int[] fixtureArr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Begin sorting...");
        }
        StringBuffer oBuffer = new StringBuffer();
        int noOfFixtures = fixtureArr.length;
        List<Fixture> fixtureList = new ArrayList<Fixture>();
        for (int i = 0; i < noOfFixtures; i++) {
            Fixture fixture = getFixtureById(new Long(fixtureArr[i]));
            if (fixture == null) {
                logger.error(fixtureArr[i] + ": There is no Fixture");
                continue;
            }
            fixtureList.add(fixture);
        }
        Collections.sort(fixtureList, new FixtureSorter());
        if (fixtureList.size() > 0) {
            fixtureArr = null;
            fixtureArr = new int[fixtureList.size()];
            Fixture oFixture = null;
            for (int count = 0; count < fixtureList.size(); count++) {
                oFixture = (Fixture) fixtureList.get(count);
                fixtureArr[count] = oFixture.getId().intValue();
                // oBuffer.append("(").append(oFixture.getId()).append(")=>").append(oFixture.getFixtureName()).append(" ");
            }
            fixtureList.clear();
            fixtureList = null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(oBuffer.toString());
            logger.debug("End sorting...");
        }
        return fixtureArr;
    }

    /**
     * Sorts fixture based on their co-ordinates.
     * 
     * @author yogesh
     * 
     */
    class FixtureSorter implements Comparator<Fixture> {

        public int compare(Fixture o1, Fixture o2) {
            switch (ServerMain.getInstance().getSortPath()) {
            default:
            case ServerConstants.TOP_TO_BOTTOM:
                if (o1.getYaxis() > o2.getYaxis()) {
                    return -1;
                } else if (o1.getYaxis() < o2.getYaxis()) {
                    return 1;
                }
                break;
            case ServerConstants.BOTTOM_TO_TOP:
                if (o1.getYaxis() > o2.getYaxis()) {
                    return 1;
                } else if (o1.getYaxis() < o2.getYaxis()) {
                    return -1;
                }
                break;
            case ServerConstants.RIGHT_TO_LEFT:
                if (o1.getXaxis() > o2.getXaxis()) {
                    return -1;
                } else if (o1.getXaxis() < o2.getXaxis()) {
                    return 1;
                }
                break;
            case ServerConstants.LEFT_TO_RIGHT:
                if (o1.getXaxis() > o2.getXaxis()) {
                    return 1;
                } else if (o1.getXaxis() < o2.getXaxis()) {
                    return -1;
                }
                break;
            }
            return 0;
        }

    }

    public int getCommissioningStatus() {

        return DiscoverySO.getInstance().getCommissioningStatus();

    } // end of method getCommissioningStatus

    public void enableHopper(long fixtureId, boolean enable) {

        DeviceServiceImpl.getInstance().enableHopper(fixtureId, enable);

    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void updateFixtureVersionSyncedState(Fixture fixture) {
        fixtureDao.updateFixtureVersionSyncedState(fixture);
    }

    public List<OutageBasePower> getAllOutageBasePowerList() {

        return fixtureDao.getAllOutageBasePowerList();

    } // end of method getAllOutageBasePowerList

    public List<OutageBasePower> getFixtureOutageBasePowerList(long fixtureId) {

        return fixtureDao.getFixtureOutageBasePowerList(fixtureId);

    } // end of method getFixtureOutageBasePowerList

    public void saveOutageBasePower(OutageBasePower outageBasePower) {

        fixtureDao.saveObject(outageBasePower);

    } // end of method saveOutageBasePower

    public OutageBasePower getOutageBasePower(long fixtureId, short voltLevel) {

        return fixtureDao.getOutageBasePower(fixtureId, voltLevel);

    } // end of method getOutageBasePower

    public List<Fixture> getAllCommissionedFixtureList() {

        return fixtureDao.getAllCommissionedFixtureList();

    }

    public void restoreImage(long fixtureId) {

        DeviceServiceImpl.getInstance().rebootFixture(fixtureId, 1);

    } // end of method restoreImage

    /**
     * Fetch fixture count by the property association
     * 
     * @param property
     * @param pid
     * @return Integer count of fixtures.
     */
    public Long getFixtureCount(String property, Long pid) {
        return fixtureDao.getFixtureCount(property, pid);
    }
    
    public Long getFixtureCountByTemplateId(Long templateId) {
    	return fixtureDao.getFixtureCountByTemplateId(templateId);
    }

    public FixtureList loadFixtureList(String property, Long pid, String order, String orderWay, Boolean bSearch,
            String searchField, String searchString, String searchOper, int offset, int limit) {
        FixtureList fl =  fixtureDao.loadFixtureList(property, pid, order, orderWay, bSearch, searchField, searchString,
                searchOper, offset, limit);
        if(fl.getFixture() != null) {
        	for(Fixture f: fl.getFixture()) {
        		setRealTimeStats(f);
        	}
        }
        return fl;
    }

    public FixtureList loadFixtureListWithSpecificAttrs(String property, Long pid, String order, String orderWay,
            Boolean bSearch, String searchField, String searchString, String searchOper, int offset, int limit) {
        FixtureList fl = fixtureDao.loadFixtureListWithSpecificAttrs(property, pid, order, orderWay, bSearch, searchField,
                searchString, searchOper, offset, limit);
        if(fl.getFixture() != null) {
        	for(Fixture f: fl.getFixture()) {
        		setRealTimeStats(f);
        	}
        }
        return fl;
    }

    public List<Fixture> getFixtureOutFixtureList(String property, Long pid) {
        return fixtureDao.getFixtureOutFixtureList(property, pid);
    }

    public List<FixtureOutageVO> getFixtureOutageList(String property, Long pid) {
        return fixtureDao.getFixtureOutageList(property, pid);
    }
    
    public Map<Integer, Object[]> getRecentFixtureDetails() {
        return fixtureDao.getRecentFixtureDetails();
    }

    @SuppressWarnings("rawtypes")
	public List getAllSensorData(long captureDate) {

        return fixtureDao.getAllSensorData(captureDate);

    } // end of method getAllSensorData

    @SuppressWarnings("rawtypes")
	public List getSensorData(long fixtureId, long captureDate) {

        return fixtureDao.getSensorData(fixtureId, captureDate);

    } // end of method getAllSensorData

    public List<Fixture> loadAllFixtureByFloorId(Long id) {
        return fixtureDao.loadAllFixtureByFloorId(id);
    }

    public List<Fixture> loadHoppersBySecGwId(long gwId) {

        return fixtureDao.loadHoppersBySecGwId(gwId);

    } // end of method loadHoppersBySecGwId

    public void triggerMotionBits(int[] fixtureArr, byte bitLevel, byte frequency, byte motion_detection_interval,
            Date startTime, Date endTime) {
        DeviceServiceImpl.getInstance().sendMotionBitCommand(fixtureArr,
                ServerConstants.SU_TRIGGER_MOTION_BIT_MSG_TYPE, bitLevel, frequency, motion_detection_interval,
                startTime, endTime);
    }

    public FixtureCustomGroupsProfile loadCustomGroupByFixureId(Long fixtureId) {
        return fixtureDao.loadCustomGroupByFixureId(fixtureId);
    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void syncFixtureCustomProfile(ProfileHandler oNewPFH, Fixture fixture, long groupId) {
        FixtureCache.getInstance().invalidateDeviceCache(fixture.getId());
        fixtureDao.syncFixtureCustomProfile(oNewPFH, fixture.getId(), groupId);
    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public Long assignSUGroupProfileToFixture(Fixture fixture, byte profileno) {
        // Fetch the tenant id (currently set to null)
        Long groupId = groupManager.getGroupByProfileAndTenantDetails(profileno, null);
        Groups oGroup = groupManager.getGroupById(groupId);
        if (oGroup != null) {
            logger.info("Group no: " + oGroup.getId() + ", " + oGroup.getName());
            FixtureCache.getInstance().invalidateDeviceCache(fixture.getId());
            return fixtureDao.assignSUGroupProfileToFixture(fixture.getId(), oGroup);
        }
        return 0L; // profileno should match
    }

    public int getProfileNoForFixture(Fixture fixture) {
    	return groupManager.getGroupById(fixture.getGroupId()).getProfileNo().intValue();
    }

    public List<Fixture> loadFixtureByTemplateId(Long id) {
        logger.info("Entering loadFixtureByTemplateId");
        List<Fixture> fixtures = fixtureDao.loadFixtureByTemplateId(id);
        logger.info("Exiting loadFixtureByTemplateId");
        return fixtures;
    }

    @CacheEvict(value = {"fixture_id", "fixture_snap"}, allEntries=true)
    public void changeGroupsSyncPending(Fixture fixture) {
        fixtureDao.changeGroupsSyncPending(fixture);
    } // end of method changeGroupsSyncPending

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void changeGroupsSyncPending(Fixture fixture, boolean bEnable) {
        fixtureDao.changeGroupsSyncPending(fixture.getId(), bEnable);
    }
    
    public void calibrateFixtures(int[] fixtureArr) {
    	DeviceServiceImpl.getInstance().calibrateFixtures(fixtureArr);
    }

    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public Fixture updateAreaID(Fixture fixture) {
        try {
            return (Fixture) fixtureDao.updateAreaID(fixture);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Updated on the return Ack for the hopper command from the SU.
     * @param fixtureId
     * @param isHopper
     */
    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void updateHopperState(Fixture fixture, Integer isHopper) {
    	logger.debug(fixture.getId() + ": Hooper (" + isHopper + ")");
    	Fixture dbFx = getFixtureById(fixture.getId());
    	dbFx.setIsHopper(isHopper);
    	fixtureDao.update(dbFx);       
    }
    
    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void updateFixtureType(Fixture fixture, Integer fxType) {
    	logger.debug(fixture.getId() + ": FxType (" + fxType + ")");
    	Fixture dbFx = getFixtureById(fixture.getId());
    	dbFx.setFixtureType(fxType);
    	fixtureDao.update(dbFx);     
    }

    /**
     * Goal is to reset the fixture commission status, put fixtures in auto and then take them in validation mode.
     * 
     * @param fixtureList
     * @param gw
     */
    @CacheEvict(value = {"fixture_id", "fixture_snap"}, allEntries=true)
    public void unStrobeFixture(List<Fixture> fixtureList, Long gatewayId) {
        int noOfFixtures = fixtureList.size();
        int[] discoveredSelectedFixturesArr = new int[noOfFixtures];
        for (int i = 0; i < noOfFixtures; i++) {
            int fixtureId = fixtureList.get(i).getId().intValue();
            discoveredSelectedFixturesArr[i] = fixtureId;
        }

        // Update commissioned status to unknown (this will put the selected strobed fixtues back to the bottom list
        updateCommissionStatus(discoveredSelectedFixturesArr, ServerConstants.COMMISSION_STATUS_UNKNOWN);
        // send auto command to these fixtures, this will bring the end sensor out of validation mode
        auto(discoveredSelectedFixturesArr);
        ServerUtil.sleep(3);
        //Now remove the  strobed cache list, so that we honor the storbing of the fixture here.
        for (int i = 0; i < noOfFixtures; i++) {
            int fixtureId = fixtureList.get(i).getId().intValue();
            DiscoverySO.getInstance().unStrobeFixture(fixtureId);
        }
        
        // put sensors in validation mode
        DeviceServiceImpl.getInstance().sendValidationCmd(fixtureList, discoveredSelectedFixturesArr, 0, 3, gatewayId);
    }

    /**
     * Goal is to identify the selected fixture by taking it into a dimming cycle and then put it in validation mode.
     * 
     * @param fixture
     */
    
    @Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
    public void identifyFixture(Fixture fixture) {
        int[] discoveredSelectedFixturesArr = new int[1];
        discoveredSelectedFixturesArr[0] = fixture.getId().intValue();
        // Update commissioned status to unknown (this will put the selected strobed fixtues back to the bottom list
        updateCommissionStatus(discoveredSelectedFixturesArr, ServerConstants.COMMISSION_STATUS_UNKNOWN);
        // FULL OFF
        DeviceServiceImpl.getInstance().absoluteDimFixture(fixture.getId().intValue(), 0, 0);
        ServerUtil.sleep(3);
        // FULL ON
        DeviceServiceImpl.getInstance().absoluteDimFixture(fixture.getId().intValue(), 100, 0);
        ServerUtil.sleep(3);
        // put selected sensor in validation mode
        Fixture fx = getFixtureById(fixture.getId());
        List<Fixture> fxList = new ArrayList<Fixture>();
        fxList.add(fx);
        
        DeviceServiceImpl.getInstance().sendValidationCmd(fxList, discoveredSelectedFixturesArr, 0, 3, fx.getGateway().getId());
//        DeviceServiceImpl.getInstance().sendValidationCmd(fixtureId.intValue(), 0, ServerConstants.VAL_DETECT_MOTION_LIGHT_TYPE);
    }

	public List<Fixture> get1_0CommissionedDrReactiveFixtureList() {
		return fixtureDao.get1_0CommissionedDrReactiveFixtureList();
	}

	public Integer getFixtureCountByBulbId(Long id) {
		return fixtureDao.getFixtureCountByBulbId(id);
	}

	public List<FixtureLampStatusVO> getLampOutStatusFixtureList(String property, Long pid) {
        return fixtureDao.getLampOutStatusFixtureList(property, pid);
    }
	
	public List<FixtureLampStatusVO> getFixtureOutStatusFixtureList(String property, Long pid) {
        return fixtureDao.getFixtureOutStatusFixtureList(property, pid);
    }
	
	public List<FixtureLampStatusVO> getCalibratedFixtureList(String property, Long pid) {
        return fixtureDao.getCalibratedFixtureList(property, pid);
    }
	public int[] getUnCalibratedFixtureList() {
        return fixtureDao.getUnCalibratedFixtureList();
    }
	
	public List<Sensor> getDimLevels() {
		return fixtureDao.getDimLevels();
	}
	
	public List<Sensor> getFixtureOutages() {
		return fixtureDao.getFixtureOutages();
	}
	
	public List<Sensor> getLastOccupancy() {
		return fixtureDao.getLastOccupancy();
	}
	
	public List<Sensor> getRealTimeStats() {
		return fixtureDao.getRealTimeStats();
	}

	public FixtureLampStatusVO getOutageTypeByFixtureId(long fixtureId) {
		return fixtureDao.getOutageTypeByFixtureId(fixtureId);
	}
	
	@Caching(evict = {@CacheEvict (value = "fixture_id", key="#fixture.id"),
			@CacheEvict (value = "fixture_snap", key="#fixture.snapAddress")	})
	public void setUseFxCurveFlag(Fixture fixture,Boolean flag) {
		fixtureDao.setUseFxCurveFlag(fixture.getId(),flag);
	}
	public int[] getFixtureIdsListUsingBallastCurve(Long ballastId,Double voltage) {
		return fixtureDao.getFixtureIdsListUsingBallastCurve(ballastId,voltage);
	}
		
	@CacheEvict(value = {"fixture_id", "fixture_snap"}, allEntries=true)
	public void updateFixtureSecGw(List<Fixture> fixtures, Gateway gw) {
		Iterator<Fixture> itr = fixtures.iterator();
		while (itr.hasNext()) {
			Fixture fixture = (Fixture) itr.next();
			Fixture fxObj = getFixtureById(fixture.getId());
			fxObj.setGateway(gw);
			fxObj.setSecGwId(gw.getId());
		    save(fxObj);
			FixtureCache.getInstance().invalidateDeviceCache(fxObj.getId());
		}

	}
	public HashMap<String, Long> getFixturesCountByModelNo()
    {
	    //Merge the NULL model no into SU2 and also Put Fixture Type like SU2, Ruggedized, Compact into Map
	    List<Object[]> inventoryReportSensorList = fixtureDao.getFixturesCountByModelNo();
	    Long su2Count=(long) 0;
	    Long totalFixtures=(long) 0;
	    Long compactSuCount = (long) 0;
	    Long ruggedizedSuCount = (long) 0;
	    Long unIdentifiedSuCount = (long) 0;
	    HashMap<String, Long> totalCommissionedSensors = new HashMap<String, Long>();
	    if (inventoryReportSensorList != null && !inventoryReportSensorList.isEmpty()) {
    	    for (Iterator<Object[]> iterator = inventoryReportSensorList.iterator(); iterator.hasNext();) {
                Object[] object = (Object[]) iterator.next();
                String modelNo = (String) object[0];
                Long count = ((BigInteger) object[1]).longValue();
                if(modelNo!=null)
                {
                    if(modelNo.startsWith("SU-2"))
                    {
                        su2Count+=count;
                    }else if(modelNo.startsWith("RS"))
                    {
                    	ruggedizedSuCount+=count;
                       
                    }else if(modelNo.startsWith("SU-3E"))
                    {
                        compactSuCount+=count;
                    }
                    else
                    {
                        unIdentifiedSuCount+=count;
                    }
                }else
                {
                    unIdentifiedSuCount+=count;
                }
                totalFixtures+=count;
             }
    	   
    	    totalCommissionedSensors.put("SU2", su2Count);
    	    totalCommissionedSensors.put("Unidentified SU", unIdentifiedSuCount);
    	    totalCommissionedSensors.put("Ruggedized", ruggedizedSuCount);
            totalCommissionedSensors.put("Compact", compactSuCount);
    	    totalCommissionedSensors.put("TotalCount", totalFixtures);
	   }
	   return totalCommissionedSensors;
    }
	public List<Object[]>  getCusCountByVersionNo()
	{
	    List<Object[]> inventoryReportCusList = fixtureDao.getCusCountByVersionNo();
	    return inventoryReportCusList;
	}
	
	@CacheEvict(value = {"fixture_id", "fixture_snap"}, allEntries=true)
	public Response getFixturesBaseline(List<Fixture> fixtures, Byte calibration)
	{
	    StringBuilder fixtString = new StringBuilder("");
        int[] fixtureList = new int[fixtures.size()];
        int count = 0;
        Iterator<Fixture> itr = fixtures.iterator();
        
        LampCalibrationConfiguration lcc = fixtureCalibrationManager.getCalibrationConfiguration();
        short warmuptime = 5000;
        short stablizationtime = 5000;
        if (lcc != null) {
            warmuptime = lcc.getWarmupTime().shortValue();
            stablizationtime = lcc.getStabilizationTime().shortValue();
        }
        
        while (itr.hasNext()) {
            Fixture fixture = (Fixture) itr.next();
            fixtureList[count++] = fixture.getId().intValue();
            
            setUseFxCurveFlag(fixture, true);
            FixtureLampCalibration flc_orig = fixtureCalibrationManager.getFixtureCalibrationMapByFixtureId(fixture.getId());
            if (flc_orig == null && calibration.intValue()==1) {
                flc_orig = new FixtureLampCalibration();
                flc_orig.setFixtureId(fixture.getId());
                flc_orig.setCaptureAt(new Date());
                flc_orig.setInitial(true);
                flc_orig.setWarmupTime(warmuptime);
                flc_orig.setStabilizationTime(stablizationtime);
                try {
                    fixtureCalibrationManager.save(flc_orig);
                } catch (Exception e) {
                    logger.warn("Error saving fixture lamp calibration " + e.getMessage());
                }  
            }
            FixtureCache.getInstance().invalidateDeviceCache(fixture.getId().longValue());
            try {
                if (FixtureCache.getInstance().getDevice(fixture.getId()) != null) {
                    fixtString.append(FixtureCache.getInstance().getDevice(fixture.getId()).getFixtureName() + ",");
                } else {
                    if (fixture.getId() != null) {
                        fixtString.append(fixture.getId() + ",");
                    }
                }
            } catch (Exception e) {
                logger.warn("Error during baselining " + e.getMessage());
            }

        }
        DeviceServiceImpl.getInstance().getFixtureBaseLine(fixtureList, calibration, warmuptime, stablizationtime);
       
        if(calibration.intValue()==1){
            userAuditLoggerUtil.log("Initiate power usage characterization for fixtures " + fixtString, UserAuditActionType.Initiate_Power_Usage_Characterization.getName());
        }else{
            userAuditLoggerUtil.log("Retrieve Power Usage characterization for fixtures " + fixtString, UserAuditActionType.Retrieve_Power_Usage_Characterization.getName());
        }
        return new Response();
	}
	
	public void setFlushMode(FlushMode mode) {
		fixtureDao.getSession().setFlushMode(mode);
	}
	
	public void flush() {
		fixtureDao.getSession().flush();
	}
	
	@CacheEvict(value = {"fixture_id", "fixture_snap"}, allEntries=true)
	public void editFixtureBallast(Ballast ballast,List<BigInteger> fixturesIdList) {
		fixtureDao.editFixtureBallast(ballast, fixturesIdList);
	}
	
	@CacheEvict(value = {"fixture_id", "fixture_snap"}, allEntries=true)
	public void updateFixtureClassChanges(Long id, Long ballastId, Long bulbId, Integer noOfBallasts, Short voltage, Integer lampNum) {
		fixtureDao.updateFixtureClassChanges(id, ballastId, bulbId, noOfBallasts, voltage, lampNum);
	}
	
	@CacheEvict(value = {"fixture_id", "fixture_snap"}, allEntries=true)
	public Long bulkFixtureTypessignToFixture(String fixtureIdsList,
			Long currentFixturetypeId) {
		Long totalRecordUpdated = null;
        synchronized (this) {
            totalRecordUpdated = fixtureDao.bulkFixtureTypessignToFixture(fixtureIdsList, currentFixturetypeId);
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
	
	@CacheEvict(value = {"fixture_id", "fixture_snap"}, allEntries = true)
	public void fixtureProfileUpgrade(Long oldGroupId, Long newGroupId,String currentGroupName) {
		fixtureDao.fixtureProfileUpgrade(oldGroupId, newGroupId, currentGroupName);
	}
	
	@CacheEvict(value = {"fixture_id", "fixture_snap"}, allEntries = true)
	public void resetAllFixtureGroupSyncFlag() {
		fixtureDao.resetAllFixtureGroupSyncFlag() ;
	}
	
	public Integer getFixtureCountByFixtureClassId(Long id) {
		return fixtureDao.getFixtureCountByFixtureClassId(id);
	}
	
	public List<Fixture> getFixtureListByFixtureClassId(Long id) {
		return fixtureDao.getFixtureListByFixtureClassId(id);
	}
	
	@CacheEvict(value = {"fixture_id", "fixture_snap"}, allEntries = true)
    public void updateGroupFixtureSyncPending(Long gemsGroupId, boolean bEnable) {
        fixtureDao.updateGroupFixtureSyncPending(gemsGroupId, bEnable);
    }
	
}
