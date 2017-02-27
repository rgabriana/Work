package com.communicator.manager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.communication.template.SecureCloudConnectionTemplate;
import com.communication.utils.CloudHttpResponse;
import com.communication.utils.CommonStateUtils;
import com.communicator.dao.CloudConfigDao;
import com.communicator.dao.EnergySyncUpDao;
import com.communicator.dao.SyncTasksDao;
import com.communicator.dao.SystemConfigDao;
import com.communicator.dao.WALDao;
import com.communicator.type.SyncTaskStatus;
import com.communicator.util.CommunicatorConstant;
import com.communicator.util.SyncTasks;

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
	CloudConfigDao cloudConfigDao;

	@Resource
	SecureCloudConnectionTemplate securecloudConnectionTemplate;

	@Resource
	EnergySyncUpDao energySyncUpDao;
	@Resource
	CloudManager cloudManager;
	@Resource
	ReplicaServerInfoManager replicaServerInfoManager;
	@Resource
	SyncTasksDao syncTasksDao;

	public long getLastWalSyncId() {
		long lastWalSyncId = -101;

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

	@SuppressWarnings("unchecked")
	public void sendDataSPPA() {

		logger.info("Starting new data sync event at " + new Date());

		Long lastWalLogDataId = null;

		try {
			lastWalLogDataId = getLastWalSyncId();
			if (lastWalLogDataId.compareTo(-100L) != 0 && lastWalLogDataId.compareTo(-101L) != 0) {
				
				syncTasksDao.updateSyncTaskStatus(lastWalLogDataId, SyncTaskStatus.Success);
				
				ByteArrayOutputStream baos_other = new ByteArrayOutputStream();
				FileInputStream fis = null;
				SyncTasks syncTasks  = syncTasksDao.getSyncTask(lastWalLogDataId);
				
				if(syncTasks != null) {
					try {
						if(syncTasks.getStatus().equals(SyncTaskStatus.InProgress.getName())) {
							syncTasksDao.increaseFailedAttempts(syncTasks.getLastWalId());
						}
						else {
							syncTasksDao.updateSyncTaskStatus(syncTasks.getLastWalId(), SyncTaskStatus.InProgress);
						}

						MultipartEntity parts = new MultipartEntity();
						
						fis = new FileInputStream(syncTasks.getFilename());
						byte[] buf = new byte[1024];
				        try {
				            for (int readNum; (readNum = fis.read(buf)) != -1;) {
				            	baos_other.write(buf, 0, readNum); 
				            }
				        } catch (IOException e) {
				           logger.error(e.getMessage(), e);
				        }
						logger.info("Compressed  data size = " + baos_other.toByteArray().length);
	
						ByteArrayBody bytearray = new ByteArrayBody(
								baos_other.toByteArray(), "other");
						parts.addPart("other", bytearray);
	
						if (syncTasks.getAction().equals("UpgradeDump")) {
	
							CloudHttpResponse response = securecloudConnectionTemplate
									.executePost(CommunicatorConstant.upgradeStatusService,parts,serverInfoManager.getReplicaServerIP());
							String upgradeStatus = new String(response.getResponse());
	
							logger.info("Upgrade Status = " + upgradeStatus);
	
							if ("N".equals(upgradeStatus)) {
								ZipInputStream zipOtherIn = null;
								ObjectInputStream objectIn = null;
								InputStream is = null;
								Map<String, Object> map = null;
								
								byte[] dumpcontents = null;
								try {
									is = new FileInputStream(syncTasks.getFilename());
									zipOtherIn = new ZipInputStream(is);
									zipOtherIn.getNextEntry();
									objectIn = new ObjectInputStream(zipOtherIn);
									map = (HashMap<String, Object>) objectIn.readObject();
									File dumpFile = new File(map.get("upgradeFilePath")
											.toString());
									map.put("filename", dumpFile.getName());
									dumpcontents = FileUtils.readFileToByteArray(dumpFile);
								} catch (IOException e) {
									logger.error(e.getMessage(), e);
								}
								finally {
									if(is != null){
										try{
											is.close();
										}catch (IOException e) {					
											logger.error(e.getMessage(), e);
										}
									}
									if(zipOtherIn != null){
										try {
											zipOtherIn.close();
										} catch (IOException e) {					
											logger.error(e.getMessage(), e);
										}
									}
									if(objectIn != null){
										try{
											objectIn.close();
										}catch (IOException e) {					
											logger.error(e.getMessage(), e);
										}
									}
								}
								map.put("dump", dumpcontents);
	
								ByteArrayOutputStream baos_upgrade = new ByteArrayOutputStream();
								ZipOutputStream zipUpgradeOut = new ZipOutputStream(
										baos_upgrade);
	
								ObjectOutputStream outUpgradeObject = null;
								try {
	
									zipUpgradeOut.putNextEntry(new ZipEntry("other"));
									outUpgradeObject = new ObjectOutputStream(zipUpgradeOut);
									outUpgradeObject.writeObject(map);
									zipUpgradeOut.closeEntry();
	
									logger.info("Compressed upgrade data size = " + baos_upgrade.toByteArray().length);
	
									ByteArrayBody upgradebytearray = new ByteArrayBody(baos_upgrade.toByteArray(), "other");
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
								cloudConfigDao.updateCloudConfig(CommunicatorConstant.sysConfigSuccessfulSyncTime, ((Long)(new Date().getTime())).toString());
							} else {
								logger.info("WAL LOG SYNC FAILED!!!!!");
							}
						}
	
					} catch (Exception e) {
						logger.error(e.toString(), e);
					} finally {
						if (fis != null) {
							try {
								fis.close();
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
				}
				else {
					logger.info("No sync task in queue.");
				}
			} else {
				if (lastWalLogDataId.compareTo(-100L) == 0) {
					logger.info("another sync is already in progress on replica");
				}
				else {
					logger.info("Could not connect to cloud server.");
				}
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
						System.getenv().get("ENLIGHTED_HOME")+"/clouddata/sppa_migration.sql");
				if (temp1.exists()) {
					temp1.delete();
				}
				temp1 = new File(
						System.getenv().get("ENLIGHTED_HOME")+"/clouddata/sppa_migration_energy.sql");
				if (temp1.exists()) {
					temp1.delete();
				}
				// create dumps
				Runtime rt = Runtime.getRuntime();
				Process proc;
				proc = rt
						.exec(new String[] { "/bin/bash",
								System.getenv().get("OPT_ENLIGHTED")+"/communicator/generateMigrationDump.sh" });
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
				systemConfigDao.setMaxIdValues();
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
					System.getenv().get("ENLIGHTED_HOME")+"/clouddata/sppa_migration.sql");
			File energyFile = new File(
					System.getenv().get("ENLIGHTED_HOME")+"/clouddata/sppa_migration_energy.sql");
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
					cloudConfigDao.updateCloudConfig(CommunicatorConstant.sysConfigSuccessfulSyncTime, ((Long)(new Date().getTime())).toString());
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
	
	/*
	 * Method to delete entries related to inventory tables from files and wal_logs table 
	 * */
	@SuppressWarnings("unchecked")
	public void deleteWalLogs(){
		long lastWalSyncId = getLastWalSyncId();
		//deletes from wal_logs table
		walDao.deleteInventory(lastWalSyncId);
		logger.info("Deleting inventory records from wal_logs Successfull...");
		List<String> queuedList = new ArrayList<String>();
		queuedList = syncTasksDao.getQueuedEntries();

		for (String fileName: queuedList){
			try {
				ZipInputStream zipOtherIn = null;
				ObjectInputStream objectIn = null;
				InputStream is = null;
				Map<String, Object> fileValues_map = null;

				try{
					is = new FileInputStream(fileName);
					zipOtherIn = new ZipInputStream(is);
					zipOtherIn.getNextEntry();
					objectIn = new ObjectInputStream(zipOtherIn);

					fileValues_map = (HashMap<String, Object>) objectIn.readObject();
					List<String> statements = new ArrayList<String>();
					statements = (List<String>) fileValues_map.get("data");
					Iterator<String> sqlIterator = statements.iterator();
					
					while (sqlIterator.hasNext()) {
						String sql = sqlIterator.next();
						if(!(sql.contains("energy_consumption") ||sql.contains("energy_consumption_hourly") || sql.contains("energy_consumption_daily") || sql.contains("em_motion_bits")))
						{
							sqlIterator.remove();
						}
					}
				}catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				finally{
					if(is != null){
						try{
							is.close();
							//taking backup of existing file 
							File ExistingFile = new File(fileName);
							String BackupFile = fileName.replaceAll(".data","_old.data");
							ExistingFile.renameTo(new File(BackupFile));
						}catch (IOException e) {					
							logger.error(e.getMessage(), e);
						}
					}
					if(zipOtherIn != null){
						try {
							zipOtherIn.close();
						} catch (IOException e) {					
							logger.error(e.getMessage(), e);
						}
					}
					if(objectIn != null){
						try{
							objectIn.close();
						}catch (IOException e) {					
							logger.error(e.getMessage(), e);
						}
					}
				}

				OutputStream fos = null;
				ByteArrayOutputStream baos_other = null;
				ZipOutputStream zipOtherOut = null;

				try {
					fos = new FileOutputStream(fileName) ;
					baos_other = new ByteArrayOutputStream();
					zipOtherOut = new ZipOutputStream(baos_other);
					
					zipOtherOut.putNextEntry(new ZipEntry("sql_statements"));
					ObjectOutputStream outObj = new ObjectOutputStream(zipOtherOut);
					try {
						outObj.writeObject(fileValues_map);
						zipOtherOut.closeEntry();
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					} finally {
						if (outObj != null) {
							try {
								outObj.close();
							} catch (Exception e) {
								System.out.println("Exception: "+ e);
							}
						}
					}
					baos_other.writeTo(fos);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}finally{
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
					if (fos != null) {
						try {
							fos.close();
							logger.info("Deleting inventory records from "+fileName+" Successfull...");
						} catch (Exception e) {
							logger.error(e.toString(), e);
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public boolean doRestrictedReMigration() {
		Map<String, Object> map = new HashMap<String, Object>();
		// Stop tomcat server before running migration
		logger.info("Shutting down tomcat server for re-migration purpose");
		serverInfoManager.startStopTomcatServer("stop");
		if (doBeforeMigrationcleanUp()
				&& !serverInfoManager.istomcatServerRunning()) {
			//Deletes inventory entries from wal_logs table and files
			deleteWalLogs();
			Boolean status = false;
			ByteArrayOutputStream baos_other = new ByteArrayOutputStream();
			ZipOutputStream zipOtherOut = new ZipOutputStream(baos_other);

			try {
				// clean up if files already exist
				File temp1 = new File(
						System.getenv().get("ENLIGHTED_HOME")+"/clouddata/sppa_migration.sql");
				if (temp1.exists()) {
					temp1.delete();
				}
				temp1 = new File(
						System.getenv().get("ENLIGHTED_HOME")+"/clouddata/sppa_migration_energy.sql");
				if (temp1.exists()) {
					temp1.delete();
				}
				// create dumps
				Runtime rt = Runtime.getRuntime();
				Process proc;
				proc = rt
						.exec(new String[] { "/bin/bash",
								System.getenv().get("OPT_ENLIGHTED")+"/communicator/generateMigrationDump.sh" });
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
				
				systemConfigDao.setMaxIdValues();
			} catch (InterruptedException e) {

				logger.error(e.toString(), e);

			} catch (IOException e) {

				logger.error(e.toString(), e);
			} catch (Exception e) {

				logger.error(e.toString(), e);
			} finally {
				logger.info("Re-Migration related task on EM done.");
				logger.info("Starting tomcat server again");
				serverInfoManager.startStopTomcatServer("start");
				if (!serverInfoManager.istomcatServerRunning()) {
					logger.info("Starting tomcat server failed for some reason. Retrying again. ");
					serverInfoManager.startStopTomcatServer("start");
					logger.info("Status"
							+ serverInfoManager.istomcatServerRunning());
				}
			}
			logger.info("Sending Re-Migration dumps to cloud");
			// send data
			byte[] dumpcontents = null;
			byte[] upgradecontents = null;
			byte[] energyContents = null;
			File energyFile = new File(
					System.getenv().get("ENLIGHTED_HOME")+"/clouddata/sppa_migration_energy.sql");
			File dumpFile = new File(
					System.getenv().get("ENLIGHTED_HOME")+"/clouddata/sppa_migration.sql");
			File upgradeFile = new File(
					System.getenv().get("ENLIGHTED_HOME")+"/upgradeSQL.sql");
			try {
				dumpcontents = FileUtils.readFileToByteArray(dumpFile);
				upgradecontents = FileUtils.readFileToByteArray(upgradeFile);
				energyContents = FileUtils.readFileToByteArray(energyFile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			map.put("macId", serverInfoManager.getMacAddress());
			map.put("version", serverInfoManager.getAppVersion());
			map.put("dump", dumpcontents);
			map.put("upgrade", upgradecontents);
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
						.executePost(CommunicatorConstant.dataRestrictedReMigrationService,
								parts, serverInfoManager.getReplicaServerIP());

				if (response.getStatus() == 200) {
					logger.info("Migration successful.");
					cloudConfigDao.updateCloudConfig(CommunicatorConstant.sysConfigSuccessfulSyncTime, ((Long)(new Date().getTime())).toString());
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
		return systemConfigDao.doBeforeMigrationcleanUp();
	}

}
