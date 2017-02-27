package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.LocatorDeviceDao;
import com.ems.model.LocatorDevice;


@Service("locatorDeviceManager")
@Transactional(propagation = Propagation.REQUIRED)
public class LocatorDeviceManager {
    
    @Resource
    private LocatorDeviceDao locatorDeviceDao;
    
    public List<LocatorDevice> loadLocatorDevicesByFloorId(Long id) {
        return locatorDeviceDao.loadLocatorDevicesByFloorId(id);
    }
    
    public List<LocatorDevice> loadLocatorDevicesByCampusId(Long id) {
        return locatorDeviceDao.loadLocatorDevicesByCampusId(id);
    }
    
    public List<LocatorDevice> loadLocatorDevicesByBuldingId(Long id) {
        return locatorDeviceDao.loadLocatorDevicesByBuildingId(id);
    }
    
    public List<LocatorDevice> loadAllLocatorDevices() {
        return locatorDeviceDao.loadAllLocatorDevices();
    }
    
    public LocatorDevice getLocatorDeviceByNameandFloorId(String name,Long floorId) {
        return locatorDeviceDao.getLocatorDeviceByNameandFloorId(name,floorId);
    }
    
    public LocatorDevice getLocatorDeviceById(Long id) {
        return locatorDeviceDao.getLocatorDeviceById(id);
    }
    
    public LocatorDevice updatePositionById(LocatorDevice locatorDeviceval) {
        return locatorDeviceDao.updatePositionById(locatorDeviceval);
    }
        
    
    public void addLocatorDevice(String name, String locatorDeviceType, String strFloorId,String strXaxis,String strYaxis) {
        
        locatorDeviceDao.addLocatorDevice(name, locatorDeviceType, strFloorId,strXaxis,strYaxis);
    }
    
    
    
    public void updateLocatorDevice(String id,String name) {
        
        locatorDeviceDao.updateLocatorDevice(id,name);
        
    }
    
    /**
     * delete locatorDevice details in database 
     * 
     * @param locatorDevice
     */
    public int deleteLocatorDevice(Long id) {
        return locatorDeviceDao.deleteLocatorDevice(id);
    }
    

}
