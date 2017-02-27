package com.emcloudinstance.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import javax.annotation.Resource;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.springframework.stereotype.Service;

import com.emcloudinstance.dao.MonitoringDao;
import com.emcloudinstance.util.CommonUtils;
import com.emcloudinstance.util.SchedulerManager;
import com.emcloudinstance.vo.EmHealthDataVO;

@Service("monitoringManager")
public class MonitoringManager {

	@Resource
	MonitoringDao monitoringDao;

	@Resource
	CommonUtils commonUtils;
	
	public void doMonitoring(String mac) {
		monitoringDao.updateFixtureDiagnostic(mac);
		EmHealthDataVO emHealthDataVO = monitoringDao.getDeviceHealthData(mac);
		commonUtils.updateDeviceHealthStatus(mac, emHealthDataVO.getGatewaysTotal(), emHealthDataVO.getGatewaysUnderObservationNo(), 
				emHealthDataVO.getGatewaysCriticalNo(), emHealthDataVO.getSensorsTotal(), emHealthDataVO.getSensorsUnderObservationNo(), 
				emHealthDataVO.getSensorsCriticalNo());
	}

	public void scheduleDiagnostics(String mac) {
		try {
			// Let a separate thread handle migration
			String jobId = "diagnostics_job_" + mac;
			String triggerId = "diagnostics_trigger_" + mac;
			// Create quartz job
			JobDetail monitoringJob;
			
			if(SchedulerManager.getInstance().getScheduler().checkExists(new JobKey(jobId, SchedulerManager.getInstance().getScheduler().getSchedulerName()))){
				return;
			}

			monitoringJob = newJob(MonitoringJob.class)
					.withIdentity(
							jobId,
							SchedulerManager.getInstance().getScheduler()
									.getSchedulerName())
					.usingJobData("mac", mac).build();

			// Create Quartz trigger
			SimpleTrigger migrationTrigger = (SimpleTrigger) newTrigger()
					.withIdentity(
							triggerId,
							SchedulerManager.getInstance().getScheduler()
									.getSchedulerName()).startNow().build();

			SchedulerManager.getInstance().getScheduler()
					.scheduleJob(monitoringJob, migrationTrigger);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
