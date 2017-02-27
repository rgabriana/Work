package com.ems.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class TagData {
	@XmlElement(name = "tagid")
	private String tagId;
	@XmlElement(name = "rssi")
	private String rssi;
	@XmlElement(name= "accelerometer")
	private String accelerometer;
	
	public TagData() {
	}

	public String getTagId() {
		return tagId;
	}

	public void setTagId(String tagId) {
		this.tagId = tagId;
	}

	public String getRssi() {
		return rssi;
	}

	public void setRssi(String rssi) {
		this.rssi = rssi;
	}
	
	public String getAccelerometer() {
		return accelerometer;
	}

	public void setAccelerometer(String accelerometer) {
		this.accelerometer = accelerometer;
	}
	
}