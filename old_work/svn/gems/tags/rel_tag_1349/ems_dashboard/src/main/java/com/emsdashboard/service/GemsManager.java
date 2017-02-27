package com.emsdashboard.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emsdashboard.dao.GemsDao;
import com.emsdashboard.model.GemsServer;

@Service("gemsManager")
@Transactional(propagation = Propagation.REQUIRED)
public class GemsManager {

	@Resource
	private GemsDao gemsDao;

	public void saveGEMSData(String data) {
	    gemsDao.saveGEMSData(data);
	}
	
	public void saveGEMSData(GemsServer gemsToSave) {
	    gemsDao.saveGEMSData(gemsToSave);
    }
	public List<GemsServer> loadGEMSData() {
		return gemsDao.loadGEMSData();
	}

	public void removeGEMSData(long nodeValue) {
	    gemsDao.removeGEMSData(nodeValue);
	}
	public String getGEMSVersion(long nodeValue) {
        return gemsDao.getGEMSVersion(nodeValue);
    }
	
	/**
     * Load GEMS object if id is given
     * 
     * @param id
     *            database id(primary key)
     * @return com.emsdashboard.model.GemsServer object
     */
    public GemsServer loadGEMSById(Long id) {
        return (GemsServer) gemsDao.loadGEMSById(id);
    }
    /**
     * Load GEMS object if ip is given
     * 
     * @param ip
     *            
     * @return com.emsdashboard.model.GemsServer object
     */
    public GemsServer loadGEMSByGemsIp(String gemsIp) {
        return (GemsServer) gemsDao.loadGEMSByGemsIp(gemsIp);
    }
    /**
     * Save gems server data to database
     * 
     * @param Gems Server
     *            
     * @return com.emsdashboard.model.GemsServer object
     */
    public void saveGEMSDataWithApiKey(GemsServer gemsToSave) {
	    gemsDao.saveGEMSDataWithApiKey(gemsToSave);
    }

    /**
     * Activate GEM
     * 
     * @param Gems ID
     *            
     * @return
     */
    public void activateGEMS(Long gemID) {
        gemsDao.activateGEMS(gemID);
    }
    
    /**
     * DeActivate GEM
     * 
     * @param Gems ID
     *            
     * @return
     */
    public void deActivateGEMS(Long gemID) {
        gemsDao.deActivateGEMS(gemID);
    }
    
    /**
     * Update GEM
     * 
     * @param Gems Server
     *            
     * @return
     */

    public void updateGEMSData(GemsServer gemsToSave) {
        gemsDao.updateGEMSData(gemsToSave);
    }
    public String getGEMSStatus(String gemsIp) {
	   return gemsDao.getGEMSStatus(gemsIp);
	}

}
