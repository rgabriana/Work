package com.ems.vo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ContactClosureVo {
	
	@XmlElement(name = "ipAddress")
	private String ipAddress;
	
	@XmlElement(name = "macAddress")
	private String macAddress;
	
	@XmlElement(name = "productId")
	private String productId;
	
	@XmlElement(name = "hwType")
	private String hwType;
	
	@XmlElement(name = "fwVersion")
	private String fwVersion;
	
	@XmlElement(name = "contactClosureControlsList")
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
