package com.emscloud.constant;

public class ReplicaServerWebserviceUrl {
	
	public static String emInstanceCloudContext = "/em_cloud_instance" ;
	public static final String CREATE_DATABASE = "/ecloud/services/org/communicate/em/lastWalSynced/";
	public static final String GET_GATEWAY_HEALTH_DATA= emInstanceCloudContext+"/services/org/monitor/list/gateway/";
	public static final String GET_FIXTURE_HEALTH_DATA = emInstanceCloudContext+"/services/org/monitor/list/fixture/";
	public static final String GET_DEQUEUE_SYNC_DATA = emInstanceCloudContext+"/services/org/communicate/em/data/dequeue/";
	public static final String GET_DATA_PULL_REQUEST_DATA = emInstanceCloudContext+"/services/org/datapullrequest/getdata/";
	public static final String DATA_PULL_REQUEST_CANCEL_DELETE = emInstanceCloudContext+"/services/org/datapullrequest/canceldelete/";

}
