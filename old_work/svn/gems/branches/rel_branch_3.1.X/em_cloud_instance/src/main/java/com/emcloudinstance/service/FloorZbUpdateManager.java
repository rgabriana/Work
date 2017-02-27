package com.emcloudinstance.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emcloudinstance.dao.FloorZbUpdateDao;
import com.emcloudinstance.vo.FloorZbUpdate;


@Service("floorZbUpdateManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FloorZbUpdateManager {

	@Resource
	FloorZbUpdateDao floorZbUpdateDao;

	
	public void update(FloorZbUpdate fZb,String mac) {
		floorZbUpdateDao.update(fZb,mac);
	}
	
	public List<FloorZbUpdate> loadAllUnProcessedFloorZbUpdate(String mac) {
		return floorZbUpdateDao.loadAllUnProcessedFloorZbUpdate(mac);
	}
	
	public Boolean isTableAvailable(String mac){
		return floorZbUpdateDao.isTableAvailable(mac);
	}
	
}
