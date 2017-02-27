package com.ems.server.processor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;

import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.log4j.Logger;

import org.codehaus.jackson.JsonGenerationException;

import org.codehaus.jackson.map.JsonMappingException;

import com.ems.action.SpringContext;

import com.ems.model.SystemConfiguration;

import com.ems.server.EmsShutdownObserver;
import com.ems.server.device.DeviceServiceImpl;

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
			fixtureLogger.info("Starting BLEForwarder Service...");
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
					// simply forward entire packet as raw octet stream
					sendBleEvent(data, sc.getValue());

				} catch (Exception e) {
					fixtureLogger.error(e.getMessage());
				}
			}
		}
	}

	public void sendBleEvent(byte [] event, String url) throws IllegalStateException, IOException {
		HttpPost postRequest = new HttpPost(url);
		HttpEntity content;
		try {
			content = new ByteArrayEntity(event, ContentType.APPLICATION_OCTET_STREAM);
			postRequest.setEntity(content);

			total_count += 1;
			HttpResponse response = client.execute(postRequest);
			fixtureLogger.debug("Raw Data BLE send status - "
					+ response.getStatusLine().getStatusCode() + " #" + total_count);
			 // Must call this to release the connection
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				entity.getContent().close();
			}
		} catch (JsonGenerationException e) {
			fixtureLogger.error("BLE - " + e.getMessage());
		} catch (JsonMappingException e) {
			fixtureLogger.error("BLE - " + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			fixtureLogger.error("BLE - " + e.getMessage());
		} catch (IOException e) {
			fixtureLogger.error("BLE - " + e.getMessage());
		} catch (Exception e) {
			fixtureLogger.error("BLE - " + e.getMessage());
		}
	}

	@Override
	public void cleanUp() {
		isProcessing = false;

	}

}
