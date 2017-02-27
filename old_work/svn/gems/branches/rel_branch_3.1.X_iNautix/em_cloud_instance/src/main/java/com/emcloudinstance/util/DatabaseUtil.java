package com.emcloudinstance.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Component;

import com.communication.template.CloudConnectionTemplate;
import com.communication.types.CloudParamType;
import com.communication.types.TaskStatus;
import com.communication.utils.CloudHttpResponse;
import com.communication.utils.CloudRequest;
import com.communication.utils.JsonUtil;
import com.communication.utils.NameValue;

@Component("databaseUtil")
public class DatabaseUtil {
	
	public String port = "5433";
	
	@Resource
	public Map<String, String> dbMap;
	@Resource
	UidUtil uidUtil;
	@Resource
	CloudConnectionTemplate cloudConnectionTemplate;
	static final Logger logger = Logger.getLogger("EmCloudInstance");
	@PostConstruct
	public void init(){
		try {
			Process pr = null;
			String[] cmdArr = { "/bin/bash", "/var/lib/tomcat6/webapps/em_cloud_instance/adminscripts/getPostgresPort.sh" };
			BufferedReader br = null;
			pr = Runtime.getRuntime().exec(cmdArr);
			pr.waitFor();
			br = new BufferedReader(new InputStreamReader(
					pr.getInputStream()));
			String localPort = null;
			while (true) {
				localPort = br.readLine();
				if (localPort != null && !"".equals(localPort)) {
					break;
				}
			}
			localPort = localPort.trim();
			this.port = localPort;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void createDatabase(String databaseName) {
        Process pr = null;
         try {
            String[] cmdArr = { "/usr/bin/createdb", "-U", "postgres", databaseName };
            pr = Runtime.getRuntime().exec(cmdArr);
            pr.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	public void dropDatabase(String databaseName) {
        Process pr = null;
        
        try {
        	//First Release all connection 
        	String query ="SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = '"+databaseName+"'" ;
            String[] releaseConnections = { "/usr/bin/psql", "-U", "postgres", "-c" ,query } ;
            pr = Runtime.getRuntime().exec(releaseConnections);
            pr.waitFor();
          
            // then drop database
            String[] cmdArr = { "/usr/bin/dropdb", "-U", "postgres", databaseName };
            pr = Runtime.getRuntime().exec(cmdArr);
            pr.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage() , e);
        }
    }
	
	
	public String getDbNameByMac(String macId) {
		String dbname = null;
		if (dbMap != null) {
			dbname = dbMap.get(macId);
		}
		if(dbname != null && !"".equals(dbname)) {
			return dbname;
		}
		else {
			CloudRequest cloudRequest = new CloudRequest();
			ArrayList<NameValue> paramList = new ArrayList<NameValue>();
			paramList.add(new NameValue(CloudParamType.ReplicaServerUID, uidUtil.uid));
			paramList.add(new NameValue(CloudParamType.TaskStatus, TaskStatus.FAILED.getName()));
			cloudRequest.setNameval(paramList);
			CloudHttpResponse response =  cloudConnectionTemplate.executePost(Constants.MAC_DB_CACHE_SERVICE, JsonUtil.getJSONString(cloudRequest), 
					Constants.ECLOUD_IP, MediaType.TEXT_PLAIN);
			
			JsonUtil<HashMap<String, String>> jsonUtil = new JsonUtil<HashMap<String, String>>();
			dbMap  = jsonUtil.getObject(response.getResponse(), new TypeReference<HashMap<String, String>>() {} );

			return dbMap.get(macId);
		}
		
	}

}
