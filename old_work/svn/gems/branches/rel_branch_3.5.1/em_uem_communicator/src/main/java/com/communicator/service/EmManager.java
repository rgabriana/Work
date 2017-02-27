package com.communicator.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communicator.util.Communicator;
import com.communicator.util.Globals;
import com.communicator.util.JsonUtil;
import com.communicator.util.Response;

@Repository("emManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmManager {
	
	public static final Logger logger = Logger.getLogger(EmManager.class.getName());
	
	public void getUemInfo() {
		StringBuilder sb = Communicator.getInstance().emWebServiceGetRequest(Globals.getUemInfoUrl, Globals.emConnectionTimeout, "application/json");
		if(Globals.CONNECTION_FAILURE.equals(sb.toString())) {
			return;
		}
		JsonUtil<Response> jsonUtil = new JsonUtil<Response>();
		Response resp = jsonUtil.getObject(sb.toString(), Response.class);
		logger.info(resp.getMsg());
		if(resp.getMsg() != null && !"".equals(resp.getMsg())) {
			String[] vals = resp.getMsg().split("::::");
			if(vals != null && vals.length == 4 && vals[0].equals("1")) {
				Globals.uem_password = vals[1];
				Globals.uem_username = vals[2];
				Globals.uem_ip = vals[3];
			}
		}
		
		try {
			File manifestFile = new File(
					"/var/lib/tomcat6/webapps/ems/META-INF/MANIFEST.MF");
			Manifest mf = new Manifest();
			mf.read(new FileInputStream(manifestFile));
			Attributes atts = mf.getAttributes("ems");
			if (atts != null) {
				Globals.appVersion = atts.getValue("Implementation-Version") + "."
						+ atts.getValue("Build-Version");
			}
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		
	}
	
	public String getSensorData(String mac) {
		StringBuilder sb = Communicator.getInstance().emWebServiceGetRequest(Globals.getSensorInfo + mac, Globals.emConnectionTimeout, "application/json");
		if(Globals.CONNECTION_FAILURE.equals(sb.toString())) {
			return Globals.CONNECTION_FAILURE;
		}
		JsonUtil<Response> jsonUtil = new JsonUtil<Response>();
		Response resp = jsonUtil.getObject(sb.toString(), Response.class);
		logger.info(resp.getMsg());
		return resp.getMsg();
	}
	
	public String getEmFacilityTree() {
		StringBuilder sb = Communicator.getInstance().emWebServiceGetRequest(Globals.getEmFacilityTree, Globals.emConnectionTimeout, "application/json");
		if(Globals.CONNECTION_FAILURE.equals(sb.toString())) {
			return Globals.CONNECTION_FAILURE;
		}
		JsonUtil<Response> jsonUtil = new JsonUtil<Response>();
		Response resp = jsonUtil.getObject(sb.toString(), Response.class);
		logger.info(resp.getMsg());
		return resp.getMsg();
	}
	
	public String getAllSensors() {
		StringBuilder sb = Communicator.getInstance().emWebServiceGetRequest(Globals.getAllSensors, Globals.emConnectionTimeout, "application/json");
		if(Globals.CONNECTION_FAILURE.equals(sb.toString())) {
			return Globals.CONNECTION_FAILURE;
		}
		JsonUtil<Response> jsonUtil = new JsonUtil<Response>();
		Response resp = jsonUtil.getObject(sb.toString(), Response.class);
		logger.info(resp.getMsg());
		return resp.getMsg();
	}
	
	public String getFloorPlan(Long id) {
		StringBuilder sb = Communicator.getInstance().emWebServiceGetRequest(Globals.getFloorPlan + id, Globals.emConnectionTimeout, "application/octet-stream");
		if(Globals.CONNECTION_FAILURE.equals(sb.toString())) {
			return Globals.CONNECTION_FAILURE;
		}
		logger.info(sb.length());
		return sb.toString();
	}
	
	public String setPeriodicAndRealTimeHB(String fixturesArr, Short enableHb, Short enableRealTime, Short triggerDelayTime) {
		String sb = Communicator.getInstance().emWebServicePostRequest(fixturesArr, Globals.setPeriodicAndRealTimeHB + 
																	enableHb + "/" + 
																	enableRealTime + "/" + 
																	triggerDelayTime, 
																	Globals.emConnectionTimeout, "application/xml");
		if(Globals.CONNECTION_FAILURE.equals(sb.toString())) {
			return Globals.CONNECTION_FAILURE;
		}
		JsonUtil<Response> jsonUtil = new JsonUtil<Response>();
		Response resp = jsonUtil.getObject(sb.toString(), Response.class);
		logger.info(resp.getMsg());
		return "";
	}
	
	public String setDimLevel(String data, Integer percentage, Integer time) {
		String sb = Communicator.getInstance().emWebServicePostRequest(data, Globals.setDimLevelURL + percentage + "/" + time, Globals.emConnectionTimeout, "application/xml");
		if(Globals.CONNECTION_FAILURE.equals(sb.toString())) {
			return Globals.CONNECTION_FAILURE;
		}
		Response resp = new Response();
		JAXBContext jc;
		if(sb != null && !"".equals(sb)) {
			try {
				InputStream stream = new ByteArrayInputStream(sb.getBytes("UTF-8"));
				jc = JAXBContext.newInstance(Response.class);
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				resp = (Response)unmarshaller.unmarshal(stream);
			} catch (JAXBException e) {
				logger.error(e);
			} catch (UnsupportedEncodingException e) {
				logger.error(e);
			}
		}
		logger.info(resp.getMsg());
		return "";
	}
	
	public String getDimLevelAndLastConnectivityAt(String data) {
		String sb = Communicator.getInstance().emWebServicePostRequest(data, Globals.getDimLevelsAndConnectivityURL, Globals.emConnectionTimeout, "application/xml");
		JsonUtil<Response> jsonUtil = new JsonUtil<Response>();
		Response resp = jsonUtil.getObject(sb.toString(), Response.class);
		logger.info(resp.getMsg());
		return resp.getMsg();
	}
	
	public String addUpdateUEMGateway(String host, String port, String key) {
		StringBuilder sb = Communicator.getInstance().emWebServiceGetRequest(Globals.addUpdateUEMGatewayURL + host + "/" + port + "/" + key, Globals.emConnectionTimeout, "application/json");
		if(Globals.CONNECTION_FAILURE.equals(sb.toString())) {
			return Globals.CONNECTION_FAILURE;
		}
		JsonUtil<Response> jsonUtil = new JsonUtil<Response>();
		Response resp = jsonUtil.getObject(sb.toString(), Response.class);
		logger.info(resp.getMsg());
		return resp.getMsg();
	}

}
