package com.ems.service;

import javax.annotation.Resource;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.DeviceDao;
import com.ems.model.Device;

@Service("deviceManager")
@Transactional(propagation = Propagation.REQUIRED)
public class DeviceManager {
	
	@Resource
	DeviceDao deviceDao;
	
	public Device getDeviceBySnapAddress(String snapAddress) {
		return deviceDao.getDeviceBySnapAddress(snapAddress);
	}
	
	@CacheEvict(value = {"gateway_id", "fixture_id", "fixture_snap"}, allEntries=true)
	public void update(Device device) {
		deviceDao.update(device);
	}

}
