package com.ems.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DevicePlugload implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@XmlElement(name = "sno")
	Integer number;
	
	@XmlElement(name = "plugloadName")
	String plugloadName;
	
	@XmlElement(name="plugloadId")
	Long plugloadId;
	
	@XmlElement(name = "status")
	String status;

	@XmlElement(name = "version")
	String version;

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPlugloadName() {
		return plugloadName;
	}

	public void setPlugloadName(String plugloadName) {
		this.plugloadName = plugloadName;
	}

	public Long getPlugloadId() {
		return plugloadId;
	}

	public void setPlugloadId(Long plugloadId) {
		this.plugloadId = plugloadId;
	}
}
