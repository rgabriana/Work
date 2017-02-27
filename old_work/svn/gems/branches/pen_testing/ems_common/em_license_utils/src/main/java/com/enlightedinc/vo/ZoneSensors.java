package com.enlightedinc.vo;

import java.util.List;

public class ZoneSensors {
	private Boolean enabled;
	
	private List<ZoneSensorsLicenseInstance> zoneSensorsLicenseInstanceList;
	
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setZoneSensorsLicenseInstanceList(
			List<ZoneSensorsLicenseInstance> zoneSensorsLicenseInstanceList) {
		this.zoneSensorsLicenseInstanceList = zoneSensorsLicenseInstanceList;
	}

	public List<ZoneSensorsLicenseInstance> getZoneSensorsLicenseInstanceList() {
		return zoneSensorsLicenseInstanceList;
	}
	
}
