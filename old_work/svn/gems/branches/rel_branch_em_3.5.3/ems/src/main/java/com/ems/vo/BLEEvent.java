/**
 * 
 */
package com.ems.vo;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author enlighted
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BLEEvent {
	@XmlElement(name = "sensorId")
	private String sensorId;
	@XmlElement(name = "ts")
	private Date ts;
	@XmlElement(name = "version")
	private Short version;
	@XmlElement(name = "scanData")
	private String scanData;
	@XmlElement(name = "tagdata")
	private List<TagData> tagData;
	

	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}

	public Date getTs() {
		return ts;
	}

	public void setTs(Date ts) {
		this.ts = ts;
	}
	
	public Short getVersion() {
		return version;
	}

	public void setVersion(Short version) {
		this.version = version;
	}
	
	public String getScanData() {
		return scanData;
	}

	public void setScanData(String scanData) {
		this.scanData = scanData;
	}

	public List<TagData> getTagData() {
		return tagData;
	}

	public void setTagData(List<TagData> tagData) {
		this.tagData = tagData;
	}
}
