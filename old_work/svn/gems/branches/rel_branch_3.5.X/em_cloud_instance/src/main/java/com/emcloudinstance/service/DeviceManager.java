/**
 * 
 */
package com.emcloudinstance.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emcloudinstance.dao.DeviceDao;
import com.emcloudinstance.vo.Fixture;

/**
 * @author yogesh
 *
 */
@Service("deviceManager")
@Transactional(propagation = Propagation.REQUIRED)
public class DeviceManager {
	@Resource
	DeviceDao deviceDao;
	
	
	public List<Fixture> loadFixtures(String emMac, String property, Long pid, String limit) {
		return deviceDao.loadFixtures(emMac, property, pid, limit);
	}
}
