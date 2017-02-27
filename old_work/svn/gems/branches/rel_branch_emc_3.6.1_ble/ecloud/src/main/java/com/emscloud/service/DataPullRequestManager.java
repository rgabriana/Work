package com.emscloud.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SimpleTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.types.DataPullRequestStateType;
import com.communication.vo.DataPullRequestVO;
import com.emscloud.communication.ResponseWrapper;
import com.emscloud.communication.adaptor.CloudAdapter;
import com.emscloud.constant.ReplicaServerWebserviceUrl;
import com.emscloud.dao.DataPullRequestDao;
import com.emscloud.dao.EmInstanceDao;
import com.emscloud.job.DataPullRequestCleanUpJob;
import com.emscloud.model.DataPullRequest;
import com.emscloud.util.SchedulerManager;
import com.emscloud.vo.DataPullRequestList;


@Service("dataPullRequestManager")
@Transactional(propagation = Propagation.REQUIRED)
public class DataPullRequestManager {
	
	static final Logger logger = Logger.getLogger(DataPullRequestManager.class.getName());
	
	@Resource
	DataPullRequestDao dataPullRequestDao;
	
	@Resource
	EmInstanceDao emInstanceDao;
	
	@Resource
	CloudAdapter cloudAdapter;
	
	@Resource
	ReplicaServerManager replicaServerManager;
	
	public DataPullRequest getTopDataPullRequestByReplicaId(Long replicaId) {
		return dataPullRequestDao.getTopDataPullRequestByReplicaId(replicaId);
	}
	
	public DataPullRequestVO getTopDataPullRequestVOByReplicaId(Long replicaId) {
		DataPullRequest dpr = getTopDataPullRequestByReplicaId(replicaId);
		if(dpr != null) {
			DataPullRequestVO dprVO = new DataPullRequestVO();
			dprVO.setId(dpr.getId());
			dprVO.setDbName(dpr.getEm().getDatabaseName());
			dprVO.setTableName(dpr.getTableName());
			dprVO.setFromDate(dpr.getFromDate());
			dprVO.setToDate(dpr.getToDate());
			dprVO.setState(dpr.getState());
			dprVO.setRetry(dpr.getRetry());
			return dprVO;
		}
		return null;
	}
	
	public DataPullRequest changeState(Long id, DataPullRequestStateType state, Boolean retry) {
		DataPullRequest dpr = (DataPullRequest)dataPullRequestDao.loadObject(DataPullRequest.class, id);
		if(dpr != null) {
			dpr.setState(state);
			dpr.setLastUpdatedAt(new Date());
			if(retry) {
				dpr.setRetry(dpr.getRetry()+ 1);
			}
			return (DataPullRequest)dataPullRequestDao.saveObject(dpr);
		}
		return null;
		
	}
	
	public DataPullRequest getDataPullRequestById(Long id) {
		Object o = dataPullRequestDao.getSession().get(DataPullRequest.class, id);
		if(o != null) {
			return (DataPullRequest)o;
		}
		else {
			return null;
		}
	}
	
	
	public String updateDataPullRequestState(Long requestId ,String newState) {
		logger.info("Data pull request Id = " + requestId + " and new state = " + newState);
		DataPullRequest dpr = getDataPullRequestById(requestId);
		
		try {
			if(dpr != null && DataPullRequestStateType.valueOf(newState).getName().equals(newState)) {
				DataPullRequestStateType state = DataPullRequestStateType.valueOf(newState);  
				DataPullRequestStateType oldState = dpr.getState();
				switch (state) {
					case Deleted: {
						changeState(requestId, state, false);
						return "S";
					}
					case Processing: {
						if(oldState.compareTo(DataPullRequestStateType.Queued) == 0) {
							changeState(requestId, state, false);
							return "S";
						}
						break;
					}
					case Successful: {
						if (oldState.compareTo(DataPullRequestStateType.Processing) == 0) {
							changeState(requestId, state, false);
							return "S";
						}
						break;
					}
					case Failed: {
						if (oldState.compareTo(DataPullRequestStateType.Processing) == 0) {
							if(dpr.getRetry() < 3) {
								changeState(requestId, DataPullRequestStateType.Processing, true);
								return "S";
							}
							else {
								changeState(requestId, state, false);
								return "S";
							}
						}
						break;
					}
					case Cancelled: {
						if (oldState.compareTo(DataPullRequestStateType.Processing) == 0 || oldState.compareTo(DataPullRequestStateType.Queued) == 0) {
							changeState(requestId, state, false);
							return "S";
						}
						break;
					}
					default: {
						break;
					}
				}
			}
		}
		catch(Exception e) {
			logger.error(e.getMessage());
		}
		return "F";
	}
	
	public String purgeRequest(DataPullRequest dpr, String op) {
		if (dpr != null && dpr.getState() != null && 
				(op.equals("delete") || 
						(dpr.getState().compareTo(DataPullRequestStateType.Processing) == 0 || dpr.getState().compareTo(DataPullRequestStateType.Queued) == 0))) {
			logger.info("data pull request " + dpr.getId() + ". starting " + op + ".");
			ResponseWrapper<String> response = cloudAdapter.executePost(dpr.getEm().getReplicaServer(), 
					ReplicaServerWebserviceUrl.DATA_PULL_REQUEST_CANCEL_DELETE + op + "/" + dpr.getId(), 
					MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN, 
					String.class, "");  	        
			if (response != null && response.getStatus() == javax.ws.rs.core.Response.Status.OK.getStatusCode() && "S".equals(response.getItems())) {
				logger.info("Cleaning up successful for request " + dpr.getId());
				return "S";
			}
			else {
				logger.warn("Cleaning up failed for request " + dpr.getId());
			}
		}
		else {
			logger.warn("data pull request " + dpr == null ? "" : dpr.getId() + " cannot be cancelled/deleted.");
		}
		return "F";
	}
	
	
	public List<DataPullRequest>  getOlderRequests(Long replicaId, Integer days) {
		return dataPullRequestDao.getOlderRequests(replicaId, days);
	}
	
	public void startCleanUpJob() {
		try {
			
		SchedulerManager.getInstance().getScheduler().deleteJob(new JobKey("DataPullRequestCleanUpJob", SchedulerManager.getInstance().getScheduler().getSchedulerName()));
		// Create quartz job
        JobDetail dataPullRequestCleanUpJob = newJob(DataPullRequestCleanUpJob.class)
                .withIdentity("DataPullRequestCleanUpJob", SchedulerManager.getInstance().getScheduler().getSchedulerName())
                .build();

        // Create Quartz trigger
        SimpleTrigger dataPullRequestCleanUpJobTrigger = (SimpleTrigger) newTrigger() 
                .withIdentity("DataPullRequestCleanUpJobTrigger", SchedulerManager.getInstance().getScheduler().getSchedulerName())
                .startNow()
                .withSchedule(simpleSchedule()
                //.withIntervalInSeconds(45)
                .withIntervalInHours(24)
            	.repeatForever())
                .build();
        
        SchedulerManager.getInstance().getScheduler().scheduleJob(dataPullRequestCleanUpJob, dataPullRequestCleanUpJobTrigger);	
		
		} 
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	public DataPullRequest save(DataPullRequest d) {
		return (DataPullRequest)dataPullRequestDao.saveObject(d);
	}
	
	public DataPullRequestList loadDataByCustomerId(Long id, String orderby, String orderway, int offset, int limit) {
		return dataPullRequestDao.loadDataByCustomerId(id, orderby, orderway, offset, limit);
	}

}
