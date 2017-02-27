package com.emcloudinstance.ws;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.springframework.stereotype.Component;

import com.communication.template.CloudConnectionTemplate;
import com.communication.types.DatabaseState;
import com.communication.utils.CommonStateUtils;
import com.emcloudinstance.service.CloudConfigManager;
import com.emcloudinstance.service.DatabaseManager;
import com.emcloudinstance.service.EmUpgradeJob;
import com.emcloudinstance.service.MigrationJob;
import com.emcloudinstance.service.MigrationJobV2;
import com.emcloudinstance.service.MonitoringManager;
import com.emcloudinstance.util.CommonUtils;
import com.emcloudinstance.util.DatabaseUtil;
import com.emcloudinstance.util.SchedulerManager;
import com.sun.jersey.multipart.FormDataParam;

@Component
@Path("/org/communicate")
public class CommunicatorWebservices {

	static final Logger logger = Logger.getLogger("EmCloudInstance");

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	@Resource
	CloudConfigManager cloudConfigManager;
	@Resource
	DatabaseManager databaseManager;
	@Resource
	DatabaseUtil databaseUtil;
	@Resource
	CommonUtils commonUtils;
	@Resource
	CloudConnectionTemplate cloudConnectionTemplate ;
	
	@Resource
	MonitoringManager monitoringManager;

	private static String basedir = "/var/lib/tomcat6/Enlighted/ecloud/instance/";

	@Path("em/lastWalSynced/{emMacId}")
	@GET
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public String lastWalSynced(@PathParam("emMacId") String macId) {
		String walSynchId =  cloudConfigManager.getLastWALSynched(macId);
		logger.info(macId + ":Last WAL Synched for Mac:" +  walSynchId);
		return walSynchId;
	}

	@Path("em/dataMigrated/{emMacId}")
	@GET
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public String dataMigrated(@PathParam("emMacId") String macId) {
	
		String migrated = "false";
		String dbName = databaseUtil.getDbNameByMac(macId);
		Connection connection = null;
		Statement stmt = null;
		if (dbName != null) {
			try {
				String conString = "jdbc:postgresql://localhost:"
						+ databaseUtil.port + "/" + dbName
						+ "?characterEncoding=utf-8";
				connection = DriverManager.getConnection(conString, "postgres",
						"postgres");
				stmt = connection.createStatement();
				ResultSet rs = stmt
						.executeQuery("select val from cloud_config where name = 'migrationDone'");

				rs.next();
				migrated = rs.getString("val");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (connection != null) {
					try {
						connection.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		logger.info(macId + ":Data Migration status:" +  migrated);
		return migrated;

	}

	/**
	 * Update WAL logs from local 
	 * @param otherDataStream - WAL log data 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Path("em/data")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response walLogsUpdate(
			@FormDataParam("other") InputStream otherDataStream) {
		String walSynchId = null ;
		logger.info("Update recieved from a local EM server");

		boolean success = false;
		ZipInputStream zipOtherIn = null;
		ObjectInputStream objectIn = null;
		String macId = "";
		try {
			zipOtherIn = new ZipInputStream(otherDataStream);
			zipOtherIn.getNextEntry();
			objectIn = new ObjectInputStream(zipOtherIn);
			Map<String, Object> map = (HashMap<String, Object>) objectIn
					.readObject();
			macId =  (String)map.get("macId");
			logger.info("Update recieved from a local EM server with [macId,Version,Max WAL Id}: ["
					+ map.get("macId")
					+ ","
					+ map.get("version")
					+ ","
					+ map.get("maxWalLogDataId") + "]");

			success = this.databaseManager.walLogsUpdate(
					(String) map.get("macId"),
					((Long) map.get("maxWalLogDataId")).toString(),
					(List<String>) map.get("data"));
			logger.info(macId + ":WAL log update successful:" +  success);
			
			// notifying the last sync wal id to cloud
			 walSynchId =  cloudConfigManager.getLastWALSynched(macId);
			
			 walSynchId = walSynchId +"@" + commonUtils.getTime();
		} catch (Exception e) {
			logger.info(macId + ":WAL log update failed:" +  e.getMessage());
			// notify cloud that exception has happened.
			walSynchId = e.getMessage() ;
			walSynchId = walSynchId +"@" +  commonUtils.getTime() ;
		}finally{
			if(zipOtherIn != null){
				try {
					zipOtherIn.close();
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}
			if(objectIn != null){
				try{
					objectIn.close();
				}catch (IOException e) {					
					e.printStackTrace();
				}
			}
			commonUtils.setReplicaSyncFlagLogOnCloud(macId,DatabaseState.SYNC_READY.getName(), walSynchId );
			
		}
		if (success) {
			//monitoringManager.scheduleDiagnostics(macId);
			return Response.status(200).entity("").build();
		} else {
			return Response.status(500).entity("").build();
		}

		

	}

	/**
	 * Synch the table data. The table data is inserted as a batch
	 * 
	 * @param macId - Mac of the local EM
	 * @param tableName - table in which the data to be synched
	 * @param otherDataStream - data 
	 * 
	 * @return - success/failure
	 */
	@Path("em/tabledata/{emMacId}/{tableName}")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response synchTableData(@PathParam("emMacId") String macId,
			@PathParam("tableName") String tableName,
			@FormDataParam("other") InputStream otherDataStream) {
		boolean success = false;
		success = this.databaseManager.synchTableData(macId, tableName,otherDataStream);		
		logger.info(macId + ":" +tableName + ":Data Synched status success:" +success);
		if (!success) {
			return Response.status(500).entity("").build();
		} else {
			return Response.status(200).entity("").build();
		}
	}
	
	@Path("em/set/state/migration/{emMacId}/{state}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response synchTableData(@PathParam("emMacId") String macId,
			@PathParam("state") String state) {
		boolean success = false;
		success = commonUtils.setReplicaMigrationFlagOnCloud(macId, state);		
		if (!success) {
			logger.info(macId + ":Failed to update to state:"+ state) ;
			return Response.status(500).entity("fail").build();
		} else {
			logger.info(macId + ":Updated to state:"+ state) ;
			/*if(state.equalsIgnoreCase(DatabaseState.SYNC_READY.getName()))
			{ 
				databaseManager.fireRelTrigger(macId) ;
			}*/
			return Response.status(200).entity("success").build();
			
		}
		
	}
	/**
	 * The function returns the minimum id synched for a table. This is 
	 * useful when we are synching from higher to lower.
	 * 
	 * @param macId - Mac of the local EM server
	 * @param tableName - Table for which the the min id is returned
	 * @return - Response contains the minimum id
	 */
	@Path("em/lastmintabledatasynched/{emMacId}/{tableName}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getLastMinTaleDataSynched(
			@PathParam("emMacId") String macId,
			@PathParam("tableName") String tableName) {
		boolean success = false;
		Long minIdSynched = null;
		try {
			minIdSynched = this.databaseManager.getLastMinTableDataSynched(
					macId, tableName);
			if(minIdSynched==null|| minIdSynched == 0l)
			{
				minIdSynched=-1l ;
			}
			success = true;
			logger.info(macId + ":" + tableName + ":Last Min Table Id Synched:"+ minIdSynched) ;
		} catch (Exception e) {
			success = false;
		}
		if (!success) {
			return Response.status(500).entity("").build();
		} else {
			return Response.status(200).entity(String.valueOf(minIdSynched)).build();
		}
	}

	/**
	 * The function returns true when called. Just to check Connectivity.
	 * 
	 */
	@Path("em/check/connectivity")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getLastMinTaleDataSynched() {
		Boolean success = true;
		
		if (!success) {
			return Response.status(500).entity("").build();
		} else {
			return Response.status(200).entity(String.valueOf(success.toString())).build();
		}
	}



	/**
	 * Migration service
	 * Use the next version of this webservice
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	@Path("em/data/migrate")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response sppa_migration(
			@FormDataParam("other") InputStream otherDataStream) {
		
		boolean fail = false;
		ZipInputStream zipOtherIn = new ZipInputStream(otherDataStream);
		Connection connection = null;
		Statement stmt = null;
		String mac = null ;

		try {
			zipOtherIn.getNextEntry();
			ObjectInputStream objectIn = new ObjectInputStream(zipOtherIn);
			Map<String, Object> map = (HashMap<String, Object>) objectIn
					.readObject();
			mac=map.get("macId").toString() ;
			System.out.println(map.get("macId") + "   " + map.get("version"));
			logger.info(mac + ":Going to migrate") ;
			String dbName = databaseUtil.getDbNameByMac(map.get("macId")
					.toString());
			commonUtils.setReplicaMigrationFlagOnCloud(map.get("macId").toString(),DatabaseState.MIGRATION_IN_PROGRESS.getName());
			// drop db and then recreate it 
			databaseUtil.dropDatabase(dbName) ;
			databaseUtil.createDatabase(dbName) ;
			String dirPath = basedir + dbName + "/migration/";

			File f = new File(dirPath);
			if (!f.mkdir()) {
				if (!f.mkdirs() && !f.exists()) {
					fail = true;
					return Response.status(500).entity("").build();
				}
			}
			File dump = new File(dirPath + "sppa_migration.sql");
			File energy = new File(dirPath + "sppa_energy_schema.sql");
			if (!dump.exists()) {
				dump.createNewFile();
			} else {
				dump.delete();
				dump.createNewFile();
			}

			if (!energy.exists()) {
				energy.createNewFile();
			} else {
				energy.delete();
				energy.createNewFile();
			}

			byte[] dumpcontents = (byte[]) map.get("dump");
			byte[] energyContents = (byte[]) map.get("energy");
			FileUtils.writeByteArrayToFile(dump, dumpcontents);
			FileUtils.writeByteArrayToFile(energy, energyContents);
			fail = false;

			// Let a seperate thread handle migration
			String jobId = "job_migration_" + dbName;
			String triggerId = "trigger_migration_" + dbName;
			// Create quartz job
			JobDetail migrationJob = newJob(MigrationJob.class)
					.withIdentity(
							jobId,
							SchedulerManager.getInstance().getScheduler()
									.getSchedulerName())
					.usingJobData("migrationDumpPath", dump.getAbsolutePath())
					.usingJobData("energySchemaPath", energy.getAbsolutePath())
					.usingJobData("macId",map.get("macId").toString())
					.usingJobData("dbName", dbName).build();

			// Create Quartz trigger
			SimpleTrigger migrationTrigger = (SimpleTrigger) newTrigger()
					.withIdentity(
							triggerId,
							SchedulerManager.getInstance().getScheduler()
									.getSchedulerName()).startNow().build();

			SchedulerManager.getInstance().getScheduler()
					.scheduleJob(migrationJob, migrationTrigger);

		} catch (IOException e) {
			fail = true;
			e.printStackTrace();
		} catch (Exception e) {
			fail = true;
			e.printStackTrace();
		} finally {

			if (zipOtherIn != null) {
				try {
					zipOtherIn.close();
				} catch (IOException e) {
					fail = true;
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					fail = true;
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					fail = true;
					e.printStackTrace();
				}
			}
		}

		if (fail) {
			commonUtils.setReplicaMigrationFlagOnCloud(mac,DatabaseState.MIGRATION_FAIL
					.getName());
			logger.info(mac + ":Migration Failed") ;
			return Response.status(500).entity("").build();
			
		} else {
			return Response.status(200).entity("").build();
		}

	}

	/**
	 * Migration service version 2
	 */
	@SuppressWarnings("unchecked")
	@Path("em/data/migrate/v2")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response sppa_migration_v2(
			@FormDataParam("other") InputStream otherDataStream) {
		Map<String, Object> map = new HashMap<String, Object> () ;
		boolean fail = false;
		ZipInputStream zipOtherIn = new ZipInputStream(otherDataStream);
		Connection connection = null;
		Statement stmt = null;
		String mac = null ;
		try {
			zipOtherIn.getNextEntry();
			ObjectInputStream objectIn = new ObjectInputStream(zipOtherIn);
			map = (HashMap<String, Object>) objectIn
					.readObject();
			mac=map.get("macId").toString() ;
			System.out.println(map.get("macId") + "   " + map.get("version"));
			logger.info(mac + ":Going to migrate") ;
			String dbName = databaseUtil.getDbNameByMac(map.get("macId")
					.toString());
			commonUtils.setReplicaMigrationFlagOnCloud(map.get("macId").toString(),DatabaseState.MIGRATION_IN_PROGRESS.getName());
			// drop db and then recreate it 
			databaseUtil.dropDatabase(dbName) ;
			databaseUtil.createDatabase(dbName) ;
			String dirPath = basedir + dbName + "/migration/";

			File f = new File(dirPath);
			if (!f.mkdir()) {
				if (!f.mkdirs() && !f.exists()) {
					fail = true;
					return Response.status(500).entity("").build();
				}
			}
			File dump = new File(dirPath + "sppa_migration.sql");
			File energy = new File(dirPath + "sppa_energy_schema.sql");
			if (!dump.exists()) {
				dump.createNewFile();
			} else {
				dump.delete();
				dump.createNewFile();
			}

			if (!energy.exists()) {
				energy.createNewFile();
			} else {
				energy.delete();
				energy.createNewFile();
			}

			byte[] dumpcontents = (byte[]) map.get("dump");
			byte[] energyContents = (byte[]) map.get("energy");
			FileUtils.writeByteArrayToFile(dump, dumpcontents);
			FileUtils.writeByteArrayToFile(energy, energyContents);
			fail = false;

			// Let a seperate thread handle migration
			String jobId = "job_migration_" + dbName;
			String triggerId = "trigger_migration_" + dbName;
			// Create quartz job
			JobDetail migrationJob = newJob(MigrationJobV2.class)
					.withIdentity(
							jobId,
							SchedulerManager.getInstance().getScheduler()
									.getSchedulerName())
					.usingJobData("migrationDumpPath", dump.getAbsolutePath())
					.usingJobData("energySchemaPath", energy.getAbsolutePath())
					.usingJobData("macId",map.get("macId").toString())
					.usingJobData("dbName", dbName).build();
			migrationJob.getJobDataMap().putAll(map);

			// Create Quartz trigger
			SimpleTrigger migrationTrigger = (SimpleTrigger) newTrigger()
					.withIdentity(
							triggerId,
							SchedulerManager.getInstance().getScheduler()
									.getSchedulerName()).startNow().build();

			SchedulerManager.getInstance().getScheduler()
					.scheduleJob(migrationJob, migrationTrigger);

		} catch (IOException e) {
			fail = true;
			e.printStackTrace();
		} catch (Exception e) {
			fail = true;
			e.printStackTrace();
		} finally {

			if (zipOtherIn != null) {
				try {
					zipOtherIn.close();
				} catch (IOException e) {
					fail = true;
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					fail = true;
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					fail = true;
					e.printStackTrace();
				}
			}
		}

		if (fail) {
			commonUtils.setReplicaMigrationFlagOnCloud(mac,DatabaseState.MIGRATION_FAIL
					.getName());
			logger.info(mac + ":Migration Failed") ;
			return Response.status(500).entity("").build();
			
		} else {
			return Response.status(200).entity("").build();
		}

	}

	


	/**
	 * upgrade service
	 */
	@SuppressWarnings("unchecked")
	@Path("em/data/upgrade")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response sppa_upgrade(
			@FormDataParam("other") InputStream otherDataStream) {

		boolean fail = false;
		ZipInputStream zipOtherIn = new ZipInputStream(otherDataStream);
		Connection connection = null;
		Statement stmt = null;

		try {
			zipOtherIn.getNextEntry();
			ObjectInputStream objectIn = new ObjectInputStream(zipOtherIn);
			Map<String, Object> map = (HashMap<String, Object>) objectIn
					.readObject();
			String macId = (String)map.get("macId");
			String dbName = databaseUtil.getDbNameByMac(map.get("macId")
					.toString());
			String dirPath = basedir + dbName + "/upgrade/";

			File f = new File(dirPath);
			if (!f.mkdir()) {
				if (!f.mkdirs() && !f.exists()) {
					fail = true;
					return Response.status(500).entity("").build();
				}
			}

			File dump = new File(dirPath + map.get("filename"));
			if (!dump.exists()) {
				dump.createNewFile();
			} else {
				dump.delete();
				dump.createNewFile();
			}
			byte[] dumpcontents = (byte[]) map.get("dump");
			FileUtils.writeByteArrayToFile(dump, dumpcontents);

			fail = false;

			String jobId = "job_upgrade_" + dbName;
			String triggerId = "trigger_upgrade_" + dbName;
			// Create quartz job
			JobDetail upgradeJob = newJob(EmUpgradeJob.class)
					.withIdentity(
							jobId,
							SchedulerManager.getInstance().getScheduler()
									.getSchedulerName())
					.usingJobData("upgradeFile", dump.getAbsolutePath())
					.usingJobData("dbName", dbName)
					.usingJobData("version", map.get("version").toString())
					.usingJobData("upgradeWalId",
							map.get("upgradeWalId").toString()).build();

			// Create Quartz trigger
			SimpleTrigger upgradeTrigger = (SimpleTrigger) newTrigger()
					.withIdentity(
							triggerId,
							SchedulerManager.getInstance().getScheduler()
									.getSchedulerName()).startNow().build();

			SchedulerManager.getInstance().getScheduler()
					.scheduleJob(upgradeJob, upgradeTrigger);

			logger.info(macId + ":Upgrade Scheduled") ;
		} catch (IOException e) {
			fail = true;
			e.printStackTrace();
		} catch (Exception e) {
			fail = true;
			e.printStackTrace();
		} finally {

			if (zipOtherIn != null) {
				try {
					zipOtherIn.close();
				} catch (IOException e) {
					fail = true;
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					fail = true;
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					fail = true;
					e.printStackTrace();
				}
			}
		}

		if (fail) {
			return Response.status(500).entity("").build();
		} else {
			return Response.status(200).entity("").build();
		}

	}

	@SuppressWarnings("unchecked")
	@Path("em/upgradeStatus")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public String upgradeStatus(
			@FormDataParam("other") InputStream otherDataStream) {

		ZipInputStream zipOtherIn = new ZipInputStream(otherDataStream);
		String status = "N";
		try {
			zipOtherIn.getNextEntry();
			ObjectInputStream objectIn = new ObjectInputStream(zipOtherIn);
			Map<String, Object> map = (HashMap<String, Object>) objectIn
					.readObject();
			System.out.println(map.get("macId") + "   " + map.get("version")
					+ "  " + map.get("upgradeWalId").toString());

			String dbName = databaseUtil.getDbNameByMac(map.get("macId")
					.toString());

			Scheduler sched = SchedulerManager.getInstance().getScheduler();

			if (sched.checkExists(new JobKey("job_upgrade_" + dbName, sched
					.getSchedulerName()))
					|| (new Long(map.get("upgradeWalId").toString()))
							.compareTo(new Long(cloudConfigManager
									.getCloudConfig(dbName, "lastWalSyncId"))) == 0) {
				status = "P";
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (zipOtherIn != null) {
				try {
					zipOtherIn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return status;
	}


}
