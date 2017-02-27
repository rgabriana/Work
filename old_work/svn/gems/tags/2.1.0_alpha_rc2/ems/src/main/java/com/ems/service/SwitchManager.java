package com.ems.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.BuildingDao;
import com.ems.dao.FixtureDao;
import com.ems.dao.FloorDao;
import com.ems.dao.GatewayDao;
import com.ems.dao.SceneDao;
import com.ems.dao.SceneLevelDao;
import com.ems.dao.SwitchDao;
import com.ems.dao.SwitchFixtureDao;
import com.ems.dao.UserDao;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.Scene;
import com.ems.model.SceneLevel;
import com.ems.model.Switch;
import com.ems.model.SwitchFixtures;
import com.ems.model.User;
import com.ems.utils.ArgumentUtils;
import com.ems.vo.model.SwitchDetail;

@Service("switchManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SwitchManager {

	static final Logger logger = Logger
			.getLogger(SwitchManager.class.getName());

	@Resource
	private SwitchDao switchDao;

	@Resource
	private UserDao userDao;

	@Resource
	private SceneDao sceneDao;

	@Resource
	private SwitchFixtureDao switchFixtureDao;

	@Resource
	private SceneLevelDao sceneLevelDao;

	@Resource
	private FixtureDao fixtureDao;

	@Resource
	private GatewayDao gatewayDao;

	@Resource
	private FloorDao floorDao;

	@Resource
	private BuildingDao buildingDao;

	public void deleteSwitch(Long id) {
		switchDao.removeObject(Switch.class, id);
		List<Scene> scenes = this.loadSceneBySwitchId(id);
		List<SwitchFixtures> switchFixtures = this
				.loadSwitchFixturesBySwitchId(id);
		List<SceneLevel> levels = this.loadSceneLevelsBySwitchId(id);
		if (!ArgumentUtils.isNullOrEmpty(scenes)) {
			for (Scene scene : scenes) {
				sceneDao.removeObject(Scene.class, scene.getId());
			}
		}
		if (!ArgumentUtils.isNullOrEmpty(switchFixtures)) {
			for (SwitchFixtures switchFix : switchFixtures) {
				switchFixtureDao.removeObject(SwitchFixtures.class,
						switchFix.getId());
			}
		}
		if (!ArgumentUtils.isNullOrEmpty(levels)) {
			for (SceneLevel level : levels) {
				sceneLevelDao.removeObject(SceneLevel.class, level.getId());
			}
		}

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

	public List<Switch> loadAllSwitches() {
		return switchDao.loadAllSwitches();
	}

	public Switch update(Switch switchval) {
		try {
			return (Switch) switchDao.update(switchval);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void deleteScene(Long id) {
		sceneDao.removeObject(Scene.class, id);
		List<SceneLevel> levels = this.loadSceneLevelsBySceneId(id);
		if (levels != null) {
			for (int i = 0; i < levels.size(); i++) {
				SceneLevel level = levels.get(i);
				sceneLevelDao.removeObject(SceneLevel.class, level.getId());
			}
		}
	}

	public List<Fixture> loadFixturesBySwitchId(Long id) {

		List<Fixture> list = switchFixtureDao.loadFixturesbySwitchId(id);
		return list;
		// return null;
	}

	public List<Scene> loadSceneBySwitchId(Long id) {
		// TODO Auto-generated method stub
		return sceneDao.loadSceneBySwitchId(id);
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

	public List<SwitchFixtures> loadSwitchFixturesByFixtureId(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SceneLevel> loadLevelsBySwitchAndFixtureId(Long switchId,
			Long fixtureId) {
		return sceneLevelDao
				.loadLevelsBySwitchAndFixtureId(switchId, fixtureId);
	}

	public List<SwitchFixtures> loadSwitchFixturesBySwitchId(Long id) {
		// TODO Auto-generated method stub
		return switchFixtureDao.loadSwitchFixturebySwitchId(id);
	}

	public List<SceneLevel> loadLevelsBySwitchAndSceneId(Long switchId,
			Long sceneId) {
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
		Switch currSwitch = this.loadSwitchByNameandFloorId(
				switchval.getName(), switchval.getFloorId());
		while (currSwitch == null) {
			currSwitch = this.loadSwitchByNameandFloorId(switchval.getName(),
					switchval.getFloorId());
		}
		Long id = currSwitch.getId();
		scene.setSwitchId(id);

		return (Scene) sceneDao.saveObject(scene);
	}

	public SceneLevel saveSceneLevel(SceneLevel sceneLevel) {
		SceneLevel level = sceneLevelDao.loadLevelBySceneSwitchAndFixtureId(
				sceneLevel.getSceneId(), sceneLevel.getSwitchId(),
				sceneLevel.getFixtureId());
		if (level != null) {
			// Update scene level
			level.setLightLevel(sceneLevel.getLightLevel());
			return (SceneLevel) sceneDao.saveObject(level);
		} else {
			// Create new scene level
			return (SceneLevel) sceneDao.saveObject(sceneLevel);
		}
	}

	public SceneLevel saveSceneLevel(Switch switchval, String sceneName,
			SceneLevel sceneLevel) {
		if (sceneLevel.getId() != null && sceneLevel.getId() == 0) {
			sceneLevel.setId(null);
		}
		Switch currSwitch = this.loadSwitchByNameandFloorId(
				switchval.getName(), switchval.getFloorId());
		while (currSwitch == null) {
			currSwitch = this.loadSwitchByNameandFloorId(switchval.getName(),
					switchval.getFloorId());
		}
		Long switchId = currSwitch.getId();
		sceneLevel.setSwitchId(switchId);
		Scene scene = sceneDao.getSceneByNameandId(sceneName, switchId);
		while (scene == null) {
			scene = sceneDao.getSceneByNameandId(sceneName, switchId);
		}
		sceneLevel.setSceneId(scene.getId());
		long sceneId = scene.getId().longValue();
		Long fixtureId = sceneLevel.getFixtureId();
		List<SceneLevel> oSceneLevels = sceneLevelDao
				.loadLevelsBySwitchAndFixtureId(switchId, fixtureId);
		if (oSceneLevels != null) {
			Iterator<SceneLevel> itr = oSceneLevels.iterator();
			SceneLevel oSLevel = null;
			long oSLevelSceneId = 0L;
			boolean bFound = false;
			while (itr.hasNext()) {
				oSLevel = itr.next();
				if (oSLevel != null) {
					oSLevelSceneId = oSLevel.getSceneId().longValue();
					if (sceneId == oSLevelSceneId) {
						bFound = true;
						break;
					}
				}
			}
			if (bFound) {
				oSLevel.setLightLevel(sceneLevel.getLightLevel());
				return (SceneLevel) sceneLevelDao.update(oSLevel);
			}
		}

		return (SceneLevel) sceneLevelDao.saveObject(sceneLevel);
	}

	public Switch saveSwitch(Switch switchval) {
		if (switchval.getId() != null && switchval.getId() == 0) {
			switchval.setId(null);
		}
		// Floor floor = floorDao.getFloorById(switchval.getFloorId());
		// if (floor != null) {
		// Building building = floor.getBuilding();
		// if (building != null)
		// {
		// switchval.setBuildingId(building.getId());
		// } else {
		// switchval.setBuildingId(new Long (5));
		// }
		// Campus campus = building.getCampus();
		// if (campus != null) {
		// switchval.setCampusId(campus.getId());
		// } else {
		// switchval.setCampusId(new Long (6));
		// }
		// }

		return (Switch) switchDao.saveObject(switchval);
	}

	public SwitchFixtures saveSwitchFixtures(SwitchFixtures switchFixture) {
		return (SwitchFixtures) switchFixtureDao.saveObject(switchFixture);
	}

	public void saveSwitchFixturesList(List<SwitchFixtures> switchFixture) {
		for (SwitchFixtures swtFxtr : switchFixture) {
			switchFixtureDao.saveObject(swtFxtr);
		}
	}

	public SwitchFixtures saveSwitchFixtures(Switch switchval, Fixture fixture) {
		SwitchFixtures switchFixtures = new SwitchFixtures();
		// switchFixtures.setId(null);
		/*
		 * if (switchval.getName() != null) {
		 * switchFixtures.setSwitchId(this.getSwitchName
		 * (switchval.getName()).getId()); } else {
		 * switchFixtures.setSwitchId(new Long (2)); }
		 */
		Switch currSwitch = this.loadSwitchByNameandFloorId(
				switchval.getName(), switchval.getFloorId());
		while (currSwitch == null) {
			currSwitch = this.loadSwitchByNameandFloorId(switchval.getName(),
					switchval.getFloorId());
		}
		Long switchId = currSwitch.getId();
		switchFixtures.setSwitchId(switchId);
		// switchFixtures.setSwitchId(this.getSwitchName(name).getId());

		switchFixtures.setFixtureId(fixture.getId());

		return (SwitchFixtures) switchFixtureDao.saveObject(switchFixtures);
	}

	public Long getLastSwitchId() {
		return (Long) switchDao.getLastSwitchId();
	}

	public Switch updatePosition(Switch switchval) {
		return switchDao.updatePosition(switchval.getName(),
				switchval.getXaxis(), switchval.getYaxis());
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

	public void deleteSwitchFixture(Long id) {
		switchFixtureDao.removeObject(SwitchFixtures.class, id);
	}

	public void deleteSceneLevel(Long id) {
		sceneLevelDao.removeObject(SceneLevel.class, id);
	}

	public Switch updateDimmerControl(Switch switchval) {

		return switchDao.updateDimmerControl(switchval.getId(),
				switchval.getDimmerControl());

	}

	public Switch updateActiveControl(Switch switchval) {

		return switchDao.updateActiveControl(switchval.getId(),
				switchval.getActiveControl());
	}

	public Switch updateSceneId(Switch switchval) {
		return switchDao.updateSceneId(switchval.getId(),
				switchval.getSceneId());
	}

	public Switch updatePositionById(Switch switchval) {
		return switchDao.updatePositionById(switchval);
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
					if (switchDetails.get(i).getId().longValue() == ((Long) rowResult[0])
							.longValue()) {
						Long SceneId = (Long) rowResult[4];
						String sceneName = (String) rowResult[5];
						switchDetails.get(i).setScene(SceneId, sceneName);
						switchDetails.get(i).setScenecount(
								switchDetails.get(i).getScenecount() + 1);
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
					Integer dimmerControlValue = (Integer) rowResult[3];
					sw.setCurrentLightLevel(dimmerControlValue);
					Long SceneId = (Long) rowResult[4];
					String sceneName = (String) rowResult[5];
					sw.setScene(SceneId, sceneName);
					switchDetails.add(sw);
				}
			}

		}

 		return switchDetails;

	}

}
