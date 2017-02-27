package com.communicator.job;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.log4j.Logger;
import org.codehaus.jackson.type.TypeReference;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.communication.template.CloudConnectionTemplate;
import com.communication.template.SecureCloudConnectionTemplate;
import com.communication.types.CloudParamType;
import com.communication.types.TaskProgressStatus;
import com.communication.types.TaskStatus;
import com.communication.utils.ArgumentUtils;
import com.communication.utils.CloudHttpResponse;
import com.communication.utils.CloudRequest;
import com.communication.utils.JsonUtil;
import com.communication.utils.NameValue;
import com.communicator.manager.ServerInfoManager;
import com.communicator.util.CommonUtils;
import com.communicator.util.CommunicatorConstant;
import com.communicator.util.SpringContext;

public class LogUploadJob implements Job {
	private static final String ALLFILES = "all";
	private static final String ALL = ALLFILES;
	private static final String CURRENT = "CURRENT";
	static final Logger logger = Logger.getLogger(LogUploadJob.class.getName());
	String tmpLogFolder = "/tmp/log/" ;
	String logZipFolder = "/tmp/log.zip";
	private String taskId  ;
	private String fileName ;
	private String typeOfUpload ;
	private String attempts ;
	ServerInfoManager serverInfoManager;
	SecureCloudConnectionTemplate secureCloudConnectionTemplate;
	CloudConnectionTemplate cloudConnectionTemplate;
	
	public LogUploadJob()
	{
		serverInfoManager = (ServerInfoManager)SpringContext.getBean("serverInfoManager");
		secureCloudConnectionTemplate = (SecureCloudConnectionTemplate) SpringContext.getBean("secureCloudConnectionTemplate");
		cloudConnectionTemplate = (CloudConnectionTemplate)SpringContext.getBean("cloudConnectionTemplate");
	}
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try{
			logger.info("New job at " + new Date() + " taskId = " + taskId);
			String fileList  = null ;
			//Collect all file paths 
			ArrayList<String> finalPathList = new ArrayList<String>() ;
			String[] fileNameArr ;
			if(!fileName.equalsIgnoreCase(ALLFILES))
			{	fileList = CommonUtils.getPropertyWithName(fileName, CommunicatorConstant.fileBasePathDir);
					
				if(ArgumentUtils.isNullOrEmpty(fileList))
				{
					logger.info("upload of "+fileName + " logs are not supported with this communicator version please contact administrator.") ;
					return ;
				}
				// get the base path 
				String fileBasePath =fileList.split(",")[0] ;
				//get the list of file names 
				fileNameArr = fileList.substring(fileList.indexOf(",")+1).split(",");
				ArrayList<String> finalPath = getFinalPath(fileNameArr , fileBasePath ,typeOfUpload );
				finalPathList.addAll(finalPath) ;
			}else
			{
			 
				 Iterator it = CommonUtils.getPropertiesMap( CommunicatorConstant.fileBasePathDir).entrySet().iterator();
				 while(it.hasNext())
				 {	 
					 Map.Entry pairs = (Map.Entry)it.next();
					 fileNameArr = pairs.getValue().toString().substring(pairs.getValue().toString().indexOf(",")+1).split(",");
					 ArrayList<String> finalPath = getFinalPath(fileNameArr , pairs.getValue().toString().split(",")[0] ,typeOfUpload );
					 finalPathList.addAll(finalPath) ;
				 }
					
			}
			logger.info(finalPathList.toString()) ;
			// Zip them up
			collectFilesAndZip(finalPathList) ;
			//send them
			MultipartEntity parts = new MultipartEntity();
			parts.addPart("logZip", new FileBody(new File(logZipFolder)) );
			CloudHttpResponse response = secureCloudConnectionTemplate.executePost(CommunicatorConstant.uploadLogService+serverInfoManager.getMacAddress(), parts, serverInfoManager.getHost());

			if(response.getStatus() == 200) {
						
				CloudRequest cloudRequest = new CloudRequest();
				ArrayList<NameValue> paramList = new ArrayList<NameValue>();
				paramList.add(new NameValue(CloudParamType.TaskId, taskId));
				paramList.add(new NameValue(CloudParamType.TaskAttempts, "1"));
				paramList.add(new NameValue(CloudParamType.TaskStatus, TaskStatus.SUCCESS.getName()));
				paramList.add(new NameValue(CloudParamType.TaskProgressStatus, TaskProgressStatus.LogUploadSuccess.getName()));
				cloudRequest.setNameval(paramList);
				
				CloudHttpResponse cloudHttpResponse = secureCloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
						serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
			}
			else {
				logger.error("Some error happend on the server status :-" + response.getStatus() + ". Job will be tried 3 times before failing. Attempts done till now:-"+ attempts);
			}
			
		}catch(Exception e)
		{
			
			logger.info(e.getMessage() +  " Job will be tried 3 times before failing. Attempts done till now:-"+ attempts);
			
		}
		
	}

	private void collectFilesAndZip(ArrayList<String> finalPath) {
		
		//copying file
		Iterator<String> itr = finalPath.iterator() ;
		
		try {
			FileUtils.deleteDirectory(new File(tmpLogFolder));
			FileUtils.forceMkdir(new File(tmpLogFolder));
		while(itr.hasNext())
		{		String filePath = itr.next() ;
				File src = new File(filePath);
				File dest = new File(tmpLogFolder+src.getName());
				FileUtils.copyFile(src, dest);
	
		}
			CommonUtils.zipFolder(tmpLogFolder, logZipFolder);
		} catch (IOException e) {
			logger.error(e.getMessage(), e) ;
		} catch (Exception e) {
			logger.error(e.getMessage(), e) ;
		}
		
		
	}
	private ArrayList<String>  getFinalPath(String[] fileNameList, String fileBasePath ,String typeOfUpload) {
		 ArrayList<String> temp = new ArrayList<String>() ;
		 	for(int j=0 ; j<fileNameList.length ; j++){
			if(typeOfUpload.equalsIgnoreCase(CURRENT))
			{
					String filePath=fileBasePath + fileNameList[j] ;
					if(new File(filePath).exists()){
						temp.add(filePath) ;	
					}
			}else if(typeOfUpload.equalsIgnoreCase(ALL))
			{
				String path = null ;
				// as some log files are just name and do not have .log ext and some have .out ext.
				if(fileNameList[j].contains(".out"))
				{
					path =fileNameList[j].substring(0,fileNameList[j].indexOf("."));
					path =path+"*" ;
					
				}
				else if(fileNameList[j].contains("."))
				{
					path =fileNameList[j].substring(0,fileNameList[j].indexOf("."));
					path =path+"*.log*" ;
					
				}
				else 
				{
					path = fileNameList[j]+"*" ;
				}
				
				FileFilter fileFilter = new WildcardFileFilter(path);
				File dir = new File(fileBasePath);
				File[] files = dir.listFiles(fileFilter);
				for (int i = 0; i < files.length; i++) 
				{	
							temp.add(files[i].getPath());	
				}
				//searching sub directory
				File[] dirFile =dir.listFiles() ;
				for (int i = 0; i < dirFile.length; i++) 
				{	
					if(dirFile[i].isDirectory())
					{
						File[] sudDirFiles = dirFile[i].listFiles(fileFilter);
						for (int k = 0; k < sudDirFiles.length; k++) 
						{	
									temp.add(sudDirFiles[k].getPath());	
						}
					}
				}
				
				
			}
		 	}
		
		return temp;
	}

	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getTypeOfUpload() {
		return typeOfUpload;
	}
	public void setTypeOfUpload(String typeOfUpload) {
		this.typeOfUpload = typeOfUpload;
	}
	public String getAttempts() {
		return attempts;
	}
	public void setAttempts(String attempts) {
		this.attempts = attempts;
	}
}
