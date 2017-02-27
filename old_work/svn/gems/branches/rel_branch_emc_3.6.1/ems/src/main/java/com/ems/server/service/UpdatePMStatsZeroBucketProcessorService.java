package com.ems.server.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.cache.FixtureCache;
import com.ems.dao.FixtureDao;
import com.ems.cache.DeviceInfo;
import com.ems.server.data.ZeroBucketStruct;
import com.ems.server.util.ServerUtil;
import com.ems.service.EnergyConsumptionManager;

@Service("updatePMStatsZeroBucketProcessorService")
@Transactional(propagation = Propagation.REQUIRED)
public class UpdatePMStatsZeroBucketProcessorService {

	private static Logger timingLogger = Logger.getLogger("TimingLogger");
	public static int FIVE_MINUTE_INTERVAL = 5 * 60 * 1000;

	@Resource
	private FixtureDao fixtureDao;

	@Resource
	private EnergyConsumptionManager energyConsumptionManager;

	public void processStats(List<ZeroBucketStruct> processingQueue) {
		long startTime = System.currentTimeMillis();

		try {
			fixtureDao.getSession().setFlushMode(FlushMode.MANUAL);

			for (ZeroBucketStruct struct : processingQueue) {
				updateZeroBuckets(struct);
			}

			fixtureDao.getSession().flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		timingLogger.debug("Update Zero Bucket Packets (Stats):"
				+ (System.currentTimeMillis() - startTime) + " Processed: "
				+ processingQueue.size());
	}

	private void updateZeroBuckets(ZeroBucketStruct struct) {

		Long fixtureId = struct.getFixtureId();
		DeviceInfo device = FixtureCache.getInstance().getDevice(fixtureId);
		// this is the start time from when zero buckets have to be filled. this
		// is 5 minutes
		// after last stats received
		Date startTime = null;
		Date lastStatsRcvdTime = struct.getLastStatsRcvdTime();
		int newCU = 0;
		if (device != null) {
			//startTime = device.getBootTime();
			if(ServerUtil.isNewCU(device.getFixture())) {
			  newCU = 1;
			}
		}
		if (startTime == null
				|| startTime.before(struct.getLastStatsRcvdTime())) {
			startTime = new Date(struct.getLastStatsRcvdTime().getTime()
					+ FIVE_MINUTE_INTERVAL);
		} else {
			long startMillis = startTime.getTime();
			startMillis = startMillis
					+ (FIVE_MINUTE_INTERVAL - startMillis
							% FIVE_MINUTE_INTERVAL);
			startTime.setTime(startMillis);
			lastStatsRcvdTime = new Date(startMillis - FIVE_MINUTE_INTERVAL);
		}
		if (startTime.getTime() >= struct.getLatestStateRcvdTime().getTime()) {
			// we got the first stats after reboot so no missing
			return;
		}
		if(timingLogger.isDebugEnabled()) {
		  timingLogger.debug(fixtureId + ":update zero buck:startTime - "
				+ startTime + " last received time - "
				+ struct.getLastStatsRcvdTime() + " latest received time - "
				+ struct.getLatestStateRcvdTime());
		}
		//System.out.println("new cu =- " + newCU);
		energyConsumptionManager.updateZeroBuckets(fixtureId,
				lastStatsRcvdTime, startTime, struct.getLatestStateRcvdTime(), newCU,
				struct.isSweepEnabled());

	} // end of method updateZeroBuckets
}
