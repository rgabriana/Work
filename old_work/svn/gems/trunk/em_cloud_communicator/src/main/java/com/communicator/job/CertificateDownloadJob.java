package com.communicator.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.communication.template.CloudConnectionTemplate;
import com.communication.types.CloudParamType;
import com.communication.types.TaskProgressStatus;
import com.communication.utils.CloudRequest;
import com.communication.utils.EmTasksUUIDUtil;
import com.communication.utils.JsonUtil;
import com.communication.utils.NameValue;
import com.communicator.manager.ServerInfoManager;
import com.communicator.util.CommunicatorConstant;
import com.communicator.util.SpringContext;


public class CertificateDownloadJob  implements Job {
		
	private String tsCertFile;
	private String ksCertFile;
	private String taskId;
	private String md5ts;
	private String md5ks;
	private String ksUuid;
	private String taskProgressStatus;
		
	
	CloudConnectionTemplate cloudConnectionTemplate;
	ServerInfoManager serverInfoManager;
	
	static final Logger logger = Logger.getLogger(CertificateDownloadJob.class.getName());
	
	public CertificateDownloadJob(){
		cloudConnectionTemplate = (CloudConnectionTemplate)SpringContext.getBean("cloudConnectionTemplate");
		serverInfoManager = (ServerInfoManager)SpringContext.getBean("serverInfoManager");
	}
	
	@SuppressWarnings("unused")
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("New job at " + new Date() +
				"  and trust store certificate name = " + tsCertFile + " taskId = " + taskId);
		
		CloudRequest cloudRequest = new CloudRequest();
		ArrayList<NameValue> paramList = new ArrayList<NameValue>();
		paramList.add(new NameValue(CloudParamType.TaskId, taskId));		
		cloudRequest.setNameval(paramList);		
		
		//case Trust Store certificate download request
		if(getTaskProgressStatus() == null){
			
			cloudConnectionTemplate.downloadAndDump(CommunicatorConstant.downloadTSCertificateService, JsonUtil.getJSONString(cloudRequest), 
					serverInfoManager.getHost(), getTsCertFile());	
			
			try {
				String newMd5 = DigestUtils.md5Hex(new FileInputStream(getTsCertFile()));
				if(!md5ts.equals(newMd5)) {
					logger.error("Trust store certificate not downloaded completely. Server md5 = " + md5ts + ", localMd5 = " + newMd5 + ". Deleting local file.");
					File file = new File(getTsCertFile());
					file.delete();
					return;
				}
				else {
					logger.info("Trust store certificate downloaded completely. Server md5 = " + md5ts + ", localMd5 = " + newMd5 + ".");
						cloudRequest = new CloudRequest();
						paramList = new ArrayList<NameValue>();
						paramList.add(new NameValue(CloudParamType.TaskId, taskId));
						paramList.add(new NameValue(CloudParamType.TaskAttempts, "1"));
						paramList.add(new NameValue(CloudParamType.TaskProgressStatus, TaskProgressStatus.TSCertDownloadSuccess.getName()));
						cloudRequest.setNameval(paramList);
						
						cloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
								serverInfoManager.getHost(), MediaType.TEXT_PLAIN);					
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} 
		} else if("TSCertDownloadSuccess".equals(getTaskProgressStatus())){ //case Key Store certificate download request
			
			if (getKsCertFile() != null) {
				paramList = new ArrayList<NameValue>();
				paramList.add(new NameValue(CloudParamType.TaskId, taskId));
				try {
					paramList.add(new NameValue(CloudParamType.EmTaskUUID, EmTasksUUIDUtil.encrypt(EmTasksUUIDUtil.SECRET_LICENSE_KEY, getKsUuid())));
				} catch (Exception e) {
					e.printStackTrace();
				}
				cloudRequest.setNameval(paramList);
				
				cloudConnectionTemplate.downloadAndDump(CommunicatorConstant.downloadKSCertificateService, JsonUtil.getJSONString(cloudRequest),
						serverInfoManager.getHost(), getKsCertFile());
				
				try {
					String newMd5 = DigestUtils.md5Hex(new FileInputStream(getKsCertFile()));
					if(!md5ks.equals(newMd5)) {
						logger.error("Key store certificate not downloaded completely. Server md5 = " + md5ks + ", localMd5 = " + newMd5 + ". Deleting local file.");
						File file = new File(getKsCertFile());
						file.delete();
						return;
					}
					else {
						logger.info("Key store certificate downloaded completely. Server md5 = " + md5ks + ", localMd5 = " + newMd5 + ".");
							cloudRequest = new CloudRequest();
							paramList = new ArrayList<NameValue>();
							paramList.add(new NameValue(CloudParamType.TaskId, taskId));
							paramList.add(new NameValue(CloudParamType.TaskAttempts, "1"));
							paramList.add(new NameValue(CloudParamType.TaskProgressStatus, TaskProgressStatus.KSCertDownloadSuccess.getName()));
							cloudRequest.setNameval(paramList);
							
							cloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
									serverInfoManager.getHost(), MediaType.TEXT_PLAIN);					
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}	
		
	}
	
	/**
	 * @return the taskId
	 */
	public String getTaskId() {
		return taskId;
	}

	/**
	 * @param taskId the taskId to set
	 */
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	/**
	 * @return the trust store imageFile
	 */
	public String getTsCertFile() {
		return tsCertFile;
	}

	/**
	 * @param tsCertFile the tsCertFile to set
	 */
	public void setTsCertFile(String tsCertFile) {
		this.tsCertFile = tsCertFile;
	}

	/**
	 * @return the key store imageFile
	 */
	public String getKsCertFile() {
		return ksCertFile;
	}

	/**
	 * @param ksCertFile the ksCertFile to set
	 */
	public void setKsCertFile(String ksCertFile) {
		this.ksCertFile = ksCertFile;
	}

	public String getMd5ts() {
		return md5ts;
	}

	public void setMd5ts(String md5ts) {
		this.md5ts = md5ts;
	}

	public String getMd5ks() {
		return md5ks;
	}

	public void setMd5ks(String md5ks) {
		this.md5ks = md5ks;
	}

	public String getKsUuid() {
		return ksUuid;
	}

	public void setKsUuid(String ksUuid) {
		this.ksUuid = ksUuid;
	}

	public String getTaskProgressStatus() {
		return taskProgressStatus;
	}

	public void setTaskProgressStatus(String taskProgressStatus) {
		this.taskProgressStatus = taskProgressStatus;
	}
	
	
}
