package com.communicator.manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.communication.template.CloudConnectionTemplate;
import com.communication.types.CloudParamType;
import com.communication.utils.CloudHttpResponse;
import com.communication.utils.CloudRequest;
import com.communication.utils.CloudResponse;
import com.communication.utils.JsonUtil;
import com.communicator.dao.SystemConfigDao;
import com.communicator.util.CommunicatorConstant;

@Service("serverInfoManager")
public class ServerInfoManager {


	static final Logger logger = Logger.getLogger(ServerInfoManager.class
			.getName());

	@Resource 
	SystemConfigDao systemConfigDao;
	@Resource 
	ServerInfoManager serverInfoManager;
	@Resource 
	CloudConnectionTemplate cloudConnectionTemplate;
	
	private String appVersion = null;
	private String macAddress = null;
	private String host = null;
	private String cloudSyncType = null;
	private String replicaServerIP = null ;

	
	public void init() {
		try {
			File manifestFile = new File(
					"/var/lib/tomcat6/webapps/ems/META-INF/MANIFEST.MF");
			Manifest mf = new Manifest();
			mf.read(new FileInputStream(manifestFile));
			Attributes atts = mf.getAttributes("ems");
			if (atts != null) {
				appVersion = atts.getValue("Implementation-Version") + "."
						+ atts.getValue("Build-Version");
			}

			File drUserFile = new File(
					"/var/lib/tomcat6/Enlighted/cloudServerInfo.xml");
			if (drUserFile.exists()) {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(drUserFile.getAbsoluteFile());
				NodeList server = doc.getElementsByTagName("server");
				if (server != null && server.getLength() > 0) {
					NodeList each = server.item(0).getChildNodes();
					host = each.item(0).getFirstChild().getNodeValue();
					macAddress = each.item(1).getFirstChild().getNodeValue();
					CloudConnectionTemplate.macId = macAddress.toLowerCase();
				}
				logger.info("Cloud Server Info & EM Mac Id = " + host + " "
						+ macAddress);
			}
		} catch (FileNotFoundException e) {
			logger.error( e.toString(), e);
		} catch (IOException e) {
			logger.error( e.toString(), e);
		} catch (SAXException e) {
			logger.error( e.toString(), e);
		} catch (ParserConfigurationException e) {
			logger.error( e.toString(), e);
		} catch (Exception e) {
			logger.error( e.toString(), e);
		}
		CloudRequest cloudrequest = new CloudRequest(macAddress, appVersion);
		CloudHttpResponse response = cloudConnectionTemplate.executePost(CommunicatorConstant.communicatorInfo, JsonUtil.getJSONString(cloudrequest) ,host, MediaType.TEXT_PLAIN);
		logger.info(response.getResponse());
		
		JsonUtil<CloudResponse> jsonUtil = new JsonUtil<CloudResponse>();
		CloudResponse cloudresponse = jsonUtil.getCloudResponseObject(response.getResponse(), CloudResponse.class);
		HashMap<CloudParamType, String> respMap = cloudresponse.getNameValueMap();
		cloudSyncType = respMap.get(CloudParamType.EmCloudSyncStatus);

		if ("0".equals(cloudSyncType)) {
			logger.error("EM not registered with Cloud server. Please do so.");
		}
		

	}

	public String checkCloudConnectivity() {
		return systemConfigDao.checkCloudConnectivity();	
	}
	
	public String checkEMAcess() {

		Runtime rt = Runtime.getRuntime();
		Process proc;
		try {
			proc = rt.exec(new String[] { "/bin/bash",
					"/opt/enLighted/communicator/check_em_access.sh" });
			BufferedReader outputStream = new BufferedReader(
					new InputStreamReader(proc.getInputStream()));
			String output = null;

			while ((output = outputStream.readLine()) != null) {
				if (output.contains("200 OK")) {
					return "TRUE";
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return "FALSE";
	}

	public String getAppVersion() {
		return appVersion;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public String getHost() {
		return host;
	}


	public String getReplicaServerIP() {
		
			return replicaServerIP;
	}

	public void setReplicaServerIP(String replicaServerIP) {
		this.replicaServerIP = replicaServerIP;
	}

	/**
	 * @return the cloudSyncType
	 */
	public String getCloudSyncType() {
		return cloudSyncType;
	}

	/**
	 * @param cloudSyncType the cloudSyncType to set
	 */
	public void setCloudSyncType(String cloudSyncType) {
		this.cloudSyncType = cloudSyncType;
	}
	
	public void startStopTomcatServer(String state)
	{
		//fire the dump on the database.
		File wd = new File("/bin");
		Runtime rt = Runtime.getRuntime();
		Process proc;
		BufferedReader in = null ;
		PrintWriter out  =null ;
		try {
			proc = rt.exec("/bin/bash", null , wd);
		
		if (proc != null) {
			   in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			   out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
			   out.println("/etc/init.d/tomcat6 " + state);
			   out.println("exit");
			  
			      String line;
			      while ((line = in.readLine()) != null) {
			    	  logger.info(line);
			      }
			      proc.waitFor();
			      in.close();
			      out.close();
			      proc.destroy();
			   }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage() , e) ;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage() , e) ;
		}
	}
	//returns true on running else false
	public boolean istomcatServerRunning()
	{
		boolean running =false;
		//fire the dump on the database.
		File wd = new File("/bin");
		Runtime rt = Runtime.getRuntime();
		Process proc;
		BufferedReader in = null ;
		PrintWriter out  =null ;
		try {
			proc = rt.exec("/bin/bash", null , wd);
		
		if (proc != null) {
			   in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			   out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
			   out.println("/etc/init.d/tomcat6 status");
			   out.println("exit");
			  
			      String line;
			      while ((line = in.readLine()) != null) {
			    	  logger.info(line);
			    	  if(line.contains("Tomcat servlet engine is running"))
			    	  {
			    		  running=true ; 
			    		  break ;
			    	  }
			      }
			      proc.waitFor();
			      in.close();
			      out.close();
			      proc.destroy();
			   }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage() , e) ;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage() , e) ;
		}
		return running ;
	}
	
}
