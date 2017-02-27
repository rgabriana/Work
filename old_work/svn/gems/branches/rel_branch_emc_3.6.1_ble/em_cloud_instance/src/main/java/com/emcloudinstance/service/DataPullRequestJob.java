package com.emcloudinstance.service;

import java.io.File;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.communication.types.DataPullRequestStateType;
import com.emcloudinstance.util.SpringContext;

public class DataPullRequestJob  implements Job{
	
	Logger logger = Logger.getLogger(DataPullRequestJob.class.getName());
	DataPullRequestManager dataPullRequestManager;
	
	private String taskId;
	private String dbName;
	private String tableName;
	private String fromDate;
	private String toDate;
	
	public DataPullRequestJob() {
		dataPullRequestManager = (DataPullRequestManager)SpringContext.getBean("dataPullRequestManager");
	}
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		logger.info("Starting data pull request job for task " + taskId + ", dbName = " + dbName + ", tableName = " + tableName + ", fromDate = " + fromDate + ", toDate = " + toDate);
		Runtime rt = Runtime.getRuntime();
		Process proc;
		DataPullRequestStateType newState;
		
		try {
			proc = rt.exec(new String[]{"/bin/bash", "/var/lib/tomcat6/webapps/em_cloud_instance/adminscripts/dataPullRequest.sh", taskId.toString(), dbName, tableName, fromDate, toDate});
			int result = proc.waitFor();
			logger.info("data pull request output = " + result + " from task = " + taskId);
			
			String path = "/var/lib/tomcat6/Enlighted/dataPullRequests";
			File mFile = new File(path + "/" + taskId + "/" + taskId + ".gz");
			
			if(result == 0 && mFile.exists()) {
				newState = DataPullRequestStateType.Successful;
			}
			else {
				newState = DataPullRequestStateType.Failed;
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			newState = DataPullRequestStateType.Failed; 
		}
		Boolean updateStatus = dataPullRequestManager.updateDataPullJobState(new Long(taskId), newState);
		logger.info("Data pull request for task = " + taskId + " completed with new state = " + newState.getName() +
				" and updated on master cloud = " + updateStatus);
		
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}
	
	

}
