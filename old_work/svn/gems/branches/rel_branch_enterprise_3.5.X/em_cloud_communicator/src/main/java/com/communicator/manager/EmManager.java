package com.communicator.manager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.utils.JsonUtil;
import com.communicator.CommunicatorEntryPoint;
import com.communicator.util.CommunicatorConstant;
import com.communicator.util.Response;

@Repository("emManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmManager {
	
	public static final Logger logger = Logger.getLogger(EmManager.class.getName());
		
	public String getSensorData(String mac) {
		StringBuilder sb = CommunicatorEntryPoint.emCommunicator.emWebServiceGetRequest(CommunicatorConstant.getSensorInfo + mac, CommunicatorConstant.emConnectionTimeout, "application/json", MediaType.APPLICATION_JSON);
		if(CommunicatorConstant.CONNECTION_FAILURE.equals(sb.toString())) {
			return CommunicatorConstant.CONNECTION_FAILURE;
		}
		JsonUtil<Response> jsonUtil = new JsonUtil<Response>();
		Response resp = jsonUtil.getObject(sb.toString(), Response.class);
		logger.info(resp.getMsg());
		return resp.getMsg();
	}
	
	public String getEmFacilityTree() {
		StringBuilder sb = CommunicatorEntryPoint.emCommunicator.emWebServiceGetRequest(CommunicatorConstant.getEmFacilityTree, CommunicatorConstant.emConnectionTimeout, "application/json", MediaType.APPLICATION_JSON);
		if(CommunicatorConstant.CONNECTION_FAILURE.equals(sb.toString())) {
			return CommunicatorConstant.CONNECTION_FAILURE;
		}
		JsonUtil<Response> jsonUtil = new JsonUtil<Response>();
		Response resp = jsonUtil.getObject(sb.toString(), Response.class);
		logger.info(resp.getMsg());
		return resp.getMsg();
	}
	
	public String getAllSensors() {
		StringBuilder sb = CommunicatorEntryPoint.emCommunicator.emWebServiceGetRequest(CommunicatorConstant.getAllSensors, CommunicatorConstant.emConnectionTimeout, "application/json", MediaType.APPLICATION_JSON);
		if(CommunicatorConstant.CONNECTION_FAILURE.equals(sb.toString())) {
			return CommunicatorConstant.CONNECTION_FAILURE;
		}
		JsonUtil<Response> jsonUtil = new JsonUtil<Response>();
		Response resp = jsonUtil.getObject(sb.toString(), Response.class);
		logger.info(resp.getMsg());
		return resp.getMsg();
	}
	
	public String getFloorPlan(Long id) {
		StringBuilder sb = CommunicatorEntryPoint.emCommunicator.emWebServiceGetRequest(CommunicatorConstant.getFloorPlan + id, CommunicatorConstant.emConnectionTimeout, null, "application/octet-stream");
		if(CommunicatorConstant.CONNECTION_FAILURE.equals(sb.toString())) {
			return CommunicatorConstant.CONNECTION_FAILURE;
		}
		logger.info(sb.length());
		return sb.toString();
	}
	
	public String setOccChangeTrigger(String fixturesArr, Short enable, Short triggerDelayTime, Short ack) {
		String sb = CommunicatorEntryPoint.emCommunicator.emWebServicePostRequest(fixturesArr, CommunicatorConstant.setOccChangeTrigger + 
				enable + "/" + 
				triggerDelayTime + "/" + 
				ack, 
				CommunicatorConstant.emConnectionTimeout, "application/xml", MediaType.APPLICATION_JSON);
		if(CommunicatorConstant.CONNECTION_FAILURE.equals(sb.toString())) {
			return CommunicatorConstant.CONNECTION_FAILURE;
		}
		JsonUtil<Response> jsonUtil = new JsonUtil<Response>();
		Response resp = jsonUtil.getObject(sb.toString(), Response.class);
		logger.info(resp.getMsg());
		return "";
	}
	
	public String setDimLevel(String data, Integer percentage, Integer time) {
		String sb = CommunicatorEntryPoint.emCommunicator.emWebServicePostRequest(data, CommunicatorConstant.setDimLevelURL + percentage + "/" + time, CommunicatorConstant.emConnectionTimeout, "application/xml", MediaType.APPLICATION_XML);
		if(CommunicatorConstant.CONNECTION_FAILURE.equals(sb.toString())) {
			return CommunicatorConstant.CONNECTION_FAILURE;
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
				logger.error(e.getMessage(), e) ;
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage(), e) ;
			}
		}
		logger.info(resp.getMsg());
		return "";
	}
	
	public String getDimLevelAndLastConnectivityAt(String data) {
		String sb = CommunicatorEntryPoint.emCommunicator.emWebServicePostRequest(data, CommunicatorConstant.getDimLevelsAndConnectivityURL, CommunicatorConstant.emConnectionTimeout, "application/xml", MediaType.APPLICATION_JSON);
		JsonUtil<Response> jsonUtil = new JsonUtil<Response>();
		Response resp = jsonUtil.getObject(sb.toString(), Response.class);
		logger.info(resp.getMsg());
		return resp.getMsg();
	}
	
	public String addUpdateUEMGateway(String host, String port, String key) {
		StringBuilder sb = CommunicatorEntryPoint.emCommunicator.emWebServiceGetRequest(CommunicatorConstant.addUpdateUEMGatewayURL + host + "/" + port + "/" + key, CommunicatorConstant.emConnectionTimeout, "application/json", MediaType.APPLICATION_JSON);
		if(CommunicatorConstant.CONNECTION_FAILURE.equals(sb.toString())) {
			return CommunicatorConstant.CONNECTION_FAILURE;
		}
		JsonUtil<Response> jsonUtil = new JsonUtil<Response>();
		Response resp = jsonUtil.getObject(sb.toString(), Response.class);
		logger.info(resp.getMsg());
		return resp.getMsg();
	}
	
	public String editSystemConfig(String name, String value) {
		String sb = CommunicatorEntryPoint.emCommunicator.emWebServicePostRequest("<systemConfiguration><name>" + name + "</name><value>" + value + "</value></systemConfiguration>",
				CommunicatorConstant.editSystemConfigURL, CommunicatorConstant.emConnectionTimeout, "application/xml", null);
		if(CommunicatorConstant.CONNECTION_FAILURE.equals(sb.toString())) {
			return CommunicatorConstant.CONNECTION_FAILURE;
		}
		logger.info(sb.toString());
		return sb.toString();
	}
	
	public String getAllFloors() {
		StringBuilder sb = CommunicatorEntryPoint.emCommunicator.emWebServiceGetRequest(CommunicatorConstant.getAllFloorsOfCompany, CommunicatorConstant.emConnectionTimeout, MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN);
		if(CommunicatorConstant.CONNECTION_FAILURE.equals(sb.toString())) {
			return CommunicatorConstant.CONNECTION_FAILURE;
		}
		if(sb.length() > 10) {
			return null;
		}
		return sb.toString();
	}
	
	public void getUemInfo() {
		StringBuilder sb = CommunicatorEntryPoint.emCommunicator.emWebServiceGetRequest(CommunicatorConstant.getUemInfoUrl, CommunicatorConstant.emConnectionTimeout, "application/json", MediaType.APPLICATION_JSON);
		if(CommunicatorConstant.CONNECTION_FAILURE.equals(sb.toString())) {
			return;
		}
		JsonUtil<Response> jsonUtil = new JsonUtil<Response>();
		Response resp = jsonUtil.getObject(sb.toString(), Response.class);
		logger.info(resp.getMsg());
		if(resp.getMsg() != null && !"".equals(resp.getMsg())) {
			String[] vals = resp.getMsg().split("::::");
			if(vals != null && vals.length == 4 && vals[0].equals("1")) {
				CommunicatorConstant.uem_password = vals[1];
				CommunicatorConstant.uem_username = vals[2];
				CommunicatorConstant.uem_ip = vals[3];
			}
		}
		
		try {
			File manifestFile = new File(
					CommunicatorConstant.ENL_APP_HOME+"/webapps/ems/META-INF/MANIFEST.MF");
			Manifest mf = new Manifest();
			mf.read(new FileInputStream(manifestFile));
			Attributes atts = mf.getAttributes("ems");
			if (atts != null) {
				CommunicatorConstant.appVersion = atts.getValue("Implementation-Version") + "."	+ atts.getValue("Build-Version");
			}
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		
	}

}
