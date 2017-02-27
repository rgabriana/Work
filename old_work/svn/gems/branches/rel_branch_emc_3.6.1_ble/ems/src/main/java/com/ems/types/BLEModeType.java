package com.ems.types;

public enum BLEModeType {
    OFF((byte)0),
    SCAN((byte)1),
    ADVERTISE((byte)2),
    BEACON((byte)3),
    SCAN_RAW((byte)4),
    SCAN_NOCEAN((byte)6);

    private final byte value;

    BLEModeType(byte i) {
		value = i;
	}

	public byte value() {
        return value;
    }

	public String getName() {
		return this.name();
	}

    public String getLowerCaseName() {
        return this.toString().toLowerCase();
    }
}
