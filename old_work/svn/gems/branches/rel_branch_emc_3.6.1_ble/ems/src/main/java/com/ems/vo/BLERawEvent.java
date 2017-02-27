package com.ems.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author mark.clark
 *
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.NONE)
public class BLERawEvent implements BLEEvent {
	@XmlElement(name = "sensorId")
	private String sensorId;
	@XmlElement(name = "rawData")
	private String rawData;

	@Override
	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}

	public String getRawData() {
		return rawData;
	}

	public void setRawData(String rawData) {
		this.rawData = rawData;
	}
}
