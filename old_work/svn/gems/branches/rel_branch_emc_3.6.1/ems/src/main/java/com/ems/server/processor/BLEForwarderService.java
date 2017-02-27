package com.ems.server.processor;

import java.nio.ByteBuffer;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;

import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;

import com.ems.model.SystemConfiguration;

import com.ems.server.EmsShutdownObserver;
import com.ems.server.ServerConstants;
import com.ems.server.device.DeviceServiceImpl;

import com.ems.server.kafka.client.KafkaProxyEndpoint;

import com.ems.service.SystemConfigurationManager;

/**
 * Support sending BLE enabled events in json encoded format to registered URL.
 * URL can be set in system_configuration table for "ble.forwarding.server" key
 * @author Yogesh
 *
 */

public class BLEForwarderService extends Thread implements EmsShutdownObserver {
	private static BLEForwarderService m_instance = null;
	private boolean isProcessing = false;
	private static Logger fixtureLogger = Logger.getLogger("FixtureLogger");
	private LinkedBlockingQueue<byte[]> bleEventQueue = null;
	private HttpClient client = new DefaultHttpClient();
	private int total_count = 0;

	private SystemConfigurationManager sysConfigManager;

	private BLEForwarderService() {

	}

	public static BLEForwarderService getInstance() {
		if (m_instance == null) {
			m_instance = new BLEForwarderService();
		}
		return m_instance;
	}

	public void run() {
		if (sysConfigManager == null) {
			sysConfigManager = (SystemConfigurationManager) SpringContext
					.getBean("systemConfigurationManager");
		}

		if (isProcessing == false) {
			isProcessing = true;
			if(fixtureLogger.isInfoEnabled()) {
				fixtureLogger.info("Starting BLEForwarder Service...");
			}
			bleEventQueue = DeviceServiceImpl.getInstance()
					.getSuBLEEventQueue();
			byte[] data = null;

			while (isProcessing) {
				try {
					SystemConfiguration sc = sysConfigManager
							.loadConfigByName("ble.forwarding.server");
					if (sc == null || sc.getValue() == null
							|| sc.getValue().equals("")) {
						Thread.sleep(10000);
						continue;
					}
					data = bleEventQueue.poll(5, TimeUnit.SECONDS);
					if (data == null) {
						continue;
					}

					// forward the packet to the kafka
					sendKafkaBleEvent(data);
				
				} catch (Exception e) {
					fixtureLogger.error(e.getMessage());
				}
			}
		}
	}

	/*
	 * sends the raw ble event
	 */
	public void sendKafkaBleEvent(byte[] bleEvent) {
		
		total_count++;
		KafkaProxyEndpoint.getInstance(ServerConstants.KAFKA_BLE_NETWORK).sendMessageToTopic(ByteBuffer.wrap(bleEvent), 
				ServerConstants.KAFKA_BLE_TOPIC);
		if(fixtureLogger.isDebugEnabled()) {
			fixtureLogger.debug("sent " + total_count + " messages");
		}
		
	} //end of method sendKafkaBleEvent
	
	@Override
	public void cleanUp() {
		isProcessing = false;

	}

}
