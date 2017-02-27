package com.ems.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.dao.BuildingDao;
import com.ems.model.Building;
import com.ems.model.InventoryDevice;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Service("buildingManager")
@Transactional(propagation = Propagation.REQUIRED)
public class BuildingManager {

    @Resource
    private BuildingDao buildingDao;
    @Resource(name = "profileManager")
    private ProfileManager profileManager;
    @Resource(name = "campusManager")
    private CampusManager campusManager;

    /**
     * save building details.
     * 
     * @param building
     *            com.ems.model.Building object.
     */
    public Building save(Building building) throws SQLException, IOException {
        if (building.getId() != null && building.getId() == 0) {
            building.setId(null);
        }
        building.setName(building.getName().trim());
        return (Building) buildingDao.saveObject(building);
    }

    /**
     * update building details.
     * 
     * @param building
     *            com.ems.model.Building object
     */
    public Building update(Building building) {
    	building.setName(building.getName().trim());
        return (Building) buildingDao.saveObject(building);
    }

    //TODO no use case found. getAllBuildingsByCampusId already exists.
    /**
     * Load building by campus id
     * 
     * @param id
     *            Campus's database id.
     * @return com.ems.model.Building collection. load only id,name details of building. other details loads as null.
     */
    public List<Building> loadBuildingByCampusId(Long id) {
        return buildingDao.loadBuildingByCampusId(id);
    }

    /**
     * Delete Building details
     * 
     * @param id
     *            database id(primary key)
     */
    public void deleteBuilding(Long id) {
        buildingDao.removeObject(Building.class, id);
    }

    public List<Building> getAllBuildingsByCampusId(Long campusId) {
        return buildingDao.getAllBuildingsByCampusId(campusId);
    }

    public Building getBuildingById(Long id) {
        return buildingDao.getBuildingById(id);
    }
    
    public Building getBuildingByUid(String uid) {
      return buildingDao.getBuildingByUid(uid);
  }

    //TODO no use case found. getBuildingById already exists.
    /**
     * Load Building if id given
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.Building
     */
    public Building loadBuilding(Long id) {
        return buildingDao.loadBuilding(id);
    }

    public Building editName(Building building) {
    	Building b1 = getBuildingById(building.getId());
    	b1.setName(building.getName().trim());
    	b1.setUseOrgLocation(building.getUseOrgLocation());
    	b1.setLatitude(building.getLatitude());
    	b1.setLongitude(building.getLongitude());
        return update(b1);
    }

    /**
     * Check if UnCommissioned fixture available
     * 
     * @return
     */
    public boolean isUnCommissionedFixtureAvailable(Long id) {
        InventoryDeviceService inventoryDeviceService = (InventoryDeviceService) SpringContext
                .getBean("inventoryDeviceService");
        List<InventoryDevice> inventoryDevices = inventoryDeviceService.loadInventoryDeviceByBuildingId(id);
        if (inventoryDevices != null && !inventoryDevices.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public Building createBuilding(Building building) {
        building.setCampus(campusManager.loadCampusById(Long.valueOf(building.getCampus().getId())));
        building.setProfileHandler(profileManager.loadProfileHandler(building.getCampus().getProfileHandler().getId()));
        building.setName(building.getName().trim());
        Building savedBuilding = null;
        try {
            savedBuilding = save(building);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return savedBuilding;
    }
    
    public Long getBuildingCount() {
    	return buildingDao.getBuildingCount();
    }

    public Building getBuildingByNameAndCampusId(String bldgName, Long campusId)
    {
    	return buildingDao.getBuildingByNameAndCampusId(bldgName, campusId);
    }
}
