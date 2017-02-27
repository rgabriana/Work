package com.enlightedinc.vo;

import java.util.List;

public class Em {
	private Boolean enabled;
	
	private List<EmLicenseInstance> emLicenseInstanceList;

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEmLicenseInstanceList(List<EmLicenseInstance> emLicenseInstanceList) {
		this.emLicenseInstanceList = emLicenseInstanceList;
	}

	public List<EmLicenseInstance> getEmLicenseInstanceList() {
		return emLicenseInstanceList;
	}
}
