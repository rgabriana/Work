package com.ems.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.FixtureDao;
import com.ems.dao.GatewayDao;
import com.ems.dao.GemsGroupDao;
import com.ems.dao.GemsGroupPlugloadDao;
import com.ems.dao.PlugloadSceneLevelDao;
import com.ems.dao.SceneDao;
import com.ems.dao.SceneLevelDao;
import com.ems.dao.SwitchDao;
import com.ems.dao.SwitchGroupDao;
import com.ems.dao.UserDao;
import com.ems.dao.WdsDao;
import com.ems.model.ButtonManipulation;
import com.ems.model.Fixture;
import com.ems.model.Floor;
import com.ems.model.Gateway;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.GemsGroupPlugload;
import com.ems.model.Plugload;
import com.ems.model.PlugloadSceneLevel;
import com.ems.model.Scene;
import com.ems.model.SceneLevel;
import com.ems.model.SceneLightLevelTemplate;
import com.ems.model.SceneTemplate;
import com.ems.model.Switch;
import com.ems.model.SwitchGroup;
import com.ems.model.SystemConfiguration;
import com.ems.model.User;
import com.ems.model.Wds;
import com.ems.server.ServerConstants;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.util.ServerUtil;
import com.ems.types.DeviceType;
import com.ems.types.GGroupType;
import com.ems.types.RoleType;
import com.ems.utils.ArgumentUtils;
import com.ems.vo.model.SwitchDetail;

@Service("switchManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SwitchManager {

    static final Logger logger = Logger.getLogger("SwitchLogger");
    
    @Resource
    UserManager userManager;

    @Resource
    private SwitchDao switchDao;

    @Resource
    private UserDao userDao;

    @Resource
    private SceneDao sceneDao;

    @Resource
    private SceneLevelDao sceneLevelDao;

    @Resource
    private FixtureDao fixtureDao;

    @Resource
    private GatewayDao gatewayDao;


    @Resource
    private GemsGroupDao gemsGroupDao;
    
    @Resource
    private GemsGroupPlugloadDao gemsGroupPlugloadDao;

    @Resource
    private WdsManager wdsManager;

    @Resource
    private WdsDao wdsDao;

    @Resource
    private SwitchGroupDao switchGroupDao;

    @Resource
    private GemsGroupManager gemsGroupManager;
    @Resource
    private GemsPlugloadGroupManager gemsPlugloadGroupManager;

    @Resource
    private SystemConfigurationManager sysMgr;

    @Resource
    private PlugloadSceneLevelDao plugloadSceneLevelDao;
    
    public SwitchGroup getSwitchGroup(Long id)
    {
    	return switchGroupDao.getSwitchGroupByGemsGroupId(id);   	
    }

    public void deleteSwitch(Long id) {
        List<Scene> scenes = this.loadSceneBySwitchId(id);
        if (!ArgumentUtils.isNullOrEmpty(scenes)) {
            for (Scene scene : scenes) {
                deleteScene(scene.getId());
            }
        }
        Long groupId = getSwitchById(id).getGemsGroup().getId();
        SwitchGroup switchGroup = switchGroupDao.getSwitchGroupByGemsGroupId(groupId);
        switchGroupDao.removeObject(SwitchGroup.class, switchGroup.getId());
        gemsGroupDao.deleteGemsGroup(groupId);
        switchDao.removeObject(Switch.class, id);
    }

    public List getFloorData(Long id) {
        List<Object> list = new ArrayList<Object>();
        List<Fixture> fixturelist = fixtureDao.loadFixtureByFloorId(id);
        if (fixturelist != null) {
            list.addAll(fixturelist);
        }
        List<Switch> switchlist = switchDao.loadSwitchByFloorId(id);
        if (switchlist != null) {
            list.addAll(switchlist);
        }
        List<Gateway> gatewaylist = gatewayDao.loadFloorGateways(id);
        if (gatewaylist != null) {
            list.addAll(gatewaylist);
        }
        return list;
    }

    public Switch getSwitchById(long id) {
        return (Switch) switchDao.getObject(Switch.class, id);
    }

    public Switch getSwitchName(String name, int time) {
        Switch switchval = null;
        for (int i = 0; i < time; i++) {
            switchval = switchDao.getswitchName(name);
        }
        return switchval;
    }

    public List<Switch> loadSwitchByBuildingId(Long id) {
        return switchDao.loadSwitchByBuildingId(id);
    }

    public List<Switch> loadSwitchByCampusId(Long id) {
        return switchDao.loadSwitchByCampusId(id);
    }

    public List<Switch> loadSwitchByFloorId(Long id) {
        return switchDao.loadSwitchByFloorId(id);
    }

    public List<Switch> loadSwitchByAreaId(Long id) {
        return switchDao.loadSwitchByAreaId(id);
    }

    public List<Switch> loadAllSwitches() {
        return switchDao.loadAllSwitches();
    }

    public Switch updateAreaID(Switch switchval) {
        try {
            return (Switch) switchDao.updateAreaID(switchval);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteScene(Long id) {
        Scene oScene = sceneDao.getSceneById(id);
        if (oScene != null) {
            Long switchId = oScene.getSwitchId();
            Integer deletedSceneOrderId = oScene.getSceneOrder();
            List<SceneLevel> levels = this.loadSceneLevelsBySceneId(id);
            if (levels != null) {
                for (SceneLevel sl : levels) {
                    sceneLevelDao.removeObject(SceneLevel.class, sl.getId());
                }
            }
            
            //Remove plugload lightlevel
            List<PlugloadSceneLevel> levels1 = this.loadPlugloadSceneLevelsBySceneId(id);
            if (levels1 != null) {
                for (PlugloadSceneLevel sl : levels1) {
                	plugloadSceneLevelDao.removeObject(PlugloadSceneLevel.class, sl.getId());
                }
            }
            
            sceneDao.removeObject(Scene.class, id);
            sceneDao.updateSceneOrder(switchId, deletedSceneOrderId);
        }
    }

    public List<Fixture> loadFixturesBySwitchId(Long id) {
        /*
         * List<Fixture> list = switchFixtureDao.loadFixturesbySwitchId(id); return list;
         */
        return null;
    }

    
    public List<Plugload> loadPlugloadsBySwitchId(Long switchId) {
    
    	Switch oSwitch = switchDao.getSwitchById(switchId);
        if (oSwitch == null) {
            return null;
        }
        SwitchGroup oSwitchGroup = switchDao.loadSwitchGroupByGemsGroupId(oSwitch.getGemsGroup().getId());
        if (oSwitchGroup == null) {
            return null;
        }
        List<GemsGroupPlugload> list = gemsGroupDao.getGemsGroupPlugloadByGroup(oSwitchGroup.getGemsGroup().getId());
        final List<Plugload> plugloads = new ArrayList<Plugload>();
        if(list != null){
        	for(final GemsGroupPlugload gemsGroupPlugLoad : list){
        		final Plugload plugload = gemsGroupPlugLoad.getPlugload();
        		if(plugload != null){
        			plugloads.add(plugload);
        		}
        	}
        }
        return plugloads;
    }
    
    
    public List<Scene> loadSceneBySwitchId(Long id) {
        // TODO Auto-generated method stub
        return sceneDao.loadSceneBySwitchId(id);
    }

    public List<Scene> loadSceneInCreationOrderBySwitchId(Long id) {
        return sceneDao.loadSceneInCreationOrderBySwitchId(id);
    }

    public Scene loadScenebyNameAndSwitchId(String name, Long id) {
        return sceneDao.getSceneByNameandId(name, id);
    }

    public List<SceneLevel> loadSceneLevelsBySceneId(Long id) {
        return sceneLevelDao.loadLevelsBySceneId(id);
    }

    public List<SceneLevel> loadSceneLevelsBySwitchId(Long id) {
        return sceneLevelDao.loadLevelsBySwitchId(id);
    }

    public List<Scene> loadScenesByFixtureId(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    public int countSwitchAttachedToFixturebyFixtureId(Long id) {
        return 0;// switchFixtureDao.countSwitchAttachedToFixturebyFixtureId(id);
    }

    public List<SceneLevel> loadLevelsBySwitchAndFixtureId(Long switchId, Long fixtureId) {
        return sceneLevelDao.loadLevelsBySwitchAndFixtureId(switchId, fixtureId);
    }

    public List<SceneLevel> loadLevelsBySwitchAndSceneId(Long switchId, Long sceneId) {
        return sceneLevelDao.loadLevelsBySwitchAndSceneId(switchId, sceneId);
    }

    public void runScene(Long id) {
        // TODO Auto-generated method stub

    }

    public Scene saveScene(Scene scene) {
        return (Scene) sceneDao.saveObject(scene);
    }

    public Scene saveScene(Switch switchval, String sceneName) {
        Scene scene = new Scene();
        scene.setName(sceneName);
        Switch currSwitch = this.loadSwitchByNameandFloorId(switchval.getName(), switchval.getFloorId());
        while (currSwitch == null) {
            currSwitch = this.loadSwitchByNameandFloorId(switchval.getName(), switchval.getFloorId());
        }
        Long id = currSwitch.getId();
        scene.setSwitchId(id);

        return (Scene) sceneDao.saveObject(scene);
    }

    public void saveSceneLevel(List<SceneLevel> sceneLevels, Long switchId) {

        Set<Long> currentSwitchSceneFixtureIds = loadSwitchSceneFixtures(switchId);
        for (SceneLevel sl : sceneLevels) {
            SceneLevel level = sceneLevelDao.loadLevelBySceneSwitchAndFixtureId(sl.getSceneId(), sl.getSwitchId(),
                    sl.getFixtureId());
            if (level == null) {
                sceneDao.saveObject(sl);
            }
            currentSwitchSceneFixtureIds.remove(sl.getFixtureId());
        }
        for (Long fid : currentSwitchSceneFixtureIds) {
            List<SceneLevel> scenefixturelevels = sceneLevelDao.loadLevelsBySwitchAndFixtureId(switchId, fid);
            if (scenefixturelevels != null && scenefixturelevels.size() > 0) {
                for (SceneLevel sl : scenefixturelevels) {
                    sceneLevelDao.removeObject(SceneLevel.class, sl.getId());
                }
            }
        }

    }

    public Switch saveSwitch(Switch switchval) {
        if (switchval.getId() != null && switchval.getId() == 0) {
            switchval.setId(null);
        }

        return (Switch) switchDao.saveObject(switchval);
    }

    /*
     * public void reassignSwitchFixturesList(List<SwitchFixtures> switchFixture) { if (switchFixture != null &&
     * switchFixture.size() > 0) { Long switchId = switchFixture.get(0).getSwitchId(); List<SwitchFixtures>
     * currentSwitchFixtures = loadSwitchFixturesBySwitchId(switchId); Set<Long> currentSwitchFixtureIds = new
     * HashSet<Long>(); if (currentSwitchFixtures != null && currentSwitchFixtures.size() > 0) { for (SwitchFixtures
     * currentSF : currentSwitchFixtures) { currentSwitchFixtureIds.add(currentSF.getFixtureId()); } } for
     * (SwitchFixtures swtFxtr : switchFixture) { if (!currentSwitchFixtureIds.contains(swtFxtr.getFixtureId())) {
     * switchFixtureDao.saveObject(swtFxtr); } currentSwitchFixtureIds.remove(swtFxtr.getFixtureId()); } for (Long
     * fixtureId : currentSwitchFixtureIds) { SwitchFixtures sf =
     * switchFixtureDao.loadSwitchFixturebySwitchIdFixtureId(switchId, fixtureId); if (sf != null) {
     * switchFixtureDao.removeObject(SwitchFixtures.class, sf.getId()); } } } }
     * 
     * public SwitchFixtures saveSwitchFixtures(Switch switchval, Fixture fixture) { SwitchFixtures switchFixtures = new
     * SwitchFixtures(); Switch currSwitch = this.loadSwitchByNameandFloorId(switchval.getName(),
     * switchval.getFloorId()); while (currSwitch == null) { currSwitch =
     * this.loadSwitchByNameandFloorId(switchval.getName(), switchval.getFloorId()); } Long switchId =
     * currSwitch.getId(); switchFixtures.setSwitchId(switchId); //
     * switchFixtures.setSwitchId(this.getSwitchName(name).getId());
     * 
     * switchFixtures.setFixtureId(fixture.getId());
     * 
     * return (SwitchFixtures) switchFixtureDao.saveObject(switchFixtures); }
     */
    public Long getLastSwitchId() {
        return (Long) switchDao.getLastSwitchId();
    }

    public Switch updatePosition(Switch switchval) {
        return switchDao.updatePosition(switchval.getName(), switchval.getXaxis(), switchval.getYaxis());
    }

    public Switch updateLocation(Switch switchval) {
        return switchDao.updateLocation(switchval);
    }

    public Switch getSwitchToMove(Integer x) {
        return switchDao.getSwitchToMove(x);
    }

    public Switch loadSwitchByNameandFloorId(String name, Long id) {
        return switchDao.loadSwitchByNameandFloorId(name, id);

    }

    public Switch updateName(Switch switchval) {
        return switchDao.updateName(switchval.getName(), switchval.getId());
    }

    /*
     * public void deleteSwitchFixture(Long id) { switchFixtureDao.removeObject(SwitchFixtures.class, id); }
     */

    public void deleteSceneLevel(Long id) {
        sceneLevelDao.removeObject(SceneLevel.class, id);
    }

    /*
     * public Switch updateDimmerControl(Switch switchval) {
     * 
     * return switchDao.updateDimmerControl(switchval.getId(), switchval.getDimmerControl());
     * 
     * }
     * 
     * public Switch updateActiveControl(Switch switchval) {
     * 
     * return switchDao.updateActiveControl(switchval.getId(), switchval.getActiveControl()); }
     * 
     * public Switch updateSceneId(Switch switchval) { return switchDao.updateSceneId(switchval.getId(),
     * switchval.getSceneId()); }
     */

    public Switch updatePositionById(Switch switchval) {
        return switchDao.updatePositionById(switchval);
    }
    
    public List loadSwitchDetailsByFacilitiesForUserId(String uId) {
    	User user = userManager.loadUserById(Long.parseLong(uId));
    	if(user.getRole().getRoleType() == RoleType.Admin)
    	{
    		return switchDao.loadSwitchByFacilityAssignedForAdmin();
    	}
    	else
    	{
    		return switchDao.loadSwitchByFacilityAssignedForUser(uId);	
    	}    	
    }
    
    public List<SwitchDetail> loadSwitchDetailsByUserIdAndFacilities(String uId) {

        // User user = userDao.loadUserById(Long.valueOf(uId));
         List<SwitchDetail> switchDetails = new ArrayList<SwitchDetail>();
         List results;
         
         List switchList = loadSwitchDetailsByFacilitiesForUserId(uId);
         
         for(int i=0; i < switchList.size(); i++)
         {
         	Object[] rowResult = (Object [])switchList.get(i);
         	SwitchDetail sw = new SwitchDetail();
         	Long id = (Long)rowResult[0];
         	sw.setId(id);
         	sw.setName((String)rowResult[1]);  
         	
         	List<Scene> sceneList = loadSceneBySwitchId(id);
         	
         	if (sceneList != null) {
                for (int j = 0; j < sceneList.size(); j++) {
                    Scene s = sceneList.get(j);
                    sw.setScene(s.getId(), s.getName(), s.getSceneOrder());
                }
                sw.setScenecount(loadSceneBySwitchId(id).size());
            }
         	switchDetails.add(sw);        	
         }
         return switchDetails;
     }

    public List<SwitchDetail> loadSwitchDetailsByUserId(String uId) {

        User user = userDao.loadUserById(Long.valueOf(uId));
        List<SwitchDetail> switchDetails = new ArrayList<SwitchDetail>();
        List results;
        // First check if the user is admin if so return all switches else
        // return according to uid
        if (user.getRole().getId() != 1l) {
            results = switchDao.loadSwitchDetailsByUserId(uId);
        } else {
            results = switchDao.loadSwitchDetailsForAdmin();
        }

        if (results != null && !results.isEmpty()) {
            Iterator it = results.iterator();
            while (it.hasNext()) {
                boolean flag = false;
                Object[] rowResult = (Object[]) it.next();
                for (int i = 0; i < switchDetails.size(); i++) {
                    // check whether switch already in the switchDetails if
                    // so
                    // update it's scene field.
                    if (switchDetails.get(i).getId().longValue() == ((Long) rowResult[0]).longValue()) {
                        Long SceneId = (Long) rowResult[3];
                        String sceneName = (String) rowResult[4];
                        Integer sceneOrder = (Integer) rowResult[5];
                        switchDetails.get(i).setScene(SceneId, sceneName,sceneOrder);
                        switchDetails.get(i).setScenecount(switchDetails.get(i).getScenecount() + 1);
                        flag = true;
                    }
                }
                if (!flag) {
                    // if switch is not in switchDetails create new and add.
                    SwitchDetail sw = new SwitchDetail();
                    sw.setId((Long) rowResult[0]);
                    sw.setName((String) rowResult[1]);
                    Integer sceneCount = ((Long) rowResult[2]).intValue();
                    sw.setScenecount(sceneCount);
                    /*
                    Integer dimmerControlValue = (Integer) rowResult[3];
                    sw.setCurrentLightLevel(dimmerControlValue);
                    */
                    Long SceneId = (Long) rowResult[3];
                    String sceneName = (String) rowResult[4];
                    Integer sceneOrder = (Integer) rowResult[5];
                    sw.setScene(SceneId, sceneName,sceneOrder);
                    switchDetails.add(sw);
                }
            }

        }

        return switchDetails;

    }

    /*
     * public void deleteSwitchFixturesForFixture(long fixtureId) {
     * switchFixtureDao.deleteSwitchFixturesForFixture(fixtureId); }
     */
    public Switch loadSwitchByNameandAreaId(String name, Long pid) {
        return switchDao.loadSwitchByNameandAreaId(name, pid);

    }

    public void updateSceneLevel(SceneLevel sceneLevel) {
        SceneLevel level = sceneLevelDao.loadLevelBySceneSwitchAndFixtureId(sceneLevel.getSceneId(),
                sceneLevel.getSwitchId(), sceneLevel.getFixtureId());
        if (level != null) {
            // Update scene level
            level.setLightLevel(sceneLevel.getLightLevel());
            sceneDao.saveObject(level);
        } else {
            // Create new scene level
            sceneDao.saveObject(sceneLevel);
        }
    }

    public void updatePlugloadSceneLevel(PlugloadSceneLevel sceneLevel) {
    	PlugloadSceneLevel level = plugloadSceneLevelDao.loadLevelBySceneSwitchAndPlugloadId(sceneLevel.getSceneId(),
                sceneLevel.getSwitchId(), sceneLevel.getPlugloadId());
        if (level != null) {
            // Update scene level
            level.setLightLevel(sceneLevel.getLightLevel());
            plugloadSceneLevelDao.saveObject(level);
        } else {
            // Create new scene level
        	plugloadSceneLevelDao.saveObject(sceneLevel);
        }
    }
    
    /*
     * public Switch createDummyWDS(Long floorId) { List<Switch> oriSwitchList = switchDao.loadSwitchByFloorId(floorId);
     * Switch switchObj = new Switch(); switchObj.setId((long) (oriSwitchList.get(oriSwitchList.size() - 1).getId()) +
     * 1); switchObj.setName("WDS002"); switchObj.setCampusId((long) 1); switchObj.setBuildingId(floorId);
     * switchObj.setFloorId((long) floorId); switchObj.setSwitchType("Real"); switchObj.setState("DISCOVERED"); return
     * switchObj; }
     */

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

    public Switch createNewSwitch(String switchName, Floor floor, GemsGroup gemsGroup) {
    	Integer initialTime = 0;
    	Integer extendTime = 0;
		SystemConfiguration oConfig = sysMgr
				.loadConfigByName("switch.initial_scene_active_time");
		if (oConfig != null) {
			try {
				initialTime = Integer.parseInt(oConfig.getValue());
			} catch (NumberFormatException nfe) {
				initialTime = 0;
			}
		}
		oConfig = sysMgr.loadConfigByName("switch.extend_scene_active_time");
		if (oConfig != null) {
			try {
				extendTime = Integer.parseInt(oConfig.getValue());
			} catch (NumberFormatException nfe) {
				extendTime = 0;
			}
		}

        Switch newSwitch = new Switch();
        newSwitch.setName(switchName);
        newSwitch.setFloorId(floor.getId());
        newSwitch.setBuildingId(floor.getBuilding().getId());
        newSwitch.setCampusId(floor.getBuilding().getCampus().getId());
        newSwitch.setXaxis(0);
        newSwitch.setYaxis(0);
        newSwitch.setModeType(new Short("0"));
        newSwitch.setOperationMode(new Short("0"));
        newSwitch.setInitialSceneActiveTime(initialTime);
        newSwitch.setExtendSceneActiveTime(extendTime);
        newSwitch.setGemsGroup(gemsGroup);
        Switch sw = switchDao.createNewSwitch(newSwitch);
        sceneDao.saveObject(new Scene(null, sw.getId(), "All On", 0));
        sceneDao.saveObject(new Scene(null, sw.getId(), "All Off", 1));
        return sw;
    }

    /*
     * public void updateState(Switch oSwitch) { switchDao.updateState(oSwitch); }
     */

    public int manageGroupMembership(Long switchId) {
        Switch oSwitch = switchDao.getSwitchById(switchId);
        int result = 0;
        if (oSwitch == null) {
            return 1;
        }
        SwitchGroup oSwitchGroup = switchDao.loadSwitchGroupByGemsGroupId(oSwitch.getGemsGroup().getId());
        if (oSwitchGroup == null) {
            return 2;
        }
        int groupNo = Integer.parseInt(oSwitchGroup.getGroupNo().toString(), 16);
        List<GemsGroupFixture> oGGFxList = gemsGroupDao.getGemsGroupFixtureByGroup(oSwitch.getGemsGroup().getId());
        if (oGGFxList == null) {
        	// Even if there are no fixtures still WDS changes need to be sent
        	if(logger.isInfoEnabled()) {
            logger.info(switchId + ": Send wds configuration");
        	}
            sendSwitchGroupWdsParams(switchId);
            return 3;
        }
        
        // Step 1 (Join / Leave Switch group)
        if(logger.isInfoEnabled()) {
        	logger.info(oGGFxList.size() + " fixture(s) to join switch group " + groupNo);
        }
        gemsGroupManager.asssignFixturesToGroup(oSwitch.getGemsGroup().getId(), oGGFxList, groupNo,
                GGroupType.SwitchGroup.getId());

        // Step 2 (Send Switch configuration only if the SU is in the group)
        if(logger.isInfoEnabled()) {
        	logger.info(switchId + ": Send switch configuration");
        }
        Iterator<GemsGroupFixture> itr = oGGFxList.iterator();
        while (itr.hasNext()) {
            GemsGroupFixture groupFixture = itr.next();
            if ((groupFixture.getNeedSync() & GemsGroupFixture.SYNC_STATUS_GROUP_SYNCD) == GemsGroupFixture.SYNC_STATUS_GROUP_SYNCD) {
                if (groupFixture.getUserAction() == GemsGroupFixture.USER_ACTION_DEFAULT
                        || groupFixture.getUserAction() == GemsGroupFixture.USER_ACTION_SCENE_DELETE
                        || groupFixture.getUserAction() == GemsGroupFixture.USER_ACTION_SCENE_ORDER
                        || groupFixture.getUserAction() == GemsGroupFixture.USER_ACTION_SCENE_PUSH)
                    sendSwitchGroupParams(switchId, groupFixture.getFixture().getId());
            } else {
            	if(logger.isInfoEnabled()) {
                logger.info(groupFixture.getFixture().getId() + " with gems group " + oSwitch.getGemsGroup().getId()
                        + ": group has not sync'd yet");
            	}
            }
        }

        // Step 3 (Send Wds configuration only if WDS is associated with the switch)
        if(logger.isInfoEnabled()) {
        	logger.info(switchId + ": Send wds configuration");
        }
        sendSwitchGroupWdsParams(switchId);
        return result;
    }
    
    public int managePlugloadGroupMembership(Long switchId) {
    	
    	Switch oSwitch = switchDao.getSwitchById(switchId);
      int result = 0;
      if (oSwitch == null) {
          return 1;
      }
      SwitchGroup oSwitchGroup = switchDao.loadSwitchGroupByGemsGroupId(oSwitch.getGemsGroup().getId());
      if (oSwitchGroup == null) {
          return 2;
      }
      int groupNo = Integer.parseInt(oSwitchGroup.getGroupNo().toString(), 16);
      List<GemsGroupPlugload> oGGPlList = gemsGroupPlugloadDao.getGemsGroupPlugloadByGroup(oSwitch.getGemsGroup().getId());
      if (oGGPlList == null) {
      	// Even if there are no plugloads still WDS changes need to be sent
      	if(logger.isInfoEnabled()) {
      		logger.info(switchId + ": Send wds configuration");
      	}
      	sendSwitchGroupWdsParamsToPlugloads(switchId);
      	return 3;
      }
      // Step 1 (Join / Leave Switch group)
      if(logger.isInfoEnabled()) {
      	logger.info(oGGPlList.size() + " plugload(s) to join switch group " + groupNo);
      }
      gemsPlugloadGroupManager.asssignPlugloadsToGroup(oSwitch.getGemsGroup().getId(), oGGPlList, groupNo,
              GGroupType.SwitchGroup.getId());

      // Step 2 (Send Switch configuration only if the Plugload is in the group)
      if(logger.isInfoEnabled()) {
      	logger.info(switchId + ": Send switch configuration to plugload");
      }
      Iterator<GemsGroupPlugload> itr = oGGPlList.iterator();
      while (itr.hasNext()) {
      	GemsGroupPlugload groupPlugload = itr.next();      	
      	if ((groupPlugload.getNeedSync() & GemsGroupPlugload.SYNC_STATUS_GROUP_SYNCD) == GemsGroupPlugload.SYNC_STATUS_GROUP_SYNCD) {
      		if (groupPlugload.getUserAction() == GemsGroupPlugload.USER_ACTION_DEFAULT
      				|| groupPlugload.getUserAction() == GemsGroupPlugload.USER_ACTION_SCENE_DELETE
      				|| groupPlugload.getUserAction() == GemsGroupPlugload.USER_ACTION_SCENE_ORDER
      				|| groupPlugload.getUserAction() == GemsGroupPlugload.USER_ACTION_SCENE_PUSH)
      			sendSwitchGroupParamsToPlugload(switchId, groupPlugload.getPlugload().getId());
      	} else {
      		if(logger.isInfoEnabled()) {
      			logger.info(groupPlugload.getPlugload().getId() + " with gems group " + oSwitch.getGemsGroup().getId()
      				+ ": group has not sync'd yet");
      		}
      	}
      }

      // Step 3 (Send Wds configuration only if WDS is associated with the switch)
      if(logger.isInfoEnabled()) {
      	logger.info(switchId + ": Send wds configuration");
      }
      sendSwitchGroupWdsParamsToPlugloads(switchId);
      return result;
      
    } //end of method managePlugloadGroupMembership
    
    public int sendSwitchGroupParamsToPlugload(Long switchId, Long plugloadId) {
    	
    	int[] plugloadArr = { plugloadId.intValue() };
    	Switch oSwitch = switchDao.getSwitchById(switchId);
    	int result = 0;
    	if (oSwitch == null) {
    		return 1;
    	}
    	SwitchGroup oSwitchGroup = switchDao.loadSwitchGroupByGemsGroupId(oSwitch.getGemsGroup().getId());
    	if (oSwitchGroup == null) {
    		return 2;
    	}
    	// 32 bits
    	int groupNo = Integer.parseInt(oSwitchGroup.getGroupNo().toString(), 16);
    	List<PlugloadSceneLevel> oList = plugloadSceneLevelDao.loadSceneLevelListInCreationOrderBySwitchAndPlugloadId(switchId, plugloadId);
    	if (oList == null) {
    		return 3;
    	}

    	byte[] swGrpParamsData = getSwitchGroupParamsDataOfPlugload(oSwitch, plugloadId);
    	if(swGrpParamsData == null) {
    		return -1;
    	}
    	ByteArrayOutputStream output = new ByteArrayOutputStream();

    	try {
    		output.write(ServerUtil.intToByteArray(groupNo));
    		output.write(swGrpParamsData);
    		if(logger.isInfoEnabled()) {
    			logger.info(switchId + ", " + plugloadId + ": " + ServerUtil.getLogPacket(output.toByteArray()));
    		}
    		DeviceServiceImpl.getInstance().sendSwitchGroupConfiguration(plugloadArr, output.toByteArray(),
    				ServerConstants.CMD_SET_SWITCH_GRP_PARMS, DeviceType.Plugload.getName());
    		if (DeviceServiceImpl.getInstance().getSuWirelessGrpConfigChangeAckStatus(plugloadId)) {
    			GemsGroupPlugload groupPlugload = gemsGroupPlugloadDao.getGemsGroupPlugload(oSwitch.getGemsGroup().getId(), plugloadId);
    			groupPlugload.setNeedSync(groupPlugload.getNeedSync() | GemsGroupFixture.SYNC_STATUS_SWITCHCONF);
    			gemsGroupPlugloadDao.saveGemsGroupPlugload(groupPlugload);
    		} else {
    			logger.error(plugloadId + " with gems group " + oSwitch.getGemsGroup().getId() + ": unable to send switch configuration");
    		}	
    	} catch (IOException ioe) {
    		logger.error(ioe.getMessage());
    		result = -1;
    	}
    	finally {
    		try {
    			output.close();
    		}
    		catch(Exception ex) { }
    	}
    	return result;
        
    } //end of method sendSwitchGroupParamsToPlugload
    
    public int sendSwitchGroupParams(Long switchId, Long fixtureId) {
      int[] fixtureArr = { fixtureId.intValue() };
      Switch oSwitch = switchDao.getSwitchById(switchId);
      int result = 0;
      if (oSwitch == null) {
          return 1;
      }
      SwitchGroup oSwitchGroup = switchDao.loadSwitchGroupByGemsGroupId(oSwitch.getGemsGroup().getId());
      if (oSwitchGroup == null) {
          return 2;
      }
      // 32 bits
      int groupNo = Integer.parseInt(oSwitchGroup.getGroupNo().toString(), 16);
      List<SceneLevel> oList = sceneLevelDao.loadSceneLevelListInCreationOrderBySwitchAndFixtureId(switchId, fixtureId);
      if (oList == null) {
          return 3;
      }

      byte[] swGrpParamsData = getSwitchGroupParamsData(oSwitch, fixtureId);
      if(swGrpParamsData == null) {
      	return -1;
      }
      ByteArrayOutputStream output = new ByteArrayOutputStream();

      try {
          output.write(ServerUtil.intToByteArray(groupNo));
          output.write(swGrpParamsData);
          if(logger.isInfoEnabled()) {
          	logger.info(switchId + ", " + fixtureId + ": " + ServerUtil.getLogPacket(output.toByteArray()));
          }
          DeviceServiceImpl.getInstance().sendSwitchGroupConfiguration(fixtureArr, output.toByteArray(),
                  ServerConstants.CMD_SET_SWITCH_GRP_PARMS, DeviceType.Fixture.getName());
          if (DeviceServiceImpl.getInstance().getSuWirelessGrpConfigChangeAckStatus(fixtureId)) {
              GemsGroupFixture groupFixture = gemsGroupDao.getGemsGroupFixture(oSwitch.getGemsGroup().getId(),
                      fixtureId);
              groupFixture.setNeedSync(groupFixture.getNeedSync() | GemsGroupFixture.SYNC_STATUS_SWITCHCONF);
              gemsGroupDao.saveGemsGroupFixtures(groupFixture);
          } else {
              logger.error(fixtureId + " with gems group " + oSwitch.getGemsGroup().getId()
                      + ": unable to send switch configuration");
          }
      } catch (IOException ioe) {
          logger.error(ioe.getMessage());
          result = -1;
      }
      finally {
      	try {
      		output.close();
      	}
      	catch(Exception ex) { }
    	}
      return result;
      
  }
    
    public byte[] getSwitchGroupParamsDataOfPlugload(Switch oSwitch, Long plugloadId) {
      
      List<PlugloadSceneLevel> oList = plugloadSceneLevelDao.loadSceneLevelListInCreationOrderBySwitchAndPlugloadId(oSwitch.getId(), 
      		plugloadId);      
      // 8 bits
      byte scene_cnt = (byte) oList.size();
      byte[] scene_level = new byte[scene_cnt]; // level for each scene, ends with 0xF (ending terminator)
      for (int i = 0; i < scene_cnt; i++) {
          scene_level[i] = oList.get(i).getLightLevel().byteValue();
      }
      // 8 bits initial length of time in min to apply scene control (0-240) (zero is forever)
      int init_apply_time = oSwitch.getInitialSceneActiveTime() != null ? oSwitch.getInitialSceneActiveTime() : 0;
      // 8 bits length of time in min to extend scene control (0-240)
      int extend_apply_time = oSwitch.getExtendSceneActiveTime() != null ? oSwitch.getExtendSceneActiveTime() : 0;
      // 8 bits
      byte control_type = oSwitch.getModeType().byteValue();

      ByteArrayOutputStream output = new ByteArrayOutputStream();
      try {          
      	output.write(ServerUtil.intToByteArray(init_apply_time));
      	output.write(ServerUtil.intToByteArray(extend_apply_time));
      	output.write(control_type);
      	output.write((byte) scene_cnt);
      	output.write(scene_level);
      	return output.toByteArray();
      } catch (IOException ioe) {
      	logger.error(ioe.getMessage());
      }
      finally {
      	try {
      		output.close();
      	}
      	catch(Exception ex) { }
      }
      return null;
      
    } //end of method getSwitchGroupParamsDataOfPlugload
    
    public byte[] getSwitchGroupParamsData(Switch oSwitch, Long fixtureId) {
    
      List<SceneLevel> oList = sceneLevelDao.loadSceneLevelListInCreationOrderBySwitchAndFixtureId(oSwitch.getId(), fixtureId);      
      // 8 bits
      if(oList != null){
    	  
    	  byte scene_cnt = (byte) oList.size();
          byte[] scene_level = new byte[scene_cnt]; // level for each scene, ends with 0xF (ending terminator)
          for (int i = 0; i < scene_cnt; i++) {
              scene_level[i] = oList.get(i).getLightLevel().byteValue();
          }
          // 8 bits initial length of time in min to apply scene control (0-240) (zero is forever)
    		int init_apply_time = oSwitch.getInitialSceneActiveTime() != null ? oSwitch
    				.getInitialSceneActiveTime() : 0;
          // 8 bits length of time in min to extend scene control (0-240)
    		int extend_apply_time = oSwitch.getExtendSceneActiveTime() != null ? oSwitch
    				.getExtendSceneActiveTime() : 0;
          // 8 bits
          byte control_type = oSwitch.getModeType().byteValue();

          ByteArrayOutputStream output = new ByteArrayOutputStream();
          try {          
              output.write(ServerUtil.intToByteArray(init_apply_time));
              output.write(ServerUtil.intToByteArray(extend_apply_time));
              output.write(control_type);
              output.write((byte) scene_cnt);
              output.write(scene_level);
              return output.toByteArray();
          } catch (IOException ioe) {
              logger.error(ioe.getMessage());
          }
          finally {
          	try {
          		output.close();
          	}
          	catch(Exception ex) { }
          }
    	  
      }
      
      return null;
      
    } //end of method getSwitchGroupParamsData
    
    public int sendSwitchGroupWdsParamsToPlugloads(Long switchId) {
    	
      int result = 0;
      Switch oSwitch = switchDao.getSwitchById(switchId);
      if (oSwitch == null) {
      	return 1;
      }
      SwitchGroup oSwitchGroup = switchDao.loadSwitchGroupByGemsGroupId(oSwitch.getGemsGroup().getId());
      if (oSwitchGroup == null) {
      	return 2;
      }
      List<Scene> oList = sceneDao.loadSceneInCreationOrderBySwitchId(switchId);
      if (oList == null) {
      	return 3;
      }
      List<Wds> oWdsList = wdsDao.loadCommissionedWdsBySwitchId(switchId);
      if (oWdsList == null) {
      	return 4;
      }

      List<GemsGroupPlugload> oGGPlList = gemsGroupPlugloadDao.getGemsGroupPlugloadByGroup(oSwitch.getGemsGroup().getId());
      if (oGGPlList == null) {
      	return 5;
      }

      // Now go on to push the WDS configuration if required for the rest of the fixtures.
      int[] plugloadArr = new int[oGGPlList.size()];
      for (int count = 0; count < oGGPlList.size(); count++) {
      	plugloadArr[count] = oGGPlList.get(count).getPlugload().getId().intValue();
      }
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      try {
      	int groupNo = Integer.parseInt(oSwitchGroup.getGroupNo().toString(), 16);
      	output.write(ServerUtil.intToByteArray(groupNo));
      	byte[] swGrpWdsParamsData = getSwitchGroupWdsParamsData(oSwitch);
      	output.write(swGrpWdsParamsData);

      	for (int count = 0; count < plugloadArr.length; count++) {
      		// Sending one fixture at a time.
      		int plugloadId = plugloadArr[count];
      		int[] plArr = { plugloadId };
      		Long plId = new Long(plugloadId);
      		GemsGroupPlugload groupPlugload = gemsGroupPlugloadDao.getGemsGroupPlugload(oSwitch.getGemsGroup().getId(), plId);
      		if ((groupPlugload.getNeedSync() & GemsGroupFixture.SYNC_STATUS_SWITCHCONF_SYNCD) == GemsGroupFixture.SYNC_STATUS_SWITCHCONF_SYNCD) {
      			if (groupPlugload.getUserAction() == GemsGroupFixture.USER_ACTION_DEFAULT
      					|| groupPlugload.getUserAction() == GemsGroupFixture.USER_ACTION_SCENE_DELETE
      					|| groupPlugload.getUserAction() == GemsGroupFixture.USER_ACTION_SCENE_ORDER
      					|| groupPlugload.getUserAction() == GemsGroupFixture.USER_ACTION_SCENE_PUSH) {
      				if(logger.isInfoEnabled()) {
      					logger.info(switchId + ", " + plId + ": " + ServerUtil.getLogPacket(output.toByteArray()));
      				}
      				DeviceServiceImpl.getInstance().sendSwitchGroupConfiguration(plArr, output.toByteArray(),
      						ServerConstants.CMD_SET_SWITCH_GRP_WDS, DeviceType.Plugload.getName());
      				if (DeviceServiceImpl.getInstance().getSuWirelessGrpConfigChangeAckStatus(plId)) {
      					groupPlugload.setUserAction(GemsGroupFixture.USER_ACTION_DEFAULT);
      					groupPlugload.setNeedSync(groupPlugload.getNeedSync() | GemsGroupFixture.SYNC_STATUS_WDSCONF);
      					gemsGroupPlugloadDao.saveGemsGroupPlugload(groupPlugload);
      				} else {
      					if(logger.isInfoEnabled()) {
      						logger.info(plugloadId + " with gems group " + oSwitch.getGemsGroup().getId() + ": unable to wds configuration");
      					}
      				}
      			}
      		} else {
      			if(logger.isInfoEnabled()) {
      				logger.info("pl- " + plugloadId + " with gems group " + oSwitch.getGemsGroup().getId() + 
      						": switch configuration has not sync'd yet");
      			}
      		}
      	}
      } catch (IOException ioe) {
      	logger.error(ioe.getMessage());
      	result = -1;
      }
      finally {
      	try {
      		output.close();
      	}
      	catch(Exception ex) {}
      }
      return result;
      
    } //end of method sendSwitchGroupWdsParamsToPlugloads

    public int sendSwitchGroupWdsParams(Long switchId) {
        int MAX_SCENES = 8;
        int NUM_BUTTONS = 4;
        int result = 0;
        Switch oSwitch = switchDao.getSwitchById(switchId);
        if (oSwitch == null) {
            return 1;
        }
        SwitchGroup oSwitchGroup = switchDao.loadSwitchGroupByGemsGroupId(oSwitch.getGemsGroup().getId());
        if (oSwitchGroup == null) {
            return 2;
        }
        List<Scene> oList = sceneDao.loadSceneInCreationOrderBySwitchId(switchId);
        if (oList == null) {
            return 3;
        }
        List<Wds> oWdsList = wdsDao.loadCommissionedWdsBySwitchId(switchId);
        if (oWdsList == null) {
            return 4;
        }
        
        // Clean up deleted WDS or dissassocited WDS;
        List<Wds> oMarkedDelWdsList = wdsDao.loadNotAssociatedWdsBySwitchId(switchId);
        if (oMarkedDelWdsList != null && oMarkedDelWdsList.size() > 0) {
            Wds oDelWds = null;
            int iDelStatus = 0;
            for (int count = 0; count < oMarkedDelWdsList.size(); count++) {
                oDelWds = oMarkedDelWdsList.get(count);
                if (iDelStatus == 0) {
                    if (oDelWds.getState().equals(ServerConstants.WDS_STATE_DELETED_STR)) {
                        // Now remove this from the DB
                        wdsManager.deleteWds(oDelWds.getId());
                    }else {
                        oDelWds.setWdsSwitch(null);
                        oDelWds.setSwitchGroup(null);
                        oDelWds.setAssociationState(ServerConstants.WDS_STATE_NOT_ASSOCIATED);
                        wdsManager.update(oDelWds);
                    }
                }
            }
        }

        List<GemsGroupFixture> oGGFxList = gemsGroupDao.getGemsGroupFixtureByGroup(oSwitch.getGemsGroup().getId());
        if (oGGFxList == null) {
            return 5;
        }

        // Now go on to push the WDS configuration if required for the rest of the fixtures.
        int[] fixtureArr = new int[oGGFxList.size()];
        for (int count = 0; count < oGGFxList.size(); count++) {
            fixtureArr[count] = oGGFxList.get(count).getFixture().getId().intValue();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            int groupNo = Integer.parseInt(oSwitchGroup.getGroupNo().toString(), 16);
            output.write(ServerUtil.intToByteArray(groupNo));
            byte[] swGrpWdsParamsData = getSwitchGroupWdsParamsData(oSwitch);
            output.write(swGrpWdsParamsData);

            for (int count = 0; count < fixtureArr.length; count++) {
                // Sending one fixture at a time.
                int fixtureId = fixtureArr[count];
                int[] fxArr = { fixtureId };
                Long fxId = new Long(fixtureId);
                GemsGroupFixture groupFixture = gemsGroupDao.getGemsGroupFixture(oSwitch.getGemsGroup().getId(), fxId);
                if ((groupFixture.getNeedSync() & GemsGroupFixture.SYNC_STATUS_SWITCHCONF_SYNCD) == GemsGroupFixture.SYNC_STATUS_SWITCHCONF_SYNCD) {
                    if (groupFixture.getUserAction() == GemsGroupFixture.USER_ACTION_DEFAULT
                            || groupFixture.getUserAction() == GemsGroupFixture.USER_ACTION_SCENE_DELETE
                            || groupFixture.getUserAction() == GemsGroupFixture.USER_ACTION_SCENE_ORDER
                            || groupFixture.getUserAction() == GemsGroupFixture.USER_ACTION_SCENE_PUSH) {
                    	if(logger.isInfoEnabled()) {
                        logger.info(switchId + ", " + fxId + ": " + ServerUtil.getLogPacket(output.toByteArray()));
                    	}
                        DeviceServiceImpl.getInstance().sendSwitchGroupConfiguration(fxArr, output.toByteArray(),
                                ServerConstants.CMD_SET_SWITCH_GRP_WDS, DeviceType.Fixture.getName());
                        if (DeviceServiceImpl.getInstance().getSuWirelessGrpConfigChangeAckStatus(fxId)) {
                            groupFixture.setUserAction(GemsGroupFixture.USER_ACTION_DEFAULT);
                            groupFixture.setNeedSync(groupFixture.getNeedSync() | GemsGroupFixture.SYNC_STATUS_WDSCONF);
                            gemsGroupDao.saveGemsGroupFixtures(groupFixture);
                        } else {
                        	if(logger.isInfoEnabled()) {
                            logger.info(fixtureId + " with gems group " + oSwitch.getGemsGroup().getId()
                                    + ": unable to wds configuration");
                        	}
                        }
                    }
                } else {
                	if(logger.isInfoEnabled()) {
                    logger.info(fixtureId + " with gems group " + oSwitch.getGemsGroup().getId()
                            + ": switch configuration has not sync'd yet");
                	}
                }
            }
        } catch (IOException ioe) {
            logger.error(ioe.getMessage());
            result = -1;
        }
        finally {
        	try {
        		output.close();
        	}
        	catch(Exception ex) {}
        }
        return result;
    }
    
    public byte[] getSwitchGroupWdsParamsData(Switch oSwitch) {
    	
      int MAX_SCENES = 8;
                  
      List<Scene> oList = sceneDao.loadSceneInCreationOrderBySwitchId(oSwitch.getId());      
      List<Wds> oWdsList = wdsDao.loadCommissionedWdsBySwitchId(oSwitch.getId());      
      
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      try {          
          byte wds_cnt = (byte) oWdsList.size(); // number of WDSs
          output.write(wds_cnt);
          Wds oWds;
          for (int count = 0; count < wds_cnt; count++) {
              oWds = oWdsList.get(count);
              ButtonManipulation oBM = wdsDao.loadWdsButtonManipulationById(oWds.getButtonMap().getId());
              int wds_id = Integer.parseInt(oWds.getWdsNo().toString(), 16); // 32 bit WDS number;
              output.write(ServerUtil.intToByteArray(wds_id));
              byte scn_toggle_cnt = (byte)(oList.size()-2);
              byte[] scene_toggle_order_list = null;
              if (scn_toggle_cnt < MAX_SCENES) {
                  scene_toggle_order_list = new byte[scn_toggle_cnt]; 
              }else {
                  scn_toggle_cnt = (byte)MAX_SCENES;
                  scene_toggle_order_list = new byte[MAX_SCENES]; 
              }
              output.write(scn_toggle_cnt); // no of scenes to toggle (All on and All off are defaults, so they are excluded)
              Arrays.fill(scene_toggle_order_list, (byte) 0xF);
              for (int pos = 0, idx = 2; idx < oList.size(); pos++, idx++) {
                  // TODO: Take this from ButtonManipulation table as well.
                  if (pos < MAX_SCENES) {
                      Integer lOrder = oList.get(idx).getSceneOrder();
                      scene_toggle_order_list[lOrder.intValue()-2] = (byte)idx;
                  }
              }
              output.write(scene_toggle_order_list);
              if (oBM != null) {
                  // 16 * 4 (64 bits)
                  output.write(ServerUtil.longToByteArray(oBM.getButtonManipAction()));
              }
          }
          return output.toByteArray();          
      } catch (IOException ioe) {
          logger.error(ioe.getMessage());          
      }
      finally {
      	try {
      		output.close();
      	}
      	catch(Exception ex) {}
      }
      return null;
      
    } //end of method getSwitchGroupWdsParamsData
    
    public int sendWdsDeletionMsg(Long wdsId) {
        int result = 0;
        Wds oWds = wdsDao.getWdsSwitchById(wdsId);
        if (oWds == null) {
            return 0;
        }
        if (oWds.getWdsSwitch() == null) {
            return 0;
        }
        Long switchId = oWds.getWdsSwitch().getId();
        Switch oSwitch = switchDao.getSwitchById(switchId);
        if (oSwitch == null) {
            return 1;
        }
        result = sendSwitchGroupWdsParams(switchId);
        /*
        SwitchGroup oSwitchGroup = switchDao.loadSwitchGroupByGemsGroupId(oSwitch.getGemsGroup().getId());
        if (oSwitchGroup == null) {
            return 2;
        }
        List<GemsGroupFixture> oGGFxList = gemsGroupDao.getGemsGroupFixtureByGroup(oSwitch.getGemsGroup().getId());
        if (oGGFxList == null) {
            return 3;
        }
        // 32 bits
        int groupNo = Integer.parseInt(oSwitchGroup.getGroupNo().toString(), 16);
        // 32 bits
        int switchGrpNo = Integer.parseInt(oWds.getWdsNo().toString(), 16);
        
        int[] fixtureArr = new int[oGGFxList.size()];
        for (int count = 0; count < oGGFxList.size(); count++) {
            fixtureArr[count] = oGGFxList.get(count).getFixture().getId().intValue();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            output.write(ServerUtil.intToByteArray(switchGrpNo));
            output.write(ServerUtil.intToByteArray(groupNo));
            logger.info("Removing wds " + wdsId + " from switch " + switchId + ": " + ServerUtil.getLogPacket(output.toByteArray()));
            for (int count = 0; count < fixtureArr.length; count++) {
                // Sending one fixture at a time.
                int fixtureId = fixtureArr[count];
                int[] fxArr = { fixtureId };
                Long fxId = new Long(fixtureId);
                GemsGroupFixture groupFixture = gemsGroupDao.getGemsGroupFixture(oSwitch.getGemsGroup().getId(), fxId);
                DeviceServiceImpl.getInstance().sendWdsGroupConfiguration(fxArr, output.toByteArray(),
                        ServerConstants.CMD_SWITCH_GRP_DEL_WDS);
                if (DeviceServiceImpl.getInstance().getSuWirelessGrpConfigChangeAckStatus(fxId)) {
                    groupFixture.setUserAction(GemsGroupFixture.USER_ACTION_DEFAULT);
                    gemsGroupDao.saveGemsGroupFixtures(groupFixture);
                    result = 0;
                    logger.debug("Successfully removed wds " + wdsId + " from fixture: " + fixtureId);
                } else {
                    logger.warn("Failed removing wds " + wdsId + " from fixture: " + fixtureId);
                    result = 5;
                }
            }
        } catch (IOException ioe) {
            logger.error(ioe.getMessage());
            result = -1;
        }
        */
        return result;
    }
    
    public int markWdsForDeletion(Long wdsId) {
        int result = 0;
        Wds oWds = wdsDao.getWdsSwitchById(wdsId);
        if (oWds == null) {
            return 0;
        }
        Long switchId = oWds.getWdsSwitch().getId();
        Switch oSwitch = switchDao.getSwitchById(switchId);
        if (oSwitch == null) {
            return 1;
        }
        SwitchGroup oSwitchGroup = switchDao.loadSwitchGroupByGemsGroupId(oSwitch.getGemsGroup().getId());
        if (oSwitchGroup == null) {
            return 2;
        }
        List<GemsGroupFixture> oGGFxList = gemsGroupDao.getGemsGroupFixtureByGroup(oSwitch.getGemsGroup().getId());
        if (oGGFxList == null) {
            return 3;
        }
        for (int count = 0; count < oGGFxList.size(); count++) {
            GemsGroupFixture oGGFixture = oGGFxList.get(count);
            oGGFixture.setUserAction(GemsGroupFixture.USER_ACTION_WDS_REMOVE);
        }
        return result;
    }

    public Integer nextSceneOrder(Long switchId) {
        return sceneDao.nextSceneOrder(switchId);
    }

    public Set<Long> loadSwitchSceneFixtures(Long switchId) {
        return sceneLevelDao.loadSwitchSceneFixtures(switchId);
    }

    public void deleteSceneLevelsForSwitch(Long switchId, Long fixtureId) {
        sceneLevelDao.deleteSceneLevelsForSwitch(switchId, fixtureId);
    }

    public SwitchGroup createSwitchGroup(SwitchGroup switchGroup) {
        return (SwitchGroup) switchGroupDao.saveObject(switchGroup);
    }

    public SwitchGroup getSwitchGroupByGemsGroupId(Long gemsGroupId) {
        return switchGroupDao.getSwitchGroupByGemsGroupId(gemsGroupId);
    }
    
    public List<GemsGroup> loadSwitchGroupsByFloor(Long floorId) {
        List<GemsGroup> switchGroups = switchGroupDao.loadSwitchGroupsByFloor(floorId);
        return switchGroups;
    }
    
    public Switch getSwitchByGemsGroupId(Long gemsGroupId) {
    	return switchDao.loadSwitchByGemsGroupId(gemsGroupId);
    }
    
    public SwitchGroup getSwitchGroupByGroupNo(int groupNo) {
    	return switchGroupDao.getSwitchGroupByGroupNo(groupNo);
    }
    
    public int sendSwitchGroupMsgToFixture(Long switchId, String action, int argument) {
        Switch oSwitch = switchDao.getSwitchById(switchId);
        if (oSwitch == null) {
            return 1;
        }
        SwitchGroup oSwitchGroup = switchDao.loadSwitchGroupByGemsGroupId(oSwitch.getGemsGroup().getId());
        if (oSwitchGroup == null) {
            return 2;
        }
        List<GemsGroupFixture> oGGFxList = gemsGroupDao.getGemsGroupFixtureByGroup(oSwitchGroup.getGemsGroup().getId());
        
        List<GemsGroupPlugload> oGGPgList = gemsGroupDao.getGemsGroupPlugloadByGroup(oSwitchGroup.getGemsGroup().getId());
        
        if (oGGFxList == null && oGGPgList == null) {
            return 3;
        }
        if ((oGGFxList != null && oGGFxList.size() == 0) && (oGGPgList!= null && oGGPgList.size() == 0)) {
            return 4;
        }
        
        int[] fixtureArr=null,plugloadArr=null,deviceArr=null;
        int totalRecords = 0;
        if(oGGFxList != null && oGGFxList.size() > 0){
        	fixtureArr = new int[oGGFxList.size()];
            for (int count = 0; count < oGGFxList.size(); count++) {            
                fixtureArr[count] = oGGFxList.get(count).getFixture().getId().intValue();                
            }
            totalRecords = oGGFxList.size();
        }        
        if(oGGPgList != null && oGGPgList.size() > 0){
        	plugloadArr = new int[oGGPgList.size()];
            for (int count = 0; count < oGGPgList.size(); count++) {            
            	plugloadArr[count] = oGGPgList.get(count).getPlugload().getId().intValue();            	
            }
            totalRecords += oGGPgList.size();	
        }
      
        deviceArr = new int[totalRecords];
    	if(fixtureArr!= null) {
    		System.arraycopy(fixtureArr, 0, deviceArr, 0, fixtureArr.length);
    	}
    	if(plugloadArr!= null) {
    		int destLen  = 0;
    		if(fixtureArr != null) destLen = fixtureArr.length;
    		System.arraycopy(plugloadArr, 0, deviceArr, destLen, plugloadArr.length);
    	}
    	
    	
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            int groupNo = Integer.parseInt(oSwitchGroup.getGroupNo().toString(), 16);
            output.write(ServerUtil.intToByteArray(groupNo));
            byte scene_control_action = 0;
            if (action.equalsIgnoreCase("scene")) {  
                if (argument < 0 || argument > 8) {
                    argument = 0;
                }
                scene_control_action = (byte) ((ServerConstants.SU_SWITCH_GRP_BUTTON_MAP_SET_SCENE << 4) + argument);
            } else if (action.equalsIgnoreCase("dimup")) {
                if (argument < 0 || argument > 100) {
                    argument = 10;
                }
                scene_control_action = (byte) ((ServerConstants.SU_SWITCH_GRP_BUTTON_MAP_SET_DIM_UP << 4) + argument);
            } else if (action.equalsIgnoreCase("dimdown")) {
                if (argument < 0 || argument > 100) {
                    argument = 10;
                }
                scene_control_action = (byte) ((ServerConstants.SU_SWITCH_GRP_BUTTON_MAP_SET_DIM_DOWN << 4) + argument);
            } else {            	
                scene_control_action = ServerConstants.SU_SWITCH_GRP_BUTTON_MAP_SET_TO_AUTO << 4;
            }
            output.write(scene_control_action);
           
            if(deviceArr != null && deviceArr.length > 0){            	
            	DeviceServiceImpl.getInstance().sendGroupBcastCommand(deviceArr, ServerConstants.CMD_SWITCH_GRP_APPLY_ACTION,
                        output.toByteArray(),DeviceType.Device.getName());            	
            }
            
        } catch (IOException ioe) {
            logger.error(ioe.getMessage());
            return -1;
        }
        return 0;
    }
    
    
    public List<SceneTemplate> getAllSceneTemplates() {
    	return switchDao.getAllSceneTemplates();
    }
    public List<PlugloadSceneLevel> loadPlugloadSceneLevelsBySceneId(Long id) {
        return plugloadSceneLevelDao.loadLevelsBySceneId(id);
    }
    public List<SceneLightLevelTemplate> getAllLightLevelsForSceneTemplate(Long sceneTemplateId) {
    	return switchDao.getAllLightLevelsForSceneTemplate(sceneTemplateId);
    }

	public Set<Long> loadSwitchScenePlugloads(Long switchId) {
		 return plugloadSceneLevelDao.loadSwitchScenePlugloads(switchId);
	}
	  public void deletePlugloadSceneLevelsForSwitch(Long switchId, Long fixtureId) {
		  plugloadSceneLevelDao.deleteSceneLevelsForSwitch(switchId, fixtureId);
	    }

	public List<PlugloadSceneLevel> loadPlugloadSceneLevelsBySceneId(long sceneId) {
		 return plugloadSceneLevelDao.loadPlugloadSceneLevelsBySceneId(sceneId);
	}
	public int getSceneOrderForGivenSwitchAndScene(long switchId,long sceneId)
	{
		int sceneOrder =-1;
		Scene scene = sceneDao.getSceneById(sceneId);
		if(scene!=null && scene.getSwitchId() == switchId)
			sceneOrder =  scene.getSceneOrder();
			
		return sceneOrder;
	}

	public List<GemsGroup> getSwitchGroupByFacility(String property, Long pid) {
		return switchGroupDao.getSwitchGroupByFacility(property,pid);
	}
}
