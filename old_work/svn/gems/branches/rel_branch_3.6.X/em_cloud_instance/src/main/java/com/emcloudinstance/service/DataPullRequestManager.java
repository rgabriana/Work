package com.emcloudinstance.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.template.CloudConnectionTemplate;
import com.communication.types.DataPullRequestStateType;
import com.communication.utils.CloudHttpResponse;
import com.emcloudinstance.util.Constants;
import com.emcloudinstance.util.SchedulerManager;


@Service("dataPullRequestManager")
@Transactional(propagation = Propagation.REQUIRED)
public class DataPullRequestManager {
	static final Logger logger = Logger.getLogger(DataPullRequestManager.class.getName());
	@Resource
	CloudConnectionTemplate cloudConnectionTemplate ;
	
	
	public static void startMonitorJob() {
		try {
		// Create quartz job
        JobDetail monitorDataPullRequestJob = newJob(MonitorDataPullRequestJob.class)
                .withIdentity("MonitorDataPullRequestJobName", SchedulerManager.getInstance().getScheduler().getSchedulerName())
                .build();

        // Create Quartz trigger
        SimpleTrigger monitorDataPullRequestTrigger = (SimpleTrigger) newTrigger() 
                .withIdentity("MonitorDataPullRequestTriggerName", SchedulerManager.getInstance().getScheduler().getSchedulerName())
                .startNow()
                .withSchedule(simpleSchedule()
                .withIntervalInSeconds(120)
            	.repeatForever())
                .build();
        
        SchedulerManager.getInstance().getScheduler().scheduleJob(monitorDataPullRequestJob, monitorDataPullRequestTrigger);	
		
		} 
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public boolean updateDataPullJobState(Long taskId,DataPullRequestStateType state) {
		try {
			CloudHttpResponse response = cloudConnectionTemplate.executePost(
					Constants.UPDATE_DATA_PULL_REQUEST + taskId	+ "/state/" + state.getName(), 
					"", Constants.ECLOUD_IP, MediaType.TEXT_PLAIN);
			if(response.getStatus() != 200) {
				logger.error("Error updating data pull request with id " + taskId + " to state " + state.getName() + " on master cloud. Request status = " + response.getStatus());
				return false;
			}
		} catch (Exception ex) {
			logger.error("Error updating data pull request with id " + taskId + " to state " + state.getName() + " on master cloud. Please contact Administrator", ex);
			return false;
		}
		return true;
	}

}
