package com.ems.dao;


import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Building;
import com.ems.model.Floor;
import com.ems.model.LocatorDevice;
import com.ems.service.BuildingManager;
import com.ems.service.FloorManager;
import com.ems.types.LocatorDeviceType;
import com.ems.utils.ArgumentUtils;

@Repository("locatorDeviceDao")
@Transactional(propagation = Propagation.REQUIRED)
public class LocatorDeviceDao extends BaseDaoHibernate{
    
    static final Logger logger = Logger.getLogger(LocatorDeviceDao.class.getName());
    
    @Resource
    private FloorManager floorManager;
    
    @Resource
    private BuildingManager buildingManager;

    /**
     * Return LocatorDevices details if floorId given
     * 
     * @param floorId
     * @return com.ems.model.LocatorDevice
     */
    @SuppressWarnings("unchecked")
    public List<LocatorDevice> loadLocatorDevicesByFloorId(Long floorId) {
        Session session = getSession();
        List<LocatorDevice> locatorDeviceList = session.createCriteria(LocatorDevice.class).add(Restrictions.eq("floor.id", floorId))
                .list();
        if (!ArgumentUtils.isNullOrEmpty(locatorDeviceList)) {
            return locatorDeviceList;
        } else {
            return null;
        }

    }
    
    /**
     * Return LocatorDevices details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.LocatorDevice
     */
    @SuppressWarnings("unchecked")
    public List<LocatorDevice> loadLocatorDevicesByCampusId(Long campusId) {
        Session session = getSession();
        List<LocatorDevice> locatorDeviceList = session.createCriteria(LocatorDevice.class).add(Restrictions.eq("campusId", campusId))
                .list();
        if (!ArgumentUtils.isNullOrEmpty(locatorDeviceList)) {
            return locatorDeviceList;
        } else {
            return null;
        }

    }
    
    /**
     * Return LocatorDevices details if buildingId given
     * 
     * @param buildingId
     * @return com.ems.model.LocatorDevice
     */
    @SuppressWarnings("unchecked")
    public List<LocatorDevice> loadLocatorDevicesByBuildingId(Long buildingId) {
        Session session = getSession();
        List<LocatorDevice> locatorDeviceList = session.createCriteria(LocatorDevice.class).add(Restrictions.eq("buildingId", buildingId))
                .list();
        if (!ArgumentUtils.isNullOrEmpty(locatorDeviceList)) {
            return locatorDeviceList;
        } else {
            return null;
        }

    }
    
    /**
     * Return All LocatorDevices details 
     * 
     * 
     * @return com.ems.model.LocatorDevice
     */
    @SuppressWarnings("unchecked")
    public List<LocatorDevice> loadAllLocatorDevices() {
        Session session = getSession();
        List<LocatorDevice> locatorDeviceList = session.createCriteria(LocatorDevice.class)
                .list();
        if (!ArgumentUtils.isNullOrEmpty(locatorDeviceList)) {
            return locatorDeviceList;
        } else {
            return null;
        }

    }
    
    public int deleteLocatorDevice(Long id){
        int iStatus = 0;
        LocatorDevice locatorDevice = null;
        try {
            locatorDevice = (LocatorDevice) getObject(LocatorDevice.class, id);
        } catch (ObjectRetrievalFailureException orfe) {
            orfe.printStackTrace();
        }
        if (locatorDevice != null) {
                   
          removeObject(LocatorDevice.class, id);
          iStatus = 1;
        }
        return iStatus;
    }
    
    public void updateLocatorDevice(String Id, String name) {
        Long id = Long.parseLong(Id);
        Session session = getSession();
        LocatorDevice locatorDeviceval = (LocatorDevice) session.load(LocatorDevice.class, id);
        locatorDeviceval.setName(name);
        session.saveOrUpdate(locatorDeviceval);
        
    }
    
    public void addLocatorDevice(String name, String locatorDeviceType, String strFloorId,String strXaxis,String strYaxis) {
        
        Long floorId = Long.parseLong(strFloorId);
        Integer xaxis = Integer.parseInt(strXaxis);
        Integer yaxis = Integer.parseInt(strYaxis);
        
        LocatorDevice locatorDeviceObj = new LocatorDevice();
        
        locatorDeviceObj.setXaxis(xaxis);
        locatorDeviceObj.setYaxis(yaxis);
        
        Floor floor = null;
		try {
			floor = floorManager.getFloorById(floorId);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        locatorDeviceObj.setFloor(floor);
        
        long buildingId = floor.getBuilding().getId();
        locatorDeviceObj.setBuildingId(buildingId);
        Building building = buildingManager.getBuildingById(buildingId);
        locatorDeviceObj.setCampusId(building.getCampus().getId());
        // Set Locator Device location.
        String location = "";
        try {
            location = floor.getName();
            location = building.getName() + " -> " + location;
            location = building.getCampus().getName() + " -> " + location;
            locatorDeviceObj.setLocation(location);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        locatorDeviceObj.setName(name);
        locatorDeviceObj.setLocatorDeviceType(LocatorDeviceType.valueOf(locatorDeviceType));
        saveObject(locatorDeviceObj);
    }
    
    @SuppressWarnings("unchecked")
    public LocatorDevice getLocatorDeviceById(Long id) {
        Session session = getSession();
        List<LocatorDevice> locatorDeviceList = session.createCriteria(LocatorDevice.class).add(Restrictions.eq("id", id))
                .list();
        if (!ArgumentUtils.isNullOrEmpty(locatorDeviceList)) {
            return locatorDeviceList.get(0);
        } else {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public LocatorDevice getLocatorDeviceByNameandFloorId(String name,Long floorId) {
                
        Session session = getSession();
        List<LocatorDevice> locatorDeviceList = session.createCriteria(LocatorDevice.class).add(Restrictions.eq("floor.id", floorId)).add(Restrictions.eq("name", name))
                .list();
        if (!ArgumentUtils.isNullOrEmpty(locatorDeviceList)) {
            return locatorDeviceList.get(0);
        } else {
            return null;
        }
    }
    
    public LocatorDevice updatePositionById(LocatorDevice locatorDeviceval) {
        Session session = getSession();
        LocatorDevice oLocatorDevice = (LocatorDevice) session.load(LocatorDevice.class, locatorDeviceval.getId());
        oLocatorDevice.setXaxis(locatorDeviceval.getXaxis());
        oLocatorDevice.setYaxis(locatorDeviceval.getYaxis());
        session.saveOrUpdate("xaxis", oLocatorDevice);
        session.saveOrUpdate("yaxis", oLocatorDevice);
        return oLocatorDevice;
    }
        
}