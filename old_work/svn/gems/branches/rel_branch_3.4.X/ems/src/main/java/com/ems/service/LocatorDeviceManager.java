package com.ems.service;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
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
        
    
    public void addLocatorDevice(String name, String locatorDeviceType, Long estimatedBurnHours, String strFloorId,String strXaxis,String strYaxis) {
        
        locatorDeviceDao.addLocatorDevice(name, locatorDeviceType, estimatedBurnHours, strFloorId,strXaxis,strYaxis);
    }
    
    
    
    public void updateLocatorDevice(String id,String name, Long estimatedBurnHours) {
        
        locatorDeviceDao.updateLocatorDevice(id,name,estimatedBurnHours);
        
    }
    
    /**
     * delete locatorDevice details in database 
     * 
     * @param locatorDevice
     */
    public int deleteLocatorDevice(Long id) {
        return locatorDeviceDao.deleteLocatorDevice(id);
    }
    
    public HashMap<String, Long> getOtherDevicesCount()
    {
        List<Object[]> inventoryReportOtherDeviceList = locatorDeviceDao.getOtherDevicesCount();
        Long unManagedFixtureCnt=(long) 0;
        Long unManagedEmergencyFixtureCnt=(long) 0;
        Long totalOtherDeviceCnt=(long) 0;
        HashMap<String, Long> totalOtherDevices = new HashMap<String, Long>();
        if (inventoryReportOtherDeviceList != null && !inventoryReportOtherDeviceList.isEmpty()) {
            for (Iterator<Object[]> iterator = inventoryReportOtherDeviceList.iterator(); iterator.hasNext();) {
                Object[] object = (Object[]) iterator.next();
                String deviceType = (String) object[0];
                Long count = ((BigInteger) object[1]).longValue();
                if(deviceType!=null)
                {
                    if(deviceType.startsWith("Unmanaged_fixture"))
                    {
                    	unManagedFixtureCnt+=count;
                    }else if(deviceType.startsWith("Unmanaged_emergency_fixture"))
                    {
                    	unManagedEmergencyFixtureCnt+=count;
                    }
                }
                totalOtherDeviceCnt+=count;
            }
        }
        totalOtherDevices.put("Unmanaged Emergency Fixture", unManagedEmergencyFixtureCnt);
        totalOtherDevices.put("Unmanaged Fixture", unManagedFixtureCnt);
        totalOtherDevices.put("TotalCount", totalOtherDeviceCnt);
       return totalOtherDevices;
    }

}
