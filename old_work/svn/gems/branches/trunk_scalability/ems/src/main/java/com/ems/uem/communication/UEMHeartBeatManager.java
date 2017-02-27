package com.ems.uem.communication;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;

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
	
	/**
	 * Method used by Em(Scheduler) to continuously poll the UEM after every 5 minutes.It gets the apikey and secretkey from the UEM 
	 * (After activation only once) , with each ping it updates the last connectivity time at UEM end.
	 * This method always pings UEM with macaddress , ipaddress and version (Other details can also be filled up  , they are
	 * mentioned in EmSync class). (They are never null or empty).
	 * 
	 */
	public static void callUEM() {

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
		
		FloorManager floorManager = (FloorManager) SpringContext
        .getBean("floorManager");
		
		String noOfFloors = "0";
		
		try {
			//TODO OPTIMIZE (get noOfFloors from the cache)
			noOfFloors = Integer.toString(floorManager.getAllFloorsOfCompany().size());
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		if (uemPingFlag != null) {
			if (UemConstants.uemIsEnabled.equalsIgnoreCase(uemPingFlag.getValue())) {
				if (uemIp != null) {
					if (!uemIp.getValue().equalsIgnoreCase("")) {
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
							
							if(UemConstants.emAck==1)
							{
								//UemConstants.emAck = 1;
								jsonDataObject.put("ack", UemConstants.emAck);
							}
							else if(localApiKey==null || localApiKey.equalsIgnoreCase(""))
							{
								UemConstants.emAck = 0;
								jsonDataObject.put("ack", UemConstants.emAck);
							}
							else if(localApiKey!=null && !localApiKey.equalsIgnoreCase(""))
							{
								UemConstants.emAck = 2;
								jsonDataObject.put("ack", UemConstants.emAck);
							}							

							WebResource webResource = client
									.resource(wsAddress);
							String input = jsonDataObject.toString();
							ClientResponse response = webResource.type(
									"application/json").post(
									ClientResponse.class, input);
							
							Response rs = response.getEntity(Response.class);							
							String mApiKeyFromUem = rs.getMsg();	
							String mSecretKeyFromUem = rs.getCommMessage(); 
							UemConstants.mAckFromUem= rs.getStatus();		
														
							
							if(UemConstants.emAck==0 && UemConstants.mAckFromUem==1)
							{
								//set the apikey
								if(mApiKeyFromUem!=null && !mApiKeyFromUem.equalsIgnoreCase(""))
								{						
									if(mSecretKeyFromUem != null && !mSecretKeyFromUem.equalsIgnoreCase(""))
									{
										//Store the secret key as well
										uemLocalSecretKey.setValue(mSecretKeyFromUem);
										systemConfigurationManager.update(uemLocalSecretKey);
									}
								uemLocalApiKey.setValue(mApiKeyFromUem);
								systemConfigurationManager.update(uemLocalApiKey);
								UemConstants.emAck=1;
								UEMHeartBeatManager.callUEM();																
								}																
							}	
							else if(UemConstants.emAck==0 && UemConstants.mAckFromUem==2)
							{
								//UemConstants.emAck = 0;
							}
							else if(UemConstants.emAck==1 && UemConstants.mAckFromUem==2)
							{
								UemConstants.emAck = 2;								
							}
							else if(UemConstants.emAck==1 && UemConstants.mAckFromUem==0)
							{
								//EM has lost the previous 1 packet from UEM and it is set to 2 at his end
								UemConstants.emAck = 0;
								uemLocalApiKey.setValue(null);
								systemConfigurationManager.update(uemLocalApiKey);	
								UEMHeartBeatManager.callUEM();
							}
							else if(UemConstants.emAck==2 && UemConstants.mAckFromUem==0)
							{
								UemConstants.emAck = 0;
								//Reset the database values of the apiKey
								uemLocalApiKey.setValue(null);
								systemConfigurationManager.update(uemLocalApiKey);	
								UEMHeartBeatManager.callUEM();
							}
							else if(UemConstants.emAck==2 && UemConstants.mAckFromUem==-1)
							{
								// Do nothing as em is deactivated at UEM end
								//Reset the apiKey
								uemLocalApiKey.setValue(null);
								systemConfigurationManager.update(uemLocalApiKey);								
							}								
							else if(UemConstants.mAckFromUem==-1)
							{
								uemLocalApiKey.setValue(null);
								systemConfigurationManager.update(uemLocalApiKey);																
							}							
						} catch (Exception e) {
							// TODO: handle exception
							//Set Em Ack to 0 if UEM connect exception or any kind of connection error 
							//UemConstants.emAck=0;
							System.err.println("could not send the packet over SSL to UEM: " + e.getMessage());
						}
					}
				}
			}
		}
	}
}