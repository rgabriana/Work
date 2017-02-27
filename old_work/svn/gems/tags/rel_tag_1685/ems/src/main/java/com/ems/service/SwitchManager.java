package com.ems.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
	public List<Switch> loadSwitchByAreaId(Long id) {
		return switchDao.loadSwitchByAreaId(id);
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

    public int countSwitchAttachedToFixturebyFixtureId(Long id) {
        return switchFixtureDao.countSwitchAttachedToFixturebyFixtureId(id);
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

	public void saveSceneLevel(List<SceneLevel> sceneLevels, Long switchId) {

		Set<Long> currentSwitchSceneFixtureIds = sceneLevelDao.loadSwitchSceneFixtures(switchId);
		for(SceneLevel sl: sceneLevels) {
			SceneLevel level = sceneLevelDao.loadLevelBySceneSwitchAndFixtureId(
					sl.getSceneId(), sl.getSwitchId(),
					sl.getFixtureId());
			if (level == null) {
				sceneDao.saveObject(sl);
			}
			currentSwitchSceneFixtureIds.remove(sl.getFixtureId());
		}
		for(Long fid: currentSwitchSceneFixtureIds) {
			List<SceneLevel> scenefixturelevels = sceneLevelDao.loadLevelsBySwitchAndFixtureId(switchId, fid);
			if(scenefixturelevels != null && scenefixturelevels.size() >0) {
				for(SceneLevel sl: scenefixturelevels) {
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

	public SwitchFixtures saveSwitchFixtures(SwitchFixtures switchFixture) {
		return (SwitchFixtures) switchFixtureDao.saveObject(switchFixture);
	}

	public void reassignSwitchFixturesList(List<SwitchFixtures> switchFixture) {
		if(switchFixture != null && switchFixture.size() > 0) {
			Long switchId = switchFixture.get(0).getSwitchId();
			List<SwitchFixtures> currentSwitchFixtures = loadSwitchFixturesBySwitchId(switchId);
			Set<Long> currentSwitchFixtureIds = new HashSet<Long>();
			if(currentSwitchFixtures != null && currentSwitchFixtures.size() > 0) {
				for(SwitchFixtures currentSF: currentSwitchFixtures) {
					currentSwitchFixtureIds.add(currentSF.getFixtureId());
				}
			}
			for (SwitchFixtures swtFxtr : switchFixture) {
				if(!currentSwitchFixtureIds.contains(swtFxtr.getFixtureId())) {
					switchFixtureDao.saveObject(swtFxtr);
				}
				currentSwitchFixtureIds.remove(swtFxtr.getFixtureId());
			}
			for (Long fixtureId: currentSwitchFixtureIds) {
				SwitchFixtures sf = switchFixtureDao.loadSwitchFixturebySwitchIdFixtureId(switchId, fixtureId);
				if(sf != null) {
					switchFixtureDao.removeObject(SwitchFixtures.class, sf.getId());
				}
			}
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
	
	public void deleteSwitchFixturesForFixture(long fixtureId){
		switchFixtureDao.deleteSwitchFixturesForFixture(fixtureId);
	}

	public Switch loadSwitchByNameandAreaId(String name, Long pid) {
		return switchDao.loadSwitchByNameandAreaId(name, pid);
		
	}
	
	
	public void updateSceneLevel(SceneLevel sceneLevel) {
		SceneLevel level = sceneLevelDao.loadLevelBySceneSwitchAndFixtureId(
				sceneLevel.getSceneId(), sceneLevel.getSwitchId(),
				sceneLevel.getFixtureId());
		if (level != null) {
			// Update scene level
			level.setLightLevel(sceneLevel.getLightLevel());
			sceneDao.saveObject(level);
		} else {
			// Create new scene level
			sceneDao.saveObject(sceneLevel);
		}
	}

}
