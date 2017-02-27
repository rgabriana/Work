/**
 * 
 */
package com.communicator.callhome;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
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
import org.codehaus.jackson.type.TypeReference;

import com.communication.template.CloudConnectionTemplate;
import com.communication.template.SecureCloudConnectionTemplate;
import com.communication.types.CloudParamType;
import com.communication.types.DatabaseState;
import com.communication.types.EmStatus;
import com.communication.utils.CloudHttpResponse;
import com.communication.utils.CloudRequest;
import com.communication.utils.CloudResponse;
import com.communication.utils.JsonUtil;
import com.communication.utils.NameValue;
import com.communicator.em.EMInstance;
import com.communicator.em.EMManager;
import com.communicator.util.CommunicatorConstant;
import com.constants.CloudConstants;

/**
 * @author yogesh
 * 
 */
public class CallHomeThread extends Thread {
	private static final Logger logger = Logger.getLogger(CallHomeThread.class
			.getName());

	private CloudConnectionTemplate m_CC = null;
	private SecureCloudConnectionTemplate m_Secure = null;
	private EMInstance m_oInstance = null;
	private Long m_seqId = 0L;
	private EMManager oManager = EMManager.getInstance();

	public CallHomeThread(EMInstance instance) {
		setName("CH-" + instance.getsMAC());
		m_CC = new CloudConnectionTemplate();
		m_Secure = new SecureCloudConnectionTemplate();
		m_oInstance = instance;
	}

	public void run() {
		logger.info(getName() + " starting...");
		m_Secure.setUpCertificateDetails(
				SecureCloudConnectionTemplate.JKS_STORE_TYPE,
				"deploy/certs/enlighted.ts", "enlighted",
				SecureCloudConnectionTemplate.PKCS_STORE_TYPE,
				"deploy/certs/em_101_101.pfx", "em_101_101");
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
					oManager.getsPPAHostServer(),
					MediaType.APPLICATION_OCTET_STREAM);
			String responseBody = response.getResponse();
			Integer emInstance = -1;
			emInstance = Integer.parseInt(responseBody);
			logger.info("EM ID is " + emInstance);
			if (emInstance == -1) {
				logger.info(m_oInstance.getsMAC()
						+ ": EM not registered with Cloud server.");
			} else {
				sendCallHomev2();
			}
			oManager.incTotalHealthCheckCount();
		} catch (Exception e) {
			logger.info(m_oInstance.getsMAC() + ": " + e);
		} finally {
			logger.info(m_oInstance.getsMAC() + ": END HB event");
		}
	}

	private void sendCallHomev2() {
		CloudRequest cloudrequest = new CloudRequest(m_oInstance.getsMAC(),
				m_oInstance.getsVersion());
		ArrayList<NameValue> list = new ArrayList<NameValue>();
		cloudrequest.setNameval(list);

		try {
			TimeZone tz = Calendar.getInstance().getTimeZone();
			list.add(new NameValue(CloudParamType.EmTimezone, tz.getID()));
			list.add(new NameValue(CloudParamType.StatsEmAccessible, "true"));

			// start fill simulation
			Random oRandom = new Random();
			m_seqId++;
			list.add(new NameValue(CloudParamType.StatsId, String
					.valueOf(m_seqId)));
			list.add(new NameValue(CloudParamType.StatsCaptureAt, String
					.valueOf(System.currentTimeMillis())));
			list.add(new NameValue(CloudParamType.StatsActiveThreadCount,
					String.valueOf(oRandom.nextInt(85))));
			list.add(new NameValue(CloudParamType.StatsGcCount, String
					.valueOf(oRandom.nextInt(150))));
			list.add(new NameValue(CloudParamType.StatsGcTime, String
					.valueOf(oRandom.nextInt(5000))));
			list.add(new NameValue(CloudParamType.StatsHeadUsed, String
					.valueOf(oRandom.nextInt(100))));
			list.add(new NameValue(CloudParamType.StatsNonHeapUsed, String
					.valueOf(oRandom.nextInt(100))));
			list.add(new NameValue(CloudParamType.StatsSysLoad, String
					.valueOf(oRandom.nextInt(10))));
			list.add(new NameValue(CloudParamType.StatsCpuPercentage, String
					.valueOf(oRandom.nextInt(100))));
			// end fill simulation

			logger.info(m_oInstance.getsMAC() + ": " + m_seqId + " sending {"
					+ JsonUtil.getJSONString(cloudrequest) + "}");
			CloudHttpResponse response = m_Secure.executePost(
					CommunicatorConstant.callHomeService,
					JsonUtil.getJSONString(cloudrequest),
					oManager.getsPPAHostServer(), MediaType.TEXT_PLAIN);
			JsonUtil<CloudResponse> jsonUtil = new JsonUtil<CloudResponse>();
			CloudResponse cloudresponse = jsonUtil.getCloudResponseObject(
					response.getResponse(), CloudResponse.class);
			HashMap<CloudParamType, String> respMap = cloudresponse
					.getNameValueMap();
			logger.info(m_oInstance.getsMAC() + ":- "
					+ JsonUtil.getJSONString(cloudresponse));

			if (respMap.containsKey(CloudParamType.ReplicaServerIp)) {
				m_oInstance.setsReplicaServerIP(respMap
						.get(CloudParamType.ReplicaServerIp));
				oManager.setReplicaServer(m_oInstance);
			}

			if (respMap.containsKey(CloudParamType.EmCloudSyncStatus)) {
				m_oInstance
						.setCurrentEmStatus(respMap.get(
								CloudParamType.EmCloudSyncStatus)
								.equalsIgnoreCase("1") ? EmStatus.CALL_HOME
								: EmStatus.SPPA);
			}
			if (respMap.containsKey(CloudParamType.MigrationStatusDetails)) {
				try {
					ArrayList<HashMap<CloudParamType, String>> MigrationDetails = null;
					JsonUtil<ArrayList<HashMap<CloudParamType, String>>> jsonUtilListMap = new JsonUtil<ArrayList<HashMap<CloudParamType, String>>>();
					MigrationDetails = jsonUtilListMap
							.getObject(
									respMap.get(CloudParamType.MigrationStatusDetails),
									new TypeReference<ArrayList<HashMap<CloudParamType, String>>>() {
									});

					for (HashMap<CloudParamType, String> state : MigrationDetails) {
						if (state
								.containsKey(CloudParamType.CurrentMigrationStatus)) {
							m_oInstance
									.setCurrentMigrationState(DatabaseState.valueOf(state
											.get(CloudParamType.CurrentMigrationStatus)));
						}

					}
				} catch (Exception e) {
					logger.error("Error while getting migration Details from replica server : "
							+ e.getMessage());
				}
			}

			if (cloudresponse.getStatus() != 0) {
				logger.info(m_oInstance.getsMAC() + ": CALL HOME SYNC MIGHT HAVE FAILED!!");
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
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
