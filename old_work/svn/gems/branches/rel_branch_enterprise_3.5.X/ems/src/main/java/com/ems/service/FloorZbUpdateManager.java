package com.ems.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.FloorZbUpdateDao;
import com.ems.model.Fixture;
import com.ems.model.FloorZbUpdate;
import com.ems.utils.DateUtil;

@Service("floorZbUpdateManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FloorZbUpdateManager {

	@Resource
	FloorZbUpdateDao floorZbUpdateDao;

	@Resource
	FloorManager floorManager;

	@Resource
	FixtureManager fixtureManger;

	public void storeZbUpdateForFloor(long fixtureId, Date startDate,
			Date latestStatsDate) {
		try {
			String format = "yyyyMMddHHmmss";
			startDate = DateUtil.parseString(
					DateUtil.formatDate(startDate, format), format);
			latestStatsDate = DateUtil.parseString(
					DateUtil.formatDate(latestStatsDate, format), format);
			Fixture fix = fixtureManger.getFixtureById(fixtureId);
			Long floorId = null;
			if(fix != null){
				floorId = fix.getFloorId();				
			}			
			if (floorId != null) {
				FloorZbUpdate fZb = floorZbUpdateDao
						.loadUnProcessedFloorZbUpdateByFoorId(floorId);
				if (fZb != null) {
					// update if already present and not processed.
					Date fZbStartTime = DateUtil.parseString(
							DateUtil.formatDate(fZb.getStartTime(), format),
							format);
					Date fZbEndTime = DateUtil.parseString(
							DateUtil.formatDate(fZb.getEndTime(), format),
							format);
					if (startDate.before(fZbStartTime)) {
						fZb.setStartTime(startDate);
					}
					if (latestStatsDate.after(fZbEndTime)) {
						fZb.setEndTime(latestStatsDate);
					}
					floorZbUpdateDao.saveOrUpdate(fZb);
				} else {
					// create new if not present and set processed to 0.
					FloorZbUpdate newFZb = new FloorZbUpdate();
					newFZb.setProcessedState(0l);
					newFZb.setStartTime(startDate);
					newFZb.setEndTime(latestStatsDate);
					newFZb.setFloorId(floorId);
					floorZbUpdateDao.saveOrUpdate(newFZb);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void saveOrUpdate(FloorZbUpdate fZb) {
		floorZbUpdateDao.saveOrUpdate(fZb);
	}
	
	public List<FloorZbUpdate> loadAllUnProcessedFloorZbUpdate() {
		return floorZbUpdateDao.loadAllUnProcessedFloorZbUpdate();
	}
	
}
