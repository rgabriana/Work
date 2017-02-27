package com.emscloud.model.oldem;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmSync {	
	
	static String enlightedString = "enlightedAdmin123##";
	
	@XmlElement(name = "id")
	private Long id;
	
	@XmlElement(name = "macaddress")
	private String macAddress;
	
	@XmlElement(name = "ipaddress")
	private String ipAddress;
	
	@XmlElement(name = "apikey")
	private String apiKey;
	
	@XmlElement(name = "active")
	private Boolean active;
	
	@XmlElement(name = "emid")
	private String emId;
	
	@XmlElement(name = "version")
	private String version;
	
	@XmlElement(name = "timeZone")
	private String timeZone;
	
	@XmlElement(name = "ack")
	private String ACK;
	
	@XmlElement(name = "emname")
	private String emName;
	
	@XmlElement(name = "noOfFloors")
	private String noOfFloors;
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getEmId() {
		return emId;
	}

	public void setEmId(String emId) {
		this.emId = emId;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setACK(String aCK) {
		ACK = aCK;
	}

	public String getACK() {
		return ACK;
	}

	public void setEmName(String emName) {
		this.emName = emName;
	}

	public String getEmName() {
		return emName;
	}

	public void setNoOfFloors(String noOfFloors) {
		this.noOfFloors = noOfFloors;
	}

	public String getNoOfFloors() {
		return noOfFloors;
	}	
}