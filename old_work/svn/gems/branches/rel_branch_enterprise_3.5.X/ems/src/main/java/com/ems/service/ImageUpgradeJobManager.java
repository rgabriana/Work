package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.ImageUpgradeJobDao;
import com.ems.model.ImageUpgradeDBJob;
import com.ems.model.ImageUpgradeJobList;

@Service("imageUpgradeJobManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ImageUpgradeJobManager {
	
	@Resource
	ImageUpgradeJobDao imageUpgradeJobDao;

	public List<ImageUpgradeDBJob> loadAllImageUpgradeJobs() {
		return imageUpgradeJobDao.loadAllImageUpgradeJobs();
	}
	
	public ImageUpgradeDBJob loadImageUpgradeJobById( Long Id) {
		return imageUpgradeJobDao.loadImageUpgradeJobById(Id);
	}
	
	public ImageUpgradeJobList loadImageUpgradeJobList(String orderby,String orderway, Boolean bSearch, String searchField, String searchString, int offset, int limit) {
		return imageUpgradeJobDao.loadImageUpgradeJobList(orderby,orderway, bSearch, searchField, searchString, offset, limit);
	}
	
}
