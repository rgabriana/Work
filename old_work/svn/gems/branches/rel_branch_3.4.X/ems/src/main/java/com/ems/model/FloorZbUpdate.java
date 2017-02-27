package com.ems.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FloorZbUpdate {
	
	Long id ;
	Long floorId ;
	Date startTime;
	Date endTime ;
	Long processedState ;
	
	@XmlElement(name = "id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@XmlElement(name = "floorId")
	public Long getFloorId() {
		return floorId;
	}
	public void setFloorId(Long floorId) {
		this.floorId = floorId;
	}
	@XmlElement(name = "startTime")
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	@XmlElement(name = "endTime")
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	@XmlElement(name = "processedState")
	public Long getProcessedState() {
		return processedState;
	}
	public void setProcessedState(Long processedState) {
		this.processedState = processedState;
	}

}
