/**
 * 
 */
package com.communicator.callhome;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.core.MediaType;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.log4j.Logger;

import com.communication.template.CloudConnectionTemplate;
import com.communication.utils.CloudHttpResponse;
import com.communicator.em.EMInstance;
import com.communicator.em.EMManager;
import com.constants.CloudConstants;

/**
 * @author yogesh
 * 
 */
public class CallHomeThread extends Thread {
	private static final Logger logger = Logger.getLogger(CallHomeThread.class
			.getName());

	private CloudConnectionTemplate m_CC = null;
	private CloudConnectionTemplate m_Secure = null;
	private EMInstance m_oInstance = null;
	private Long m_seqId = 0L;
	private EMManager oManager = EMManager.getInstance();

	public CallHomeThread(EMInstance instance) {
		setName("CH-" + instance.getsMAC());
		m_CC = new CloudConnectionTemplate();
		m_Secure = new CloudConnectionTemplate();
		m_oInstance = instance;
	}

	public void run() {
		logger.info(getName() + " starting...");
		m_Secure.setUpCertificateDetails(
				CloudConnectionTemplate.JKS_STORE_TYPE,
				"deploy/certs/enlighted.ts", "enlighted",
				CloudConnectionTemplate.PKCS_STORE_TYPE,
				"deploy/certs/em_50_200.pfx", "em_50_200");
		while (true) {
			try {
				sendHeartBeat();
				Thread.sleep(CloudConstants.INTERVAL);
			} catch (InterruptedException e) {
				logger.warn(e.getMessage());
			}
		}
	}

	/**
	 * Called for first time registration...
	 */
	private void sendHeartBeat() {

		logger.info(m_oInstance.getsMAC() + ": START HB event");
		try {
			CloudHttpResponse response = m_CC.executePost(
					"/ecloud/services/org/communicate/em/info",
					m_oInstance.getsMAC() + "#" + m_oInstance.getsVersion(),
					oManager.getsPPAHostServer(), MediaType.APPLICATION_OCTET_STREAM);
			String responseBody = response.getResponse();
			Integer emInstance = -1;
			emInstance = Integer.parseInt(responseBody);
			logger.info("EM ID is " + emInstance);
			if (emInstance == -1) {
				logger.info(m_oInstance.getsMAC()
						+ ": EM not registered with Cloud server.");
			} else {
				sendCallHome();
			}
			oManager.incTotalHealthCheckCount();
		} catch (Exception e) {
			logger.info(m_oInstance.getsMAC() + ": " + e);
		} finally {
			logger.info(m_oInstance.getsMAC() + ": END HB event");
		}
	}

	/**
	 * Called every 5 mins
	 */
	private void sendCallHome() {
		logger.info(m_oInstance.getsMAC() + ": START CH event");

		ByteArrayOutputStream baos_other = new ByteArrayOutputStream();
		ZipOutputStream zipOtherOut = new ZipOutputStream(baos_other);
		try {
			Map<String, Object> map = new HashMap<String, Object>();

			TimeZone tz = Calendar.getInstance().getTimeZone();

			map.put("macId", m_oInstance.getsMAC());
			map.put("version", m_oInstance.getsVersion());
			map.put("timeZone", tz);
			map.put("em_accessible", true);

			// start fill simulation
			m_seqId++;
			Random oRandom = new Random();
			map.put("id", m_seqId);
			map.put("capture_at", new Date());
			map.put("active_thread_count", oRandom.nextInt(85));
			Long gcCount = new Long(oRandom.nextInt(150));
			map.put("gc_count", gcCount);
			Long gcTime = new Long(oRandom.nextInt(5000));
			map.put("gc_time", gcTime);
			BigDecimal obj = new BigDecimal(oRandom.nextInt(100));
			map.put("heap_used", obj);
			obj = new BigDecimal(oRandom.nextInt(100));
			map.put("non_heap_used", obj);
			obj = new BigDecimal(oRandom.nextInt(10));
			map.put("sys_load", obj);
			obj = new BigDecimal(oRandom.nextInt(100));
			map.put("cpu_percentage", obj);
			logger.info(m_oInstance.getsMAC() + ": " + m_seqId + " sending {"
					+ map.toString() + "}");
			// end fill simulation

			zipOtherOut.putNextEntry(new ZipEntry("data"));
			ObjectOutputStream outObj = new ObjectOutputStream(zipOtherOut);

			try {
				outObj.writeObject(map);
				zipOtherOut.closeEntry();
			} catch (Exception e) {
				logger.error(m_oInstance.getsMAC() + ": " + e);
			} finally {
				if (outObj != null) {
					try {
						outObj.close();
					} catch (Exception e) {
						logger.error(m_oInstance.getsMAC() + ": " + e);
					}
				}
			}
			logger.info(m_oInstance.getsMAC() + ": compressed  data size = "
					+ baos_other.toByteArray().length);

			MultipartEntity parts = new MultipartEntity();

			ByteArrayBody bytearray = new ByteArrayBody(
					baos_other.toByteArray(), "data");
			parts.addPart("data", bytearray);

			CloudHttpResponse response = m_Secure.executePost(
					"/ecloud/services/org/communicate/em/stats", parts,
					oManager.getsPPAHostServer());
			String ip = response.getResponse();
			if (ip.equalsIgnoreCase("fail")) {
				logger.warn(m_oInstance.getsMAC()
						+ ": SYNC MIGHT HAVE FAILED! {"
						+ ip.replaceAll("\n", "") + "}");
			} else {
				if (!ip.equalsIgnoreCase("sucess")) {
					m_oInstance.setsReplicaServerIP(ip.trim());
					oManager.setReplicaServer(m_oInstance);
				}
				logger.info(m_oInstance.getsMAC()
						+ ": SYNC status, replica server assigned => " + ip);
			}
			oManager.incTotalCallHomeCount();
		} catch (Exception e) {
			logger.error(m_oInstance.getsMAC() + ": " + e);
		} finally {
			if (zipOtherOut != null) {
				try {
					zipOtherOut.close();
				} catch (Exception e) {
					logger.error(m_oInstance.getsMAC() + ": " + e);
				}
			}
			if (baos_other != null) {
				try {
					baos_other.close();
				} catch (Exception e) {
					logger.error(m_oInstance.getsMAC() + ": " + e);
				}
			}
			logger.info(m_oInstance.getsMAC() + ": END CH event");
		}
	}

}
