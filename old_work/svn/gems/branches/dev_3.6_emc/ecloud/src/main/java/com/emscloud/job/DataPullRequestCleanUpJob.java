package com.emscloud.job;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.emscloud.action.SpringContext;
import com.emscloud.communication.ResponseWrapper;
import com.emscloud.communication.adaptor.CloudAdapter;
import com.emscloud.constant.ReplicaServerWebserviceUrl;
import com.emscloud.model.DataPullRequest;
import com.emscloud.model.ReplicaServer;
import com.emscloud.model.SystemConfiguration;
import com.emscloud.service.DataPullRequestManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.ReplicaServerManager;
import com.emscloud.service.SystemConfigurationManager;

public class DataPullRequestCleanUpJob implements Job  {

	public static final Logger logger = Logger.getLogger(DataPullRequestCleanUpJob.class.getName());
	
	SystemConfigurationManager sysCnfManager;
	ReplicaServerManager replicaServerManager;
	DataPullRequestManager dataPullRequestManager;
	CloudAdapter cloudAdapter;
	EmInstanceManager emInstanceManager;
	
	public DataPullRequestCleanUpJob() {
		sysCnfManager = (SystemConfigurationManager) SpringContext.getBean("systemConfigurationManager");
		replicaServerManager = (ReplicaServerManager) SpringContext.getBean("replicaServerManager");
		dataPullRequestManager = (DataPullRequestManager) SpringContext.getBean("dataPullRequestManager");
		cloudAdapter = (CloudAdapter) SpringContext.getBean("gemAdapter");
		emInstanceManager = (EmInstanceManager) SpringContext.getBean("emInstanceManager");
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("Starting data pull requests clean up job.");
		Integer days = 30;
		SystemConfiguration sc = sysCnfManager.loadConfigByName("data.pull.request.cleanup.days");
		if(sc != null && sc.getValue() != null && !sc.getValue().equals("")) {
			days = Integer.parseInt(sc.getValue());
		}
		logger.info("Cleaning jobs older than " + days + " days");
		List<ReplicaServer> replicas= replicaServerManager.getAllReplicaServers();
		if(replicas != null && replicas.size() > 0) {
			for(ReplicaServer rs : replicas) {
				logger.info("Start cleaning from " + rs.getName());
				List<DataPullRequest> requests = dataPullRequestManager.getOlderRequests(rs.getId(), days);
				if(requests != null && requests.size() > 0) {
					for(DataPullRequest r: requests) {
						logger.info("Cleaning up for request " + r.getId());
						ResponseWrapper<String> response = cloudAdapter.executePost(rs, 
										ReplicaServerWebserviceUrl.DATA_PULL_REQUEST_CANCEL_DELETE + "delete/" + r.getId(), 
										MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN, 
										String.class, "");  	        
						if (response != null && response.getStatus() == javax.ws.rs.core.Response.Status.OK.getStatusCode() && "S".equals(response.getItems())) {
							logger.info("Cleaning up successful for request " + r.getId());
						} 
						else {
							logger.info("Cleaning up false for request " + r.getId());
						}
					}
				}
			}
		}

	}
}
