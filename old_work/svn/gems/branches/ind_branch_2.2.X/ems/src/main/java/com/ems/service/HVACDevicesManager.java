/**
 * 
 */
package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.HVACDevicesDao;
import com.ems.model.HVACDevice;

/**
 * @author yogesh
 * 
 */
@Service("hvacDevicesManager")
@Transactional(propagation = Propagation.REQUIRED)
public class HVACDevicesManager {
	private static final Logger logger = Logger.getLogger("HVACLogger");

	@Resource
	HVACDevicesDao hvacDevicesDao;

	public void save(HVACDevice oDevice) {
		hvacDevicesDao.saveObject(oDevice);
	}

	public List<HVACDevice> loadHVACDevicesByFacilityId(String facility,
			Long facilityId) {
		return hvacDevicesDao.loadHVACDevicesByFloor(facility, facilityId);

	}

	public void updateGatewayPosition(HVACDevice hvDevice) {
		hvacDevicesDao.updateGatewayPosition(hvDevice);
	}

	public int deleteHvacDevice(Long id) {
		int iStatus = 0;
		try {
			hvacDevicesDao.removeObject(HVACDevice.class, id);
			iStatus = 1;
		} catch (Exception orfe) {
			orfe.printStackTrace();
		}
		return iStatus;
	}

	public HVACDevice loadHvacByUserName(String hvacname) {
		return hvacDevicesDao.loadHvacByUserName(hvacname);
	}

	public HVACDevice loadHvacById(Long hvacId) {
		return hvacDevicesDao.loadHvacById(hvacId);
		
	}
}
