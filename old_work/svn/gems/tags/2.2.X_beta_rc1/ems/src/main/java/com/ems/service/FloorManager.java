package com.ems.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.rowset.serial.SerialBlob;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.dao.FloorDao;
import com.ems.dao.PlanMapDao;
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

    @Resource
    private PlanMapDao planMapDao;
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
    public Floor save(Floor floor) throws SQLException, IOException {
        if (floor.getId() != null && floor.getId() == 0) {
            floor.setId(null);
        }
        floor.setName(floor.getName().trim());
        floorDao.saveObject(floor);
        if (floor.getPlanMap() != null) {

            if (floor.getPlanMap().getId() != null) {
                PlanMap planMap = new PlanMap();
                planMap = planMapDao.loadPlanMapById(floor.getPlanMap().getId());
                byte[] data = new byte[(int) planMap.getPlan().length()];
                planMap.getPlan().getBinaryStream().read(data);
                floor.setByteImage(data);
            }
        }
        return floor;

    }

    // TODO: replace this method with getFloorbyID
    public Floor getFloorusingId(Long floorId) {
        return floorDao.getFloorById(floorId);
    }

    /**
     * update floor details.
     * 
     * @param floor
     *            com.ems.model.Floor
     */
    public Floor update(Floor floor) throws SQLException, IOException {
    	floor.setName(floor.getName().trim());
        floorDao.saveObject(floor);
        if (floor.getPlanMap() != null) {
            PlanMap planMap = new PlanMap();
            if (floor.getPlanMap().getId() != null) {
                planMap = planMapDao.loadPlanMapById(floor.getPlanMap().getId());
                byte[] data = new byte[(int) planMap.getPlan().length()];
                planMap.getPlan().getBinaryStream().read(data);
                floor.setByteImage(data);
            }
        }
        return floor;
    }

    //TODO not required.
    public void updateFloorForNameAndImage(Floor floor) {
    	floor.setName(floor.getName().trim());
        floorDao.saveObjectUpload(floor);
    }

    //TODO no use case found. getAllFloorsByBuildingId exists.
    /**
     * Load Floor
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.Floor collection load only id,name,description details of floor. other details loads as
     *         null
     */
    public List<Floor> loadFloorByBuildingId(Long id) throws SQLException, IOException {
        List<Floor> floors = floorDao.loadFloorByBuildingId(id);
        if (floors != null) {
            for (int i = 0; i < floors.size(); i++) {
                if (floors.get(i).getPlanMap() != null) {
                    if (floors.get(i).getPlanMap().getId() != null) {
                        PlanMap planMap = new PlanMap();
                        planMap = planMapDao.loadPlanMapById(floors.get(i).getPlanMap().getId());
                        byte[] data = new byte[(int) planMap.getPlan().length()];
                        planMap.getPlan().getBinaryStream().read(data);
                        floors.get(i).setByteImage(data);
                    }
                }
            }
        }
        return floors;
    }

    /**
     * Delete Floor details
     * 
     * @param id
     *            database id(primary key)
     */
    public void deleteFloor(Long id) {
        floorDao.removeObject(Floor.class, id);
    }

    public List<Floor> getAllFloorsByBuildingId(Long buildingId) throws SQLException, IOException {
        List<Floor> floors = floorDao.getAllFloorsByBuildingId(buildingId);
        // Use loadFloorByBuildingId in case you want plan map for all the floors.
        /*
         * if(floors!=null){ for(int i=0;i<floors.size();i++){ if(floors.get(i).getPlanMap()!=null){
         * if(floors.get(i).getPlanMap().getId()!=null){ PlanMap planMap=new PlanMap(); planMap=
         * planMapDao.loadPlanMapById(floors.get(i).getPlanMap().getId()); byte[] data=new
         * byte[(int)planMap.getPlan().length()]; planMap.getPlan().getBinaryStream().read(data);
         * floors.get(i).setByteImage(data); } } } }
         */
        return floors;
    }

    //TODO not required.
    public Floor updateFloorPlan(Long floorId, PlanMap planMap) {
        return floorDao.updateFloorPlan(floorId, planMap);
    }

    public Floor getFloorById(Long id) throws SQLException, IOException {
        Floor floor = floorDao.getFloorById(id);
        if (floor.getPlanMap() != null) {
            if (floor.getPlanMap().getId() != null) {
                PlanMap planMap = new PlanMap();
                planMap = planMapDao.loadPlanMapById(floor.getPlanMap().getId());
                byte[] data = new byte[(int) planMap.getPlan().length()];
                planMap.getPlan().getBinaryStream().read(data);
                floor.setByteImage(data);
            }
        }
        return floor;
    }

    //TODO no use case found. getFloorById exists.
    /**
     * load floor details by id
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.Floor
     */
    public Floor loadFloor(Long id) throws SQLException, IOException {
        Floor floor = floorDao.loadFloor(id);
        if (floor.getPlanMap().getId() != null) {
            if (floor.getPlanMap().getId() != null) {
                PlanMap planMap = new PlanMap();
                planMap = planMapDao.loadPlanMapById(floor.getPlanMap().getId());
                byte[] data = new byte[(int) planMap.getPlan().length()];
                planMap.getPlan().getBinaryStream().read(data);
                floor.setByteImage(data);
            }
        }
        return floor;
    }

    public Floor editName(Floor floor) {

        try {
            Floor floor1 = getFloorById(floor.getId());
            floor1.setName(floor.getName().trim());
            if (!floor.getPlanMap().getFileData().isEmpty()) {
                floor1.setFloorPlanUrl(floor.getPlanMap().getFileData().getOriginalFilename());
                Blob blob = new SerialBlob(floor.getPlanMap().getFileData().getBytes());
                PlanMap planMap = null;
                if (floor1.getPlanMap() != null) {
                    planMap = planMapManager.loadPlanMapById(floor1.getPlanMap().getId());
                    planMap.setPlan(blob);
                    planMapManager.update(planMap);
                } else {
                    planMap = new PlanMap();
                    planMap.setPlan(blob);
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

    //TODO no use case found.
    public Floor updateFields(Floor floor) {
        return floorDao.updateFields(floor);
    }

    /**
     * Check if UnCommissioned fixture available
     * 
     * @return
     */
    public boolean isUnCommissionedFixtureAvailable(Long id) {
        InventoryDeviceService inventoryDeviceService = (InventoryDeviceService) SpringContext
                .getBean("inventoryDeviceService");
        List<InventoryDevice> inventoryDevices = inventoryDeviceService.loadInventoryDeviceByFloorId(id);
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

    public void createFloor(Floor floor) {
        try {
            floor.setBuilding(buildingManager.getBuildingById(floor.getBuilding().getId()));
            floor.setProfileHandler(profileManager.loadProfileHandler(floor.getBuilding().getProfileHandler().getId()));
            floor.setName(floor.getName().trim());
            if (!floor.getPlanMap().getFileData().isEmpty()) {
                floor.setFloorPlanUrl(floor.getPlanMap().getFileData().getOriginalFilename());
                Blob blob = new SerialBlob(floor.getPlanMap().getFileData().getBytes());
                PlanMap planMap = new PlanMap();
                planMap.setPlan(blob);
                planMapManager.save(planMap);
                floor.setPlanMap(planMap);
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
                Blob blob = new SerialBlob(bytes);
                PlanMap planMap = new PlanMap();
                planMap.setPlan(blob);
                planMapManager.save(planMap);
                floor.setPlanMap(planMap);
            }
            floor = save(floor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateFloor(Floor floor) {
        try {
            Floor floor1 = getFloorById(floor.getId());
            floor1.setName(floor.getName().trim());
            floor1.setDescription(floor.getDescription());
            if (!floor.getPlanMap().getFileData().isEmpty()) {
                floor1.setFloorPlanUrl(floor.getPlanMap().getFileData().getOriginalFilename());
                Blob blob = new SerialBlob(floor.getPlanMap().getFileData().getBytes());
                PlanMap planMap = null;
                if (floor1.getPlanMap() != null) {
                    planMap = planMapManager.loadPlanMapById(floor1.getPlanMap().getId());
                    planMap.setPlan(blob);
                    planMapManager.update(planMap);
                } else {
                    planMap = new PlanMap();
                    planMap.setPlan(blob);
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
    
    public List getAllFloorsOfCompany() throws SQLException, IOException {
      List floors = floorDao.getAllFloorsOfCompany();
      // Use loadFloorByBuildingId in case you want plan map for all the floors.
      /*
       * if(floors!=null){ for(int i=0;i<floors.size();i++){ if(floors.get(i).getPlanMap()!=null){
       * if(floors.get(i).getPlanMap().getId()!=null){ PlanMap planMap=new PlanMap(); planMap=
       * planMapDao.loadPlanMapById(floors.get(i).getPlanMap().getId()); byte[] data=new
       * byte[(int)planMap.getPlan().length()]; planMap.getPlan().getBinaryStream().read(data);
       * floors.get(i).setByteImage(data); } } } }
       */
      return floors;
  }
}
