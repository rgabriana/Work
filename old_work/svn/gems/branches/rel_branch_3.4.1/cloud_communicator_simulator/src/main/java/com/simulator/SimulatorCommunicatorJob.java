package com.simulator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.communication.template.SecureCloudConnectionTemplate;
import com.communication.types.DatabaseState;
import com.communication.types.EmStatus;
import com.communication.utils.CloudHttpResponse;
import com.communication.utils.CommonStateUtils;
import com.communicator.CommunicatorEntryPoint;
import com.communicator.dao.SystemConfigDao;
import com.communicator.job.CommunicatorJob;
import com.communicator.manager.CallHomeManager;
import com.communicator.manager.CloudManager;
import com.communicator.manager.MigrationManager;
import com.communicator.manager.ReplicaServerInfoManager;
import com.communicator.manager.ServerInfoManager;
import com.communicator.util.CommunicatorConstant;
import com.communicator.util.InitializeSecureConnection;
import com.communicator.util.SchedulerManager;
import com.communicator.util.SpringContext;

public class SimulatorCommunicatorJob implements Job {
	
	static final Logger logger = Logger.getLogger(SimulatorCommunicatorJob.class.getName());
	
	static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	static int numberofFailedAttempts = 0;
	
	public static Thread callHomeThread = null;
	
	ServerInfoManager serverInfoManager;
    CallHomeManager callHomeManager;
    MigrationManager migrationManager;
    ReplicaServerInfoManager replicaServerInfoManager;
    SystemConfigDao systemConfigDao;
    SecureCloudConnectionTemplate secureCloudConnectionTemplate;
    CloudManager cloudManager;
    JdbcTemplate jdbcTemplate;
    
    private void initCallHome() {
    	if(callHomeThread == null) {
    		callHomeThread = new Thread(new SimulatorCommunicatorJob.CallHomePoll());
    		callHomeThread.start();
    	}
    }
    
	
	public SimulatorCommunicatorJob() {
		serverInfoManager = (ServerInfoManager)SpringContext.getBean("serverInfoManager");
	    callHomeManager = (CallHomeManager)SpringContext.getBean("callHomeManager");
	    migrationManager = (MigrationManager)SpringContext.getBean("migrationManager");
	    replicaServerInfoManager = (ReplicaServerInfoManager)SpringContext.getBean("replicaServerInfoManager");
	    systemConfigDao = (SystemConfigDao)SpringContext.getBean("systemConfigDao");
	    secureCloudConnectionTemplate = (SecureCloudConnectionTemplate)SpringContext.getBean("secureCloudConnectionTemplate");
	    cloudManager = (CloudManager) SpringContext.getBean("cloudManager");
	    jdbcTemplate = (JdbcTemplate) SpringContext.getBean("jdbcTemplate");
	    initCallHome();
	}
	
	class CallHomePoll implements Runnable {

		@Override
		public void run() {
			while (true) {
				try{
					while(true) {
						try {
							CommunicatorEntryPoint.commReset = 0;
							if(CommunicatorJob.healthBeacon && ("1".equals(serverInfoManager.getCloudSyncType()) || "2".equals(serverInfoManager.getCloudSyncType())) ) {
								callHomeManager.sendDataCallHome();
								if(!CommunicatorJob.syncReady && CommunicatorJob.callHomeSuccess && "2".equals(serverInfoManager.getCloudSyncType())) {
									replicaCall();
								}
							}
							else {
								Thread.sleep(5000);
							}
						}
					 	catch (InterruptedException e) {
							logger.error(e);
							Thread.sleep(60000);
						}
						catch (Exception e) {
							logger.error(e);
							Thread.sleep(60000);
						}
					}
				}
				catch (Exception e) {
					logger.error(e);
				}
			}
			
		}
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			List<JobExecutionContext> jobs = SchedulerManager.getInstance().getScheduler().getCurrentlyExecutingJobs();
			for (JobExecutionContext job : jobs) {
	            if (job.getTrigger().equals(context.getTrigger()) && !job.getFireInstanceId().equals(context.getFireInstanceId())) {
	            	numberofFailedAttempts++;
	            	if(numberofFailedAttempts > 15) {
	            		System.exit(0);
	            	}
	                logger.info(numberofFailedAttempts + ": There's another instance running, so leaving" + this);
	                return;
	            }
	        }
			numberofFailedAttempts = 0;
			if(systemConfigDao.isCloudEnabled().equalsIgnoreCase("0"))
			{
				CommunicatorJob.healthBeacon = false;
				logger.info("EM is not cloud enabled. Contact Administrator");
			} else {
				CommunicatorJob.healthBeacon = true;
				String status = serverInfoManager.getCloudSyncType();
				if ("2".equals(status) && CommunicatorJob.callHomeSuccess) {
					if(CommunicatorJob.syncReady) {
						replicaCall();
					}
				}
			}
		} catch (SecurityException e) {
			logger.error(e.toString());
		} catch(Exception e) {
			logger.error(e.toString());
		}
	}
	
	private void replicaCall() throws SchedulerException {
		if(!InitializeSecureConnection.getIsSppaCertificateInitialized()) {
			InitializeSecureConnection.init(secureCloudConnectionTemplate);
		}
		if(replicaServerInfoManager.getReplicaConnectivity()) {
			//migrationManager.runMigrationOrSync required content
			if(replicaServerInfoManager.getCurrentEmStatus()== EmStatus.SPPA)
			{
				if(replicaServerInfoManager.getCurrentMigrationStateAttempts().longValue() > CommunicatorConstant.errorToleranceCount.longValue() ) {
					logger.info("Attempts for completing the " + replicaServerInfoManager.getCurrentMigrationState().getName() + " State has lapsed more than 3 times. Migration cannot progress. Contact Adminstrator.");
					String state = CommonStateUtils.getFailState(replicaServerInfoManager.getCurrentMigrationState()) ;
					if(state!=null) {
						migrationManager.setReplicaMigrationFlagOnCloud(state);
					}
					return ;
				}
				switch(replicaServerInfoManager.getCurrentMigrationState()) {
					case SYNC_READY:
						sendData();	
						break;
					case REPLICA_UNREACHABLE:
						logger.info("Em Was able to re establish connection with replica server. Resetting the flag to start process from where it was left.") ;
						migrationManager.setPreviousMigrationFlagOnCloud() ;
						break;
					default:
						logger.error("Simulation Communicator cannot understand " +replicaServerInfoManager.getCurrentMigrationState()+" state. Contact Adminstrator.");
					break;
				}
			}
		}
		else {
			migrationManager.setReplicaMigrationFlagDirectECloud( DatabaseState.REPLICA_UNREACHABLE.getName());
		}
	}
	
	private void sendData() {
		logger.info("Starting data sync event at " + new Date());
		Long lastWalLogDataId = null;
		try {
			lastWalLogDataId = cloudManager.getLastWalSyncId();
			if (lastWalLogDataId != -100) {

				ByteArrayOutputStream baos_other = new ByteArrayOutputStream();
				ZipOutputStream zipOtherOut = new ZipOutputStream(baos_other);

				Map<String, Object> map = new HashMap<String, Object>();

				try {

					MultipartEntity parts = new MultipartEntity();

					fillWALChangesAndWalId(map, lastWalLogDataId);

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

						CloudHttpResponse response = secureCloudConnectionTemplate.executePost(
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

								secureCloudConnectionTemplate.executePost(
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

						CloudHttpResponse synchResponse = secureCloudConnectionTemplate
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
	
	
	private void fillWALChangesAndWalId(Map<String, Object> map, Long lastWalLogDataId){
		if (lastWalLogDataId.compareTo(Main.lastComputedMaxWalId) == 0) {
			Main.lastKnownMaxWalId = lastWalLogDataId;
			Main.lastKnownMaxCaptureAt = Main.lastComputedMaxCaptureAt;
			Main.lastKnownECMaxId = Main.lastComputedECMaxId;
		}
		List<String> statements = new ArrayList<String>();
		Date d = new Date((new Date()).getTime() - 300000);
		Main.lastComputedMaxWalId = lastWalLogDataId;
		Main.lastComputedMaxCaptureAt = Main.lastKnownMaxCaptureAt;
		Main.lastComputedECMaxId = Main.lastKnownECMaxId; 
		while (Main.lastComputedMaxWalId < Main.lastKnownMaxWalId + 50000) {
			if(d.after(Main.lastComputedMaxCaptureAt)) {
				Main.lastComputedMaxCaptureAt = new Date(Main.lastComputedMaxCaptureAt.getTime() + 300000);
				String captureAt = df.format(Main.lastComputedMaxCaptureAt);
				List<String> l = new ArrayList<String>(Main.fixtureList.keySet());
				Collections.shuffle(l);
				for(String mac: l) {
					Main.lastComputedECMaxId++;
					Main.lastComputedMaxWalId = Main.lastComputedMaxWalId + 2;
					statements.add("insert into energy_consumption (" +
									"light_avg_level, max_volts, base_power_used, " +
									"sys_uptime, capture_at, base_cost, " +
									"light_on_seconds, saved_power_used, occ_count, " +
									"id, motion_bits, cu_status, " +
									"cost, max_temperature, light_min_level, " +
									"fixture_id, dim_percentage, occ_out, " +
									"ambient_saving, power_calc, saving_type, " +
									"saved_cost, curr_state, last_temperature, " +
									"price, occ_saving, avg_volts, " +
									"bright_offset, min_volts, energy_calib, " +
									"last_volts, zero_bucket, light_max_level, " +
									"avg_temperature, occ_in, dim_offset, " +
									"power_used, energy_cum, light_on, " +
									"manual_saving, light_off, energy_ticks, " +
									"tuneup_saving, bright_percentage, min_temperature) values " +
									"(E'4', E'0', E'68.20', " +
									"E'85335', E'" + captureAt +"', E'0.00045466665000000001', " +
									"E'0', E'66.86', null, " +
									"E'" + Main.lastComputedECMaxId + "', E'0', E'0', " +
									"E'8.9600000000000006e-06', E'23', E'4', " +
									"E'" + Main.fixtureList.get(mac) + "', null, null, " +
									"E'0.00', E'2', E'1', " +
									"E'0.00044570664999999998', E'3', E'23', " +
									"E'0.080000000000000002', E'31.26', E'0', " +
									"E'0', E'0', E'0', " +
									"E'0', E'0', E'4', " +
									"E'65', null, E'0', " +
									"E'1.34', E'28500', E'0', " +
									"E'0.00', E'0', E'112', " +
									"E'35.60', E'24', E'23');");
					statements.add("update fixture  set " +
									"push_profile = E'f', ballast_manufacturer = E'ENL', sensor_id = E'Sensor" + mac.replaceAll(":", "")+ "'," +
									" commissioned_time = null, last_cmd_sent_at = null, " +
									"last_cmd_status = null, light_level = E'0', upgrade_status = E'Success', " +
									"comm_type = E'1', bulb_wattage = E'23', profile_id = null, " +
									"bulb_life = E'100', description = null, dimmer_control = E'0', " +
									"wattage = E'1', fixture_type = E'0', voltage = E'277', " +
									"no_of_fixtures = E'1', is_hopper = E'0', last_occupancy_seen = E'53465', " +
									"last_connectivity_at = E'" + captureAt+ "', bulb_id = E'1', channel = null, " +
									"commission_status = E'18', current_state = E'Auto', bootloader_version = E'2.3', " +
									"aes_key = null, ballast_last_changed = null, profile_checksum = null, " +
									"use_fx_curve = E't', last_boot_time = E'2014-10-29 06:58:05.125', notes = null, " +
									"curr_app = E'2', push_global_profile = E'f', version_synced = E'0', " +
									"savings_type = null, ballast_last_service_date = null, global_profile_checksum = null, " +
									"state = E'COMMISSIONED', sub_area_id = null, id = E'" + Main.fixtureList.get(mac)+ "', " +
									"active = E't', no_of_bulbs = E'2', current_data_id = null, ballast_type = null, " +
									"reset_reason = E'2', groups_checksum = E'0', type = null, " +
									"bulb_manufacturer = E'Philips', groups_sync_pending = E'f', temperature_offset = null, " +
									"avg_temperature = E'65', last_stats_rcvd_time = E'2014-10-30 06:40:00', " +
									"baseline_power = E'68.20', ip_address = null, last_cmd_sent = null, " +
									"bulbs_last_service_date = null where  id = " + Main.fixtureList.get(mac) +  ";");
				}
			}
			else {
				break;
			}
		}
		Main.setPropertyWithName("ecMaxId", Main.lastComputedECMaxId.toString(), "maxCaptureAt", new Long(Main.lastComputedMaxCaptureAt.getTime()).toString());
		map.put("data", statements);
		map.put("maxWalLogDataId", Main.lastComputedMaxWalId);
	}

}
