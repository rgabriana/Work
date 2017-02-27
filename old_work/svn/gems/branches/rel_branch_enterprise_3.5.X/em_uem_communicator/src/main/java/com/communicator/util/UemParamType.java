package com.communicator.util;

public enum UemParamType {
	
	//EM Params
	EmTimezone,
	
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
	PayLoad
		
		;
	
	
	public String getName() {
		return this.toString();
	}

}
