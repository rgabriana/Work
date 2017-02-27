package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.dao.CampusDao;
import com.ems.model.Campus;
import com.ems.model.Company;
import com.ems.model.InventoryDevice;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Service("campusManager")
@Transactional(propagation = Propagation.REQUIRED)
public class CampusManager {

    @Resource
    private CampusDao campusDao;
    @Resource(name = "companyManager")
    private CompanyManager companyManager;

    /**
     * save campus details.
     * 
     * @param campus
     *            com.ems.model.Campus
     */
    public Campus save(Campus campus) {
        if (campus.getId() != null && campus.getId() == 0) {
            campus.setId(null);
        }
        campus.setName(campus.getName().trim());
        return (Campus) campusDao.saveObject(campus);
    }

    /**
     * update campus details.
     * 
     * @param campus
     *            com.ems.model.Campus
     */
    public Campus update(Campus campus) {
    	campus.setName(campus.getName().trim());
        return (Campus) campusDao.saveObject(campus);
    }

    /**
     * load Campus
     * 
     * @param id
     *            Database id(primary key)
     * @return com.ems.model.Campus object
     */
    public Campus loadCampusById(Long id) {
        return campusDao.loadCampusById(id);
    }

    /**
     * Delete Campus details
     * 
     * @param id
     *            database id(primary key)
     */
    public void deleteCampus(Long id) {
        campusDao.removeObject(Campus.class, id);
    }

    public Campus editName(Campus campus) {
    	campus.setName(campus.getName().trim());
        return campusDao.editName(campus);
    }

    /**
     * Check if UnCommissioned fixture available
     * 
     * @return
     */
    public boolean isUnCommissionedFixtureAvailable(Long id) {
        InventoryDeviceService inventoryDeviceService = (InventoryDeviceService) SpringContext
                .getBean("inventoryDeviceService");
        List<InventoryDevice> inventoryDevices = inventoryDeviceService.loadInventoryDeviceByCampusId(id);
        if (inventoryDevices != null && !inventoryDevices.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public void addCampus(Campus campus) {
        Company company = companyManager.getAllCompanies().get(0);
        campus.setProfileHandler(company.getProfileHandler());
        campus.setName(campus.getName().trim());
        company.getCampuses().add(campus);
    }

    public void updateCampus(Campus campus) {
        Campus campus1 = loadCampusById(campus.getId());
        campus1.setLocation(campus.getLocation());
        campus1.setName(campus.getName().trim());
        update(campus1);
    }

}
