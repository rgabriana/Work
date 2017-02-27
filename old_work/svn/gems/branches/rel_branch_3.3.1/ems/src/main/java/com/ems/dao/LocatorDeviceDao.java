package com.ems.dao;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Building;
import com.ems.model.Floor;
import com.ems.model.LocatorDevice;
import com.ems.types.LocatorDeviceType;
import com.ems.utils.ArgumentUtils;

@Repository("locatorDeviceDao")
@Transactional(propagation = Propagation.REQUIRED)
public class LocatorDeviceDao extends BaseDaoHibernate{
    
    static final Logger logger = Logger.getLogger(LocatorDeviceDao.class.getName());
    
    @Resource
    private FloorDao floorDao;
    
    @Resource
    private BuildingDao buildingDao;

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
    
    public void updateLocatorDevice(String Id, String name, Long estimatedBurnHours) {
        Long id = Long.parseLong(Id);
        Session session = getSession();
        LocatorDevice locatorDeviceval = (LocatorDevice) session.load(LocatorDevice.class, id);
        locatorDeviceval.setName(name);
        if(locatorDeviceval.getLocatorDeviceType() == LocatorDeviceType.Unmanaged_fixture){
        	locatorDeviceval.setEstimatedBurnHours(estimatedBurnHours);
        }
        session.saveOrUpdate(locatorDeviceval);
        
    }
    
    public void addLocatorDevice(String name, String locatorDeviceType, Long estimatedBurnHours, String strFloorId,String strXaxis,String strYaxis) {
        
        Long floorId = Long.parseLong(strFloorId);
        Integer xaxis = Integer.parseInt(strXaxis);
        Integer yaxis = Integer.parseInt(strYaxis);
        
        LocatorDevice locatorDeviceObj = new LocatorDevice();
        
        locatorDeviceObj.setXaxis(xaxis);
        locatorDeviceObj.setYaxis(yaxis);
        
        Floor floor = floorDao.getFloorById(floorId);
        locatorDeviceObj.setFloor(floor);
        
        long buildingId = floor.getBuilding().getId();
        locatorDeviceObj.setBuildingId(buildingId);
        Building building = buildingDao.getBuildingById(buildingId);
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
        if(LocatorDeviceType.valueOf(locatorDeviceType) == LocatorDeviceType.Unmanaged_fixture){
        	locatorDeviceObj.setEstimatedBurnHours(estimatedBurnHours);
        }
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
    
    public List<Object[]> getOtherDevicesCount()
    {
    	String hql = "select ld.locator_device_type as deviceType, count(ld.id) as unmanagedEmergencyFixture FROM locator_device ld WHERE ld.locator_device_type ='Unmanaged_emergency_fixture' or ld.locator_device_type ='Unmanaged_fixture' group by ld.locator_device_type;";
        Session s = getSession();
        Query q = s.createSQLQuery(hql);
        List<Object[]> oList = q.list();
        List<Object[]> result = new ArrayList<Object[]>();
        if (oList != null && !oList.isEmpty()) {
            Iterator<Object[]> iterator = oList.iterator();
            while (iterator.hasNext()) {
                Object[] itrObject = (Object[]) iterator.next();
                Object[] otherDeviceObj = new Object[2];
                otherDeviceObj[0] = (String) itrObject[0];
                otherDeviceObj[1] = ((BigInteger) itrObject[1]).longValue();
                result.add(otherDeviceObj);
            }
        }
        return oList;  
    }
        
}