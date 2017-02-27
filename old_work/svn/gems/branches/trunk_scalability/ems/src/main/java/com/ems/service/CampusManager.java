package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.cache.annotation.CacheEvict;
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
    @CacheEvict(value = {"gateway_id", "fixture_id", "fixture_snap"}, allEntries = true)
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
    @CacheEvict(value = {"gateway_id", "fixture_id", "fixture_snap"}, allEntries = true)
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
    @CacheEvict(value = {"gateway_id", "fixture_id", "fixture_snap"}, allEntries = true)
    public void deleteCampus(Long id) {
        campusDao.removeObject(Campus.class, id);
    }

    @CacheEvict(value = {"gateway_id", "fixture_id", "fixture_snap"}, allEntries = true)
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
    	InventoryDeviceManager inventoryDeviceManager = (InventoryDeviceManager) SpringContext
                .getBean("inventoryDeviceManager");
        List<InventoryDevice> inventoryDevices = inventoryDeviceManager.loadInventoryDeviceByCampusId(id);
        if (inventoryDevices != null && !inventoryDevices.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    @CacheEvict(value = {"gateway_id", "fixture_id", "fixture_snap"}, allEntries = true)
    public void addCampus(Campus campus) {
        Company company = companyManager.getAllCompanies().get(0);
        campus.setProfileHandler(company.getProfileHandler());
        campus.setName(campus.getName().trim());
        company.getCampuses().add(campus);
    }

    @CacheEvict(value = {"gateway_id", "fixture_id", "fixture_snap"}, allEntries = true)
    public void updateCampus(Campus campus) {
        Campus campus1 = loadCampusById(campus.getId());
        campus1.setLocation(campus.getLocation());
        campus1.setName(campus.getName().trim());
        update(campus1);
    }
    
    public Campus getCampusByName(String name)
    {
    	return campusDao.getCampusByName(name);
    }

}
