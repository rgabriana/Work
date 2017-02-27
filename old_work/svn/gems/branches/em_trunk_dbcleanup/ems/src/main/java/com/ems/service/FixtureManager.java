package com.ems.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.cache.FixtureCache;
import com.ems.dao.FixtureDao;
import com.ems.dao.GatewayDao;
import com.ems.dao.GemsGroupDao;
import com.ems.dao.GroupDao;
import com.ems.dao.ProfileDao;
import com.ems.model.Ballast;
import com.ems.model.BallastVoltPower;
import com.ems.model.Bulb;
import com.ems.model.Fixture;
import com.ems.model.FixtureCurrentData;
import com.ems.model.Gateway;
import com.ems.model.Groups;
import com.ems.model.OutageBasePower;
import com.ems.model.ProfileHandler;
import com.ems.server.RemoteDebugging;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.device.DeviceService;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.discovery.DiscoverySO;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.server.util.ServerUtil;
import com.ems.vo.model.FixtureList;
import com.ems.vo.model.FixtureOutageVO;

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
    private GroupDao groupDao;

    @Resource
    private ProfileDao profileDao;

    @Resource
    private GatewayDao gatewayDao;
    
    @Resource
    private GemsGroupDao gemsGroupDao;
    
	//@Resource
	//private SwitchFixtureDao switchFixtureDao;

    /**
     * save Fixture details.
     * 
     * @param floor
     *            com.ems.model.Fixture
     */
    public Fixture save(Fixture fixture) {
        return (Fixture) fixtureDao.saveObject(fixture);
    }

    /**
     * update Fixture details.
     * 
     * @param floor
     *            com.ems.model.Fixture
     */
    public Fixture update(Fixture fixture) {
        try {
            if (fixture.getIsHopper() == 0) {
                enableHopper(fixture.getId(), false);
            } else {
                enableHopper(fixture.getId(), true);
            }
            return (Fixture) fixtureDao.update(fixture);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * update Fixture details.
     */
    public Fixture updateFixture(Fixture fixture) {
        try {
            if (fixture.getIsHopper() == null)
                fixture.setIsHopper(0);
            
            if (fixture.getIsHopper() == 0) {
                enableHopper(fixture.getId(), false);
            } else {
                enableHopper(fixture.getId(), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        FixtureCache.getInstance().invalidateDeviceCache(fixture.getId());
        return fixtureDao.updateFixture(fixture);
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
     * get the fixture by state
     * 
     * @param state fixture state
     */
    public  List<Fixture> loadFixtureByState(String state) {
    	String fixtureState = null ;
		if(state.equalsIgnoreCase(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR))
		{
			fixtureState = ServerConstants.FIXTURE_STATE_COMMISSIONED_STR ;
		}
		else if(state.equalsIgnoreCase(ServerConstants.FIXTURE_STATE_DELETED_STR))
		{
			fixtureState = ServerConstants.FIXTURE_STATE_DELETED_STR ;
		}
		else if(state.equalsIgnoreCase(ServerConstants.FIXTURE_STATE_DISCOVER_STR))
		{
			fixtureState = ServerConstants.FIXTURE_STATE_DISCOVER_STR ;
		}
		else if(state.equalsIgnoreCase(ServerConstants.FIXTURE_STATE_PLACED_STR))
		{
			fixtureState = ServerConstants.FIXTURE_STATE_PLACED_STR ;
		}
		else if(state.equalsIgnoreCase(ServerConstants.FIXTURE_STATE_VALIDATED_STR))
		{
			fixtureState = ServerConstants.FIXTURE_STATE_VALIDATED_STR ;
		}
		else
		{
			return null ;
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
    public List loadFixtureLocationsByFloorId(Long id) {
        
      logger.info("Entering loadFixtureLocationsByFloorId");
      List fixtures = fixtureDao.loadFixtureLocationsByFloorId(id);
      if (fixtures != null) {
	logger.info("Exiting loadFixtureByFloorId -- " + fixtures.size());
      } else {
	logger.info("Exiting loadFixtureByFloorId -- ");       
      }
      return fixtures;
      
    } //end of method loadFixtureLocationsByFloorId

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

    public int deleteFixtureWithoutAck(Long id) {
        int iStatus = 1;
        try {
            final Fixture fixture = (Fixture) fixtureDao.getObject(Fixture.class, id);
            if (fixture == null) {
                return 3;
            }
            if (fixture.getState().equals(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR)) {
                // fixture is already commissioned so first change it to factory default
                // before deleting it
                logger.debug(id + ": sending wireless factory because of force delete");
                DeviceServiceImpl.getInstance().setWirelessFactoryDefaults(fixture, true);
            }
            FixtureCache.getInstance().invalidateDeviceCache(fixture.getId());
            if (fixture.getLatestEnergyConsumption() == null)
                fixtureDao.removeObject(Fixture.class, id);
            else {
                fixture.setState(ServerConstants.FIXTURE_STATE_DELETED_STR);
                fixtureDao.updateState(fixture);
                Gateway gateway = fixture.getGateway();
                if (gateway != null) {
                    if (gateway.getNoOfSensors() > 0)
                        gateway.setNoOfSensors(gateway.getNoOfSensors() - 1);
                    gatewayDao.updateFields(gateway);
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
    public int deleteFixture(Long id) {
        int iStatus = 1;
        try {
            final Fixture fixture = (Fixture) fixtureDao.getObject(Fixture.class, id);
            if (fixture == null) {
                return 3;
            }
            if (fixture.getState().equals(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR)) {
                // fixture is already commissioned so first change it to factory default
                // before deleting it
                logger.debug(id + ": sending wireless factory because of delete");
                Thread oSWFDThread = new Thread(fixture.getFixtureName() + "SWFD") {
                    public void run() {
                        DeviceServiceImpl.getInstance().setWirelessFactoryDefaults(fixture, true);
                    }
                };
                oSWFDThread.start();

                try {
                    oSWFDThread.join();
                } catch (InterruptedException ie) {
                    logger.warn(id + ": interrupted!");
                }
                if (DeviceServiceImpl.getInstance().getSuWirelessChangeAckStatus(fixture) == false) {
                    iStatus = 2;
                }
            }
            if (iStatus == 1) {
            	//switchFixtureDao.deleteSwitchFixturesForFixture(id);
            	gemsGroupDao.deleteGemsGroupsFromFixture(id);
            	FixtureCache.getInstance().invalidateDeviceCache(fixture.getId());
                if (fixture.getLatestEnergyConsumption() == null)
                    fixtureDao.removeObject(Fixture.class, id);
                else {
                    fixture.setState(ServerConstants.FIXTURE_STATE_DELETED_STR);
                    fixtureDao.updateState(fixture);
                    Gateway gateway = fixture.getGateway();
                    if (gateway != null) {
                        if (gateway.getNoOfSensors() > 0)
                            gateway.setNoOfSensors(gateway.getNoOfSensors() - 1);
                        gatewayDao.updateFields(gateway);
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
     * Marks the fixture as deleted when the fixture as some history.
     * 
     * @param id
     * @deprecated deleteFixture will take care of marking the fixture as deleted if required.
     */
    public void markFixtureDeleted(Long id) {
        logger.debug(id + ": markFixtureDeleted");
        Fixture fixture = null;
        try {
            fixture = (Fixture) fixtureDao.getObject(Fixture.class, id);
        } catch (ObjectRetrievalFailureException orfe) {
            orfe.printStackTrace();
        }
        if (fixture != null) {
            fixture.setState(ServerConstants.FIXTURE_STATE_DELETED_STR);
            fixtureDao.updateState(fixture);
            Gateway gateway = fixture.getGateway();
            if (gateway != null) {
                if (gateway.getNoOfSensors() > 0)
                    gateway.setNoOfSensors(gateway.getNoOfSensors() - 1);
                gatewayDao.updateFields(gateway);
            }
        }
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

    public void updateProfileHandler(ProfileHandler profileHandler) {
        fixtureDao.updateProfileHandler(profileHandler);
    }

    public void enablePushProfileForFixture(Long fixtureId) {
        fixtureDao.enablePushProfileForFixture(fixtureId);
        FixtureCache.getInstance().getDevice(fixtureId).getFixture().setPushProfile(true);
    }

    public void enablePushGlobalProfileForFixture(Long fixtureId) {
        fixtureDao.enablePushGlobalProfileForFixture(fixtureId);
        FixtureCache.getInstance().getDevice(fixtureId).getFixture().setPushGlobalProfile(true);
    }

    public void resetPushProfileForFixture(Long fixtureId) {
        fixtureDao.resetPushProfileForFixture(fixtureId);
        FixtureCache.getInstance().getDevice(fixtureId).getFixture().setPushProfile(false);
    }

    public void resetPushGlobalProfileForFixture(Long fixtureId) {
        fixtureDao.resetPushGlobalProfileForFixture(fixtureId);
        FixtureCache.getInstance().getDevice(fixtureId).getFixture().setPushGlobalProfile(false);
    }

	public void enablePushProfileAndGlobalPushProfile(Long fixtureId, boolean pushProfileStatus, boolean globalPushProfileStatus) {
		fixtureDao.enablePushProfileAndGlobalPushProfile(fixtureId,pushProfileStatus,globalPushProfileStatus);	
		FixtureCache.getInstance().getDevice(fixtureId).getFixture().setPushProfile(pushProfileStatus);
		FixtureCache.getInstance().getDevice(fixtureId).getFixture().setPushGlobalProfile(globalPushProfileStatus);
	}
	
    public void updateProfileHandler(ProfileHandler profileHandler, Long fixtureId, Long groupId,
            String currentProfile, String originalProfileFrom) {
        Long globalProfileHandlerId = profileDao.getGlobalProfileHandlerId();
        System.out.println("Global: " + globalProfileHandlerId + ", fixture: " + fixtureId + ", group: " + groupId
                + ", curr: " + currentProfile + ", origin: " + originalProfileFrom);

        if (groupId == 0) {
            // Setting the groupId to 0 explicitly for Custom profiles
            updateGroupId(fixtureId, groupId, currentProfile, originalProfileFrom);
            profileHandler.setProfileGroupId((byte) 0);
        }
        ProfileHandler copyHandler = null;
        if (profileHandler.getId().equals(globalProfileHandlerId)) {
            System.out.println("updateProfileHandler: [G] - " + globalProfileHandlerId + ", [P] -"
                    + profileHandler.getId() + ", groupId: " + groupId);
            copyHandler = profileHandler.copy();
            profileDao.saveProfileHandler(copyHandler);
            fixtureDao.updateProfileHandler(copyHandler, fixtureId, groupId, currentProfile, originalProfileFrom);
        } else {
            fixtureDao.enablePushProfileForFixture(fixtureId);
            copyHandler = profileDao.saveProfileHandler(profileHandler);
        }
        if (copyHandler != null)
            DeviceServiceImpl.getInstance().sendCustomProfile(copyHandler, fixtureId.intValue());

        // HACK: Will have to send the advance profile as well, otherwise the profile checksums will mismatch, since the
        // profile group id will be set to 0 and this not sent in the schedule packet.
        if (groupId == 0)
            DeviceServiceImpl.getInstance().setGlobalProfile(fixtureId);
    }

    public void updateAdvancedProfile(Fixture fixture) {
        // Setting the groupId to 0 explicitly for Custom profiles
        String currentProfile = fixture.getCurrentProfile();
        String originalProfileFrom = fixture.getOriginalProfileFrom();
        System.out.println("Fixture: " + fixture.getId() + ", Group: " + fixture.getGroupId() + ", current Profile: "
                + currentProfile + ", Origin Profile: " + originalProfileFrom);
        ProfileHandler profileHandler = fixture.getProfileHandler();
        if (currentProfile.equals(ServerConstants.CUSTOM_PROFILE)) {
            updateGroupId(fixture.getId(), 0, currentProfile, originalProfileFrom);
            profileHandler.setProfileGroupId((byte) 0);
        } else {
            updateGroupId(fixture.getId(), 0, ServerConstants.CUSTOM_PROFILE, currentProfile);
            profileHandler.setProfileGroupId((byte) 0);
        }

        Long globalProfileHandlerId = profileDao.getGlobalProfileHandlerId();
        if (profileHandler.getId().equals(globalProfileHandlerId)) {
            ProfileHandler copyProfileHandler = profileHandler.copy();
            profileDao.saveProfileHandler(copyProfileHandler);
            fixture.setProfileHandler(copyProfileHandler);
            fixtureDao.saveOrUpdateFixture(fixture);
        } else {
            profileDao.saveProfileHandler(profileHandler);
        }
        DeviceServiceImpl.getInstance().setGlobalProfile(fixture.getId());
    }

    public void updateAdvancedProfile(ProfileHandler profileHandler, Long groupId) {
        Long globalProfileHandlerId = profileDao.getGlobalProfileHandlerId();
        ProfileHandler copyProfileHandler = null;
        if (profileHandler.getId().equals(globalProfileHandlerId)) {
            Groups group = (Groups) groupDao.getObject(Groups.class, groupId);
            copyProfileHandler = profileHandler.copy();
            profileDao.saveProfileHandler(copyProfileHandler);
            group.setProfileHandler(copyProfileHandler);
            groupDao.saveObject(group);
        } else {
            copyProfileHandler = profileDao.saveProfileHandler(profileHandler);
        }
        DeviceServiceImpl.getInstance().updateGroupProfile(copyProfileHandler, groupId);
    }

    public ProfileHandler getProfileHandlerByFixtureId(Long fixtureId) {
        return fixtureDao.getProfileHandlerByFixtureId(fixtureId);
    }

    public Fixture updatePosition(Long fixtureId, Integer x, Integer y, String state) {

      FixtureCache.getInstance().invalidateDeviceCache(fixtureId);
        return fixtureDao.updatePosition(fixtureId, x, y, state);
    }

    public void changeFixtureProfile(Long fixtureId, Long groupId, String currentProfile, String originalProfileFrom) {
        synchronized (this) {
          FixtureCache.getInstance().invalidateDeviceCache(fixtureId);
            Long globalProfileHandlerId = profileDao.getGlobalProfileHandlerId();
            if (groupId.equals(0L)) {
                fixtureDao.updateProfileHandlerIdForFixture(fixtureId, globalProfileHandlerId, currentProfile,
                        originalProfileFrom);
            } else {
                fixtureDao.changeFixtureProfile(fixtureId, groupId, globalProfileHandlerId, currentProfile,
                        originalProfileFrom);
                DeviceServiceImpl.getInstance().updateGroupProfile(groupId, fixtureId.longValue());
            }
        }
    }

    public void assignGroupProfileToFixtureProfile(Long fixtureId, Long groupId) {
        fixtureDao.assignGroupProfileToFixtureProfile(fixtureId, groupId);
        FixtureCache.getInstance().invalidateDeviceCache(fixtureId);
    }

    public void syncFixtureProfile(ProfileHandler newPFH, Long fixtureId) {
        fixtureDao.syncFixtureProfile(newPFH, fixtureId);
        FixtureCache.getInstance().invalidateDeviceCache(fixtureId);
    }

    public void upgradeFixtureProfile(ProfileHandler newPFH, Long fixtureId) {
        fixtureDao.upgradeFixtureProfile(newPFH, fixtureId);
    }

    public ProfileHandler getProfileHandlerByGroupId(Long groupId) {
        return fixtureDao.getProfileHandlerByGroupId(groupId);
    }

    public void saveFixtures(List<Fixture> fixtures) {
        fixtureDao.updateFixtures(fixtures);
    }

    public void absoluteDimFixtures(int[] fixtureArr, int percentage, int time) {

      if(time == 1777) {
        //enable hopper
        enableHopper(fixtureArr[0], true);
        return;
      }
      if(time == 1778) {
        enableHopper(fixtureArr[0], false);
        return;
      }
        fixtureArr = sortFixtures(fixtureArr);
        DeviceService ds = DeviceServiceImpl.getInstance();
        System.out.println("called absolute dimfixtures with percentage -- " + percentage);
        DeviceServiceImpl.getInstance().absoluteDimFixtures(fixtureArr, percentage, time);

    } // end of method absoluteDimFixtures

    public void dimFixtures(int[] fixtureArr, int percentage, int time) {
        fixtureArr = sortFixtures(fixtureArr);
        DeviceService ds = DeviceServiceImpl.getInstance();
        System.out.println("called dimfixtures with percentage -- " + percentage);
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
        }else if (time == 2000) {
            // 1 bit, every 1 min
            triggerMotionBits(fixtureArr, (byte)1, (byte)1, (byte)1, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()+5*1000*60));
        }else if (time == 2001) {
            // 2 bit, every 1 min
            triggerMotionBits(fixtureArr, (byte)2, (byte)1, (byte)1, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()+5*1000*60));
        }else if (time == 2002) {
            // 1 bit, every 5 min
            triggerMotionBits(fixtureArr, (byte)1, (byte)5, (byte)1, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()+5*1000*60));
        }else if (time == 2003) {
            // 2 bit, every 5 min
            triggerMotionBits(fixtureArr, (byte)2, (byte)5, (byte)1, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()+5*1000*60));
        }else if (time == 2004) {
            // stop
            triggerMotionBits(fixtureArr, (byte)0, (byte)5, (byte)0, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()+5*1000*60));
        } else {          
            ds.dimFixtures(fixtureArr, percentage, time);
        }       
    }


    public void dimFixture(int fixtureId, int percentage) {
      
        DeviceServiceImpl.getInstance().absoluteDimFixture(fixtureId, percentage, 0);
        ArrayList fixtList = new ArrayList();
        fixtList.add(new Long(fixtureId));
        fixtureDao.updateDimmerControl(fixtList, new Integer(percentage));

    }

    public void dimFixture(int fixtureId, int percentage, int time) {

      if(time == 1777) {
        //enable hopper
        enableHopper(fixtureId, true);
        return;
      }
      if(time == 1778) {
        enableHopper(fixtureId, false);
        return;
      }
        DeviceServiceImpl.getInstance().absoluteDimFixture(fixtureId, percentage, time);
        ArrayList fixtList = new ArrayList();
        fixtList.add(new Long(fixtureId));
        fixtureDao.updateDimmerControl(fixtList, new Integer(percentage));
    }

    /**
     * update dimmerControl for all ids passed in ids List
     * 
     * @param ids
     *            collection of ids
     * @param dimmerControl
     *            new dimmerControl value
     */
    public void updateDimmerControl(List<Long> ids, Integer dimmerControl) {
        // fixtureDao.updateDimmerControl(ids, dimmerControl);
    }

    @SuppressWarnings("unchecked")
    public List getAllFixtures() {
        return fixtureDao.getAllFixtures();
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
     * return list of fixture objects associated with secondary gateway
     * 
     * @param secGwId
     * @return list of fixture objects associated with secondary gateway
     */
    public List<Fixture> getAllCommissionedFixturesWithCurrentDataBySecGwId(Long secGwId) {
      
        List<Fixture> fixtureList = fixtureDao.getAllCommissionedFixturesWithCurrentDataBySecGwId(secGwId);
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

    /**
     * Load fixture current details available in fixture current data table
     * 
     * @return
     */
    public HashMap<Long, FixtureCurrentData> loadAllFixtureCurrentData() {
        return fixtureDao.loadAllFixtureCurrentData();
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
     * get the fixture by macAddr
     * 
     * @param macAddr
     * @return the fixture by macAddr
     */
    public Fixture getFixtureByMacAddr(String macAddr) {

        return (Fixture) fixtureDao.getFixtureByMacAddr(macAddr);

    } // end of method getFixtureByIp

    /**
     * get the fixture by id
     * 
     * @param id
     * @return the fixture by id
     */
    public Fixture getFixtureById(long id) {
        return fixtureDao.getFixtureById(id);
    }

    /**
     * get the fixture details by id
     * 
     * @param id
     * @return the list with fixture and its energyconsumption by id
     */
    // public List<Object> getFixtureDetailsById(long id) {
    // return fixtureDao.getFixtureDetailsById(id);
    // }

    @SuppressWarnings("unchecked")
    public int commissionFixture(long fixtureId, int type) {
      if(logger.isDebugEnabled()) {  
	logger.debug("Calling commissionFixture API");
      }
        // DiscoverySO.getInstance().addFixtures(fixtureIds);
        int ret = DiscoverySO.getInstance().startValidation(fixtureId, type);
        if(logger.isDebugEnabled()) {
          logger.debug("Done with commissionFixture API");
        }
        FixtureCache.getInstance().invalidateDeviceCache(fixtureId);
        return ret;
    }

    @SuppressWarnings("unchecked")
    public int commissionFixtures(long gatewayId, long floorId, int type) {
      if(logger.isDebugEnabled()) {
        logger.debug("Calling bulk commissionFixtures API -- " + gatewayId);
      }
        //TODO: Needs optimization...
        List<Fixture> fixtureList = getUnCommissionedFixtureList(gatewayId);
        int ret = DiscoverySO.getInstance().startValidation(gatewayId, fixtureList, type);
        if(logger.isDebugEnabled()) {
          logger.debug("Done with bulk commissionFixtures API");
        }
        return ret;
    }

    // this is called when "commission and place fixture is called
    public boolean validateFixture(Long id) {
        System.out.println("Calling validateFixture API");
        return DiscoverySO.getInstance().commissionFixture(id);
    }

    /**
     * Will be used when called via bulk commission screen.
     */
    public boolean validateFixture(long fixtureId, long gatewayId) {
        System.out.println("Calling validateFixture API via gateway");
        return DiscoverySO.getInstance().commissionFixture(fixtureId, gatewayId);
    }

    public void startDistanceDiscovery(Long floorId) {
        System.out.println("Calling startDistanceDiscovery API");
        DiscoverySO.getInstance().startDistanceDiscovery(floorId.intValue());
        System.out.println("Done with startDistanceDiscovery API");
    }

    public void startValidation(int floorId, long gatewayId, int type) {
        System.out.println("Calling startValidation API");
        // DiscoverySO.getInstance().startValidation(floorId, gatewayIdList, type);
        System.out.println("Done with startValidation API");
    }

    public void startIdentification(int fixtureId) {
        System.out.println("Calling startIdentification API");
        DiscoverySO.getInstance().startIdentification(fixtureId);
        System.out.println("Done with startIdentification API");
    }

    public void validationFinished(int fixtureId) {
        System.out.println("Calling validationFinished API");
        DiscoverySO.getInstance().validationFinished(fixtureId);
        System.out.println("Done with validationFinished API");
    }

    public Boolean isDistanceDiscoveryInProgress() {
        System.out.println("Calling isDistanceDiscoveryInProgress....");
        boolean progress = DiscoverySO.getInstance().isDistanceDiscoveryInProgress();
        System.out.println("Distance discovery in progress....." + progress);
        return progress;
    }

    public int getDiscoveryStatus() {
        int dStatus = DiscoverySO.getInstance().getDiscoveryStatus();
        logger.debug("Discovery Status: " + dStatus);
        return dStatus;

    } // end of method getDiscoveryStatus

    public List<Fixture> getSortedFixtures() {

        return fixtureDao.getSortedFixtures();

    } // end of method getSortedFixtures

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
    public List getUnValidatedFixtureList(int floorId) {

        return fixtureDao.getUnValidatedFixtureList(floorId);

    }

    @SuppressWarnings("unchecked")
    public List<Fixture> getUnCommissionedFixtureList(long gatewayId) {

        return fixtureDao.getUnCommissionedFixtureList(gatewayId);

    }

    public String getCommissionStatus(long fixtureId) {
        return fixtureDao.getCommissionStatus(fixtureId);
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

    public List<Ballast> getAllBallasts() {
        return fixtureDao.getAllBallasts();
    }

    public List<Bulb> getAllBulbs() {
        return fixtureDao.getAllBulbs();
    }

    public void upgradeFixtures(int[] fixtureIds) {
        System.out.println("no. of upgrade fixtures --" + fixtureIds.length);
        for (int i = 0; i < fixtureIds.length; i++) {
            System.out.println("fixture id -- " + fixtureIds[i]);
        }
    }

    public void auto(int[] fixtureArr) {
        System.out.println("inside auto...");
        DeviceServiceImpl.getInstance().setAutoState(fixtureArr);

    } // end of method auto

    public void baseline(int[] fixtureArr) {
        System.out.println("inside baseline...");
        DeviceServiceImpl.getInstance().setBaselineState(fixtureArr);

    } // end of method baseline

    public void bypass(int[] fixtureArr) {

    } // end of method bypass

    public Ballast getBallastById(long id) {

        return fixtureDao.getBallastById(id);

    } // end of method getBallast

    public Bulb getBulbById(long id) {

        return fixtureDao.getBulbById(id);

    } // end of method getBulb

    public void assignGateway(Fixture fx) {
        fixtureDao.assignGateway(fx);
    }
    
    public void saveFixtureCurrentData(FixtureCurrentData currData) {
      
      try {
        fixtureDao.save(currData);
      } catch (Exception e) {
        e.printStackTrace();
      }
      //return null;
      
    } //end of method saveFixtureCurrentData

    public void updateStats(Fixture fixture) {

        try {
            fixtureDao.updateStats(fixture);
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end of method updateStats

    public void updateState(Fixture fixture) {

        try {
            fixtureDao.updateState(fixture);
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

        System.out.println("startNetworkDiscovery for - " + floorId);
        DiscoverySO.getInstance().startNetworkDiscovery(floorId, 0, ServerConstants.DEVICE_FIXTURE);

    }

    public int startNetworkDiscovery(long floorId, long gatewayId) {
        System.out.println("startNetworkDiscovery for - " + floorId + " via gateway: " + gatewayId);
        int ret = DiscoverySO.getInstance().startNetworkDiscovery(floorId, gatewayId, ServerConstants.DEVICE_FIXTURE);
        logger.debug("Start Network Discovery Status: " + ret);
        return ret;
    }

    public void cancelNetworkDiscovery() {

        DiscoverySO.getInstance().cancelNetworkDiscovery();

    }

    public void exitCommissioning(List gatewayIdList, List fixtureIdList) {
        int iUnCommissionedFixtures = 0;
        if (gatewayIdList != null) {
            if (fixtureIdList != null)
                iUnCommissionedFixtures = fixtureIdList.size();
            System.out.println("Exiting fixture commissioning process... GW (" + gatewayIdList.size()
                    + "), UnCommissioned Fixtures (" + iUnCommissionedFixtures + ")");
            Integer gwId = (Integer) gatewayIdList.get(0);
            System.out.println("gwid == " + gwId.longValue());
            int ret = DiscoverySO.getInstance().finishCommissioning(gwId, fixtureIdList);
        }
        return;
    }
    
    public int exitCommissioning(Long gatewayId) {
        int iUnCommissionedFixtures = 0;
        List<Fixture> fixtureList = getUnCommissionedFixtureList(gatewayId);
        if (fixtureList != null)
            iUnCommissionedFixtures = fixtureList.size();
        System.out.println("Exiting fixture commissioning process... GW (" + gatewayId
                + "), UnCommissioned Fixtures (" + iUnCommissionedFixtures + ")");
        System.out.println("gwid == " + gatewayId);
        return DiscoverySO.getInstance().finishCommissioning(gatewayId, fixtureList);
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
        System.out.println("inside startRDBOnFixture...");
        DeviceServiceImpl.getInstance().startRemoteDebug(fixtureId, rootcategory, subcategory);
    }

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

    public void updateVersion(String version, long id, long gwId) {
        fixtureDao.updateVersion(version, id, gwId);
    }

    public void updateFixturePlc(long id, String ipAddr, String version) {
        fixtureDao.updateFixturePlc(id, ipAddr, version);
    }

    public void updateRealtimeStats(Fixture fixture) {
        fixtureDao.updateRealtimeStats(fixture);
    }

    public void adjustLastOccupancyTime(long floorId, int sec) {
        fixtureDao.adjustLastOccupancyTime(floorId, sec);
    }

    public List<BallastVoltPower> getAllBallastVoltPowers() {
        return fixtureDao.getAllBallastVoltPowers();
    }

    public List<BallastVoltPower> getAllBallastVoltPowersFromId(long voltPowerMapId) {
        return fixtureDao.getAllBallastVoltPowersFromId(voltPowerMapId);
    }

    public void setFactoryDefault(long fixtureId) {
        System.out.println(fixtureId + ": Called set factory default");
    }

    public void updateBootInfo(Fixture fixture, String upgrStatus) {
        fixtureDao.updateBootInfo(fixture, upgrStatus);
    }

    public void updateGroupId(long id, long groupId) {
        fixtureDao.updateGroupId(id, groupId);
    }

    public void updateGroupId(long id, long groupId, String currentProfile, String originalProfileFrom) {
        fixtureDao.updateGroupId(id, groupId, currentProfile, originalProfileFrom);
    }

    public void setImageUpgradeStatus(long fixtureId, String status) {
        fixtureDao.setImageUpgradeStatus(fixtureId, status);
    } // end of method setImageUpgradeState

    public void updateCurrentState(Long id, String state) {
        fixtureDao.updateCurrentState(id, state);
    } // end of method updateCurrentState
    
    public void updateFirmwareVersion(String version, long id, long gwId) {

        fixtureDao.updateFirmwareVersion(version, id, gwId);

    } // end of method updateFirmwareVersion

    /**
     * This function will be useful in replace a dead fixture with a new fixture at the time of Commission Process
     * 
     * @param fromFixtureId
     * @param toFixtureId
     * @return True if successfull, false otherwise.
     */
    public boolean rmaFixture(Long fromFixtureId, Long toFixtureId) {
        Fixture oldFixture = getFixtureById(fromFixtureId);
        Fixture newFixture = getFixtureById(toFixtureId);
        String fixtureName = oldFixture.getFixtureName();
        String newFixtureName = newFixture.getFixtureName();
        String newSnapAddr = newFixture.getSnapAddress();
        String newSensorId = newFixture.getSensorId();
        String newMacAddr = newFixture.getMacAddress();
        Integer commissionStatus = newFixture.getCommissionStatus();
        System.out.println("RMA: From: " + fromFixtureId + "(" + fixtureName + ") => " + " To: " + toFixtureId + "("
                + newFixtureName + ")");
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
        System.out.println("saving the old fixture with new attributes");
        // delete the new fixture only if it has no data.
        if (newFixture.getLatestEnergyConsumption() == null)
            fixtureDao.removeObject(Fixture.class, newFixture.getId());

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

    public void updateCommissionStatus(Long fixtureId, int status) {
        fixtureDao.updateCommissionStatus(fixtureId, status);
    }

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
        logger.debug("Begin sorting...");
        StringBuffer oBuffer = new StringBuffer();
        int noOfFixtures = fixtureArr.length;
        List<Fixture> fixtureList = new ArrayList<Fixture>();
        for (int i = 0; i < noOfFixtures; i++) {
            Fixture fixture = getFixtureById(fixtureArr[i]);
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
        logger.debug(oBuffer.toString());
        logger.debug("End sorting...");
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

    public Bulb loadBulb(Long id) {
        return fixtureDao.loadBulb(id);
    }

    public Ballast loadBallast(Long id) {
        return fixtureDao.loadBallast(id);
    }
    
    /**
     * Fetch fixture count by the property association
     * @param property
     * @param pid
     * @return Integer count of fixtures.
     */
    public Long getFixtureCount(String property, Long pid) {
        return fixtureDao.getFixtureCount(property, pid);
    }
    
    public FixtureList loadFixtureList(String property, Long pid, String order, String orderWay, Boolean bSearch,
            String searchField, String searchString, String searchOper, int offset, int limit) {
        return fixtureDao.loadFixtureList(property, pid, order, orderWay, bSearch, searchField, searchString, searchOper, offset, limit);
    }
    
    public FixtureList loadFixtureListWithSpecificAttrs(String property, Long pid, String order, String orderWay, Boolean bSearch,
            String searchField, String searchString, String searchOper, int offset, int limit) {
        return fixtureDao.loadFixtureListWithSpecificAttrs(property, pid, order, orderWay, bSearch, searchField, searchString, searchOper, offset, limit);
    }
    
    public List<Fixture> getFixtureOutFixtureList(String property, Long pid) {
        return fixtureDao.getFixtureOutFixtureList(property, pid);
    }
    
    public List<FixtureOutageVO> getFixtureOutageList(String property, Long pid) {
    	 return fixtureDao.getFixtureOutageList(property, pid);
	}
    
    public void clearBallastVoltPowersMap() {
    	fixtureDao.clearBallastVoltPowersMap();
    }
    
    public Map<Integer, Object[]> getRecentFixtureDetails() {
        return fixtureDao.getRecentFixtureDetails();
    }
    public List getAllSensorData(long captureDate) {
  	
  	return fixtureDao.getAllSensorData(captureDate);
  	
    } //end of method getAllSensorData
  
    public List getSensorData(long fixtureId, long captureDate) {
  	
  	return fixtureDao.getSensorData(fixtureId, captureDate);
  	
    } //end of method getAllSensorData
    
    public List<Fixture> loadAllFixtureByFloorId(Long id) {
    	return fixtureDao.loadAllFixtureByFloorId(id);
    }

    public List<Fixture> loadHoppersBySecGwId(long gwId) {
      
      return fixtureDao.loadHoppersBySecGwId(gwId);
      
    } //end of method loadHoppersBySecGwId
    
    public void triggerMotionBits(int[] fixtureArr, byte bitLevel, byte frequency, byte motion_detection_interval, Date startTime, Date endTime) {
        DeviceServiceImpl.getInstance().sendMotionBitCommand(fixtureArr,
                ServerConstants.SU_TRIGGER_MOTION_BIT_MSG_TYPE, bitLevel, frequency, motion_detection_interval, startTime, endTime);
    }

    public void updateSecGwId(long fixtureId, long gwId) {
      
      fixtureDao.updateSecGwId(fixtureId, gwId);
      
    } //end of method updateSecGwId

    public void updateIsHopper(long fixtureId, int isHopper, long gwId) {
      
      fixtureDao.updateIsHopper(fixtureId, isHopper, gwId);
      
    } //end of method updateIsHopper

    public void updateVersionSynced(long fixtureId, int versionSynced) {
      
      fixtureDao.updateVersionSynced(fixtureId, versionSynced);
      
    } //end of method updateVersionSynced

	public void addVoltPowerMap(long voltMapId, HashMap<Double, Double> curveMap) {
      
      fixtureDao.addVoltPowerMap(voltMapId, curveMap);
      
    } //end of method addVoltPowerMap
    
    public long getMaxVoltPowerMapId() {
      
      return fixtureDao.getMaxVoltPowerMapId();
      
    } //end of method getMaxVoltPowerMapId
    
    public void updateBallastVoltPowerMapId(long ballastId, long voltMapId) {
      
      fixtureDao.updateBallastVoltPowerMapId(ballastId, voltMapId);
      
    } //end of method updateBallastVoltPowerMapId
    
    public long getMaxFixtureCurrentDataId() {
      
      return fixtureDao.getMaxFixtureCurrentDataId();
      
    } //end of method getMaxFixtureCurrentDataId
    
}
