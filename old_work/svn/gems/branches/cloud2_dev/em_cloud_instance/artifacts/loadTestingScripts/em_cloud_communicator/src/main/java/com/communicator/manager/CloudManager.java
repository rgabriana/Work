package com.communicator.manager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.communicator.dao.EnergySyncUpDao;
import com.communicator.dao.SystemConfigDao;
import com.communicator.dao.WALDao;
import com.communicator.util.CommunicatorConstant;
@Service("cloudManager")
public class CloudManager {

	
	static final Logger logger = Logger.getLogger(CloudManager.class
			.getName());
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
	ReplicaServerInfoManager  replicaServerInfoManager ;
	public long getLastWalSyncId() {
		long lastWalSyncId = -100;

		CloudHttpResponse response = securecloudConnectionTemplate
				.executeGet(CommunicatorConstant.getLastWalSyncService + serverInfoManager.getMacAddress() ,serverInfoManager.getReplicaServerIP());

		if (response != null) {
			lastWalSyncId = Long.parseLong(response.getResponse());
			logger.info("Last Wal Sync ID is " + lastWalSyncId);
		}
		return lastWalSyncId;

	}
	
	public  void sendDataSPPA() {

		logger.info("Starting new sync event at " + new Date());

		Long lastWalLogDataId = null;

		try {
			lastWalLogDataId = getLastWalSyncId();
			if (lastWalLogDataId != -100) {

				ByteArrayOutputStream baos_other = new ByteArrayOutputStream();
				ZipOutputStream zipOtherOut = new ZipOutputStream(baos_other);
				
				Map<String, Object> map = new HashMap<String, Object>();
				Map<String, Object> tempMap = new HashMap<String, Object>();

				try {

					MultipartEntity parts = new MultipartEntity();
					
					walDao.fillWALChangesAndWalId(map,lastWalLogDataId);
					tempMap=map ;
					String macId = serverInfoManager.getMacAddress() ;
					String  appVersion = serverInfoManager.getAppVersion() ;
					
					String[] macList = {"08:00:27:15:f0:fe","00:60:2F:4A:B5:0F","00:60:2F:06:E4:1D","00:60:2F:5D:4A:6D","00:60:2F:E0:C4:78","00:60:2F:BF:4D:71","00:60:2F:AF:B7:DC","00:60:2F:29:A1:B2","00:60:2F:C7:93:4C","00:60:2F:C4:CD:11","00:60:2F:84:CF:1C","00:60:2F:C3:B6:49","00:60:2F:CA:D9:6F","00:60:2F:83:36:E9","00:60:2F:CF:22:CD","00:60:2F:60:25:32","00:60:2F:F2:24:26","00:60:2F:A7:F7:3D","00:60:2F:3E:60:6D","00:60:2F:54:CD:50","00:60:2F:54:D8:3E","00:60:2F:4F:88:92","00:60:2F:D9:F2:3E","00:60:2F:A6:ED:39","00:60:2F:59:1A:35","00:60:2F:83:9A:26","00:60:2F:E2:8C:D0","00:60:2F:C0:15:2A","00:60:2F:EC:6D:F1","00:60:2F:42:E5:3A","00:60:2F:D3:72:C2","00:60:2F:9D:BD:C3","00:60:2F:01:44:A4","00:60:2F:30:72:2F","00:60:2F:0D:E3:3B","00:60:2F:00:93:C2","00:60:2F:44:03:21","00:60:2F:05:AD:69","00:60:2F:8D:C5:53","00:60:2F:5B:3C:43","00:60:2F:3A:0E:A7","00:60:2F:77:4E:F0","00:60:2F:CA:38:62","00:60:2F:35:B9:F5","00:60:2F:5E:F3:48","00:60:2F:AE:74:3F","00:60:2F:33:A9:71","00:60:2F:84:2F:D9","00:60:2F:90:64:BB","00:60:2F:A9:3D:F3","00:60:2F:FC:A0:C0","00:60:2F:8D:F3:12","00:60:2F:6D:C0:76"};
					int count = 0 ;
					while(count<50){
						map = tempMap ;
						baos_other = new ByteArrayOutputStream();
						zipOtherOut = new ZipOutputStream(baos_other);
						map.put("macId", macList[count++]) ;
						map.put("version", appVersion);
					
					zipOtherOut.putNextEntry(new ZipEntry("sql_statements"));
					ObjectOutputStream outObj = new ObjectOutputStream(zipOtherOut);

					try {
						outObj.writeObject(map);
						zipOtherOut.closeEntry();
					} catch (Exception e) {
						logger.error( e.toString(), e);
					} finally {
						if (outObj != null) {
							try {
								outObj.close();
							} catch (Exception e) {
								logger.error( e.toString(), e);
							}
						}
					}
					logger.info("Compressed  data size = "
							+ baos_other.toByteArray().length);
					
					ByteArrayBody bytearray = new ByteArrayBody(baos_other.toByteArray(), "other");
					parts = new MultipartEntity();
					parts.addPart("other", bytearray);
					

					
					if(map.get("nextAction") != null && "UPGRADE".equals(map.get("nextAction").toString())) {

						CloudHttpResponse response = securecloudConnectionTemplate.executePost(CommunicatorConstant.upgradeStatusService, parts, serverInfoManager.getReplicaServerIP());					
						String upgradeStatus = new String(response.getResponse());
	
						System.out.println("Upgrade Status = " + upgradeStatus);
						
						if("N".equals(upgradeStatus)) {
							byte[] dumpcontents = null ;
							File dumpFile = new File(map.get("upgradeFilePath").toString());
							map.put("filename", dumpFile.getName());
							 try {
								 dumpcontents = FileUtils.readFileToByteArray(dumpFile);
							} catch (IOException e1) {
								e1.printStackTrace();
							}		 
							map.put("dump", dumpcontents);
							
							ByteArrayOutputStream baos_upgrade = new ByteArrayOutputStream();
							ZipOutputStream zipUpgradeOut = new ZipOutputStream(baos_upgrade);
							
							ObjectOutputStream outUpgradeObject = null ;
							try {
								
								zipUpgradeOut.putNextEntry(new ZipEntry("other"));
								outUpgradeObject = new ObjectOutputStream(zipUpgradeOut);
								outUpgradeObject.writeObject(map);
								zipUpgradeOut.closeEntry();
								
								logger.info("Compressed  data size = " + baos_upgrade.toByteArray().length );

								ByteArrayBody upgradebytearray = new ByteArrayBody(baos_upgrade.toByteArray(), "other");
								parts = new MultipartEntity();
								parts.addPart("other", upgradebytearray);

								
								securecloudConnectionTemplate.executePost(CommunicatorConstant.upgradeService, parts, serverInfoManager.getReplicaServerIP());

							}
							catch (Exception e) {
								logger.error( e.toString(), e);
							}
							finally {
								if(outUpgradeObject != null) {
									try {
										outUpgradeObject.close();
									} catch (Exception e) {
										logger.error(  e.toString(), e);
									}
									
								}
								if(zipUpgradeOut != null) {
									try {
										zipUpgradeOut.close();
									} catch (Exception e) {
										logger.error(  e.toString(), e);
									}
								}
								if(baos_upgrade != null) {
									try {
										baos_upgrade.close();
									} catch (Exception e) {
										logger.error(  e.toString(), e);
									}
								}
							}
						}
					}
					else {

						CloudHttpResponse synchResponse = securecloudConnectionTemplate.executePost(CommunicatorConstant.sendSppaDataService, parts , serverInfoManager.getReplicaServerIP());
						logger.info("status " + synchResponse.getStatus());
						if (synchResponse.getStatus() == 200) {

							logger.info("Wal log sync successful.");
						} else {
							logger.info("WAL LOG SYNC FAILED!!!!!");
						}	
					}
					logger.info(count +"="+macList[count]) ;
					bytearray = null ;
					parts=null;
					if (zipOtherOut != null) {

						try {
							zipOtherOut.close();
						} catch (Exception e) {
							logger.error(  e.toString(), e);
						}
					}
					if (baos_other != null) {
						try {
							baos_other.close();
						} catch (Exception e) {
							logger.error(  e.toString(), e);
						}
					}
					Thread.sleep(500);
					}
				}  catch (Exception e) {
					logger.error(  e.toString(), e);
				} finally {
					
					if (zipOtherOut != null) {
						try {
							zipOtherOut.close();
						} catch (Exception e) {
							logger.error(  e.toString(), e);
						}
					}
					if (baos_other != null) {
						try {
							baos_other.close();
						} catch (Exception e) {
							logger.error(  e.toString(), e);
						}
					}
				}
				
			} else {
				logger.info("Could not connect to cloud server.");
			}
		} catch (Exception e) {
			logger.error(  e.toString(), e);
		}

	}
	



	
	/**
	 * This function only migrate non energy data. Energy data is migrated using SyncUpEnergyData() function and
	 *  is run in parallel until the task is finished.
	 *  
	 *  returns true if everything is as expected. 
	 */
	public  boolean doDataMigration() {
		// Stop tomcat server before running migration 
		logger.info("Shutting down tomcat server for migration purpose" );
		serverInfoManager.startStopTomcatServer("stop") ;
		if(doBeforeMigrationcleanUp()&&!serverInfoManager.istomcatServerRunning()){
			Boolean status =false ;
			ByteArrayOutputStream baos_other = new ByteArrayOutputStream();
			ZipOutputStream zipOtherOut = new ZipOutputStream(baos_other);	
		
			try {
					//clean up if files already exist
					File temp1 = new File("/home/enlighted/clouddata/sppa_migration.sql") ;
					if(temp1.exists())
					{
						temp1.delete() ;
					}
					temp1 = new File("/home/enlighted/clouddata/sppa_migration_energy.sql") ;
					if(temp1.exists())
					{
						temp1.delete() ;
					}
					// create dumps
					Runtime rt = Runtime.getRuntime();
					Process proc;
						proc = rt.exec(new String[]{"/bin/bash", "/opt/enLighted/communicator/generateMigrationDump.sh"});
						proc.waitFor() ;
					// Add triggers for wal log entry.
					systemConfigDao.addWalLogTriggers() ;
			} catch (InterruptedException e) {
				
				logger.error(  e.toString(), e);
				
				
			} catch (IOException e) {
				
				logger.error(  e.toString(), e);
			}
			catch (Exception e) {
				
				logger.error(  e.toString(), e);
			} finally
			{
				logger.info("Migration related task on EM done." );
				logger.info("Starting tomcat server again" );
				serverInfoManager.startStopTomcatServer("start") ;
				if(!serverInfoManager.istomcatServerRunning())
				{
					logger.info("Starting tomcat server failed for some reason. Retrying again. ");
					serverInfoManager.startStopTomcatServer("start") ;
					logger.info("Status" + serverInfoManager.istomcatServerRunning()) ;
				}
			}
			logger.info("Sending Migration dumps to cloud" );
			// send data
			byte[] dumpcontents = null ;
			byte[] energyContents = null ;
			File dumpFile = new File("/home/enlighted/clouddata/sppa_migration.sql") ;
			File energyFile = new File("/home/enlighted/clouddata/sppa_migration_energy.sql") ;
			 try {
				 dumpcontents = FileUtils.readFileToByteArray(dumpFile);
				 energyContents = FileUtils.readFileToByteArray(energyFile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}		 
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("macId", serverInfoManager.getMacAddress());
			map.put("version", serverInfoManager.getAppVersion());
			map.put("dump", dumpcontents);
			map.put("energy", energyContents);
			ObjectOutputStream outObj = null ;
			try {
				
				zipOtherOut.putNextEntry(new ZipEntry("emInfo"));
				 outObj = new ObjectOutputStream(zipOtherOut);
				outObj.writeObject(map);
				zipOtherOut.closeEntry();
				
			}
			catch (Exception e) {
				systemConfigDao.removeWalLogTriggers() ;
				logger.error(  e.toString(), e);
			}
			finally {
				
				if(outObj != null) {
					try {
						outObj.close();
					} catch (Exception e) {
						logger.error(  e.toString(), e);
					}
					
				}
			}
			
			try {
				logger.info("Compressed  data size = " + baos_other.toByteArray().length );
		
				MultipartEntity parts = new MultipartEntity();
				ByteArrayBody bytearray = new ByteArrayBody(baos_other.toByteArray(), "other");
				parts.addPart("other", bytearray);
				CloudHttpResponse response = securecloudConnectionTemplate.executePost(CommunicatorConstant.dataMigrationService, parts, serverInfoManager.getReplicaServerIP());

				if(response.getStatus() == 200) {
					logger.info("Migration successful.");		
					status = true ;
				}
				else {
					logger.info("Migration FAILED!!!!! Removing wal Triggers. Status Returned :-" + response.getStatus());
					systemConfigDao.removeWalLogTriggers() ;
					status = false ;
				}
				
			}	
			catch (Exception e) {
				systemConfigDao.removeWalLogTriggers() ;
				logger.error(  e.toString(), e);

			}
			return status ;
		}
		else
		{
			logger.info("Migration FAILED!!!!! due to error while clean up or tomcat server was not down. Tomcat server status"+ serverInfoManager.istomcatServerRunning() +"  Contact Admin");
			serverInfoManager.startStopTomcatServer("start") ;
			if(!serverInfoManager.istomcatServerRunning())
			{
				logger.info("Starting tomcat server failed for some reason. Retrying again. ");
				serverInfoManager.startStopTomcatServer("start") ;
				logger.info("Status" + serverInfoManager.istomcatServerRunning()) ;
			}
			return false ;		
		}

}

	private boolean doBeforeMigrationcleanUp() {
		// TODO Auto-generated method stub
		return systemConfigDao.doBeforeMigrationcleanUp();
	}

	
}
