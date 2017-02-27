package com.emcloudinstance.util;

import java.util.Calendar;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;

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
	public String getTime()
	{
		Calendar cal = Calendar.getInstance() ;
		String time = cal.get(Calendar.YEAR)+"-"+(cal.get(Calendar.MONTH) +1)+"-"+cal.get(Calendar.DAY_OF_MONTH)+"%20"+cal.get(Calendar.HOUR_OF_DAY)+ ":" +cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) ;
		return time ;
	}
}
