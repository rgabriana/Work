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
import com.communication.utils.JsonUtil;
import com.communication.utils.NameValue;
import com.communicator.manager.ServerInfoManager;
import com.communicator.util.CommunicatorConstant;
import com.communicator.util.SpringContext;


public class ImageDownloadJob  implements Job {
	
	private String imageId;
	private String imageFile;
	private String taskId;
	private String md5;
	
	CloudConnectionTemplate cloudConnectionTemplate;
	ServerInfoManager serverInfoManager;
	
	static final Logger logger = Logger.getLogger(ImageDownloadJob.class.getName());
	
	public ImageDownloadJob(){
		cloudConnectionTemplate = (CloudConnectionTemplate)SpringContext.getBean("cloudConnectionTemplate");
		serverInfoManager = (ServerInfoManager)SpringContext.getBean("serverInfoManager");
	}
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("New job at " + new Date() + " with imageId = " + imageId + 
				"  and imageName = " + imageFile + " taskId = " + taskId);
		
		CloudRequest cloudRequest = new CloudRequest();
		ArrayList<NameValue> paramList = new ArrayList<NameValue>();
		paramList.add(new NameValue(CloudParamType.TaskId, taskId));
		paramList.add(new NameValue(CloudParamType.DownloadImageId, imageId));
		cloudRequest.setNameval(paramList);
		
		cloudConnectionTemplate.downloadAndDump(CommunicatorConstant.downloadImageService, JsonUtil.getJSONString(cloudRequest), 
				serverInfoManager.getHost(), getImageFile());
		
		try {
			String newMd5 = DigestUtils.md5Hex(new FileInputStream(getImageFile()));
			if(!md5.equals(newMd5)) {
				logger.error("Image not downloaded completely. Server md5 = " + md5 + ", localMd5 = " + newMd5 + ". Deleting local file.");
				File file = new File(getImageFile());
				file.delete();
				return;
			}
			else {
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec(new String[] { "dpkg-deb", "-f",  getImageFile(),  "ValidationKey"});
				int status = proc.waitFor();
				
				if(status == 0) {
					cloudRequest = new CloudRequest();
					paramList = new ArrayList<NameValue>();
					paramList.add(new NameValue(CloudParamType.TaskId, taskId));
					paramList.add(new NameValue(CloudParamType.TaskAttempts, "0"));
					paramList.add(new NameValue(CloudParamType.TaskProgressStatus, TaskProgressStatus.ImageDownloadSuccess.getName()));
					cloudRequest.setNameval(paramList);
					
					cloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
							serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the imageId
	 */
	public String getImageId() {
		return imageId;
	}

	/**
	 * @param imageId the imageId to set
	 */
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	/**
	 * @return the imageFile
	 */
	public String getImageFile() {
		return imageFile;
	}

	/**
	 * @param imageFile the imageFile to set
	 */
	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
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

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}
	
	
}
