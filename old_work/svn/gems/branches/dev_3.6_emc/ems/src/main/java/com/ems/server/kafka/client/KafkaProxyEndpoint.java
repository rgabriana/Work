/**
 * 
 */
package com.ems.server.kafka.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.ems.server.util.ServerUtil;

/**
 * The <code>KafkaProxyWebsocketEndpoint</code> class defines the Websocket Endpoint for the Kafka Proxy. 
 * This endpoint provides a websocket based interface to Kafka cluster for the gateways
 * @author Shyam
 *
 */
public class KafkaProxyEndpoint {
	
	private ArrayList<String> subscribedGws = new ArrayList<String>();
	private ArrayList<String> reachableGws = new ArrayList<String>();
 	private static KafkaProxyEndpoint instance = null;

	/***
	 * Logger
	 */
	//private static final Logger logger = LogManager.getFormatterLogger(KafkaProxyEndpoint.class);
	private static Logger logger = Logger.getLogger("KafkaLogger");

	private KafkaProxyEndpoint() {
		
		KafkaClusterManager.init();
		
	}
	
	public static KafkaProxyEndpoint getInstance() {
		
		if(instance == null) {
			synchronized(KafkaProxyEndpoint.class) {
				if(instance == null) {
					instance = new KafkaProxyEndpoint();
				}
			}
		}
		return instance;
		
	}
	
	/****
	 * Called when a new websocket session is established. 
	 * After validation subscribes a KafkaConsumer to the relevant topics of this gateway 
	 * and publishes a session establishment message on the gateway's topic
	 * @param The websocket session
	 */
	public void subscribe(String gwId) {
		
		if(subscribedGws.contains(gwId)) {
			return;
		}
		try {
			KafkaCluster cluster = KafkaClusterManager.getCluster(gwId);
			cluster //Subscribe to topics for this gateway
			.getConsumerManager()
			.subscribe(gwId);
			subscribedGws.add(gwId);
		} catch (Exception e) {
			//logger.catching(e); //This should not happen. Put here to cover unexpected scenarios
			logger.error(gwId + " in scubscribe " + e.getMessage());
		}
	}
	
	public void toggleGatewayReachability(String gwId) {
		
		if(!isGatewayReachable(gwId)) {
			reachableGws.add(gwId);
		} else {
			reachableGws.remove(gwId);
		}
		
	}
	
	public boolean isGatewayReachable(String gwId) {
		
		return reachableGws.contains(gwId);
		
	}

	/***
	 * Called when a new binary message is received on the socket.
	 * Publishes this message on this gateways's via the KafkaConsumer
	 * @param The binary message
	 * @param The websocket session
	 */
	public void sendMessageToGateway(final ByteBuffer message, String gwId) {
		
		if(!subscribedGws.contains(gwId)) {
			subscribe(gwId);
		}
		if(!isGatewayReachable(gwId)) {
			//TODO dont send messages if the gateway is down
//			if(logger.isInfoEnabled()) {
//				logger.info(gwId + " gateway is not reachable");
//			}
			//return;
		}
		try {
			if(logger.isInfoEnabled()) {
				logger.info(gwId + ": to gw binary message: " + ServerUtil.getLogPacket(message.array()));
			}
			KafkaClusterManager
			.getCluster(gwId)
			.getProducerManager()
			.produceBinary(gwId, 
					null, 
					message.array());
		} catch (Exception e) {
			e.printStackTrace();
			//logger.catching(e); //This should not happen. Put here to cover unexpected scenarios
			logger.error("in sendmessage to gateway ", e);
		}
		
	}
	
}
