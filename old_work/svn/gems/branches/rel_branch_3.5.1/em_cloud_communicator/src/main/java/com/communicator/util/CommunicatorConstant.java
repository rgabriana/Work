package com.communicator.util;

import java.security.MessageDigest;

public class CommunicatorConstant {
	
	final public static Long recordsCount = 100000l ;
	final public static Long bulkThreadSleepCount = 5000l ;
	final public static Long errorToleranceCount = 3l ;
	final public static String fileBasePathDir = "/opt/enLighted/communicator/logFileBaseDirPaths.properties" ;
	final public static String configFilePath = "/opt/enLighted/communicator/config.properties" ;
	
	final public static String syncDataDir = "/opt/enLighted/communicator/syncdata/";
	
	final public static String sysConfigRemigrationRequired = "remigration.required";
	final public static String sysConfigSuccessfulSyncTime = "successful.sync.time";
	
	public static final String CONNECTION_FAILURE = "CONNECTION_FAILURE";
	public static int state;
	
	// communication error state
	final public static int state_comm_success				=	100;	
	final public static int state_comm_url_failed			=	101;
	final public static int state_comm_connect_failed		=	102;
	final public static int state_comm_init_failed			=	103;
	final public static int state_comm_send_failed			=	104;
	final public static int state_comm_read_failed			=	105;
	
	//Context 
		public static String ecloudContext = "/ecloud" ;
		public static String emInstanceCloudContext = "/em_cloud_instance" ;
		
	
	// Rest Api urls 
	//em_cloud_instance Rest Api
	public static final String SyncBulkDataService = emInstanceCloudContext+ "/services/org/communicate/em/tabledata/";
	
	public static final String SyncBulkDataRestrictedRemigrationService = emInstanceCloudContext+ "/services/org/communicate/em/tabledata/restricted/remigration/";

	public static final String dataMigrationService = emInstanceCloudContext+"/services/org/communicate/em/data/migrate";
	
	public static final String dataMigrationServiceV2 = emInstanceCloudContext+"/services/org/communicate/em/data/migrate/v2";
	
	public static final String dataRestrictedReMigrationService = emInstanceCloudContext+"/services/org/communicate/em/data/restricted/remigration";

	public static final String sendSppaDataService = emInstanceCloudContext+"/services/org/communicate/em/data";

	public static final String upgradeService = emInstanceCloudContext+"/services/org/communicate/em/data/upgrade";

	public static final String upgradeStatusService = emInstanceCloudContext+"/services/org/communicate/em/upgradeStatus";

	public static final String getLastWalSyncService = emInstanceCloudContext+"/services/org/communicate/v2/em/lastWalSynced/";
	
	public static final String setMigrationFlagService = emInstanceCloudContext+"/services/org/communicate/em/set/state/migration/";
	
	public static final String getLastIdSyncedService = emInstanceCloudContext+"/services/org/communicate/em/lastmintabledatasynched/";
	
	public static final String getMaxIdSyncedService = emInstanceCloudContext+"/services/org/communicate/em/lastmaxtabledatasynched/";
	
	public static final String getReplicaConnectivity = emInstanceCloudContext+"/services/org/communicate/em/check/connectivity/"; ;
	
	//Ecloud Rest Api

	public static final String getReplicaServiceIp = ecloudContext+"/services/org/communicate/em/replicaServerIp/";

	public static final String communicatorInfo = ecloudContext+"/services/org/communicate/em/cloudsyncstatus/v3";
	
	public static final String callHomeService = ecloudContext+"/services/org/communicate/em/callhome/v4";
	
	public static final String downloadImageService = ecloudContext + "/services/org/upgrades/getdebian";
	
	public static final String taskUpdateService = ecloudContext + "/services/org/emtask/update";
	
	public static final String emUpgradeService = "/em_mgmt/services/upgrade/";
	
	public static final String setPreviousMigrationFlagService = ecloudContext + "/services/org/replicaserver/set/previous/state/";
	
	public static final String uploadLogService = ecloudContext + "/services/org/emtask/upload/log/";
	
	public static final String setMigrationFlagOnEcloudService = ecloudContext+"/services/org/replicaserver/v2/set/state/migration/";
	
	public static final String updateEmBrowsability = ecloudContext + "/services/org/communicate/toggle/em/browsability/";
	
	
	public static final String getUemInfoUrl = "https://localhost/ems/services/uem/getUemInfo";
	
	public static final String getSensorInfo = "https://localhost/ems/services/uem/getSensorInfo/";
	
	public static final String getAllSensors = "https://localhost/ems/services/uem/getAllSensors/";
	
	public static final String getEmFacilityTree = "https://localhost/ems/services/uem/getEmFacilityTreeForUem";
	
	public static final String getFloorPlan = "https://localhost/ems/services/uem/getFloorPlan/";
	
	public static final String setOccChangeTrigger = "https://localhost/ems/services/uem/setOccChangeTrigger/";
	
	public static final String addUpdateUEMGatewayURL = "https://localhost/ems/services/uem/add/";
	
	public static final String setDimLevelURL = "https://localhost/ems/services/org/fixture/setDimLevel/";
	
	public static final String getDimLevelsAndConnectivityURL = "https://localhost/ems/services/uem/getDimLevelsAndLastConnectivity";
	
	public static final String editSystemConfigURL = "https://localhost/ems/services/systemconfig/edit";
	
	public static final String getAllFloorsOfCompany = "https://localhost/ems/services/org/getAllFloorsOfCompany";
	
	public static final int emConnectionTimeout = 60 * 1000;
	
	public static final int uemConnectionTimeout = 300 * 1000;
	
	public static final String  emLoginUrl="https://localhost/ems/wsaction.action" ;
	
	public static String  uemPollUrl= "/uem/services/org/communicate/em/poll" ;
	
	public static String uem_ip = "";
	public static String uem_username = "";
	public static String uem_password = "";
	public static String appVersion = "";
	public static String  loginUrl= "/uem/em.action" ;
	
	final public static String HTTPS = "https://";
	public static String webServiceToPing = "/uem/services/org/communicate/em/uemsyncstatus/";
	
	public static String emSessionId = "";
	public static String sessionId = "";
	
	public static String getPassword() {
		try {
			return getSSLAuthKey(uem_password, uem_username);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getUsername() {
		return uem_username.replaceAll(":", "");
	}
	
	private static String getSSLAuthKey(String authKey, String gwMac) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.reset();
		byte[] hardCodedArr = "EnlightedAuthKey".getBytes();
		md.update(hardCodedArr);
		byte[] keyArr = authKey.getBytes();
		md.update(keyArr);
		byte[] gwMacArr = gwMac.replaceAll(":", "").getBytes();
		md.update(gwMacArr);
		byte[] digest = md.digest();
		return getByteString(digest);
	}
	
	private static String getByteString(byte[] bytes) {
		StringBuffer oBuffer = new StringBuffer();
		int noOfBytes = bytes.length;
		for (int i = 0; i < noOfBytes; i++) {
			oBuffer.append(String.format("%x", bytes[i]));
		}
		return oBuffer.toString();
	}
	
}
