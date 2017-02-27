package com.communicator.uem;

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
		SetOccChangeTrigger,
		RequestDimLevelAndLastConnectivity,
		SetDimLevel,
		
	SuccessAck,
	PayLoad
		
		;
	
	
	public String getName() {
		return this.toString();
	}

}
