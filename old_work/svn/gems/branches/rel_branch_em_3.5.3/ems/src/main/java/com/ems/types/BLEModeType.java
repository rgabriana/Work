package com.ems.types;

public enum BLEModeType {
    OFF(0),
    SCAN(1),
    ADVERTISE(2),
    INTERLEAVE(3);
    
    private final int value;
    
    BLEModeType(int i) {
		value = i;
	}
	
	public int value() {
        return value;
    }
	
	public String getName() {
		return this.name();
	}
	
    public String getLowerCaseName() {
        return this.toString().toLowerCase();
    }
}
