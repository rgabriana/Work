package com.communication.types;

public enum CloudParamType {
	
	//EM Params
	EmTimezone,
	
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
	
	//Call Home response params
	ReplicaServerIp,
	MigrationStatus,
	EmCloudSyncStatus,
	EmTasks,
	
	//Replica Server Params
	ReplicaServerUID,
	
	
	ReplicaMapAsJson,
	
	LastWalSyncId;
	
	public String getName() {
		return this.toString();
	}

}
