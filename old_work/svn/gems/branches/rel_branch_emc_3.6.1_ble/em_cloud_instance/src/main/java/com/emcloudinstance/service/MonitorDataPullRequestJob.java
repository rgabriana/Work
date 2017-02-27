package com.emcloudinstance.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.SimpleTrigger;

import com.communication.template.CloudConnectionTemplate;
import com.communication.types.CloudParamType;
import com.communication.types.DataPullRequestStateType;
import com.communication.utils.CloudHttpResponse;
import com.communication.utils.CloudRequest;
import com.communication.utils.JsonUtil;
import com.communication.utils.NameValue;
import com.communication.vo.DataPullRequestVO;
import com.emcloudinstance.util.Constants;
import com.emcloudinstance.util.SchedulerManager;
import com.emcloudinstance.util.SpringContext;
import com.emcloudinstance.util.UidUtil;

public class MonitorDataPullRequestJob   implements Job {
	
	Logger logger = Logger.getLogger(MonitorDataPullRequestJob.class.getName());
	
	CloudConnectionTemplate cloudConnectionTemplate;
	UidUtil uidUtil;
	DataPullRequestManager dataPullRequestManager;
	
	
	public MonitorDataPullRequestJob() {
		cloudConnectionTemplate = (CloudConnectionTemplate)SpringContext.getBean("cloudConnectionTemplate");
		uidUtil = (UidUtil)SpringContext.getBean("uidUtil");
		dataPullRequestManager = (DataPullRequestManager)SpringContext.getBean("dataPullRequestManager");
	}
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		if(uidUtil.uid != null && !"".equals(uidUtil.uid)) {
			CloudRequest cloudRequest = new CloudRequest();
			ArrayList<NameValue> paramList = new ArrayList<NameValue>();
			paramList.add(new NameValue(CloudParamType.ReplicaServerUID, uidUtil.uid));
			cloudRequest.setNameval(paramList);
			CloudHttpResponse response =  cloudConnectionTemplate.executePost(Constants.GET_DATA_PULL_REQUEST, JsonUtil.getJSONString(cloudRequest), 
					Constants.ECLOUD_IP, MediaType.TEXT_PLAIN);
			
			if(response.getStatus() != 200) {
				logger.error("Error getting data pull request from master server. status = " + response.getStatus() + " message = " + response.getResponse());
				return;
			}
			
			JsonUtil<DataPullRequestVO> jsonUtil = new JsonUtil<DataPullRequestVO>();
			DataPullRequestVO dprVO  = jsonUtil.getObject(response.getResponse(), DataPullRequestVO.class );
			if(dprVO != null && dprVO.getId() != null) {
				logger.info("Data pull request id = " + dprVO.getId());
				String jobId = "DataPullJob";
				
				try {
					if(!SchedulerManager.getInstance().getScheduler().checkExists(new JobKey(jobId, SchedulerManager.getInstance().getScheduler()
											.getSchedulerName()))) {
						
						logger.info("Scheduling new DataPullJob for task id = " + dprVO.getId());
						if(dataPullRequestManager.updateDataPullJobState(dprVO.getId(), DataPullRequestStateType.Processing)) {
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							JobDetail job = newJob(DataPullRequestJob.class)
									.withIdentity(jobId,SchedulerManager.getInstance()
											.getScheduler().getSchedulerName())
									.usingJobData("taskId", dprVO.getId().toString())
									.usingJobData("dbName", dprVO.getDbName())
									.usingJobData("tableName", dprVO.getTableName())
									.usingJobData("fromDate", sdf.format(dprVO.getFromDate()))
									.usingJobData("toDate", sdf.format(dprVO.getToDate()))
									.build();
	
							// Create Quartz trigger
							SimpleTrigger trigger = (SimpleTrigger) newTrigger()
									.withIdentity(
											"DataPullJobTrigger",
											SchedulerManager.getInstance().getScheduler()
													.getSchedulerName()).startNow().build();
	
							SchedulerManager.getInstance().getScheduler()
									.scheduleJob(job, trigger);
						}
					}
					else {
						logger.info("Another DataPullJob already in progress.");
					}
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
			}
			else {
				logger.info("No pending data pull request job");
			}
		}
		else {
			logger.warn("Monitor Data Pull Request:: No uid found for replica");
		}
		
	}

}
