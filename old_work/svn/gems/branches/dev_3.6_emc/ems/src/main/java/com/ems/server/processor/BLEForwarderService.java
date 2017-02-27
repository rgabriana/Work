/**
 * 
 */
package com.ems.server.processor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.ems.action.SpringContext;
import com.ems.model.SystemConfiguration;
import com.ems.server.EmsShutdownObserver;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.util.ServerUtil;
import com.ems.service.SystemConfigurationManager;
import com.ems.vo.BLEEvent;
import com.ems.vo.TagData;

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
	private ObjectMapper mapper = new ObjectMapper();
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
					// parse pkt and forward
					BLEEvent oBEE = getBleEvent(data);
					if (oBEE != null) {
						sendBleEvent(oBEE, sc.getValue());
					}

				} catch (Exception e) {
					fixtureLogger.error(e.getMessage());
				}
			}
		}
	}

	public BLEEvent getBleEvent(byte[] pkt) {
		try {
			String sensorId = ServerUtil.getSnapAddr(pkt[8], pkt[9], pkt[10]);
			BLEEvent oBEE = new BLEEvent();
			oBEE.setSensorId(sensorId);
			oBEE.setTs(new Date());
			int pos = 16;
			byte version = pkt[pos++];
			oBEE.setVersion((short)version);
			int noOfTags = pkt[pos++];
			//we are using the getSnapAddr function but it is generic enough to handle any length of byte array
			String scanData = ServerUtil.getSnapAddr(Arrays.copyOfRange(pkt, pos, pos + 10));
			oBEE.setScanData(scanData);
			pos += 10;
			List<TagData> oTagData = new ArrayList<TagData>();
			// As per the spec we will be getting 10 tags (every time)
			for (int i = 0; i < noOfTags; i++) {
				if (pkt.length >= (pos + 10)) {
					TagData oTD = new TagData();
					oTD.setTagId(ServerUtil.getSnapAddr(Arrays.copyOfRange(pkt,
							pos, pos + 6)));
					pos += 6;
					byte rssi = pkt[pos++];
					if(rssi < 0) {
						oTD.setRssi(Integer.toString((short)(256 + rssi), 16));
					} else {
						oTD.setRssi(Integer.toString(rssi, 16));
					}
					oTD.setAccelerometer(ServerUtil.getSnapAddr(Arrays.copyOfRange(pkt, pos, pos+3)));
					pos += 3;
					oTagData.add(oTD);
				}
			}
			oBEE.setTagData(oTagData);
			return oBEE;
		} catch (Exception e) {
			fixtureLogger.error(e.getMessage());
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public void sendBleEvent(BLEEvent event, String url) {
		HttpPost postRequest = new HttpPost(url);
		StringEntity input;
		try {
			input = new StringEntity(mapper.writeValueAsString(event));
			input.setContentType("application/json");
			postRequest.setEntity(input);

			total_count += 1;
			HttpResponse response = client.execute(postRequest);
			if(fixtureLogger.isDebugEnabled()) {
				fixtureLogger.debug(event.getSensorId() + " BLE send status - "
						+ response.getStatusLine().getStatusCode() + " #" + total_count);
			}
			 // Must call this to release the connection
			HttpEntity enty = response.getEntity();
			if (enty != null) {
				enty.consumeContent();
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
