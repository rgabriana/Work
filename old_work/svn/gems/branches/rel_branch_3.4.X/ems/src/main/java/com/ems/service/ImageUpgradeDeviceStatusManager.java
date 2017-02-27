package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.ImageUpgradeDeviceStatusDao;
import com.ems.model.ImageUpgradeDeviceStatus;
import com.ems.model.ImageUpgradeDeviceStatusList;


@Service("imageUpgradeDeviceStatusManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ImageUpgradeDeviceStatusManager {
	
	@Resource
	ImageUpgradeDeviceStatusDao imageUpgradeDeviceStatusDao;

	public List<ImageUpgradeDeviceStatus> loadAllImageUpgradeDeviceStatus() {
		return imageUpgradeDeviceStatusDao.loadAllImageUpgradeDeviceStatus();
	}
	
	public ImageUpgradeDeviceStatusList loadImageUpgradeDeviceStatusList(String orderby,String orderway,Boolean bSearch, String searchField, String searchString, int offset, int limit) {
		return imageUpgradeDeviceStatusDao.loadImageUpgradeDeviceStatusList(orderby, orderway, bSearch, searchField, searchString,offset, limit);
	}
	
}
