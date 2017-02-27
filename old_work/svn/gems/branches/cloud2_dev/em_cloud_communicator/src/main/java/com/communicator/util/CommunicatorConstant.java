package com.communicator.util;

public class CommunicatorConstant {
	
	final public static Long recordsCount = 100000l ;
	final public static Long bulkThreadSleepCount = 5000l ;
	final public static Long errorToleranceCount = 3l ;
	final public static String fileBasePathDir = "/opt/enLighted/communicator/logFileBaseDirPaths.properties" ;
	final public static String configFilePath = "/opt/enLighted/communicator/config.properties" ;
	
	
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

	public static final String dataMigrationService = emInstanceCloudContext+"/services/org/communicate/em/data/migrate";

	public static final String sendSppaDataService = emInstanceCloudContext+"/services/org/communicate/em/data";

	public static final String upgradeService = emInstanceCloudContext+"/services/org/communicate/em/data/upgrade";

	public static final String upgradeStatusService = emInstanceCloudContext+"/services/org/communicate/em/upgradeStatus";

	public static final String getLastWalSyncService = emInstanceCloudContext+"/services/org/communicate/em/lastWalSynced/";
	
	public static final String setMigrationFlagService = emInstanceCloudContext+"/services/org/communicate/em/set/state/migration/";
	
	public static final String getLastIdSyncedService = emInstanceCloudContext+"/services/org/communicate/em/lastmintabledatasynched/";
	
	public static String getReplicaConnectivity = emInstanceCloudContext+"/services/org/communicate/em/check/connectivity/"; ;
	
	//Ecloud Rest Api

	public static final String getReplicaServiceIp = ecloudContext+"/services/org/communicate/em/replicaServerIp/";

	public static final String communicatorInfo = ecloudContext+"/services/org/communicate/em/cloudsyncstatus/v2";
	
	public static final String callHomeService = ecloudContext+"/services/org/communicate/em/callhome/v2";
	
	public static final String downloadImageService = ecloudContext + "/services/org/upgrades/getdebian";
	
	public static final String taskUpdateService = ecloudContext + "/services/org/emtask/update";
	
	public static final String emUpgradeService = "/em_mgmt/services/upgrade/";
	
	public static String setPreviousMigrationFlagService = ecloudContext + "/services/org/replicaserver/set/previous/state/";
	
	public static String uploadLogService = ecloudContext + "/services/org/emtask/upload/log/";
	
	public static final String setMigrationFlagOnEcloudService = ecloudContext+"/services/org/replicaserver/set/state/migration/";
	
	public static final String updateEmBrowsability = ecloudContext + "/services/org/communicate/toggle/em/browsability/";
	
}
