package com.communication.types;

public enum CloudParamType {
	
	//EM Params
	EmTimezone,
	IpAddress,
	NoOfFloors,
	ShareKey,
	
	//Global EM Mode
	CloudMode,
	
	
	//Call Home Stats Params
	StatsId,
	StatsCaptureAt,
	StatsActiveThreadCount,
	StatsGcCount,
	StatsGcTime,
	StatsHeadUsed,
	StatsNonHeapUsed,
	StatsSysLoad,
	StatsCpuPercentage,
	StatsEmAccessible,
	
	
	
	//Status of devices
	GatewayStatus,
	SensorStatus,
	
	//Call 
	ReplicaServerIp,
	EmCloudSyncStatus,
	ApiKey,
	SecretKey,
	EmTasks,
		//Task params only to be used in maps
		TaskId,
		TaskCode,
		TaskStatus,
		TaskProgressStatus,
		TaskParameters,
		TaskAttempts,
	MigrationStatusDetails,
		//Migration Params Only to be used in map
		CurrentMigrationStatus,
		MigrationAttempts,
		
	DownloadImageId,
	
	//Replica Server Params
	ReplicaServerUID,
	
	ReplicaMapAsJson,
	
	LastWalSyncId,
	//Ssh Tunnel parameters
	SshTunnelDetails,
		OpenSshTunnel,
		remoteSshTunnelPort,
	//Tunnel to browsability acess EM
	TunnelDetails,
		OpenTunnel,
		TunnelPort,
	
	//Request Queue Entry For Task Change
	TaskChange,
		
		
		//RequestTypes
		RequestType,
			RequestFacilityTree,
			AddUEMGateway,
			RequestAllSensors,
			RequestSensor,
			RequestFloorPlan,
			SetHB,
			RequestDimLevelAndLastConnectivity,
			SetDimLevel,
			
			
		SuccessAck,
			NotAllowed,
			Success,
			Failure,
		PayLoad,
		Uid;
	
	
	public String getName() {
		return this.toString();
	}

}
