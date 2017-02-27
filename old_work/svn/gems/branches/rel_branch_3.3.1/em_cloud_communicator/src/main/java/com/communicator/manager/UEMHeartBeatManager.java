package com.communicator.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.communication.template.CloudConnectionTemplate;
import com.communicator.CommunicatorEntryPoint;
import com.communicator.dao.SystemConfigDao;
import com.communicator.util.CommunicatorConstant;
import com.communicator.util.HardwareInfoUtils;

@Service("uemHeartbeatManager")
@Transactional(propagation = Propagation.REQUIRED)
public class UEMHeartBeatManager {
	private static final Logger logger = Logger.getLogger(UEMHeartBeatManager.class.getName());
	
	@Resource 
	SystemConfigDao systemConfigDao;
	@Resource
	EmManager emManager;

	/**
	 * Method used by Em(Scheduler) to continuously poll the UEM after every 5 minutes.It gets the apikey and secretkey from the UEM 
	 * (After activation only once) , with each ping it updates the last connectivity time at UEM end.
	 * This method always pings UEM with macaddress , ipaddress and version (Other details can also be filled up  , they are
	 * mentioned in EmSync class). (They are never null or empty).
	 * 
	 */
	public void callUEM() {
		logger.debug("callUEM");
		Integer emAck = 0;

		String uemPingFlag = systemConfigDao.getSysConfigValue("uem.enable");
		String uemIp = systemConfigDao.getSysConfigValue("uem.ip");	
		
		String uemLocalApiKey = systemConfigDao.getSysConfigValue("uem.apikey");
		String uemLocalSecretKey = systemConfigDao.getSysConfigValue("uem.secretkey");
			
		if (uemPingFlag != null) {
			if ("1".equalsIgnoreCase(uemPingFlag)) {
				if (uemIp != null) {
					if (!uemIp.equalsIgnoreCase("")) {
						logger.info("Communicating with " + uemIp);
						
						
						String wsAddress = CommunicatorConstant.HTTPS+ uemIp + CommunicatorConstant.webServiceToPing;
						String macAddress = null;
						
						try {
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
									macAddress = each.item(1).getFirstChild().getNodeValue();
									CloudConnectionTemplate.macId = macAddress.toLowerCase();
								}
								logger.info("EM Mac Id = " + macAddress);
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
						
						if (macAddress == null	|| macAddress.equalsIgnoreCase("")) {
							logger.error("mac address is null or empty");
							return;
						}	
						
						macAddress = macAddress.toUpperCase();
						
						try {						
							
							TimeZone tz = Calendar.getInstance().getTimeZone();
							JSONObject jsonDataObject = new JSONObject();		
							
							String emName = "EM"+macAddress.substring(macAddress.length() - 8).replaceAll(":", "");							
							jsonDataObject.put("ipaddress", HardwareInfoUtils.getIpAddress("eth0"));
							jsonDataObject.put("macaddress", macAddress);
							jsonDataObject.put("version", CommunicatorConstant.appVersion);
							jsonDataObject.put("timeZone", tz.getID());
							jsonDataObject.put("emname", emName);
							String noOfFloors = emManager.getAllFloors();
							if(noOfFloors != null && !CommunicatorConstant.CONNECTION_FAILURE.equals(noOfFloors)) {
								jsonDataObject.put("noOfFloors", noOfFloors);
							}
							
							jsonDataObject.put("ack", emAck);
							logger.info("Request: " + jsonDataObject.toString());

							
							String sb = CommunicatorEntryPoint.uemCommunicator.postWithoutLogin(jsonDataObject.toString(), wsAddress, CommunicatorConstant.uemConnectionTimeout, "application/json", "application/json");
							com.communicator.uem.JsonUtil<com.communicator.uem.Response> jsonUtil = new com.communicator.uem.JsonUtil<com.communicator.uem.Response>();
							com.communicator.uem.Response rs = jsonUtil.getObject(sb.toString(), com.communicator.uem.Response.class);
							
							String mApiKeyFromUem = rs.getMsg();	
							String mSecretKeyFromUem = rs.getCommmessage(); 
							
							logger.info("Response: " + rs.toString());
														
							if (mSecretKeyFromUem != null && !mSecretKeyFromUem.equals("") && mApiKeyFromUem != null && !mApiKeyFromUem.equals("")) {
								//set the apikey & Store the secret key as well
								if(!mSecretKeyFromUem.equals(uemLocalSecretKey) && !mApiKeyFromUem.equals(uemLocalApiKey)) {
									systemConfigDao.updateSysConfigValue("uem.apikey", mApiKeyFromUem);
									systemConfigDao.updateSysConfigValue("uem.secretkey", mSecretKeyFromUem);
									CommunicatorConstant.uem_password = null;
								}
							}else {
								if (uemLocalApiKey != null && !uemLocalApiKey.trim().equals("")) {
									systemConfigDao.updateSysConfigValue("uem.apikey", null);
									systemConfigDao.updateSysConfigValue("uem.secretkey", null);
									CommunicatorConstant.uem_password = null;
								}
							}
							//systemConfigDao.updateSysConfigValue("enable.profilefeature", "true");
						} catch (Exception e) {
							logger.error("could not send the packet over SSL to GLEM: " + e.getMessage(), e);
						}
					}
				}
			}
		}
	}
}