package com.communicator.job;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.communication.types.DatabaseState;
import com.communication.utils.Util;
import com.communicator.CommunicatorEntryPoint;
import com.communicator.dao.SyncTasksDao;
import com.communicator.dao.WALDao;
import com.communicator.manager.CloudManager;
import com.communicator.manager.ReplicaServerInfoManager;
import com.communicator.manager.ServerInfoManager;
import com.communicator.type.SyncTaskStatus;
import com.communicator.util.CommunicatorConstant;
import com.communicator.util.SpringContext;

public class DataPrepJob implements Job {
	
	WALDao walDao;
	SyncTasksDao syncTasksDao;
	ServerInfoManager serverInfoManager;
	CloudManager cloudManager;
	ReplicaServerInfoManager replicaServerInfoManager ;
	
	static final Logger logger = Logger.getLogger(DataPrepJob.class.getName());
	static SimpleDateFormat sdf =  new SimpleDateFormat("yyyyMMddHHmmss");
	
	public DataPrepJob() {
		walDao = (WALDao)SpringContext.getBean("walDao");
		syncTasksDao = (SyncTasksDao) SpringContext.getBean("syncTasksDao");
		serverInfoManager = (ServerInfoManager)SpringContext.getBean("serverInfoManager");
		cloudManager = (CloudManager)SpringContext.getBean("cloudManager");
		replicaServerInfoManager = (ReplicaServerInfoManager) SpringContext.getBean("replicaServerInfoManager");
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String status = serverInfoManager.getCloudSyncType();
		logger.info("cloud sync type = " + status + " and migration state = " + replicaServerInfoManager.getCurrentMigrationState());
		if ("2".equals(status) && !(replicaServerInfoManager.getCurrentMigrationState().equals(DatabaseState.NOT_MIGRATED)
									|| replicaServerInfoManager.getCurrentMigrationState().equals(DatabaseState.MIGRATION_READY)
									|| CommunicatorEntryPoint.remigrationRequired)) {
			
			Long maxWal = syncTasksDao.getMaxWalLogId();
			if(maxWal.compareTo(-99L) == 0) {
				if(replicaServerInfoManager.getCurrentMigrationState().equals(DatabaseState.REPLICA_UNREACHABLE)) {
					return;
				}
				logger.info("no exisiting sync tasks. fetching max wal from cloud;");
				Long lastWalSuccessId  = cloudManager.getLastWalSyncId();
				if(lastWalSuccessId.compareTo(-100L) == 0) {
					return;
				}
				else {
					walDao.deleteWalLogs(lastWalSuccessId);
					maxWal = lastWalSuccessId;
				}
			}
			logger.info("deleting wal logs before " + maxWal);
			walDao.deleteWalLogs(maxWal);
			
			if(syncTasksDao.isDataPresentInQueue()) {
				Long count = walDao.countWalLogs();
				logger.info("another unprocessed sync task already present with wal counts = " + count);
				if(count.compareTo(50000L) < 0) {
					return;
				}
			}
			
			Map<String, Object> map = new HashMap<String, Object>();
			walDao.fillWALChangesAndWalId(map, maxWal);
			
			if(!map.containsKey("upgradeWalId") && maxWal.compareTo((Long)map.get("maxWalLogDataId")) == 0 ) {
				logger.warn("Nothing new in wal logs. returning from sync task.");
				return;
			}
			map.put("macId", serverInfoManager.getMacAddress());
			map.put("version", serverInfoManager.getAppVersion());
			
			OutputStream fos = null;
			ByteArrayOutputStream baos_other = new ByteArrayOutputStream();
			ZipOutputStream zipOtherOut = new ZipOutputStream(baos_other);
			try {
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
				
				String action = "DataDump";
				Long newMaxWalId = (Long)map.get("maxWalLogDataId");
				if(map.get("nextAction") != null && "UPGRADE".equals(map.get("nextAction").toString())) {
					action = "UpgradeDump";
					newMaxWalId = (Long)map.get("upgradeWalId");
				}
				
				String syncDataFile = CommunicatorConstant.syncDataDir + newMaxWalId + "_" + sdf.format(new Date()) + ".data";
				fos = new FileOutputStream(syncDataFile);
				baos_other.writeTo(fos);
				
				
				String checksum = Util.checkSumApacheCommons(syncDataFile);
				logger.info("creating sync task with action = " + action + ", syncDataFile = " + syncDataFile + ", checksum = " + checksum + ", maxWal = " + newMaxWalId );
				syncTasksDao.createSyncTask(action, syncDataFile, SyncTaskStatus.Queued, checksum , newMaxWalId);
			}
			catch (Exception e) {
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
				if (fos != null) {
					try {
						fos.close();
					} catch (Exception e) {
						logger.error(e.toString(), e);
					}
				}
			}
		}
	}

}
