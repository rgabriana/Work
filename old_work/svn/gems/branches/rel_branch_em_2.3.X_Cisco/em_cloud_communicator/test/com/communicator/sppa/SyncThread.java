/**
 * 
 */
package com.communicator.sppa;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.log4j.Logger;

import com.communication.template.CloudConnectionTemplate;
import com.communication.template.SecureCloudConnectionTemplate;
import com.communication.utils.CloudHttpResponse;
import com.communicator.em.EMInstance;
import com.communicator.em.EMManager;
import com.communicator.util.CommunicatorConstant;
import com.constants.CloudConstants;

/**
 * @author yogesh TODO: Pending...
 * 
 */
public class SyncThread extends Thread {
	private static final Logger logger = Logger.getLogger(SyncThread.class
			.getName());

	private SecureCloudConnectionTemplate m_Secure = null;
	private EMManager oManager = EMManager.getInstance();

	public SyncThread() {
		m_Secure = new SecureCloudConnectionTemplate();
	}

	public void run() {
		m_Secure.setUpCertificateDetails(
				SecureCloudConnectionTemplate.JKS_STORE_TYPE,
				"deploy/certs/enlighted.ts", "enlighted",
				SecureCloudConnectionTemplate.PKCS_STORE_TYPE,
				"deploy/certs/em_50_200.pfx", "em_50_200");
		Set<String> oEMKeys = oManager.getEMKeys();
		Iterator<String> itr = null;
		while (true) {
			try {
				Thread.sleep(CloudConstants.INTERVAL);
				itr = oEMKeys.iterator();
				while (itr.hasNext()) {
					EMInstance oInstance = oManager.getEM(itr.next());
					logger.info(oInstance.getsMAC() + ": sync check");
					if (oInstance.getsReplicaServerIP() != null
							&& !oInstance.getsReplicaServerIP().equals("")) {
						logger.info(oInstance.getsMAC()
								+ ": ready for EM sPPA sync <"
								+ oInstance.getsReplicaServerIP() + ">");
						startMigration(oInstance);
					}
				}

			} catch (InterruptedException e) {
				logger.warn(e.getMessage());
			}
		}
	}

	/**
	 * Send the dump of the database of the local EM
	 * @param oInstance
	 */
	private void startMigration(EMInstance oInstance) {
		logger.info(oInstance.getsMAC() + ": START MIGRATION sync event");
		ByteArrayOutputStream baos_other = new ByteArrayOutputStream();
		ZipOutputStream zipOtherOut = new ZipOutputStream(baos_other);
		byte[] dumpcontents = null;
		byte[] energyContents = null;
		File dumpFile = new File("deploy/db/sppa_migration.sql");
		File energyFile = new File("deploy/db/sppa_migration_energy.sql");
		try {
			dumpcontents = FileUtils.readFileToByteArray(dumpFile);
			energyContents = FileUtils.readFileToByteArray(energyFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("macId", oInstance.getsMAC());
		map.put("version", oInstance.getsVersion());
		map.put("dump", dumpcontents);
		map.put("energy", energyContents);
		ObjectOutputStream outObj = null;
		try {

			zipOtherOut.putNextEntry(new ZipEntry("emInfo"));
			outObj = new ObjectOutputStream(zipOtherOut);
			outObj.writeObject(map);
			zipOtherOut.closeEntry();

		} catch (Exception e) {
			logger.info(oInstance.getsMAC() + ": " + e);
		} finally {
			if (outObj != null) {
				try {
					outObj.close();
				} catch (Exception e) {
					logger.info(oInstance.getsMAC() + ": " + e);
				}

			}
		}

		logger.info("Compressed  data size = "
				+ baos_other.toByteArray().length);

		MultipartEntity parts = new MultipartEntity();
		ByteArrayBody bytearray = new ByteArrayBody(baos_other.toByteArray(),
				"other");
		parts.addPart("other", bytearray);
		CloudHttpResponse response = m_Secure.executePost(
				CommunicatorConstant.dataMigrationService, parts,
				oInstance.getsReplicaServerIP());
		if (response != null) {
			logger.info(oInstance.getsMAC() + ": status {" + response.getResponse()
					+ "}");
		}
		oManager.incTotalsPPAMigrationCount();
	}

	// private void startWalLoadSimulator() {
	// logger.info(m_sMAC + ": START WAL sync event");
	//
	// Long lastWalLogDataId = null;
	//
	// try {
	// lastWalLogDataId = getLastWalSyncId(m_sMAC, m_sReplicaServerIP);
	// logger.info(m_sMAC + ": received lastWal " + lastWalLogDataId);
	// if (lastWalLogDataId != -100) {
	//
	// ByteArrayOutputStream baos_other = new ByteArrayOutputStream();
	// ZipOutputStream zipOtherOut = new ZipOutputStream(baos_other);
	//
	// Map<String, Object> map = new HashMap<String, Object>();
	//
	// try {
	//
	// MultipartEntity parts = new MultipartEntity();
	// // Part[] parts = new Part[1];
	//
	// // walDao.fillWALChangesAndWalId(map,lastWalLogDataId);
	//
	// map.put("macId", m_sMAC);
	// map.put("version", m_sVersion);
	//
	// zipOtherOut.putNextEntry(new ZipEntry("sql_statements"));
	// ObjectOutputStream outObj = new ObjectOutputStream(
	// zipOtherOut);
	//
	// try {
	// outObj.writeObject(map);
	// zipOtherOut.closeEntry();
	// } catch (Exception e) {
	// logger.error(e.toString(), e);
	// } finally {
	// if (outObj != null) {
	// try {
	// outObj.close();
	// } catch (Exception e) {
	// logger.error(e.toString(), e);
	// }
	// }
	// }
	// logger.info(m_sMAC + " compressed  data size = "
	// + baos_other.toByteArray().length);
	// CloudHttpResponse synchResponse = m_CC.executePost(
	// CommunicatorConstant.sendSppaDataService, parts,
	// m_sReplicaServerIP);
	// logger.info("status " + synchResponse.getStatus());
	// if (synchResponse.getStatus() == 200) {
	//
	// logger.info("sync successful.");
	// } else {
	// logger.info("SYNC FAILED!!!!!");
	// }
	// } catch (Exception e) {
	// logger.error(e.toString(), e);
	// } finally {
	//
	// if (zipOtherOut != null) {
	// try {
	// zipOtherOut.close();
	// } catch (Exception e) {
	// logger.error(e.toString(), e);
	// }
	// }
	// if (baos_other != null) {
	// try {
	// baos_other.close();
	// } catch (Exception e) {
	// logger.error(e.toString(), e);
	// }
	// }
	// }
	// } else {
	// logger.info("Could not connect to cloud server.");
	// }
	// } catch (Exception e) {
	// logger.error(e.toString(), e);
	// }
	// }
	//
	// private long getLastWalSyncId(String m_sMAC, String sReplicaServer) {
	// long lastWalSyncId = -100;
	//
	// CloudHttpResponse response = m_CC.executeGet(
	// CommunicatorConstant.getLastWalSyncService + m_sMAC,
	// sReplicaServer);
	//
	// if (response != null) {
	// lastWalSyncId = Long.parseLong(response.getResponse());
	// logger.info("Last Wal Sync ID is " + lastWalSyncId);
	// }
	// return lastWalSyncId;
	//
	// }
}
