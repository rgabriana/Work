package com.ems.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.InventoryDeviceDao;
import com.ems.model.InventoryDevice;

@Service("inventoryDeviceService")
@Transactional(propagation = Propagation.REQUIRED)
public class InventoryDeviceService {

    @Resource
    InventoryDeviceDao inventoryDeviceDao;

    public InventoryDevice getInventoryDeviceBysnapAddr(String snapAddr) {
        return inventoryDeviceDao.getInventoryDeviceBysnapAddr(snapAddr);
    }

    public InventoryDevice getInventoryDeviceByMacAddr(String macAddr) {
        return inventoryDeviceDao.getInventoryDeviceByMacAddr(macAddr);
    }

    synchronized public void addInventoryDevice(InventoryDevice device) {

        inventoryDeviceDao.addInventoryDevice(device);

    }

    /**
     * Load all InventoryDevice associated with given floor
     * 
     * @param id
     *            floor id
     * @return Collection of InventoryDevice
     */
    public List<InventoryDevice> loadInventoryDeviceByFloorId(Long id) {
        System.out.println("loadinventorydevicebyfloorid called -- " + id.longValue());
        List<InventoryDevice> oList = new ArrayList<InventoryDevice>();
        List<InventoryDevice> ofetchList = inventoryDeviceDao.loadInventoryDeviceByFloorId(id);
        if (ofetchList != null)
            oList.addAll(ofetchList);
        return oList;
    }

    /**
     * Load all InventoryDevice associated with given building
     * 
     * @param id
     *            building id
     * @return Collection of InventoryDevice
     */
    public List<InventoryDevice> loadInventoryDeviceByBuildingId(Long id) {
        return inventoryDeviceDao.loadInventoryDeviceByBuildingId(id);
    }

    /**
     * Load all InventoryDevice associated with given campus
     * 
     * @param id
     *            campus id
     * @return Collection of InventoryDevice
     */
    public List<InventoryDevice> loadInventoryDeviceByCampusId(Long id) {
        return inventoryDeviceDao.loadInventoryDeviceByCampusId(id);
    }

    /**
     * Load all InventoryDevice
     * 
     * @return Collection of InventoryDevice
     */
    public List<InventoryDevice> loadAllInventoryDevice() {
        return inventoryDeviceDao.loadAllInventoryDevice();
    }

    /**
     * Load all InventoryDevice by type
     * 
     * @return Collection of InventoryDevice
     */
    @SuppressWarnings("unchecked")
    public List<InventoryDevice> loadAllInventoryDeviceByType(Integer deviceType) {
        return inventoryDeviceDao.loadAllInventoryDeviceByType(deviceType);
    }

    /**
     * delete InventoryDevice from database
     * 
     * @param id
     */
    public void deleteInventoryDevice(Long id) {
        inventoryDeviceDao.removeObject(InventoryDevice.class, id);
    }

    public InventoryDevice getIventoryDeviceById(Long id) {

        return (InventoryDevice) inventoryDeviceDao.getObject(InventoryDevice.class, id);

    }

    public void deleteUnplacedFixtures(long gwId) {

        inventoryDeviceDao.deleteInventoryDevices(gwId);

    } // end of method deleteUnplacedFixtures

}
