package com.emcloudinstance.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;

import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.communication.template.CloudConnectionTemplate;
import com.communication.utils.CloudHttpResponse;
@Component("commonUtils")
public class CommonUtils {
	static final Logger logger = Logger.getLogger("CommonUtils");
	@Resource
	CloudConnectionTemplate cloudConnectionTemplate ;
	public  Boolean setReplicaMigrationFlagOnCloud(String mac_id ,String migrationStatus )
	{
		try{
			CloudHttpResponse response = cloudConnectionTemplate.executePost(Constants.SET_MIGRATION_FLAG+mac_id+"/"+migrationStatus ,"Dummy post",Constants.ECLOUD_IP, MediaType.TEXT_PLAIN);
			String state= response.getResponse() ;
			if(!state.equalsIgnoreCase(migrationStatus))
			{
				logger.error("Error while Setting  migration state "+migrationStatus +" on cloud. Please contact Administrator") ;
				return false ;
			}} catch(Exception ex)
			{
				logger.error("Error while Setting replica server migration state "+migrationStatus +" on cloud. Please contact Administrator") ;
				ex.printStackTrace() ;
				return false ;
			}
		return true ;
	}
	public  void setReplicaMigrationFlagLogOnCloud(String mac_id ,String migrationStatus , String log)
	{
		try{
			CloudHttpResponse response = cloudConnectionTemplate.executePost(Constants.SET_MIGRATION_FLAG_LOG+mac_id+"/"+migrationStatus+"/"+log ,"Dummy post",Constants.ECLOUD_IP, MediaType.TEXT_PLAIN);
		} catch(Exception ex)
			{
				logger.error("Error while Setting log for replica server migration state "+migrationStatus +" on cloud. Please contact Administrator") ;
				ex.printStackTrace() ;
		
			}
	}
	public  void setReplicaSyncFlagLogOnCloud(String mac_id ,String migrationStatus , String log)
	{
		try{
			CloudHttpResponse response = cloudConnectionTemplate.executePost(Constants.SET_SYNC_FLAG_LOG+mac_id+"/"+migrationStatus+"/"+log ,"Dummy post",Constants.ECLOUD_IP, MediaType.TEXT_PLAIN);
		} catch(Exception ex)
			{
				logger.error("Error while Setting log for replica server migration state "+migrationStatus +" on cloud. Please contact Administrator") ;
				ex.printStackTrace() ;
		
			}
	}
	
	
	public Boolean updateDeviceHealthStatus(String mac,Integer totalGW, Integer uoGW, Integer cGW, 
			Integer totalSensors, Integer uoSensors, Integer criticalSensors) {
		try {
			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
     		nameValuePairs.add(new BasicNameValuePair("totalGW", totalGW.toString())); 
     		nameValuePairs.add(new BasicNameValuePair("uoGW", uoGW.toString())); 
     		nameValuePairs.add(new BasicNameValuePair("cGW", cGW.toString())); 
     		nameValuePairs.add(new BasicNameValuePair("totalSensors", totalSensors.toString())); 
     		nameValuePairs.add(new BasicNameValuePair("uoSensors", uoSensors.toString())); 
     		nameValuePairs.add(new BasicNameValuePair("criticalSensors", criticalSensors.toString())); 
     		
     		
			CloudHttpResponse response = cloudConnectionTemplate.executePost(
					Constants.UPDATE_DEVICE_HEALTH + mac, nameValuePairs,
					Constants.ECLOUD_IP, MediaType.APPLICATION_FORM_URLENCODED);
			
			String state = response.getResponse();
		} catch (Exception ex) {
			logger.error("Error while updateing device health status for EM with mac "
					+ mac + " on cloud. Please contact Administrator");
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	public String getTime()
	{
		Calendar cal = Calendar.getInstance() ;
	    Date creationDate = cal.getTime();
	    SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    logger.info("Time:- " +date_format.format(creationDate)) ;
	    return date_format.format(creationDate).replaceAll(" " , "%20");
	}
}