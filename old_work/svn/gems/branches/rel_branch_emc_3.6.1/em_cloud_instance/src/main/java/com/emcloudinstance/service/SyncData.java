package com.emcloudinstance.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.communication.types.DatabaseState;
import com.emcloudinstance.dao.JdbcConnectionTemplate;
import com.emcloudinstance.util.CommonUtils;
import com.emcloudinstance.util.DatabaseUtil;

@Service("syncData")
public class SyncData {
	
	static final Logger logger = Logger.getLogger(SyncData.class.getName());
	
	final public static String syncDataDir = "/var/lib/tomcat6/Enlighted/syncdata/";
	
	final File folder = new File (syncDataDir);
	
	@Resource
	DatabaseUtil databaseUtil;
	@Resource
	CloudConfigManager cloudConfigManager;
	@Resource
	DatabaseManager databaseManager;
	@Resource
	CommonUtils commonUtils;
	@Resource
	JdbcConnectionTemplate jdbcConnectionTemplate;
	
	private ConcurrentHashMap<String, SyncDataDetails> dataCache = new ConcurrentHashMap<String, SyncDataDetails>();
	
	private ExecutorService executor = Executors.newFixedThreadPool(CommonUtils.getNoOfThreads());

	public ConcurrentHashMap<String, SyncDataDetails> getDataCache() {
		return dataCache;
	}

	public void setDataCache(ConcurrentHashMap<String, SyncDataDetails> dataCache) {
		this.dataCache = dataCache;
	}
	
	public void queueSyncData(String mac, InputStream is) {
		logger.info("Adding sync data to the queue for mac " + mac);
		if(commonUtils.isRestrictedMigrationReady(mac)){
			logger.info("Skipping sync data as Part RMA set for mac " + mac);
			return;
		}
		String mac1 = mac.trim().replaceAll(":", "");
		Pattern pattern = Pattern.compile(mac1 + "_.*");
		final File[] files = folder.listFiles();
		boolean isData = false;
		if (files != null) {
			for (final File file : files) {
				if (pattern.matcher(file.getName()).matches()) {
					logger.info("Deleting file " + file.getName() + " = " + file.delete());
				}
			}
		}
		String filename = mac1 + "_" + (new Date()).getTime();
		
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(syncDataDir + filename);
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = is.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
				isData = true;
			}	
			logger.info("Written sync data from em " + mac + " to location " + filename );
		}
		catch (Exception e) {
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
			if(outputStream != null){
				try{
					outputStream.close();
				}catch (IOException e) {					
					logger.error(e.getMessage(), e);
				}
			}
		}
		if(isData) {
			SyncDataDetails syncDataDetails = new SyncDataDetails(mac1, filename);
			dataCache.put(mac1, syncDataDetails);
			executor.execute(syncDataDetails);
		}
	}
	/**
	 * DeQueue SynData added for not proceding if user selects Restricted Migration  
	 * @param mac
	 */
	public void deQueueSyncData(String mac) {
		String mac1 = mac.trim().replaceAll(":", "");
		SyncDataDetails details = getDataCache().get(mac1);
		if(details!=null){
			details.doNotUpdateStatus = true;
			logger.info("Job found to deque for mac = " + mac1 +" doNotUpdateStatus: "+details.doNotUpdateStatus);
		}else{
			logger.info("No Job found to deque for mac = " + mac1);
		}
	}
	
	public class SyncDataDetails implements Runnable {
		
		String macId;
		boolean doNotUpdateStatus = false;
		public String getMacId() {
			return macId;
		}

		public void setMacId(String macId) {
			this.macId = macId;
		}

		String filename;
		
		public SyncDataDetails(String macId, String filename) {
			logger.info("New sync detail: mac " + macId + ", filename " + filename);
			this.macId = macId;
			this.filename = filename;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			logger.info("check doNotUpdateStatus: "+doNotUpdateStatus +" for mac: "+macId);
			if(doNotUpdateStatus){
				logger.info("doNotUpdateStatus is true so returning" +" for mac: "+macId);
				return;
			}	

			String walSynchId = null ;
			ZipInputStream zipOtherIn = null;
			ObjectInputStream objectIn = null;
			Date lastSyncSuccessTime = null;
			DatabaseState emState = null;
			InputStream is = null;
			Map<String, Object> map = null;
			String mac = null;
			try {
				is = new FileInputStream(syncDataDir + filename);
				zipOtherIn = new ZipInputStream(is);
				zipOtherIn.getNextEntry();
				objectIn = new ObjectInputStream(zipOtherIn);
				map = (HashMap<String, Object>) objectIn.readObject();
				mac = (String)map.get("macId");
				if(mac == null || !mac.trim().replaceAll(":", "").equals(macId)) {
					logger.error("Some issue with data for em " + macId + " or " + mac + " and file " + filename);
					return;
				}
				logger.info("Update recieved from a local EM server with [macId,Version,Max WAL Id, DbName}: ["
						+ map.get("macId")
						+ ","
						+ map.get("version")
						+ ","
						+ map.get("maxWalLogDataId") + "," 
						+ databaseUtil.getDbNameByMac(mac) +
						"]");
				
				DataSourceTransactionManager transactionManager = jdbcConnectionTemplate.createAndGetTransactionManager(mac);
				TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
				final JdbcTemplate jdbcTemplate = new JdbcTemplate(transactionManager.getDataSource());

				boolean success = databaseManager.walLogsUpdate(
						(String) map.get("macId"),
						((Long) map.get("maxWalLogDataId")).toString(),
						(List<String>) map.get("data"), transactionTemplate, jdbcTemplate);
				logger.info(mac + ":WAL log update successful:" +  success);
				
				// notifying the last sync wal id to cloud
				 walSynchId =  cloudConfigManager.getLastWALSynched(mac, jdbcTemplate);
				
				 walSynchId = walSynchId +"@" + commonUtils.getTime();
				 lastSyncSuccessTime = new Date();
				 emState = DatabaseState.SYNC_READY;
			} catch (Exception e) {
				if(map != null && map.get("data") != null) {
					String fn = syncDataDir + "data_" + filename;
					logger.info("Stored data for em " + mac + " from " + filename  + " to file " + fn);
					PrintWriter out = null;
					try {
						out = new PrintWriter(fn);
						for(String s: (List<String>) map.get("data")) {
							out.println(s);
						}
						out.close();
					} catch (Exception e1) {
						logger.error(e1.getMessage(), e1);
					}
				}
				logger.error(mac + "::"  +  databaseUtil.getDbNameByMac(mac) + "::" + filename + "::WAL log update failed::", e);
				// notify cloud that exception has happened.
				walSynchId = com.communication.utils.Util.checkSumApacheCommons(syncDataDir + filename);
				emState = DatabaseState.SYNC_FAILED;
			}finally{
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
				logger.info("Removing cache for mac " + mac);
				logger.info("doNotUpdateStatus for RMA: "+doNotUpdateStatus +" for mac: "+macId);
				if(!doNotUpdateStatus){
					commonUtils.setReplicaSyncFlagLogOnCloud(mac,emState.getName(), walSynchId, lastSyncSuccessTime);
				}	
				getDataCache().remove(macId);
			}
		}
	}
	

}