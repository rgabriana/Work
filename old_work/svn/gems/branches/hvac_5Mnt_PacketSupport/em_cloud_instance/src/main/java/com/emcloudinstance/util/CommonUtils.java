package com.emcloudinstance.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.communication.template.CloudConnectionTemplate;
import com.communication.utils.CloudHttpResponse;
import com.emcloudinstance.service.SyncData;

@Component("commonUtils")
public class CommonUtils {
	static final Logger logger = Logger.getLogger("CommonUtils");
	@Resource
	CloudConnectionTemplate cloudConnectionTemplate ;
	public  Boolean setReplicaMigrationFlagOnCloud(String mac_id ,String migrationStatus, Date lastSuccessfulSyncTime )
	{
		try{
			CloudHttpResponse response = cloudConnectionTemplate.executePost(Constants.SET_MIGRATION_FLAG+mac_id+"/"+migrationStatus + "/" + (lastSuccessfulSyncTime == null ? "-1" : lastSuccessfulSyncTime.getTime()),"Dummy post",Constants.ECLOUD_IP, MediaType.TEXT_PLAIN);
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
	public  void setReplicaMigrationFlagLogOnCloud(String mac_id ,String migrationStatus , String log, Date lastSuccessfulSyncTime)
	{
		try{
			CloudHttpResponse response = cloudConnectionTemplate.executePost(Constants.SET_MIGRATION_FLAG_LOG+mac_id+"/"+migrationStatus+"/"+log + "/" + (lastSuccessfulSyncTime == null ? "-1" : lastSuccessfulSyncTime.getTime()),"Dummy post",Constants.ECLOUD_IP, MediaType.TEXT_PLAIN);
		} catch(Exception ex)
			{
				logger.error("Error while Setting log for replica server migration state "+migrationStatus +" on cloud. Please contact Administrator") ;
				ex.printStackTrace() ;
		
			}
	}
	public  void setReplicaSyncFlagLogOnCloud(String mac_id ,String migrationStatus , String log, Date lastSuccessfulSyncTime)
	{
		try{
			
			CloudHttpResponse response = cloudConnectionTemplate.executePost(Constants.SET_SYNC_FLAG_LOG+mac_id+"/"+migrationStatus+"/"+log + "/" + (lastSuccessfulSyncTime == null ? "-1" : lastSuccessfulSyncTime.getTime()),"Dummy post",Constants.ECLOUD_IP, MediaType.TEXT_PLAIN);
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
	
	 /**
     * Unmarshal XML to Wrapper and return List value.
     */
	public static <T> List<T> unmarshal(Unmarshaller unmarshaller,
			Class<T> clazz, String xml) throws JAXBException {
		 StreamSource xmlSource = new StreamSource(new StringReader (xml));
		Wrapper<T> wrapper = (Wrapper<T>) unmarshaller.unmarshal(xmlSource,Wrapper.class).getValue();
		return wrapper.getItems();
	}
	 /**
     * Wrap List in Wrapper, then leverage JAXBElement to supply root element
     * information.
     */
    public static String marshal(Marshaller marshaller, List<?> list, String name)
            throws JAXBException {
    	StringWriter sw = new StringWriter();
        QName qName = new QName(name);
        Wrapper wrapper = new Wrapper(list);
        JAXBElement<Wrapper> jaxbElement = new JAXBElement<Wrapper>(qName,
                Wrapper.class, wrapper);
        marshaller.marshal(jaxbElement, sw);
        return sw.toString() ;
    }  
    
    public  Boolean isSyncPaused(String mac_id) {
		CloudHttpResponse response = cloudConnectionTemplate.executeGet(Constants.IS_SYNC_PAUSED+mac_id,Constants.ECLOUD_IP);
		String state= response.getResponse() ;
		if(state != null && "N".equals(state)) {
			return false;
		}
		else {
			return true ;
		}
	}
    
    public static Integer getNoOfThreads() {
    	Properties prop = new Properties();
		String noOfThreads = null;
		int ret = 4;
		try {
			prop.load(new FileInputStream("/var/lib/tomcat6/Enlighted/config.properties"));
    		noOfThreads  = prop.getProperty("noOfThreads");
        } catch (IOException ex) {
    		logger.error(ex.getMessage(), ex);
        }
        if(noOfThreads != null && !"".equals(noOfThreads)) {
        	ret = Integer.parseInt(noOfThreads);
        }
        System.out.println("Number of executor threads allowed = " + ret);
        return ret;
    }
    
    public static void createSyncDataDir() {
    	File f = new File(SyncData.syncDataDir);
    	while (!f.exists()) {
    		logger.info("Syncdata directory does not exist. Creating new. Status = " + f.exists() );
    		boolean state = f.mkdir();
    		if(!state) {
    			try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					logger.error(e);
				}
    		}
    	}
    }

}
