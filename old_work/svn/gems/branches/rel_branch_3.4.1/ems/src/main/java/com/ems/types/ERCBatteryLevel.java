package com.ems.types;

public enum ERCBatteryLevel {

    Normal,
    Low,
    Critical;
    
    public String getName() {
        return this.toString();
    }
}
