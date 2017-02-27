package com.ems.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.AreaDao;
import com.ems.dao.PlanMapDao;
import com.ems.model.Area;
import com.ems.model.PlanMap;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Service("areaManager")
@Transactional(propagation = Propagation.REQUIRED)
public class AreaManager {

    @Resource
    private AreaDao areaDao;

    @Resource
    private PlanMapDao planMapDao;

    /**
     * save Area details.
     * 
     * @param area
     *            com.ems.model.Area object.
     * @throws IOException
     * @throws SQLException
     * @throws IOException
     * @throws SQLException
     */
    public Area save(Area area) throws SQLException, IOException {
        if (area.getId() != null && area.getId() == 0) {
            area.setId(null);
        }
        area.setName(area.getName().trim());
        areaDao.saveObject(area);
        if (area.getPlanMap() != null) {
            if (area.getPlanMap().getId() != null) {
                PlanMap planMap = new PlanMap();
                planMap = planMapDao.loadPlanMapById(area.getPlanMap().getId());
                area.setByteImage(planMap.getPlan());
            }
        }
        return area;
    }

    /**
     * update Area details.
     * 
     * @param area
     *            com.ems.model.Area object
     * @throws SQLException
     * @throws IOException
     */
    public Area update(Area area) throws SQLException, IOException {
    	area.setName(area.getName().trim());
        areaDao.saveObject(area);
        if (area.getPlanMap() != null) {
            if (area.getPlanMap().getId() != null) {
                PlanMap planMap = new PlanMap();
                planMap = planMapDao.loadPlanMapById(area.getPlanMap().getId());
                area.setByteImage(planMap.getPlan());
            }
        }
        return area;
    }
    
    
    //TODO no use case found. getAllAreasByFloorId exists.
    /**
     * Load Area details.
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.Area collection. load only id,name,description details of area object. other details loads
     *         as null.
     * @throws SQLException
     * @throws IOException
     */
    public List<Area> loadAreaByFloorId(Long id) throws SQLException, IOException {
        List<Area> areas = areaDao.loadAreaByFloorId(id);
        if (areas != null) {
            for (int i = 0; i < areas.size(); i++) {
                if (areas.get(i).getPlanMap() != null) {
                    if (areas.get(i).getPlanMap().getId() != null) {
                        PlanMap planMap = new PlanMap();
                        planMap = planMapDao.loadPlanMapById(areas.get(i).getPlanMap().getId());
                        areas.get(i).setByteImage(planMap.getPlan());
                    }
                }
            }
        }
        return areas;
    }

    /**
     * Delete Area details
     * 
     * @param id
     *            database id(primary key)
     */
    public void deleteArea(Long id) {
        areaDao.removeObject(Area.class, id);
    }

    /**
     * load area details if id is given.
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.Area
     */
    public Area getAreaById(Long id) throws SQLException, IOException {
        Area area = (Area) areaDao.getObject(Area.class, id);
        if (area.getPlanMap() != null) {
            if (area.getPlanMap().getId() != null) {
                PlanMap planMap = new PlanMap();
                planMap = planMapDao.loadPlanMapById(area.getPlanMap().getId());
                area.setByteImage(planMap.getPlan());
            }
        }
        return area;
    }

    // TODO: replace with getAreadById
    public Area getAreaUsingId(Long id) {
        return (Area) areaDao.getObject(Area.class, id);
    }

    //TODO no use case found. getAreaById exists.
    /**
     * load area details by id
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.Area
     */
    public Area loadArea(Long id) throws SQLException, IOException {
        Area area = areaDao.loadArea(id);
        if (area.getPlanMap() != null) {
            if (area.getPlanMap().getId() != null) {
                PlanMap planMap = new PlanMap();
                planMap = planMapDao.loadPlanMapById(area.getPlanMap().getId());
                area.setByteImage(planMap.getPlan());
            }
        }
        return area;
    }

    public Area editName(Area area) {
    	area.setName(area.getName().trim());
        return areaDao.editName(area);
    }

    //TODO not required.
    public Area updateAreaPlan(Long areaId, PlanMap planMap) {
        return areaDao.updateAreaPlan(areaId, planMap);
    }

    public List<Area> getAllAreasByFloorId(Long floorId) {
        return areaDao.getAllAreasByFloorId(floorId);
    }

    public void updateArea(Area area) {
        try {
            Area area1 = getAreaById(area.getId());
            area1.setName(area.getName().trim());
            area1.setDescription(area.getDescription());
            area1.setZoneSensorEnable(area.getZoneSensorEnable());
            update(area1);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

	public List<Area> getAllZoneEnableAreas() {
		return areaDao.getAllZoneEnableAreas();
	}

}
