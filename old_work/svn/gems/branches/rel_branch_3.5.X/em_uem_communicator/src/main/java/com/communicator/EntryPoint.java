package com.communicator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.communicator.service.EmManager;
import com.communicator.util.Communicator;
import com.communicator.util.Globals;
import com.communicator.util.JsonUtil;
import com.communicator.util.NameValue;
import com.communicator.util.SpringAppContext;
import com.communicator.util.UemParamType;
import com.communicator.util.UemRequest;
import com.communicator.util.UemResponse;

public class EntryPoint {

	public static final Logger logger = Logger.getLogger(EntryPoint.class.getName());
	private static ApplicationContext springContext = null ;
	private static EmManager emManager = null;
	
	private static Communicator communicator = null;
	
	static int commReset = 0;
	
	private static ArrayList<NameValue> responseNameValue = new ArrayList<NameValue>();
	
	   
	public static void main(String[] args) {
		Random rndGenerator = new Random();
		try {
			Thread.sleep(rndGenerator.nextInt(3000));
		} catch (InterruptedException e) {
			logger.error(e.toString());
		}
            
		logger.info("--------------------UEM COMMUNICATOR IS STARTING AT "+ new Date() + "----------------------------------");
		//Initialize the logging system
		BasicConfigurator.configure();
		
		//Initialize the Spring factory
		SpringAppContext.init();
		springContext = SpringAppContext.getContext();
		communicator = Communicator.getInstance();
		emManager = (EmManager) springContext.getBean("emManager");
		emManager.getUemInfo();
		
		Thread monitor = new Thread(new EntryPoint.CommunicationMonitor());
		monitor.start();
		
		
		while(true) {
			try {
				commReset = 0;
				if(!"".equals(Globals.uem_ip) && !"".equals(Globals.uem_password) && !"".equals(Globals.uem_username)) {
					poll();
				}
				else {
					Thread.sleep(60000);
					emManager.getUemInfo();
					logger.info("UEM location = " + Globals.HTTPS + Globals.uem_ip);
				}
			}
		 	catch (InterruptedException e) {
				logger.error(e);
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	
	
	private static void poll() {
		UemRequest uemRequest = new UemRequest();
		uemRequest.setNameval(responseNameValue);
		String sb = communicator.webServicePostRequest(JsonUtil.getJSONString(uemRequest) ,Globals.HTTPS + Globals.uem_ip + Globals.uemPollUrl , Globals.uemConnectionTimeout, "text/plain");
		logger.info(Globals.HTTPS + Globals.uem_ip + Globals.uemPollUrl + "::" + sb);
		if(Globals.CONNECTION_FAILURE.equals(sb)) {
			try {
				Thread.sleep(60000);
				emManager.getUemInfo();
			} catch (InterruptedException e) {
				logger.error(e);
			}
			return;
		}
		else {
			process(sb);
		}
	}
	
	private static void process(String sb) {
		responseNameValue = new ArrayList<NameValue>();
		
		JsonUtil<UemResponse> jsonUtil = new JsonUtil<UemResponse>();
		UemResponse uemResponse = jsonUtil.getUemResponseObject(sb, UemResponse.class);
		HashMap<UemParamType, String> respMap = uemResponse.getNameValueMap();

		if(respMap.containsKey(UemParamType.RequestType)) {
			try{
				if((UemParamType.valueOf(respMap.get(UemParamType.RequestType)).getName().equals(respMap.get(UemParamType.RequestType)))) {
					switch (UemParamType.valueOf(respMap.get(UemParamType.RequestType))) {
						case RequestFacilityTree: {
							String data = emManager.getEmFacilityTree(); //Get TreeNode
							if(Globals.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new NameValue(UemParamType.SuccessAck, "F"));
							}
							else {
								responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new NameValue(UemParamType.SuccessAck, "S"));
								responseNameValue.add(new NameValue(UemParamType.PayLoad, data));
							}
							break;
							
						}
						case AddUEMGateway: {
							String payload = respMap.get(UemParamType.PayLoad);
							String data = "";
							if(payload != null) {
								String [] payloads = payload.split("::::");
								data = emManager.addUpdateUEMGateway(payloads[0],payloads[1], payloads[2]); //Get status. Accept host :::: port :::: key
							}
							if(Globals.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new NameValue(UemParamType.SuccessAck, "F"));
							}
							else {
								responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new NameValue(UemParamType.SuccessAck, "S"));
								responseNameValue.add(new NameValue(UemParamType.PayLoad, data));
							}
							break;								
						}
						case RequestAllSensors: {
							String data = emManager.getAllSensors(); //Get SensorList
							if(Globals.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new NameValue(UemParamType.SuccessAck, "F"));
							}
							else {
								responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new NameValue(UemParamType.SuccessAck, "S"));
								responseNameValue.add(new NameValue(UemParamType.PayLoad, data));
							}
							break;
						}
						case RequestSensor : {
							String data = emManager.getSensorData(respMap.get(UemParamType.PayLoad));	//Get Sensor. Accept macAddress
							if(Globals.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new NameValue(UemParamType.SuccessAck, "F"));
							}
							else {
								responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new NameValue(UemParamType.SuccessAck, "S"));
								responseNameValue.add(new NameValue(UemParamType.PayLoad, data));
							}
							break;
						}
						case RequestFloorPlan : {
							String data = emManager.getFloorPlan(Long.parseLong(respMap.get(UemParamType.PayLoad))); //Get planMap encoded. Accept em floor 1.
							if(Globals.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new NameValue(UemParamType.SuccessAck, "F"));
							}
							else {
								responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new NameValue(UemParamType.SuccessAck, "S"));
								responseNameValue.add(new NameValue(UemParamType.PayLoad, data));
							}
							break;
						}
						case RequestDimLevelAndLastConnectivity : {
							String data = emManager.getDimLevelAndLastConnectivityAt(respMap.get(UemParamType.PayLoad)); //Get SensorList. Accept xml fixtures list with mac address.
							if(Globals.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new NameValue(UemParamType.SuccessAck, "F"));
							}
							else {
								responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new NameValue(UemParamType.SuccessAck, "S"));
								responseNameValue.add(new NameValue(UemParamType.PayLoad, data));
							}
							break;
						}
						case SetHB: {
							String payload = respMap.get(UemParamType.PayLoad);
							String data = "";
							if(payload != null) {
								String [] payloads = payload.split("::::");
								data = emManager.setPeriodicAndRealTimeHB(payloads[0], Short.parseShort(payloads[1]), Short.parseShort(payloads[2]), Short.parseShort(payloads[3])); //Get SensorList. Accept xml fixtures list with mac address :::: enableHb :::: enableRealTime :::: triggerDelayTime
							}
							if(Globals.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new NameValue(UemParamType.SuccessAck, "F"));
							}
							else {
								responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new NameValue(UemParamType.SuccessAck, "S"));
							}
							break;
						}
						case SetDimLevel: {
							String payload = respMap.get(UemParamType.PayLoad);
							String data = "";
							if(payload != null) {
								String [] payloads = payload.split("::::");
								data = emManager.setDimLevel(payloads[0],Integer.parseInt(payloads[1]), Integer.parseInt(payloads[2])); //Get SensorList. Accept xml fixtures list with mac address :::: percentage :::: time
							}
							if(Globals.CONNECTION_FAILURE.equals(data)) {
								responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new NameValue(UemParamType.SuccessAck, "F"));
							}
							else {
								responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
								responseNameValue.add(new NameValue(UemParamType.SuccessAck, "S"));
							}
							break;
						}
						default : {
							logger.warn("============No Task=======");
							responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
							responseNameValue.add(new NameValue(UemParamType.SuccessAck, "S"));
						}
					}
				}
			}
			catch (IllegalArgumentException e) {
				responseNameValue.add(new NameValue(UemParamType.RequestType, respMap.get(UemParamType.RequestType)));
				responseNameValue.add(new NameValue(UemParamType.SuccessAck, "E"));
				responseNameValue.add(new NameValue(UemParamType.PayLoad, e.getMessage()));
				logger.error(e);
			}
		}
		
	}
	
	static class CommunicationMonitor implements Runnable {

		@Override
		public void run() {
			while (true) {
				try{
					if(commReset > 12) {
						logger.error("Communicator is stuck for a long time. Killing the process.");
						System.exit(0);
					}
					commReset++;
					Thread.sleep(300000);
				}
				catch (Exception e) {
					logger.error(e);
				}
			}
			
		}
	}
	

        
}

	

	