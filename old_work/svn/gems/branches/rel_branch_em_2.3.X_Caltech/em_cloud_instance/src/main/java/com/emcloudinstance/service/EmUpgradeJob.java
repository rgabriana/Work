package com.emcloudinstance.service;

import java.util.Date;

import javax.annotation.Resource;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.emcloudinstance.util.DatabaseUtil;
import com.emcloudinstance.util.SpringContext;

public class EmUpgradeJob  implements Job {
		
	private String upgradeFile;
	private String dbName;
	private String version;
	private String upgradeWalId;
	
	
	DatabaseUtil databaseUtil;
	
	public EmUpgradeJob(){
		databaseUtil = (DatabaseUtil)SpringContext.getBean("databaseUtil");
	}
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			
			CloudConfigManager cloudConfigManager = (CloudConfigManager) SpringContext
                    .getBean("cloudConfigManager");
			
			System.out.println("New job at " + new Date() + " with upgradeFile = " + upgradeFile + 
					"  and dbName = " + dbName + " and version = " + version + " and upgradeWalId = " + upgradeWalId);
			cloudConfigManager.addOrUpdateConfig(dbName, "upgradeStatus", "I");
			
			Process pr = null;
			
			String[] cmdArr = { "/bin/bash", "/var/lib/tomcat6/webapps/em_cloud_instance/adminscripts/execUpgrade.sh", dbName, upgradeFile, databaseUtil.port};
			pr = Runtime.getRuntime().exec(cmdArr);
			int result = pr.waitFor();
			System.out.println(result);
			if (result == 0) {
				cloudConfigManager.addOrUpdateConfig(dbName, "lastWalSyncId", upgradeWalId);
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
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

}
