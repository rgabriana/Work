package com.emcloudinstance.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
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

public class RestrictedRemigrationJob implements Job {

	private String migrationDumpPath;
	private String upgradeSQLPath;
	private String energyDumpPath;
	private String dbName;
	private String macId;

	DatabaseUtil databaseUtil;
	CommonUtils commonUtils;

	public RestrictedRemigrationJob() {
		databaseUtil = (DatabaseUtil) SpringContext.getBean("databaseUtil");
		commonUtils = (CommonUtils) SpringContext.getBean("commonUtils");
	}

	static final Logger logger = Logger.getLogger(RestrictedRemigrationJob.class.getName());

	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		logger.info("New job at " + new Date() + " with migrationDumpPath = "
				+ migrationDumpPath + "  and dbName = " + dbName
				+ " and upgradeSQLPath = " + upgradeSQLPath);
		
		Process pr = null;
		

		String dbport = databaseUtil.port;
		String connection = "jdbc:postgresql://localhost:" + dbport + "/"
				+ dbName + "?characterEncoding=utf-8";
		JobDataMap jdMap = context.getJobDetail().getJobDataMap();

		Statement dropRelations = null;
		Statement updateWalLog = null ;
		Statement removeTriggers = null ;
		Connection conn = null;
		Statement dropTriggers = null;
		Connection conn1 = null;
		try {
			synchronized (this) {
				
				//First Release all connection 
		    	String query ="SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = '"+dbName+"'" ;
		        String[] releaseConnections = { "/usr/bin/psql", "-U", "postgres", "-c" ,query } ;
		        pr = Runtime.getRuntime().exec(releaseConnections);
		        pr.waitFor();
				
				File dump = new File(migrationDumpPath);
				File upgrade = new File(upgradeSQLPath);
				File energy = new File(energyDumpPath);

				if (dump.exists() && upgrade.exists() && energy.exists()) {
					// fire the dump on the database.
					File wd = new File("/bin");
					Runtime rt = Runtime.getRuntime();
					Process proc;
					BufferedReader in = null;
					PrintWriter out = null;
					proc = rt.exec("/bin/bash", null, wd);
					if (proc != null) {

						// Run upgradeSQL
						in = new BufferedReader(new InputStreamReader(
								proc.getErrorStream()));
						out = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(
										proc.getOutputStream())), true);
						out.println("/usr/bin/psql -U  postgres -d " + dbName
								+ "< " + upgrade.getAbsolutePath());
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
					
					
					//Again Release all connection 
			        pr = Runtime.getRuntime().exec(releaseConnections);
			        pr.waitFor();
					
					//drop relations
					conn = DriverManager.getConnection(connection, "postgres",
							"postgres");
					int i = 1;
					while(i < 5) {
						logger.info("Drop relations for em with mac " + macId + " attempt " + i); 
						dropRelations = conn.createStatement();
						dropRelations.execute("select dropRelations()");
						DatabaseMetaData meta = conn.getMetaData();
						ResultSet res = meta.getTables(null, null, "cloud_config", new String[] {"TABLE"});
						if (res != null && res.next()) {
							logger.warn("EM: " + macId + "  ::table cloud_config still exists. retry dropping relations.");
							i++;
							dropRelations.close();
							Thread.sleep(5000);
						}
						else {
							break;
						}
					}
					if(i==5) {
						logger.warn("EM: " + macId + "  ::table cloud_config still exists after multiple attempts. Cannot continue with restricted remigration.");
						commonUtils.setReplicaMigrationFlagOnCloud(macId,DatabaseState.RESTRICTED_REMIGRATION_FAIL.getName(), null);
						return;
					}
					
					
					//Dump schema/data
					proc = rt.exec("/bin/bash", null, wd);
					if (proc != null) {
						in = new BufferedReader(new InputStreamReader(
								proc.getErrorStream()));
						out = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(
										proc.getOutputStream())), true);
						out.println("/usr/bin/pg_restore -U  postgres -d "
								+ dbName + " -v " + "< "
								+ dump.getAbsolutePath());
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
					
					//Dump energy and motion bits schema
					proc = rt.exec("/bin/bash", null, wd);
					if (proc != null) {
						in = new BufferedReader(new InputStreamReader(
								proc.getErrorStream()));
						out = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(
										proc.getOutputStream())), true);
						out.println("/usr/bin/pg_restore -U  postgres -d "
								+ dbName + " -v " + "< "
								+ energy.getAbsolutePath());
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
					
					
					// Set wal Log entry to the latest in cloud config
					conn = DriverManager.getConnection(connection, "postgres",
							"postgres");
					updateWalLog = conn.createStatement();
					updateWalLog
							.executeUpdate("update cloud_config set val = (select coalesce(max(id), -99) from wal_logs) where name = 'lastWalSyncId'");
					// Remove all triggers which are related to wal log entry
					removeTriggers = conn.createStatement();
					removeTriggers.execute("select  removeAllTriggers()");
					commonUtils.setReplicaMigrationFlagOnCloud(macId,
							DatabaseState.RESTRICTED_REMIGRATION_SUCCESS.getName(),
							new Date());
					logger.info("Em With Mac " + macId + " is Migrated.");
					/*
					 * check whether tables related to states are empty if yes
					 * set state on ecloud related to that tables
					 */
					checkWhetherToPutOtherState(jdMap);
					try {
						conn1 = DriverManager.getConnection(connection,
								"postgres", "postgres");
						// Drop all em triggers not related to wal logs
						dropTriggers = conn1.createStatement();
						dropTriggers.execute("select  drop_triggers()");
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					} finally {
						try {
							if (dropTriggers != null) {
								dropTriggers.close();
							}
							if (conn1 != null) {
								conn1.close();
							}
						} catch (SQLException se) {
						}
					}
				} else {

					logger.error("Server was unable to run dumps on EM "
							+ dbName + ". dump files are not found.");
				}
			}
		} catch (SQLException e) {
			commonUtils.setReplicaMigrationFlagOnCloud(macId,
					DatabaseState.RESTRICTED_REMIGRATION_FAIL.getName(), null);
			e.printStackTrace();
		} catch (IOException e) {
			commonUtils.setReplicaMigrationFlagOnCloud(macId,
					DatabaseState.RESTRICTED_REMIGRATION_FAIL.getName(), null);
			e.printStackTrace();
		} catch (InterruptedException e) {
			commonUtils.setReplicaMigrationFlagOnCloud(macId,
					DatabaseState.RESTRICTED_REMIGRATION_FAIL.getName(), null);
			e.printStackTrace();
		} catch (Exception ex) {
			commonUtils.setReplicaMigrationFlagOnCloud(macId,
					DatabaseState.RESTRICTED_REMIGRATION_FAIL.getName(), null);
		} finally {
			try {
				if (dropRelations != null) {
					dropRelations.close();
				}
				if (updateWalLog != null) {
					updateWalLog.close();
				}
				if (removeTriggers != null) {
					removeTriggers.close();
				}
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
	 * @param migrationDumpPath
	 *            the migrationDumpPath to set
	 */
	public void setMigrationDumpPath(String migrationDumpPath) {
		this.migrationDumpPath = migrationDumpPath;
	}

	/**
	 * @return the upgradeSQLPath
	 */
	public String getUpgradeSQLPath() {
		return upgradeSQLPath;
	}

	/**
	 * @param energySchemaPath
	 *            the upgradeSQLPath to set
	 */
	public void setUpgradeSQLPath(String upgradeSQLPath) {
		this.upgradeSQLPath = upgradeSQLPath;
	}

	/**
	 * @return the dbName
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * @param dbName
	 *            the dbName to set
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
	 *            Take State machine to sync ready state if all tables are empty
	 *            or till it does not find the table that is not empty.
	 */
	private void checkWhetherToPutOtherState(JobDataMap map) {
		ArrayList<String> tableNames = CommonStateUtils.tableNameList;
		Boolean SyncReady = false;
		String table = "No Table";
		String mac = map.get("macId").toString();
		Iterator<String> itr = tableNames.iterator();
		try {
			while (itr.hasNext()) {
				table = itr.next();
				if (map.containsKey(table) && (Boolean) map.get(table)) {
					logger.info(table
							+ " of EM with mac "
							+ mac
							+ " is empty going to send all its migration state till success");
					commonUtils.setReplicaMigrationFlagOnCloud(mac,
							CommonStateUtils
									.getStateSartAccordingToTableName(table, true),
							new Date());
					commonUtils
							.setReplicaMigrationFlagOnCloud(
									mac,
									CommonStateUtils
											.getStateInProgressAccordingToTableName(table, true),
									new Date());
					commonUtils
							.setReplicaMigrationFlagOnCloud(
									mac,
									CommonStateUtils
											.getStateSuccessAccordingToTableName(table, true),
									new Date());
					SyncReady = true;
				} else {
					logger.info(table
							+ " of EM with mac "
							+ mac
							+ " is not empty. Stopping state pushing. Normal Process will take the state management to SYNC_READY state");
					SyncReady = false;
					break;
				}
			}
			if (SyncReady == true) {
				logger.info("Putting the EM with mac " + mac
						+ " in sync ready state.");
				commonUtils.setReplicaMigrationFlagOnCloud(mac,
						DatabaseState.SYNC_READY.getName(), null);
			}
		} catch (Exception e) {
			// Untill this point the Migration will be sucessfull otherwise this
			// code will not be reached so letting the normal
			// process take its way.
			logger.error(
					table
							+ " state pushing threw an error for EM with mac "
							+ mac
							+ ". Shifting Migration process state back to RESTRICTED_REMIGRATION_SUCCESS so that it can carry on with the normal process.",
					e);
			commonUtils.setReplicaMigrationFlagOnCloud(mac,
					DatabaseState.RESTRICTED_REMIGRATION_SUCCESS.getName(), null);
		}
	}

	public String getEnergyDumpPath() {
		return energyDumpPath;
	}

	public void setEnergyDumpPath(String energyDumpPath) {
		this.energyDumpPath = energyDumpPath;
	}

}