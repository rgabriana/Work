package com.ems.uem.communication;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.ems.action.SpringContext;
import com.ems.model.SystemConfiguration;
import com.ems.server.ServerMain;
import com.ems.service.FloorManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.uem.constants.UemConstants;
import com.ems.util.HardwareInfoUtils;
import com.ems.ws.util.Response;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class UEMHeartBeatManager {
	private static final Logger logger = Logger.getLogger("WSLogger");

	/**
	 * Method used by Em(Scheduler) to continuously poll the UEM after every 5 minutes.It gets the apikey and secretkey from the UEM 
	 * (After activation only once) , with each ping it updates the last connectivity time at UEM end.
	 * This method always pings UEM with macaddress , ipaddress and version (Other details can also be filled up  , they are
	 * mentioned in EmSync class). (They are never null or empty).
	 * 
	 */
	public static void callUEM() {
		logger.debug("callUEM");
		Integer emAck = 0;
		
		//Integer mAckFromUem = 0;

		// Check for the uem flag verify and then ping the UEM.		
		SystemConfigurationManager systemConfigurationManager = (SystemConfigurationManager) SpringContext
		.getBean("systemConfigurationManager");

		SystemConfiguration uemPingFlag = systemConfigurationManager
				.loadConfigByName("uem.enable");
		SystemConfiguration uemIp = systemConfigurationManager
				.loadConfigByName("uem.ip");	
		
		SystemConfiguration uemLocalApiKey = systemConfigurationManager
		.loadConfigByName("uem.apikey");
		SystemConfiguration uemLocalSecretKey = systemConfigurationManager
		.loadConfigByName("uem.secretkey");
		// Start scheduler only when flag is 1 and ip is available
		// System.out.println("UEM Flag : "+uemPingFlag.getValue());
		// System.out.println("UEM Ip : "+uemIp.getValue());
		
		SystemConfiguration enableProfileFeature = systemConfigurationManager
		.loadConfigByName("enable.profilefeature");
		
		FloorManager floorManager = (FloorManager) SpringContext
        .getBean("floorManager");
		
		String noOfFloors = "0";
		
		try {
			noOfFloors = Integer.toString(floorManager.getAllFloorsOfCompany().size());
		} catch (SQLException e1) {
			logger.error(e1.getMessage());
		} catch (IOException e1) {
			logger.error(e1.getMessage());
		}
		
		if (uemPingFlag != null) {
			if (UemConstants.uemIsEnabled.equalsIgnoreCase(uemPingFlag.getValue())) {
				if (uemIp != null) {
					if (!uemIp.getValue().equalsIgnoreCase("")) {
						logger.info("Communicating with " + uemIp.getValue());
						// End check
						String wsAddress = UemConstants.uemProtocol + uemIp.getValue()
								+ UemConstants.webServiceToPing;
						String ipAddress = HardwareInfoUtils
								.getIpAddress("eth0");
						byte[] mac = HardwareInfoUtils
								.getMacAddressForIp(ipAddress);
						String macAddress = HardwareInfoUtils.macBytetoString(
								':', mac);
						String version = ServerMain.getInstance()
								.getGemsVersion();
						String buildno = ServerMain.getInstance()
								.getGemsBuildVersion();
						if(buildno!=null){
							version=version+"."+buildno;
						}
								
						// If mac address is null or empty dont execute the web
						// service.
						if (macAddress == null
								|| macAddress.equalsIgnoreCase("")
								|| ipAddress == null
								|| ipAddress.equalsIgnoreCase("")
								|| version == null
								|| version.equalsIgnoreCase("")) {
							return;
						}	
						
						macAddress = macAddress.toUpperCase();

						try {
							//System.out.println("Calling webservice to UEM");							
							
							String localApiKey = uemLocalApiKey.getValue();							
							
							String localUemKey = uemLocalSecretKey.getValue();
							
							TimeZone tz = Calendar.getInstance().getTimeZone();
							
							Client client = ClientHelper.createClient();

							JSONObject jsonDataObject = new JSONObject();		
							
							String emName = "EM"+macAddress.substring(macAddress.length() - 8).replaceAll(":", "");							
							jsonDataObject.put("ipaddress", ipAddress);
							jsonDataObject.put("macaddress", macAddress);
							jsonDataObject.put("version", version);
							jsonDataObject.put("timeZone", tz.getID());
							jsonDataObject.put("emname", emName);
							jsonDataObject.put("noOfFloors", noOfFloors);
		
							jsonDataObject.put("ack", emAck);
							logger.debug("Request: " + jsonDataObject.toString());

							WebResource webResource = client
									.resource(wsAddress);
							String input = jsonDataObject.toString();
							ClientResponse response = webResource.type(
									"application/json").post(
									ClientResponse.class, input);
							
							Response rs = response.getEntity(Response.class);							
							String mApiKeyFromUem = rs.getMsg();	
							String mSecretKeyFromUem = rs.getCommMessage(); 
							//mAckFromUem= rs.getStatus();		
							
							logger.debug("Response: " + rs.toString());
														
							if (mSecretKeyFromUem != null && !mSecretKeyFromUem.equals("") && mApiKeyFromUem != null && !mApiKeyFromUem.equals("")) {
								//set the apikey & Store the secret key as well
								if(!mSecretKeyFromUem.equals(localUemKey) && !mApiKeyFromUem.equals(localApiKey))
								{
									uemLocalSecretKey.setValue(mSecretKeyFromUem);
									systemConfigurationManager.update(uemLocalSecretKey);
									uemLocalApiKey.setValue(mApiKeyFromUem);
									systemConfigurationManager.update(uemLocalApiKey);
									enableProfileFeature.setValue("false");
									systemConfigurationManager.update(enableProfileFeature);
									
								}
							}else {
								if (localApiKey != null && !localApiKey.trim().equals("")) {
									uemLocalSecretKey.setValue(null);
									systemConfigurationManager.update(uemLocalSecretKey);
									uemLocalApiKey.setValue(null);
									systemConfigurationManager.update(uemLocalApiKey);
									enableProfileFeature.setValue("true");
									systemConfigurationManager.update(enableProfileFeature);
								}
							}
						} catch (Exception e) {
							// TODO: handle exception
							//Set Em Ack to 0 if UEM connect exception or any kind of connection error 
							//UemConstants.emAck=0;
							logger.error("could not send the packet over SSL to GLEM: " + e.getMessage());
						}
					}
				}
			}
		}
	}
}