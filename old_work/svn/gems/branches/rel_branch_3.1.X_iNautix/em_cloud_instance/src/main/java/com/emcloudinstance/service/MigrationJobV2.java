package com.emcloudinstance.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.communication.types.DatabaseState;
import com.communication.utils.CommonStateUtils;
import com.emcloudinstance.util.CommonUtils;
import com.emcloudinstance.util.DatabaseUtil;
import com.emcloudinstance.util.SpringContext;

public class MigrationJobV2  implements Job {
	
	private String migrationDumpPath;
	private String energySchemaPath; 
	private String dbName;
	private String macId ;
	
	DatabaseUtil databaseUtil;
	CommonUtils commonUtils ;
	public MigrationJobV2(){
		databaseUtil = (DatabaseUtil)SpringContext.getBean("databaseUtil");
		commonUtils = (CommonUtils)SpringContext.getBean("commonUtils");
	}
	
	static final Logger logger = Logger.getLogger("EmCloudInstance");
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		System.out.println("New job at " + new Date() + " with migrationDumpPath = " + migrationDumpPath + 
				"  and dbName = " + dbName + " and energySchemaPath = " + energySchemaPath);
		
		
		String dbport = databaseUtil.port;
		String connection = "jdbc:postgresql://localhost:" + dbport + "/" + dbName +  "?characterEncoding=utf-8";
		JobDataMap jdMap = context.getJobDetail().getJobDataMap();
		//Statement updateMigration = null ;
		Statement updateWalLog = null ;
		Statement removeTriggers = null ;
		Connection conn = null ;
		try {
			synchronized (this) {
				File dump =new File(migrationDumpPath);
				File energy = new File(energySchemaPath) ;
				
				if (dump.exists()&&energy.exists())
				{
					//fire the dump on the database.
					File wd = new File("/bin");
					Runtime rt = Runtime.getRuntime();
					Process proc;
					BufferedReader in = null ;
					PrintWriter out  =null ;
					proc = rt.exec("/bin/bash", null , wd);
					if (proc != null) {
						   in = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
						   out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
						   out.println("/usr/bin/pg_restore -U  postgres -d "+ dbName +" -v "+ "< "+ dump.getAbsolutePath());
						   out.println("exit");
						  
						      String line;
						      while ((line = in.readLine()) != null) {
						         System.out.println(line);
						      }
						      proc.waitFor();
						      in.close();
						      out.close();
						      proc.destroy();
						   }
					
						   proc = rt.exec("/bin/bash", null , wd);
							if (proc != null) {
								    in = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
								   out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
								   out.println("/usr/bin/pg_restore -U  postgres -d "+ dbName +" -v "+ "< "+ energy.getAbsolutePath());
								   out.println("exit");
								      String line;
								      while ((line = in.readLine()) != null) {
								         System.out.println(line);
								      }
								      proc.waitFor();
								      in.close();
								      out.close();
								      proc.destroy();
								   }
					//Set wal Log entry to the latest in cloud config
					conn = DriverManager.getConnection(connection, "postgres", "postgres"); 
					updateWalLog = conn.createStatement();
					updateWalLog.executeUpdate("update cloud_config set val = (select coalesce(max(id), -99) from wal_logs) where name = 'lastWalSyncId'") ;
					// Remove all triggers which are related to wal log entry
					removeTriggers = conn.createStatement() ;
					removeTriggers.execute("select  removeAllTriggers()" );
					// Set the flag for migration done and so we can start sync now.
					//updateMigration = conn.createStatement();
					//updateMigration.executeUpdate("update cloud_config set val = 'true' where name = 'migrationDone'") ;
					commonUtils.setReplicaMigrationFlagOnCloud(macId,DatabaseState.MIGRATION_SUCCESS
							.getName());
					logger.info("Em With Mac " + macId + " is Migrated.") ;
					/* check whether tables related to states are empty if yes set state on ecloud related to that tables*/
					checkWhetherToPutOtherState(jdMap);
			}
				else
				{
					
					logger.error("Server was unable to run dumps on EM "+ dbName +". dump files are not found.") ;
				}
			}
		} catch (SQLException e) {
			commonUtils.setReplicaMigrationFlagOnCloud(macId,DatabaseState.MIGRATION_FAIL
					.getName());
			e.printStackTrace();
		} catch (IOException e) {
			commonUtils.setReplicaMigrationFlagOnCloud(macId,DatabaseState.MIGRATION_FAIL
					.getName());
			e.printStackTrace();
		} catch (InterruptedException e) {
			commonUtils.setReplicaMigrationFlagOnCloud(macId,DatabaseState.MIGRATION_FAIL
					.getName());
			e.printStackTrace();
		}catch(Exception ex)
		{
			commonUtils.setReplicaMigrationFlagOnCloud(macId,DatabaseState.MIGRATION_FAIL
					.getName());
		}finally {
			try {
				if (updateWalLog != null) {
					updateWalLog.close();
				}
				if (removeTriggers != null) {
					removeTriggers.close();
				}
				/*if (updateMigration != null) {
					updateMigration.close();
				}*/
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException se) {
				
			}
		}
		
	}

	/**
	 * @return the migrationDumpPath
	 */
	public String getMigrationDumpPath() {
		return migrationDumpPath;
	}

	/**
	 * @param migrationDumpPath the migrationDumpPath to set
	 */
	public void setMigrationDumpPath(String migrationDumpPath) {
		this.migrationDumpPath = migrationDumpPath;
	}

	/**
	 * @return the energySchemaPath
	 */
	public String getEnergySchemaPath() {
		return energySchemaPath;
	}

	/**
	 * @param energySchemaPath the energySchemaPath to set
	 */
	public void setEnergySchemaPath(String energySchemaPath) {
		this.energySchemaPath = energySchemaPath;
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

	public String getMacId() {
		return macId;
	}

	public void setMacId(String macId) {
		this.macId = macId;
	}
	
	
	
	 /**
	 * @param map
	 *  Take State machine to sync ready state if all tables are empty or till it does not find the table that is not empty.
	 */
	private void checkWhetherToPutOtherState(JobDataMap map) {
		ArrayList<String> tableNames = CommonStateUtils.tableNameList ;
		Boolean SyncReady =false;
		String table = "No Table" ;
		String mac=map.get("macId").toString() ;
		Iterator<String> itr = tableNames.iterator() ;
		try{
			while(itr.hasNext())
			{
				table = itr.next();
				if(map.containsKey(table) && (Boolean)map.get(table))
				{
					logger.info(table + " of EM with mac "+ mac +" is empty going to send all its migration state till success");
					commonUtils.setReplicaMigrationFlagOnCloud(mac,CommonStateUtils.getStateSartAccordingToTableName(table));
					commonUtils.setReplicaMigrationFlagOnCloud(mac,CommonStateUtils.getStateInProgressAccordingToTableName(table));
					commonUtils.setReplicaMigrationFlagOnCloud(mac,CommonStateUtils.getStateSuccessAccordingToTableName(table));
					SyncReady=true ;
				}else
				{
					logger.info(table + " of EM with mac "+ mac +" is not empty. Stopping state pushing. Normal Process will take the state management to SYNC_READY state");
					SyncReady=false ;
					break ;
				}
			}
			if(SyncReady==true)
			{
				logger.info("Putting the EM with mac "+ mac+" in sync ready state.");
				commonUtils.setReplicaMigrationFlagOnCloud(mac,DatabaseState.SYNC_READY
						.getName());
			}
		}catch(Exception e)
		{
			//Untill this point the Migration will be sucessfull otherwise this code will not be reached so letting the normal 
			//process take its way.
			logger.error(table + " state pushing threw an error for EM with mac "+mac+". Shifting Migration process state back to MIGRATION_SUCCESS so that it can carry on with the normal process.", e);
			commonUtils.setReplicaMigrationFlagOnCloud(mac,DatabaseState.MIGRATION_SUCCESS
					.getName());
		}
		}

	

}
