package com.emscloud.types;

public enum EventType {
	
	Sensor_Heartbeat_Failure;
	
    public String getName() {
        return this.toString().replaceAll("_", " ");
    }

}
