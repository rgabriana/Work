package com.emscloud.types;

public enum EventObjectType {
	
	FACILITY,
	SENSOR,
	ZONE,
	EM;
	
	public String getName() {
		return this.name();
	}

}
