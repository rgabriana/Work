package com.emcloudinstance.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.communication.types.DatabaseState;
import com.emcloudinstance.util.CommonUtils;
import com.emcloudinstance.util.DatabaseUtil;
import com.emcloudinstance.util.SpringContext;

public class EmUpgradeJob  implements Job {
	
	Logger logger = Logger.getLogger(EmUpgradeJob.class.getName());
		
	private String upgradeFile;
	private String dbName;
	private String version;
	private String upgradeWalId;
	private String mac;

	DatabaseUtil databaseUtil;
	
	public EmUpgradeJob(){
		databaseUtil = (DatabaseUtil)SpringContext.getBean("databaseUtil");
	}
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		String dbport = databaseUtil.port;
		String connection = "jdbc:postgresql://localhost:" + dbport + "/" + dbName +  "?characterEncoding=utf-8";
		Statement addTriggers = null ;
		Statement dropTriggers = null ;
		Connection conn1 = null ;
		Connection conn2 = null ;
		try {
			
			CloudConfigManager cloudConfigManager = (CloudConfigManager) SpringContext
                    .getBean("cloudConfigManager");
			
			CommonUtils commonUtils = (CommonUtils) SpringContext.getBean("commonUtils");
			
			logger.info("New job at " + new Date() + " with upgradeFile = " + upgradeFile + 
					"  and dbName = " + dbName + " and version = " + version + " and upgradeWalId = " + upgradeWalId);
			cloudConfigManager.addOrUpdateConfig(dbName, "upgradeStatus", "I");
			
			try {
				conn1 = DriverManager.getConnection(connection, "postgres", "postgres"); 
				//Add all em triggers not related to wal logs
				addTriggers = conn1.createStatement() ;
				addTriggers.execute("select  add_triggers()" );
			}
			catch (Exception e) {
				logger.error(e.getMessage(),e );
			}
			finally {
				try {
					if (addTriggers != null) {
						addTriggers.close();
					}
					if (conn1 != null) {
						conn1.close();
					}
				} catch (SQLException se) {
				}
			}
			
			Process pr = null;
			
			String[] cmdArr = { "/bin/bash", "/var/lib/tomcat6/webapps/em_cloud_instance/adminscripts/execUpgrade.sh", dbName, upgradeFile, databaseUtil.port};
			pr = Runtime.getRuntime().exec(cmdArr);
			int result = pr.waitFor();
			logger.info(result);
			if (result == 0) {
				cloudConfigManager.addOrUpdateConfig(dbName, "lastWalSyncId", upgradeWalId);
				commonUtils.setReplicaSyncFlagLogOnCloud(mac,DatabaseState.SYNC_READY.getName(), upgradeWalId + "@" + commonUtils.getTime(), new Date());
				try {
					conn2 = DriverManager.getConnection(connection, "postgres", "postgres"); 
					//Drop all em triggers not related to wal logs
					dropTriggers = conn2.createStatement() ;
					dropTriggers.execute("select  drop_triggers()" );
				}
				catch (Exception e) {
					logger.error(e.getMessage(),e );
				}
				finally {
					try {
						if (dropTriggers != null) {
							dropTriggers.close();
						}
						if (conn2 != null) {
							conn2.close();
						}
					} catch (SQLException se) {
					}
				}
			}
			
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * @return the upgradeFile
	 */
	public String getUpgradeFile() {
		return upgradeFile;
	}

	/**
	 * @param upgradeFile the upgradeFile to set
	 */
	public void setUpgradeFile(String upgradeFile) {
		this.upgradeFile = upgradeFile;
	}

	/**
	 * @return the dbName
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * @param dbName the dbName to set
	 */
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the upgradeWalId
	 */
	public String getUpgradeWalId() {
		return upgradeWalId;
	}

	/**
	 * @param upgradeWalId the upgradeWalId to set
	 */
	public void setUpgradeWalId(String upgradeWalId) {
		this.upgradeWalId = upgradeWalId;
	}
	
	
	
	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

}
