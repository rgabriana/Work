package com.communicator.manager;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.type.TypeReference;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.springframework.stereotype.Service;

import com.communication.template.CloudConnectionTemplate;
import com.communication.template.SecureCloudConnectionTemplate;
import com.communication.types.CloudParamType;
import com.communication.types.TaskCode;
import com.communication.types.TaskProgressStatus;
import com.communication.types.TaskStatus;
import com.communication.utils.ArgumentUtils;
import com.communication.utils.CloudHttpResponse;
import com.communication.utils.CloudRequest;
import com.communication.utils.JsonUtil;
import com.communication.utils.NameValue;
import com.communicator.job.CertificateDownloadJob;
import com.communicator.job.ImageDownloadJob;
import com.communicator.job.LogUploadJob;
import com.communicator.util.CommunicatorConstant;
import com.communicator.util.InitializeSecureConnection;
import com.communicator.util.SchedulerManager;
import com.communicator.util.SpringContext;

@Service("taskManager")
public class TaskManager {
	
	static final Logger logger = Logger.getLogger(TaskManager.class
			.getName());
	
	@Resource
	CloudConnectionTemplate cloudConnectionTemplate;
	@Resource
	SecureCloudConnectionTemplate secureCloudConnectionTemplate;
	@Resource
	ServerInfoManager serverInfoManager;
	
	private String imagesDir = CommunicatorConstant.ENL_APP_HOME+"/Enlighted/UpgradeImages/";
	//private String imagesDir = "/tmp/";
	
	
	public void processTasks(String tasks) {
		
		ArrayList<HashMap<CloudParamType, String>> taskList = null;
		JsonUtil<ArrayList<HashMap<CloudParamType, String>>> jsonUtilListMap = new JsonUtil<ArrayList<HashMap<CloudParamType, String>>>();
		taskList = jsonUtilListMap.getObject(tasks, new TypeReference<ArrayList<HashMap<CloudParamType, String>>>() { });
		
		//for (HashMap<CloudParamType, String> task: taskList) {
		// execute task in FIFO
		if(!ArgumentUtils.isNullOrEmpty(taskList)){
		HashMap<CloudParamType, String> task = taskList.get(0);
			try {
				if((TaskCode.valueOf(task.get(CloudParamType.TaskCode)).getName().equals(task.get(CloudParamType.TaskCode)))) {
					switch (TaskCode.valueOf(task.get(CloudParamType.TaskCode))) {
						case  UPGRADE : {
							try {
								upgradeTask(task);
							} catch (IllegalArgumentException e) {
								logger.error(e.toString());
								//TODO:
								//it should come here only if there is backward compatibility issue
								//convey the same to cloud server
							} catch (SchedulerException e) {
								logger.error(e.toString());
							}
							break;
						}
						case  LOG_UPLOAD : {
							try {
								logTask(task);
							} catch (IllegalArgumentException e) {
								logger.error(e.toString());
								//TODO:
								//it should come here only if there is backward compatibility issue
								//convey the same to cloud server
							} catch (SchedulerException e) {
								logger.error(e.toString());
							}
							break;
						}
						case CERTIFICATE_SYNC : {
							try {
								certificateSyncTask(task);
							} catch (IllegalArgumentException e) {
								logger.error(e.toString());
								//TODO:
								//it should come here only if there is backward compatibility issue
								//convey the same to cloud server
							} catch (SchedulerException e) {
								logger.error(e.toString());
							}
							break;							
						}
						default : {
							logger.info("====No Task====");
						}
					}
				}
			} catch (IllegalArgumentException e) {
				//TODO: 
				//it should come here only if there is a task which communicator doesn't understand
				//In such case, it should set the task status to Failed on cloud server
				logger.error(e.toString());
			}
		}
		}
		
	
	
	private void logTask(HashMap<CloudParamType, String> task) throws IllegalArgumentException,  SchedulerException {
		
		HashMap<String, String> paramMap = null;
		JsonUtil<HashMap<String, String>> jsonUtilParamMap = new JsonUtil<HashMap<String, String>>();
		paramMap = jsonUtilParamMap.getObject(task.get(CloudParamType.TaskParameters), new TypeReference<HashMap<String, String>>() { });
		
		String uploadJobName = "job_log_upload_"+ task.get(CloudParamType.TaskId) ;
		String uploadTriggerName = "trigger_log_upload_"+ task.get(CloudParamType.TaskId) ;
		
		
		logger.info("Task");
		logger.info("========");
		for(CloudParamType eachKey: task.keySet()) {
			if(eachKey.equals(CloudParamType.TaskParameters)) {
				for(String mapKey: paramMap.keySet()) {
					logger.info("Task Parameter: Key = " + mapKey + " and value = " + paramMap.get(mapKey));
				}
			}
			else {
				logger.info(eachKey.getName() + ": " + task.get(eachKey));
			}
		}
		logger.info("========");
		
		
		
		if((TaskStatus.valueOf(task.get(CloudParamType.TaskStatus)).getName().equals(task.get(CloudParamType.TaskStatus)))) {
			switch (TaskStatus.valueOf(task.get(CloudParamType.TaskStatus))) {
				case SCHEDULED: {
						CloudRequest cloudRequest = new CloudRequest();
						ArrayList<NameValue> paramList = new ArrayList<NameValue>();
						paramList.add(new NameValue(CloudParamType.TaskId, task.get(CloudParamType.TaskId)));
						paramList.add(new NameValue(CloudParamType.TaskStatus, TaskStatus.IN_PROGRESS.getName()));
						paramList.add(new NameValue(CloudParamType.TaskProgressStatus, TaskProgressStatus.LogUploadRequested.getName()));
						paramList.add(new NameValue(CloudParamType.TaskAttempts, "1"));
						cloudRequest.setNameval(paramList);
						secureCloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
								serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
					break;
				}
				case IN_PROGRESS: {				
					switch (TaskProgressStatus.valueOf(task.get(CloudParamType.TaskProgressStatus))) {
						case LogUploadRequested : {
							Scheduler sched = SchedulerManager.getInstance().getScheduler();
							if (!sched.checkExists(new JobKey(uploadJobName, sched.getSchedulerName()))) {
									CloudRequest cloudRequest = new CloudRequest();
									ArrayList<NameValue> paramList = new ArrayList<NameValue>();
									paramList.add(new NameValue(CloudParamType.TaskId, task.get(CloudParamType.TaskId)));
									paramList.add(new NameValue(CloudParamType.TaskStatus, TaskStatus.IN_PROGRESS.getName()));
									paramList.add(new NameValue(CloudParamType.TaskAttempts, "1"));
									paramList.add(new NameValue(CloudParamType.TaskProgressStatus, TaskProgressStatus.LogUploadInProgress.getName()));
									cloudRequest.setNameval(paramList);
									secureCloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
											serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
									createNewLogUploadJob(uploadJobName, uploadTriggerName, paramMap, task);
							}
							break;
						}
						case LogUploadInProgress: {
							Scheduler sched = SchedulerManager.getInstance().getScheduler();
							if (!sched.checkExists(new JobKey(uploadJobName, sched.getSchedulerName()))) {
								if("3".equals(task.get(CloudParamType.TaskAttempts))) {
									CloudRequest cloudRequest = new CloudRequest();
									ArrayList<NameValue> paramList = new ArrayList<NameValue>();
									paramList.add(new NameValue(CloudParamType.TaskId, task.get(CloudParamType.TaskId)));
									paramList.add(new NameValue(CloudParamType.TaskStatus, TaskStatus.FAILED.getName()));
									paramList.add(new NameValue(CloudParamType.TaskProgressStatus, TaskProgressStatus.LogUploadFailed.getName()));
									cloudRequest.setNameval(paramList);
									secureCloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
											serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
								}
								else {
									CloudRequest cloudRequest = new CloudRequest();
									ArrayList<NameValue> paramList = new ArrayList<NameValue>();
									paramList.add(new NameValue(CloudParamType.TaskId, task.get(CloudParamType.TaskId)));
									paramList.add(new NameValue(CloudParamType.TaskStatus, TaskStatus.IN_PROGRESS.getName()));
									paramList.add(new NameValue(CloudParamType.TaskProgressStatus, TaskProgressStatus.LogUploadInProgress.getName()));
									paramList.add(new NameValue(CloudParamType.TaskAttempts, new Integer(Integer.parseInt(task.get(CloudParamType.TaskAttempts)) + 1).toString()));
									cloudRequest.setNameval(paramList);
									secureCloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
											serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
									createNewLogUploadJob(uploadJobName, uploadTriggerName, paramMap, task);
								}
							}else {
								logger.info("Still collecting and zipping the file.");
							}
							break;
						}
						default: {
							logger.info("???NOT EXPECTED???");
						}
					}
					break;
				}
				default: {
					logger.info("???NOT EXPECTED???");
				}
			}
		}
		
		
	}

	public void certificateSyncTask(HashMap<CloudParamType, String> task) throws IllegalArgumentException, SchedulerException {
		HashMap<String, String> paramMap = null;
		JsonUtil<HashMap<String, String>> jsonUtilParamMap = new JsonUtil<HashMap<String, String>>();
		paramMap = jsonUtilParamMap.getObject(task.get(CloudParamType.TaskParameters), new TypeReference<HashMap<String, String>>() { });
		
		String downloadJobName = "job_certificate_download";
		String downloadTriggerName = "trigger_certificate_download";
		
		
		logger.info("Task");
		logger.info("========");
		for(CloudParamType eachKey: task.keySet()) {
			if(eachKey.equals(CloudParamType.TaskParameters)) {
				for(String mapKey: paramMap.keySet()) {
					logger.info("Task Parameter: Key = " + mapKey + " and value = " + paramMap.get(mapKey));
				}
			}
			else {
				logger.info(eachKey.getName() + ": " + task.get(eachKey));
			}
		}
		logger.info("========");
		
		
		
		if((TaskStatus.valueOf(task.get(CloudParamType.TaskStatus)).getName().equals(task.get(CloudParamType.TaskStatus)))) {
			switch (TaskStatus.valueOf(task.get(CloudParamType.TaskStatus))) {
				case SCHEDULED: {
					Scheduler sched = SchedulerManager.getInstance().getScheduler();
					if (!sched.checkExists(new JobKey(downloadJobName, sched.getSchedulerName()))) {
						createNewCertificateDownloadJob(downloadJobName, downloadTriggerName, paramMap, task, null);
					}
					else {
						logger.info("Still downloading certificate...");
					}
					break;
				}
				case IN_PROGRESS: {
					
					switch (TaskProgressStatus.valueOf(task.get(CloudParamType.TaskProgressStatus))) {
						case TSCertDownloadRequested : {
							Scheduler sched = SchedulerManager.getInstance().getScheduler();
							if (!sched.checkExists(new JobKey(downloadJobName, sched.getSchedulerName()))) {
								if("3".equals(task.get(CloudParamType.TaskAttempts))) {
									CloudRequest cloudRequest = new CloudRequest();
									ArrayList<NameValue> paramList = new ArrayList<NameValue>();
									paramList.add(new NameValue(CloudParamType.TaskId, task.get(CloudParamType.TaskId)));
									paramList.add(new NameValue(CloudParamType.TaskStatus, TaskStatus.FAILED.getName()));
									cloudRequest.setNameval(paramList);
									cloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
											serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
								}								
							}else {
								logger.info("Still downloading certificate...");
							}
							break;
						}
						case TSCertDownloadSuccess : {
							String downloadedFilePath = imagesDir + paramMap.get("tsCertFile");
							//copy downloaded file at location: /var/lib/tomcat6/Enlighted/certs/CA/
							File source = new File(downloadedFilePath);
							File destination = new File(CommunicatorConstant.ENL_APP_HOME+"/Enlighted/certs/CA/"+source.getName());
							try {
								FileUtils.copyFile(source, destination);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							//if key store certificate file is available, create download job; else initialize secure connection.  
							if(paramMap.get("ksCertFile") != null){
								createNewCertificateDownloadJob(downloadJobName, downloadTriggerName, paramMap, task, "TSCertDownloadSuccess");								
							} else{
								initializeSecureCloudConnection(task);								
							}							
							break;
						}
						case KSCertDownloadRequested : {
							Scheduler sched = SchedulerManager.getInstance().getScheduler();
							if (!sched.checkExists(new JobKey(downloadJobName, sched.getSchedulerName()))) {
								if("2".equals(task.get(CloudParamType.TaskAttempts))) {
									CloudRequest cloudRequest = new CloudRequest();
									ArrayList<NameValue> paramList = new ArrayList<NameValue>();
									paramList.add(new NameValue(CloudParamType.TaskId, task.get(CloudParamType.TaskId)));
									paramList.add(new NameValue(CloudParamType.TaskStatus, TaskStatus.FAILED.getName()));
									cloudRequest.setNameval(paramList);
									cloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
											serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
								}
								
							}else {
								logger.info("Still downloading certificate...");
							}
							break;							
						}
						case KSCertDownloadSuccess : {
							String downloadedFilePath = imagesDir + paramMap.get("ksCertFile");
							//copy downloaded file at location: /var/lib/tomcat6/Enlighted/certs/
							File source = new File(downloadedFilePath);
							
							//rename any existing pfx file at /var/lib/tomcat6/Enlighted/certs/
							File f = new File(CommunicatorConstant.ENL_APP_HOME+"/Enlighted/certs/");
							File[] matchingFiles = f.listFiles(new FilenameFilter() {
								public boolean accept(File dir, String name) {
									return name.endsWith("pfx");
								}
							});
							if (matchingFiles != null && matchingFiles.length > 0) {
								String sName = matchingFiles[0].getName();
								File pfxFile = new File(CommunicatorConstant.ENL_APP_HOME+"/Enlighted/certs/"+sName);
								File pfxFileRenamed = new File(CommunicatorConstant.ENL_APP_HOME+"/Enlighted/certs/"+sName+"_old");
								pfxFile.renameTo(pfxFileRenamed);
							}
							
							//destination for file copy
							File destination = new File(CommunicatorConstant.ENL_APP_HOME+"/Enlighted/certs/"+source.getName());
							try {
								FileUtils.copyFile(source, destination);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							initializeSecureCloudConnection(task);								
							break;
						}
						default: {
							logger.info("???NOT EXPECTED???");
						}
					}
					break;
				}
				default: {
					logger.info("???NOT EXPECTED???");
				}
			}
		}		
	}
	
	private void initializeSecureCloudConnection(HashMap<CloudParamType, String> task){
		
		SecureCloudConnectionTemplate secureCloudConnectionTemplate = (SecureCloudConnectionTemplate) SpringContext.getBean("secureCloudConnectionTemplate");
		boolean result = InitializeSecureConnection.init(secureCloudConnectionTemplate);
		
		if(result){
			CloudRequest cloudRequest = new CloudRequest();
			ArrayList<NameValue> paramList = new ArrayList<NameValue>();
			paramList.add(new NameValue(CloudParamType.TaskId, task.get(CloudParamType.TaskId)));
			paramList.add(new NameValue(CloudParamType.TaskStatus, TaskStatus.SUCCESS.getName()));
			paramList.add(new NameValue(CloudParamType.TaskProgressStatus, TaskProgressStatus.CertificateSyncSuccess.getName()));
			cloudRequest.setNameval(paramList);
			cloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
					serverInfoManager.getHost(), MediaType.TEXT_PLAIN);								
		}else{
			if("3".equals(task.get(CloudParamType.TaskAttempts))) {
				CloudRequest cloudRequest = new CloudRequest();
				ArrayList<NameValue> paramList = new ArrayList<NameValue>();
				paramList.add(new NameValue(CloudParamType.TaskId, task.get(CloudParamType.TaskId)));
				paramList.add(new NameValue(CloudParamType.TaskStatus, TaskStatus.FAILED.getName()));
				paramList.add(new NameValue(CloudParamType.TaskProgressStatus, TaskProgressStatus.CertificateSyncFailed.getName()));
				cloudRequest.setNameval(paramList);
				cloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
						serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
			}
			else {									
				CloudRequest cloudRequest = new CloudRequest();
				ArrayList<NameValue> paramList = new ArrayList<NameValue>();
				paramList.add(new NameValue(CloudParamType.TaskId, task.get(CloudParamType.TaskId)));
				paramList.add(new NameValue(CloudParamType.TaskAttempts, new Integer(Integer.parseInt(task.get(CloudParamType.TaskAttempts)) + 1).toString()));
				cloudRequest.setNameval(paramList);
				CloudHttpResponse cloudHttpResponse = cloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
						serverInfoManager.getHost(), MediaType.TEXT_PLAIN);									
			}								
		}		
	}

	public void upgradeTask(HashMap<CloudParamType, String> task) throws IllegalArgumentException, SchedulerException {

		HashMap<String, String> paramMap = null;
		JsonUtil<HashMap<String, String>> jsonUtilParamMap = new JsonUtil<HashMap<String, String>>();
		paramMap = jsonUtilParamMap.getObject(task.get(CloudParamType.TaskParameters), new TypeReference<HashMap<String, String>>() { });
		
		String downloadJobName = "job_image_download_" + paramMap.get("imageName");
		String downloadTriggerName = "trigger_image_download_" + paramMap.get("imageName");
		
		
		logger.info("Task");
		logger.info("========");
		for(CloudParamType eachKey: task.keySet()) {
			if(eachKey.equals(CloudParamType.TaskParameters)) {
				for(String mapKey: paramMap.keySet()) {
					logger.info("Task Parameter: Key = " + mapKey + " and value = " + paramMap.get(mapKey));
				}
			}
			else {
				logger.info(eachKey.getName() + ": " + task.get(eachKey));
			}
		}
		logger.info("========");
		
		
		
		if((TaskStatus.valueOf(task.get(CloudParamType.TaskStatus)).getName().equals(task.get(CloudParamType.TaskStatus)))) {
			switch (TaskStatus.valueOf(task.get(CloudParamType.TaskStatus))) {
				case SCHEDULED: {
					Scheduler sched = SchedulerManager.getInstance().getScheduler();
					if (!sched.checkExists(new JobKey(downloadJobName, sched.getSchedulerName()))) {
						createNewDownloadJob(downloadJobName, downloadTriggerName, paramMap, task);
					}
					else {
						logger.info("Still downloading image...");
					}
					break;
				}
				case IN_PROGRESS: {
					//TODO: Further processing should depend on the the image type EM, ADR, SU, GW, etc.
					
					switch (TaskProgressStatus.valueOf(task.get(CloudParamType.TaskProgressStatus))) {
						case ImageDownloadRequested : {
							Scheduler sched = SchedulerManager.getInstance().getScheduler();
							if (!sched.checkExists(new JobKey(downloadJobName, sched.getSchedulerName()))) {
								if("3".equals(task.get(CloudParamType.TaskAttempts))) {
									CloudRequest cloudRequest = new CloudRequest();
									ArrayList<NameValue> paramList = new ArrayList<NameValue>();
									paramList.add(new NameValue(CloudParamType.TaskId, task.get(CloudParamType.TaskId)));
									paramList.add(new NameValue(CloudParamType.TaskStatus, TaskStatus.FAILED.getName()));
									cloudRequest.setNameval(paramList);
									cloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
											serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
								}
								else {
									createNewDownloadJob(downloadJobName, downloadTriggerName, paramMap, task);
								}
							}else {
								logger.info("Still downloading image...");
							}
							break;
						}
						case ImageDownloadSuccess : {
							CloudRequest cloudRequest = new CloudRequest();
							ArrayList<NameValue> paramList = new ArrayList<NameValue>();
							paramList.add(new NameValue(CloudParamType.TaskId, task.get(CloudParamType.TaskId)));
							paramList.add(new NameValue(CloudParamType.TaskAttempts, "1"));
							paramList.add(new NameValue(CloudParamType.TaskProgressStatus, TaskProgressStatus.UpgradingEM.getName()));
							cloudRequest.setNameval(paramList);
							
							CloudHttpResponse cloudHttpResponse = cloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
									serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
							if ("S".equals(cloudHttpResponse.getResponse())) {
								Map<String, String> map = new HashMap<String, String>();
								map.put("file", paramMap.get("imageName"));
								CloudHttpResponse r = cloudConnectionTemplate.executePost(CommunicatorConstant.emUpgradeService, map, "localhost", MediaType.APPLICATION_FORM_URLENCODED);
								logger.info("EM Management Response = " + r.getResponse());
							}
							break;
						}
						case UpgradingEM: {
							
							boolean isUpgradeRunning = false;
							try {
								Runtime rt = Runtime.getRuntime();
								Process proc = rt.exec(new String[] {"head", "-n", "1", CommunicatorConstant.ENL_APP_HOME+"/Enlighted/emsmode"});
								int status = proc.waitFor();
								if(status == 0) {
									BufferedReader outputStream = new BufferedReader(
											new InputStreamReader(proc.getInputStream()));
									String output = null;

									while ((output = outputStream.readLine()) != null) {
										if (output.contains("UPGRADE_RESTORE")) {
											isUpgradeRunning = true;
										}
									}
								}
							} catch (IOException ioe) {
								ioe.printStackTrace();
								break;
							} catch (InterruptedException e) {
								e.printStackTrace();
								break;
							}
							
							if(!isUpgradeRunning) {
								Long debRevision = null;
								Long currentRevision = null;
								try {
									Runtime rt = Runtime.getRuntime();
									Process proc = rt.exec(new String[] {"dpkg-deb", "-f", imagesDir + paramMap.get("imageName") , "CurrentRevision"});
									int status = proc.waitFor();
									if(status == 0) {
										BufferedReader outputStream = new BufferedReader(
												new InputStreamReader(proc.getInputStream()));
										String output = null;

										while ((output = outputStream.readLine()) != null) {
											debRevision = Long.parseLong(output);
										}
									}
									Properties prop = new Properties();
						    		prop.load(new FileInputStream(CommunicatorConstant.ENL_APP_HOME+"/webapps/ems/META-INF/MANIFEST.MF"));
						            logger.info("Current Revision = " + prop.getProperty("Build-Version"));
						            currentRevision = Long.parseLong(prop.getProperty("Build-Version").trim());
								
								} catch (IOException ioe) {
									ioe.printStackTrace();
									break;
								} catch (InterruptedException e) {
									e.printStackTrace();
									break;
								}
								
								if(debRevision != null && currentRevision != null) {
									
									if(debRevision.compareTo(currentRevision) == 0) {
										CloudRequest cloudRequest = new CloudRequest();
										ArrayList<NameValue> paramList = new ArrayList<NameValue>();
										paramList.add(new NameValue(CloudParamType.TaskId, task.get(CloudParamType.TaskId)));
										paramList.add(new NameValue(CloudParamType.TaskStatus, TaskStatus.SUCCESS.getName()));
										paramList.add(new NameValue(CloudParamType.TaskProgressStatus, TaskProgressStatus.EMUpgradeSuccess.getName()));
										cloudRequest.setNameval(paramList);
										cloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
												serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
									}
									else {
										if("3".equals(task.get(CloudParamType.TaskAttempts))) {
											CloudRequest cloudRequest = new CloudRequest();
											ArrayList<NameValue> paramList = new ArrayList<NameValue>();
											paramList.add(new NameValue(CloudParamType.TaskId, task.get(CloudParamType.TaskId)));
											paramList.add(new NameValue(CloudParamType.TaskStatus, TaskStatus.FAILED.getName()));
											paramList.add(new NameValue(CloudParamType.TaskProgressStatus, TaskProgressStatus.EMUpgradeFailed.getName()));
											cloudRequest.setNameval(paramList);
											cloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
													serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
										}
										else {
											
											CloudRequest cloudRequest = new CloudRequest();
											ArrayList<NameValue> paramList = new ArrayList<NameValue>();
											paramList.add(new NameValue(CloudParamType.TaskId, task.get(CloudParamType.TaskId)));
											paramList.add(new NameValue(CloudParamType.TaskAttempts, new Integer(Integer.parseInt(task.get(CloudParamType.TaskAttempts)) + 1).toString()));
											cloudRequest.setNameval(paramList);
											CloudHttpResponse cloudHttpResponse = cloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
													serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
											if ("S".equals(cloudHttpResponse.getResponse())) {
												Map<String, String> map = new HashMap<String, String>();
												map.put("file", paramMap.get("imageName"));
												CloudHttpResponse r = cloudConnectionTemplate.executePost(CommunicatorConstant.emUpgradeService, map, "localhost", MediaType.APPLICATION_FORM_URLENCODED);
												logger.info("EM Management Response = " + r.getResponse());
											}
										}
									}
								}
								else {
									CloudRequest cloudRequest = new CloudRequest();
									ArrayList<NameValue> paramList = new ArrayList<NameValue>();
									paramList.add(new NameValue(CloudParamType.TaskId, task.get(CloudParamType.TaskId)));
									paramList.add(new NameValue(CloudParamType.TaskStatus, TaskStatus.FAILED.getName()));
									paramList.add(new NameValue(CloudParamType.TaskProgressStatus, TaskProgressStatus.EMUpgradeFailed.getName()));
									cloudRequest.setNameval(paramList);
									cloudConnectionTemplate.executePost(CommunicatorConstant.taskUpdateService, JsonUtil.getJSONString(cloudRequest), 
											serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
								}
							}
							break;
						}
						default: {
							logger.info("???NOT EXPECTED???");
						}
					}
					break;
				}
				default: {
					logger.info("???NOT EXPECTED???");
				}
			}
		}
		
	}
	private void createNewLogUploadJob(String uploadJobName,String uploadTriggerName, HashMap<String, String> paramMap,HashMap<CloudParamType, String> task) throws SchedulerException 
	{
					JobDetail uploadJob = newJob(LogUploadJob.class)
							.withIdentity(uploadJobName,
					SchedulerManager.getInstance().getScheduler()
							.getSchedulerName())
			.usingJobData("taskId", task.get(CloudParamType.TaskId))
			.usingJobData("attempts", task.get(CloudParamType.TaskAttempts))
			.usingJobData("fileName", paramMap.get("fileName"))
			.usingJobData("typeOfUpload", paramMap.get("typeOfUpload"))
			.build();
			SimpleTrigger uploadTrigger = (SimpleTrigger) newTrigger()
			.withIdentity(
					uploadTriggerName,
					SchedulerManager.getInstance().getScheduler()
							.getSchedulerName()).startNow().build();
			
			SchedulerManager.getInstance().getScheduler()
			.scheduleJob(uploadJob, uploadTrigger);
				
		
	}
	private void createNewDownloadJob(String downloadJobName, String downloadTriggerName, 
			HashMap<String, String> paramMap, HashMap<CloudParamType, String> task) throws SchedulerException {
		JobDetail downloadJob = newJob(ImageDownloadJob.class)
								.withIdentity(downloadJobName,
						SchedulerManager.getInstance().getScheduler()
								.getSchedulerName())
				.usingJobData("imageId", paramMap.get("imageId"))
				.usingJobData("imageFile", imagesDir + paramMap.get("imageName"))
				.usingJobData("md5", paramMap.get("md5"))
				.usingJobData("taskId", task.get(CloudParamType.TaskId))
				.build();
		SimpleTrigger downloadTrigger = (SimpleTrigger) newTrigger()
				.withIdentity(
						downloadTriggerName,
						SchedulerManager.getInstance().getScheduler()
								.getSchedulerName()).startNow().build();

		SchedulerManager.getInstance().getScheduler()
				.scheduleJob(downloadJob, downloadTrigger);
	}
	
	private void createNewCertificateDownloadJob(String downloadJobName, String downloadTriggerName, 
			HashMap<String, String> paramMap, HashMap<CloudParamType, String> task, String TaskProgressStatus) throws SchedulerException {
		JobDetail downloadJob = null;
		//case : key store certificate exists
		if (paramMap.get("ksCertFile") != null) {
			downloadJob = newJob(CertificateDownloadJob.class)
					.withIdentity(downloadJobName,
							SchedulerManager.getInstance().getScheduler().getSchedulerName())					
					.usingJobData("tsCertFile", imagesDir + paramMap.get("tsCertFile"))
					.usingJobData("md5ts", paramMap.get("md5ts"))
					.usingJobData("taskId", task.get(CloudParamType.TaskId))
					.usingJobData("ksCertFile", imagesDir + paramMap.get("ksCertFile"))
					.usingJobData("md5ks", paramMap.get("md5ks"))
					.usingJobData("ksUuid", paramMap.get("ksUuid"))
					.usingJobData("taskProgressStatus", TaskProgressStatus)
					.build();
		} else{
			downloadJob = newJob(CertificateDownloadJob.class)
					.withIdentity(downloadJobName,
							SchedulerManager.getInstance().getScheduler().getSchedulerName())					
					.usingJobData("tsCertFile", imagesDir + paramMap.get("tsCertFile"))
					.usingJobData("md5ts", paramMap.get("md5ts"))
					.usingJobData("taskId", task.get(CloudParamType.TaskId))
					.usingJobData("taskProgressStatus", TaskProgressStatus)
					.build();			
		}
		
		SimpleTrigger downloadTrigger = (SimpleTrigger) newTrigger()
				.withIdentity(
						downloadTriggerName,
						SchedulerManager.getInstance().getScheduler()
								.getSchedulerName()).startNow().build();

		SchedulerManager.getInstance().getScheduler()
				.scheduleJob(downloadJob, downloadTrigger);
	}

}
