package com.communicator.manager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.communication.template.CloudConnectionTemplate;
import com.communication.template.SecureCloudConnectionTemplate;
import com.communication.utils.CloudHttpResponse;
import com.communication.utils.CommonStateUtils;
import com.communicator.dao.EnergySyncUpDao;
import com.communicator.dao.SystemConfigDao;
import com.communicator.dao.WALDao;
import com.communicator.util.CommunicatorConstant;

@Service("cloudManager")
public class CloudManager {

	static final Logger logger = Logger.getLogger(CloudManager.class.getName());
	@Resource
	WALDao walDao;

	@Resource
	ServerInfoManager serverInfoManager;
	@Resource
	SystemConfigDao systemConfigDao;

	@Resource
	CloudConnectionTemplate cloudConnectionTemplate;

	@Resource
	SecureCloudConnectionTemplate securecloudConnectionTemplate;

	@Resource
	EnergySyncUpDao energySyncUpDao;
	@Resource
	CloudManager cloudManager;
	@Resource
	ReplicaServerInfoManager replicaServerInfoManager;

	public long getLastWalSyncId() {
		long lastWalSyncId = -100;

		CloudHttpResponse response = securecloudConnectionTemplate.executeGet(
				CommunicatorConstant.getLastWalSyncService
						+ serverInfoManager.getMacAddress(),
				serverInfoManager.getReplicaServerIP());

		if (response != null) {
			lastWalSyncId = Long.parseLong(response.getResponse());
			logger.info("Last Wal Sync ID is " + lastWalSyncId);
		}
		return lastWalSyncId;

	}

	public void sendDataSPPA() {

		logger.info("Starting new sync event at " + new Date());

		Long lastWalLogDataId = null;

		try {
			lastWalLogDataId = getLastWalSyncId();
			if (lastWalLogDataId != -100) {

				ByteArrayOutputStream baos_other = new ByteArrayOutputStream();
				ZipOutputStream zipOtherOut = new ZipOutputStream(baos_other);

				Map<String, Object> map = new HashMap<String, Object>();

				try {

					MultipartEntity parts = new MultipartEntity();

					walDao.fillWALChangesAndWalId(map, lastWalLogDataId);

					map.put("macId", serverInfoManager.getMacAddress());
					map.put("version", serverInfoManager.getAppVersion());

					zipOtherOut.putNextEntry(new ZipEntry("sql_statements"));
					ObjectOutputStream outObj = new ObjectOutputStream(
							zipOtherOut);

					try {
						outObj.writeObject(map);
						zipOtherOut.closeEntry();
					} catch (Exception e) {
						logger.error(e.toString(), e);
					} finally {
						if (outObj != null) {
							try {
								outObj.close();
							} catch (Exception e) {
								logger.error(e.toString(), e);
							}
						}
					}
					logger.info("Compressed  data size = "
							+ baos_other.toByteArray().length);

					ByteArrayBody bytearray = new ByteArrayBody(
							baos_other.toByteArray(), "other");
					parts.addPart("other", bytearray);

					if (map.get("nextAction") != null
							&& "UPGRADE".equals(map.get("nextAction")
									.toString())) {

						CloudHttpResponse response = securecloudConnectionTemplate
								.executePost(
										CommunicatorConstant.upgradeStatusService,
										parts,
										serverInfoManager.getReplicaServerIP());
						String upgradeStatus = new String(
								response.getResponse());

						System.out.println("Upgrade Status = " + upgradeStatus);

						if ("N".equals(upgradeStatus)) {
							byte[] dumpcontents = null;
							File dumpFile = new File(map.get("upgradeFilePath")
									.toString());
							map.put("filename", dumpFile.getName());
							try {
								dumpcontents = FileUtils
										.readFileToByteArray(dumpFile);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							map.put("dump", dumpcontents);

							ByteArrayOutputStream baos_upgrade = new ByteArrayOutputStream();
							ZipOutputStream zipUpgradeOut = new ZipOutputStream(
									baos_upgrade);

							ObjectOutputStream outUpgradeObject = null;
							try {

								zipUpgradeOut
										.putNextEntry(new ZipEntry("other"));
								outUpgradeObject = new ObjectOutputStream(
										zipUpgradeOut);
								outUpgradeObject.writeObject(map);
								zipUpgradeOut.closeEntry();

								logger.info("Compressed  data size = "
										+ baos_upgrade.toByteArray().length);

								ByteArrayBody upgradebytearray = new ByteArrayBody(
										baos_upgrade.toByteArray(), "other");
								parts = new MultipartEntity();
								parts.addPart("other", upgradebytearray);

								securecloudConnectionTemplate.executePost(
										CommunicatorConstant.upgradeService,
										parts,
										serverInfoManager.getReplicaServerIP());

							} catch (Exception e) {
								logger.error(e.toString(), e);
							} finally {
								if (outUpgradeObject != null) {
									try {
										outUpgradeObject.close();
									} catch (Exception e) {
										logger.error(e.toString(), e);
									}

								}
								if (zipUpgradeOut != null) {
									try {
										zipUpgradeOut.close();
									} catch (Exception e) {
										logger.error(e.toString(), e);
									}
								}
								if (baos_upgrade != null) {
									try {
										baos_upgrade.close();
									} catch (Exception e) {
										logger.error(e.toString(), e);
									}
								}
							}
						}
					} else {

						CloudHttpResponse synchResponse = securecloudConnectionTemplate
								.executePost(
										CommunicatorConstant.sendSppaDataService,
										parts,
										serverInfoManager.getReplicaServerIP());
						logger.info("status " + synchResponse.getStatus());
						if (synchResponse.getStatus() == 200) {

							logger.info("Wal log sync successful.");
						} else {
							logger.info("WAL LOG SYNC FAILED!!!!!");
						}
					}

				} catch (Exception e) {
					logger.error(e.toString(), e);
				} finally {

					if (zipOtherOut != null) {
						try {
							zipOtherOut.close();
						} catch (Exception e) {
							logger.error(e.toString(), e);
						}
					}
					if (baos_other != null) {
						try {
							baos_other.close();
						} catch (Exception e) {
							logger.error(e.toString(), e);
						}
					}
				}
			} else {
				logger.info("Could not connect to cloud server.");
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}

	}

	/**
	 * This function only migrate non energy data. Energy data is migrated using
	 * SyncUpEnergyData() function and is run in parallel until the task is
	 * finished.
	 * 
	 * returns true if everything is as expected.
	 */
	public boolean doDataMigration() {
		Map<String, Object> map = new HashMap<String, Object>();
		// Stop tomcat server before running migration
		logger.info("Shutting down tomcat server for migration purpose");
		serverInfoManager.startStopTomcatServer("stop");
		if (doBeforeMigrationcleanUp()
				&& !serverInfoManager.istomcatServerRunning()) {
			Boolean status = false;
			ByteArrayOutputStream baos_other = new ByteArrayOutputStream();
			ZipOutputStream zipOtherOut = new ZipOutputStream(baos_other);

			try {
				// clean up if files already exist
				File temp1 = new File(
						"/home/enlighted/clouddata/sppa_migration.sql");
				if (temp1.exists()) {
					temp1.delete();
				}
				temp1 = new File(
						"/home/enlighted/clouddata/sppa_migration_energy.sql");
				if (temp1.exists()) {
					temp1.delete();
				}
				// create dumps
				Runtime rt = Runtime.getRuntime();
				Process proc;
				proc = rt
						.exec(new String[] { "/bin/bash",
								"/opt/enLighted/communicator/generateMigrationDump.sh" });
				proc.waitFor();
				// Add triggers for wal log entry.
				systemConfigDao.addWalLogTriggers();

				/*
				 * Check for "is table_name table empty". If empty then there is
				 * no need to follow Other migration steps related to that table
				 * and we can send a map which will take these state management
				 * system to next state. This needs to be done before tomcat get
				 * started.
				 */
				fillMapWithIsEmptyTablesName(map);
			} catch (InterruptedException e) {

				logger.error(e.toString(), e);

			} catch (IOException e) {

				logger.error(e.toString(), e);
			} catch (Exception e) {

				logger.error(e.toString(), e);
			} finally {
				logger.info("Migration related task on EM done.");
				logger.info("Starting tomcat server again");
				serverInfoManager.startStopTomcatServer("start");
				if (!serverInfoManager.istomcatServerRunning()) {
					logger.info("Starting tomcat server failed for some reason. Retrying again. ");
					serverInfoManager.startStopTomcatServer("start");
					logger.info("Status"
							+ serverInfoManager.istomcatServerRunning());
				}
			}
			logger.info("Sending Migration dumps to cloud");
			// send data
			byte[] dumpcontents = null;
			byte[] energyContents = null;
			File dumpFile = new File(
					"/home/enlighted/clouddata/sppa_migration.sql");
			File energyFile = new File(
					"/home/enlighted/clouddata/sppa_migration_energy.sql");
			try {
				dumpcontents = FileUtils.readFileToByteArray(dumpFile);
				energyContents = FileUtils.readFileToByteArray(energyFile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			map.put("macId", serverInfoManager.getMacAddress());
			map.put("version", serverInfoManager.getAppVersion());
			map.put("dump", dumpcontents);
			map.put("energy", energyContents);
			ObjectOutputStream outObj = null;
			try {

				zipOtherOut.putNextEntry(new ZipEntry("emInfo"));
				outObj = new ObjectOutputStream(zipOtherOut);
				outObj.writeObject(map);
				zipOtherOut.closeEntry();

			} catch (Exception e) {
				systemConfigDao.removeWalLogTriggers();
				logger.error(e.toString(), e);
			} finally {

				if (outObj != null) {
					try {
						outObj.close();
					} catch (Exception e) {
						logger.error(e.toString(), e);
					}

				}
			}

			try {
				logger.info("Compressed  data size = "
						+ baos_other.toByteArray().length);

				MultipartEntity parts = new MultipartEntity();
				ByteArrayBody bytearray = new ByteArrayBody(
						baos_other.toByteArray(), "other");
				parts.addPart("other", bytearray);
				CloudHttpResponse response = securecloudConnectionTemplate
						.executePost(CommunicatorConstant.dataMigrationServiceV2,
								parts, serverInfoManager.getReplicaServerIP());

				if (response.getStatus() == 200) {
					logger.info("Migration successful.");
					status = true;
				} else {
					logger.info("Migration FAILED!!!!! Removing wal Triggers. Status Returned :-"
							+ response.getStatus());
					systemConfigDao.removeWalLogTriggers();
					status = false;
				}

			} catch (Exception e) {
				systemConfigDao.removeWalLogTriggers();
				logger.error(e.toString(), e);

			}
			return status;
		} else {
			logger.info("Migration FAILED!!!!! due to error while clean up or tomcat server was not down. Tomcat server status"
					+ serverInfoManager.istomcatServerRunning()
					+ "  Contact Admin");
			serverInfoManager.startStopTomcatServer("start");
			if (!serverInfoManager.istomcatServerRunning()) {
				logger.info("Starting tomcat server failed for some reason. Retrying again. ");
				serverInfoManager.startStopTomcatServer("start");
				logger.info("Status"
						+ serverInfoManager.istomcatServerRunning());
			}
			return false;
		}

	}

	private void fillMapWithIsEmptyTablesName(Map<String, Object> map) {
		ArrayList<String> tableName = CommonStateUtils.tableNameList ;
		Iterator<String> itr = tableName.iterator();
		try {
			while (itr.hasNext()) {
				String table = itr.next();
				map.put(table, energySyncUpDao.isTabelEmpty(table));
			}
		} catch (Exception e) {
			logger.error(
					"Some error accured while filling migration map with table name and are they empty or not status. Shifting back to normal Migration process. "
							+ e.getMessage(), e);
			// remove all entries related to empty table flag so that migration
			// can perform normally.
			while (itr.hasNext()) {
				String table = itr.next();
				if (map.containsKey(table)) {
					map.remove(table);
				}
			}
		}
	}

	private boolean doBeforeMigrationcleanUp() {
		// TODO Auto-generated method stub
		return systemConfigDao.doBeforeMigrationcleanUp();
	}

}
