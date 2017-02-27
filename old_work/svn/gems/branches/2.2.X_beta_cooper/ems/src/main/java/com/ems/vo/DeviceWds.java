package com.ems.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DeviceWds implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@XmlElement(name = "sno")
	Integer number;
	
	@XmlElement(name = "wdsName")
	String wdsName;
	
	@XmlElement(name="wdsId")
	Long wdsId;
	
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

	public String getWdsName() {
		return wdsName;
	}

	public void setWdsName(String wdsName) {
		this.wdsName = wdsName;
	}

	public Long getWdsId() {
		return wdsId;
	}

	public void setWdsId(Long wdsId) {
		this.wdsId = wdsId;
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
	
}
