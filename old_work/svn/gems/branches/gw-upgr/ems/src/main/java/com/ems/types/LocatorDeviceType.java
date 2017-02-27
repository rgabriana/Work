package com.ems.types;

public enum LocatorDeviceType {
    Energy_manager,
	Unmanaged_fixture,
	Unmanaged_emergency_fixture;
	

    public String getName() {
        return this.name().replaceAll("_", " ");
    }
}
