package com.emcloudinstance.service;

import static org.quartz.JobKey.jobKey;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import com.communication.template.CloudConnectionTemplate;
import com.communication.utils.CloudHttpResponse;
import com.emcloudinstance.util.Constants;
import com.emcloudinstance.util.SchedulerManager;
import com.emcloudinstance.util.SpringContext;
import com.emcloudinstance.util.UidUtil;

public class UidJob implements Job{

	CloudConnectionTemplate cloudConnectionTemplate;
	UidUtil uidUtil;

	public UidJob(){
		uidUtil = (UidUtil)SpringContext.getBean("uidUtil");
		cloudConnectionTemplate = (CloudConnectionTemplate)SpringContext.getBean("cloudConnectionTemplate");
	}


	public void execute(JobExecutionContext context) throws JobExecutionException {
		String macId = uidUtil.macId;
		String ecloudIp = "";
		Properties prop = new Properties();

		try {
			prop.load(new FileInputStream("/var/lib/tomcat6/Enlighted/config.properties"));
    		ecloudIp = prop.getProperty("ecloudIp");
        } catch (IOException ex) {
    		ex.printStackTrace();
        }

        Constants.ECLOUD_IP = ecloudIp;

		CloudHttpResponse response =  cloudConnectionTemplate.executePost(Constants.UID_SERVICE, macId ,Constants.ECLOUD_IP, MediaType.TEXT_PLAIN);

		String uid = response.getResponse();

		if(uid != null && !"".equals(uid) && uid.matches("^[0-9a-f]+$")){
			  try {
				uidUtil.uid = uid;
				SchedulerManager.getInstance().getScheduler().deleteJob(jobKey("UidJobName", SchedulerManager.getInstance().getScheduler().getSchedulerName()));
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
		}
	}

}
