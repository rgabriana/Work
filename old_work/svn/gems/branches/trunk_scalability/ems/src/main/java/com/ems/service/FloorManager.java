package com.ems.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.dao.FloorDao;
import com.ems.model.Floor;
import com.ems.model.InventoryDevice;
import com.ems.model.PlanMap;
import com.ems.server.ServerMain;
import com.ems.server.device.DeviceServiceImpl;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Service("floorManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FloorManager {

    @Resource
    private FloorDao floorDao;

    @Resource(name = "profileManager")
    private ProfileManager profileManager;
    @Resource(name = "buildingManager")
    private BuildingManager buildingManager;
    @Resource(name = "planMapManager")
    private PlanMapManager planMapManager;

    /**
     * save floor details.
     * 
     * @param floor
     *            com.ems.model.Floor
     */
    @CacheEvict(value = {"gateway_id", "fixture_id", "fixture_snap"}, allEntries = true)
    public Floor save(Floor floor) throws SQLException, IOException {
        if (floor.getId() != null && floor.getId() == 0) {
            floor.setId(null);
        }
        floor.setName(floor.getName().trim());
        floorDao.saveObject(floor);
        if (floor.getPlanMap() != null) {

            if (floor.getPlanMap().getId() != null) {
                PlanMap planMap = new PlanMap();
                planMap = planMapManager.loadPlanMapById(floor.getPlanMap().getId());
                floor.setByteImage(planMap.getPlan());
            }
        }
        return floor;

    }

    public Floor getFloorusingId(Long floorId) {
        return floorDao.getFloorById(floorId);
    }

    /**
     * update floor details.
     * 
     * @param floor
     *            com.ems.model.Floor
     */
    @CacheEvict(value = {"gateway_id", "fixture_id", "fixture_snap"}, allEntries = true)
    public Floor update(Floor floor) throws SQLException, IOException {
    	floor.setName(floor.getName().trim());
        floorDao.saveObject(floor);
        if (floor.getPlanMap() != null) {
            PlanMap planMap = new PlanMap();
            if (floor.getPlanMap().getId() != null) {
                planMap = planMapManager.loadPlanMapById(floor.getPlanMap().getId());
                floor.setByteImage(planMap.getPlan());
            }
        }
        return floor;
    }

    /**
     * Delete Floor details
     * 
     * @param id
     *            database id(primary key)
     */
    @CacheEvict(value = {"gateway_id", "fixture_id", "fixture_snap"}, allEntries = true)
    public void deleteFloor(Long id) {
        floorDao.removeObject(Floor.class, id);
    }

    public List<Floor> getAllFloorsByBuildingId(Long buildingId) throws SQLException, IOException {
        List<Floor> floors = floorDao.getAllFloorsByBuildingId(buildingId);
        return floors;
    }

    public Floor getFloorById(Long id) throws SQLException, IOException {
        Floor floor = floorDao.getFloorById(id);
        if (floor.getPlanMap() != null) {
            if (floor.getPlanMap().getId() != null) {
                PlanMap planMap = new PlanMap();
                planMap = planMapManager.loadPlanMapById(floor.getPlanMap().getId());
                floor.setByteImage(planMap.getPlan());
            }
        }
        return floor;
    }

    public Floor getFloorByNameAndBuildingId(String floorName, Long bldgId) throws SQLException, IOException {
        Floor floor = floorDao.getFloorByNameAndBuildingId(floorName, bldgId);
        if (floor != null && floor.getPlanMap() != null) {
            if (floor.getPlanMap().getId() != null) {
                PlanMap planMap = new PlanMap();
                planMap = planMapManager.loadPlanMapById(floor.getPlanMap().getId());
                floor.setByteImage(planMap.getPlan());
            }
        }
        return floor;
    }

    @CacheEvict(value = {"gateway_id", "fixture_id", "fixture_snap"}, allEntries = true)
    public Floor editName(Floor floor) {

        try {
            Floor floor1 = getFloorById(floor.getId());
            floor1.setName(floor.getName().trim());
            if (!floor.getPlanMap().getFileData().isEmpty()) {
                floor1.setFloorPlanUrl(floor.getPlanMap().getFileData().getOriginalFilename());
                PlanMap planMap = null;
                if (floor1.getPlanMap() != null) {
                    planMap = planMapManager.loadPlanMapById(floor1.getPlanMap().getId());
                    planMap.setPlan(floor.getPlanMap().getFileData().getBytes());
                    planMapManager.update(planMap);
                } else {
                    planMap = new PlanMap();
                    planMap.setPlan(floor.getPlanMap().getFileData().getBytes());
                    planMapManager.save(planMap);
                }

                floor1.setPlanMap(planMap);
            }
            floor = update(floor1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return floor;
    }

    /**
     * Check if UnCommissioned fixture available
     * 
     * @return
     */
    public boolean isUnCommissionedFixtureAvailable(Long id) {
    	InventoryDeviceManager inventoryDeviceManager = (InventoryDeviceManager) SpringContext
                .getBean("inventoryDeviceManager");
        List<InventoryDevice> inventoryDevices = inventoryDeviceManager.loadInventoryDeviceByFloorId(id);
        if (inventoryDevices != null && !inventoryDevices.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public void getCurrentDetails(Long floorId) {
        //System.out.println("Inside method getCurrentDetails().....");
        DeviceServiceImpl.getInstance().getRealTimeStatus(floorId);
    }

    @CacheEvict(value = {"gateway_id", "fixture_id", "fixture_snap"}, allEntries = true)
    public Floor createFloor(Floor floor) {
    	Floor savedFloor = null;
        try {
            floor.setBuilding(buildingManager.getBuildingById(floor.getBuilding().getId()));
            floor.setProfileHandler(profileManager.loadProfileHandler(floor.getBuilding().getProfileHandler().getId()));
            floor.setName(floor.getName().trim());
            if (floor.getPlanMap() != null && !floor.getPlanMap().getFileData().isEmpty()) {
                floor.setFloorPlanUrl(floor.getPlanMap().getFileData().getOriginalFilename());
                PlanMap planMap = new PlanMap();
                planMap.setPlan(floor.getPlanMap().getFileData().getBytes());
                planMapManager.save(planMap);
                floor.setPlanMap(planMap);
                floor.setUploadedOn(new Date());
            } else {
            	String tomcatLocation = ServerMain.getInstance().getTomcatLocation();
                File default_image = new File(tomcatLocation + "/themes/default/images/default_floor_plan.gif");
                FileInputStream fis = new FileInputStream(default_image);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                try {
                    for (int readNum; (readNum = fis.read(buf)) != -1;) {
                        bos.write(buf, 0, readNum);
                    }
                } catch (IOException ex) {
                   ex.printStackTrace();
                }
                floor.setFloorPlanUrl(default_image.getName());
                byte[] bytes = bos.toByteArray();
                PlanMap planMap = new PlanMap();
                planMap.setPlan(bytes);
                planMapManager.save(planMap);
                floor.setPlanMap(planMap);
            }
            savedFloor = save(floor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return savedFloor;
    }

    @CacheEvict(value = {"gateway_id", "fixture_id", "fixture_snap"}, allEntries = true)
    public void updateFloor(Floor floor) {
        try {
            Floor floor1 = getFloorById(floor.getId());
            floor1.setName(floor.getName().trim());
            floor1.setDescription(floor.getDescription());
            if (!floor.getPlanMap().getFileData().isEmpty()) {
                floor1.setFloorPlanUrl(floor.getPlanMap().getFileData().getOriginalFilename());
                PlanMap planMap = null;
                if (floor1.getPlanMap() != null) {
                    planMap = planMapManager.loadPlanMapById(floor1.getPlanMap().getId());
                    planMap.setPlan(floor.getPlanMap().getFileData().getBytes());
                    planMapManager.update(planMap);
                } else {
                    planMap = new PlanMap();
                    planMap.setPlan(floor.getPlanMap().getFileData().getBytes());
                    planMapManager.save(planMap);
                }

                floor1.setPlanMap(planMap);
            }
            floor = update(floor1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Long getFloorCount() {
    	return floorDao.getFloorCount();
    }
    
    @SuppressWarnings("rawtypes")
	public List getAllFloorsOfCompany() throws SQLException, IOException {
      List floors = floorDao.getAllFloorsOfCompany();
      // Use loadFloorByBuildingId in case you want plan map for all the floors.
      return floors;
  }
}
