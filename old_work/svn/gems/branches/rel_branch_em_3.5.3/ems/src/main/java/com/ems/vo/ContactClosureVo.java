package com.ems.vo;

import java.net.InetAddress;
import java.util.List;

public class ContactClosureVo {
	
	private String ipAddress;
	
	private String macAddress;
	
	private String productId;
	
	private String hwType;
	
	private String fwVersion;
	
	private List<ContactClosureControls> contactClosureControlsList;
	

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getProductId() {
		return productId;
	}

	public void setHwType(String hwType) {
		this.hwType = hwType;
	}

	public String getHwType() {
		return hwType;
	}

	public void setFwVersion(String fwVersion) {
		this.fwVersion = fwVersion;
	}

	public String getFwVersion() {
		return fwVersion;
	}

	public void setContactClosureControlsList(
			List<ContactClosureControls> contactClosureControlsList) {
		this.contactClosureControlsList = contactClosureControlsList;
	}

	public List<ContactClosureControls> getContactClosureControlsList() {
		return contactClosureControlsList;
	}
}
