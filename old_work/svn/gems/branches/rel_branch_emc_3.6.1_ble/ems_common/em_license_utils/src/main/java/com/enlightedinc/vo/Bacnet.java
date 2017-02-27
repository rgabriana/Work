package com.enlightedinc.vo;

import java.util.List;

public class Bacnet {
	private Boolean enabled;
	
	private List<BacnetLicenseInstance> bacnetLicenseInstanceList;
	
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setBacnetLicenseInstanceList(
			List<BacnetLicenseInstance> bacnetLicenseInstanceList) {
		this.bacnetLicenseInstanceList = bacnetLicenseInstanceList;
	}

	public List<BacnetLicenseInstance> getBacnetLicenseInstanceList() {
		return bacnetLicenseInstanceList;
	}
}
