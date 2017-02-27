package com.emscloud.types;

public enum SiteAnomalyType {
	BurnHour,     
	BaselineLoad,
	Profile,
	Connectivity,
	BlockTermRemaining,
	Consumption;

	public String getName() {
		return this.toString();
	}
}
