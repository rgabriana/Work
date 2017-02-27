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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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
import com.communication.utils.NameValue;
import com.communicator.dao.NetworkSettingsDao;
import com.communicator.dao.SystemConfigDao;
import com.communicator.util.CommunicatorConstant;
import com.communicator.util.HardwareInfoUtils;

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
	@Resource
	EmManager emManager;
	@Resource
	NetworkSettingsDao networkSettingsDao ;
	
	private String appVersion = null;
	private String macAddress = null;
	private String host = null;
	private String cloudSyncType = null;
	private String replicaServerIP = null ;
	private Boolean cloudMode = null;

	
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void init(boolean shareKey) {
		if(systemConfigDao.isCloudEnabled().equalsIgnoreCase("0")) {
			return;
		}
		try {
			File manifestFile = new File(
					CommunicatorConstant.ENL_APP_HOME+"/webapps/ems/META-INF/MANIFEST.MF");
			Manifest mf = new Manifest();
			mf.read(new FileInputStream(manifestFile));
			Attributes atts = mf.getAttributes("ems");
			if (atts != null) {
				appVersion = atts.getValue("Implementation-Version") + "."
						+ atts.getValue("Build-Version");
			}

			File drUserFile = new File(
					CommunicatorConstant.ENL_APP_HOME+"/Enlighted/cloudServerInfo.xml");
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
		ArrayList<NameValue> nvs = new ArrayList<NameValue>();
		String apiKey = systemConfigDao.getSysConfigValue("glem.apikey");
		String secretkey = systemConfigDao.getSysConfigValue("glem.secretkey");
		if(apiKey == null || "".equals(apiKey) || secretkey == null || "".equals(secretkey) || shareKey) {
			nvs.add(new NameValue(CloudParamType.ShareKey, "true"));
			cloudrequest.setNameval(nvs);
		}
		else {
			CloudConnectionTemplate.key = secretkey;
		}
		nvs.add(new NameValue(CloudParamType.IpAddress, HardwareInfoUtils.getIpAddress(networkSettingsDao.getCorporateInterfaceName())));
		String temp = emManager.getAllFloors();
		if(temp != null && !CommunicatorConstant.CONNECTION_FAILURE.equals(temp)) {
			nvs.add(new NameValue(CloudParamType.NoOfFloors, temp));
		}
			
		CloudHttpResponse response = cloudConnectionTemplate.executePost(CommunicatorConstant.communicatorInfo, JsonUtil.getJSONString(cloudrequest) ,host, MediaType.TEXT_PLAIN);
		if(response != null && response.getStatus() == 200) {
			logger.info(response.getResponse());
			
			JsonUtil<CloudResponse> jsonUtil = new JsonUtil<CloudResponse>();
			CloudResponse cloudresponse = jsonUtil.getCloudResponseObject(response.getResponse(), CloudResponse.class);
			HashMap<CloudParamType, String> respMap = cloudresponse.getNameValueMap();
			cloudSyncType = respMap.get(CloudParamType.EmCloudSyncStatus);
			if(respMap.get(CloudParamType.CloudMode) != null) {
				setCloudMode(respMap.get(CloudParamType.CloudMode).equals("true"));
			}
			if(respMap.get(CloudParamType.ApiKey) != null) {
				emManager.editSystemConfig("glem.apikey", respMap.get(CloudParamType.ApiKey));
			}
			if(respMap.get(CloudParamType.SecretKey) != null) {
				emManager.editSystemConfig("glem.secretkey", respMap.get(CloudParamType.SecretKey));
				CloudConnectionTemplate.key = respMap.get(CloudParamType.SecretKey);
			}
	
			if ("0".equals(cloudSyncType)) {
				logger.error("EM not registered with Cloud server. Please do so.");
			}
		}
		else {
			logger.error("error accessing cloud master server." + (response != null ? (" http status = " + response.getStatus()) : ""));
		}
	}
	
	public String checkEMAcess() {

		Runtime rt = Runtime.getRuntime();
		Process proc;
		try {
			proc = rt.exec(new String[] { "/bin/bash",
					System.getenv().get("OPT_ENLIGHTED")+"/communicator/check_em_access.sh" });
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
			   out.println(CommunicatorConstant.TOMCAT_SERVICE+" " + state);
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
			logger.error(e.getMessage() , e) ;
		} catch (InterruptedException e) {
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
			   out.println(CommunicatorConstant.TOMCAT_SERVICE+" status");
			   out.println("exit");
			  
			      String line;
			      while ((line = in.readLine()) != null) {
			    	  logger.info(line);
			    	  if(line.contains("is running"))
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
			logger.error(e.getMessage() , e) ;
		} catch (InterruptedException e) {
			logger.error(e.getMessage() , e) ;
		}
		return running ;
	}

	/**
	 * @return the cloudMode
	 */
	public Boolean getCloudMode() {
		return cloudMode;
	}

	/**
	 * @param cloudMode the cloudMode to set
	 */
	private void setCloudMode(Boolean cloudMode) {
		this.cloudMode = cloudMode;
	}
	
}
