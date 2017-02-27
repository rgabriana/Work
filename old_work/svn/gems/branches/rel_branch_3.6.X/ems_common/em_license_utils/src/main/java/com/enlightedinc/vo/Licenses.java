package com.enlightedinc.vo;

import java.util.Date;

public class Licenses {

	private String uuid;
	
	private Date timeStamp;
	
	private Em em;
	
	public Em getEm() {
		return em;
	}

	public void setEm(Em em) {
		this.em = em;
	}

	public Bacnet getBacnet() {
		return bacnet;
	}

	public void setBacnet(Bacnet bacnet) {
		this.bacnet = bacnet;
	}

	private Bacnet bacnet;
	
	private Hvac HVAC;
	
	private OccupancySensor occupancySensor;
	
	private ZoneSensors zoneSensors;
	
	public void setHVAC(Hvac hVAC) {
		HVAC = hVAC;
	}

	public Hvac getHVAC() {
		return HVAC;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setZoneSensors(ZoneSensors zoneSensors) {
		this.zoneSensors = zoneSensors;
	}

	public ZoneSensors getZoneSensors() {
		return zoneSensors;
	}

	public void setOccupancySensor(OccupancySensor occupancySensor) {
		this.occupancySensor = occupancySensor;
	}

	public OccupancySensor getOccupancySensor() {
		return occupancySensor;
	}

	
}
