/**
 * 
 */
package com.ems.server.kafka.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

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
 	//private static KafkaProxyEndpoint instance = null;
 	private static HashMap<String, KafkaProxyEndpoint> kafkaEndpointMap = new HashMap<String, KafkaProxyEndpoint>();
 	
 	private String network = ""; //network for which this proxy end point is created like gateway or ble 

	/***
	 * Logger
	 */
	//private static final Logger logger = LogManager.getFormatterLogger(KafkaProxyEndpoint.class);
	private static Logger logger = Logger.getLogger("KafkaLogger");

	private KafkaProxyEndpoint(String network) {
		
		this.network = network;
		KafkaClusterManager.init(network);
		
	}
	
	public static KafkaProxyEndpoint getInstance(String network) {
		
		if(!kafkaEndpointMap.containsKey(network)) {
			synchronized(KafkaProxyEndpoint.class) {
				if(!kafkaEndpointMap.containsKey(network)) {
					KafkaProxyEndpoint instance = new KafkaProxyEndpoint(network);
					kafkaEndpointMap.put(network, instance);
				}
			}
		}
		return kafkaEndpointMap.get(network);
		
	}
	
	/****
	 * Called for each gateway to receive the messages for that gateway
	 * @param gwId The gateway Id
	 */
	public void subscribe(String gwId) {
		
		if(subscribedGws.contains(gwId)) {
			return;
		}
		try {
			KafkaCluster cluster = KafkaClusterManager.getGatewayCluster(gwId);
			cluster //Subscribe to topics for this gateway
			.getConsumerManager()
			.subscribe(gwId);
			subscribedGws.add(gwId);
		} catch (Exception e) {
			//logger.catching(e); //This should not happen. Put here to cover unexpected scenarios
			logger.error(gwId + " in scubscribe ", e);
		}
	}
	
	/****
	 * Called for each gateway to remove the topic corresponding to this gateway
	 * @param gwId The gateway Id
	 */
	public void unsubscribe(String gwId) {
		
		try {
			KafkaCluster cluster = KafkaClusterManager.getGatewayCluster(gwId);
			cluster //Subscribe to topics for this gateway
			.getConsumerManager()
			.unsubscribe(gwId);
			subscribedGws.remove(gwId);
		} catch (Exception e) {
			//logger.catching(e); //This should not happen. Put here to cover unexpected scenarios
			logger.error(gwId + " in unscubscribe ", e);
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
	 * Called when a new binary message is ready to be sent to the gateway.
	 * Publishes this message to this gateways's via the KafkaConsumer
	 * @param The binary message
	 * @param the gateway id
	 */
	public void sendMessageToGateway(final ByteBuffer message, String gwId) {
		
		if(!subscribedGws.contains(gwId)) {
			subscribe(gwId);
		}
		/*
		if(!isGatewayReachable(gwId)) {
			logger.info(gwId + " gateway is not reachable");
			//return;
		}
		*/
		try {
			if(logger.isDebugEnabled()) {
				logger.debug(gwId + "- sending binary message: {}" + ServerUtil.getLogPacket(message.array()));
			}
			KafkaClusterManager
			.getGatewayCluster(gwId)
			.getProducerManager()
			.produceGatewayBinary(gwId, 
					null, 
					message.array());
		} catch (Exception e) {
			e.printStackTrace();
			//logger.catching(e); //This should not happen. Put here to cover unexpected scenarios
			logger.error(gwId + "- in sendmessage to gateway ", e);
		}
		
	}
	
	/***
	 * Called when a new binary message is ready to be sent kafka broker for other services to pick it up
	 * like BLE/Asset tracking service
	 * Publishes this message to the given network lik ble and to given topic
	 * @param The binary message
	 * @param the network like ble
	 * @param the topic like "em"
	 */
	public void sendMessageToTopic(final ByteBuffer message, String topic) {
	
		try {
			if(logger.isDebugEnabled()) {
				logger.debug(topic + ": sending binary message: {}" + ServerUtil.getLogPacket(message.array()));
			}
			KafkaClusterManager
			.getCluster(network)
			.getProducerManager()
			.produceBinary(topic, 
					null, 
					message.array());
		} catch (Exception e) {
			e.printStackTrace();
			//logger.catching(e); //This should not happen. Put here to cover unexpected scenarios
			logger.error(network + "- in sendmessage to network ", e);
		}
		
	}
	
}
