package com.emcloudinstance.util;

public class Constants {
	
	public static int state;
	
	// communication error state
	final public static int state_comm_success				=	100;	
	final public static int state_comm_url_failed			=	101;
	final public static int state_comm_connect_failed		=	102;
	final public static int state_comm_init_failed			=	103;
	final public static int state_comm_send_failed			=	104;
	final public static int state_comm_read_failed			=	105;
	
	//Context 
		public static String ecloudInstContext = "/ecloud" ;
		
	// Rest Api urls 
	//ecloud_instance Rest Api
	public static final String UID_SERVICE = ecloudInstContext + "/services/org/replicaserver/macuid";
	public static final String MAC_DB_CACHE_SERVICE = ecloudInstContext + "/services/org/replicaserver/mac/db/cache";
	public static final String SET_MIGRATION_FLAG = ecloudInstContext+"/services/org/replicaserver/v2/set/state/migration/";
	public static final String SET_MIGRATION_FLAG_LOG = ecloudInstContext+"/services/org/replicaserver/append/migration/state/log/";
	public static final String SET_SYNC_FLAG_LOG = ecloudInstContext+"/services/org/replicaserver/set/sync/state/log/";
	public static final String UPDATE_DEVICE_HEALTH = ecloudInstContext+"/services/org/replicaserver/device/health/";
	public static final String IS_SYNC_PAUSED = ecloudInstContext+"/services/org/replicaserver/isSyncPaused/";
	public static final String GET_LATEST_MIGRATION_STATUS = ecloudInstContext+"/services/org/replicaserver/get/state/migration/";
	public static final String GET_DATA_PULL_REQUEST = ecloudInstContext + "/services/org/replicaserver/getDataPullRequestForReplica";
	public static final String UPDATE_DATA_PULL_REQUEST = ecloudInstContext + "/services/org/replicaserver/updateDataPullRequest/requestId/";
	//ecloud IP
	public static String ECLOUD_IP = "";
	
}
