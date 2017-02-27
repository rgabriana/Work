package com.ems.vo.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="campusInfo")
@XmlAccessorType(XmlAccessType.NONE)
public class BuildingInfo {
	
	@XmlElement(name = "uid")
	private String uid;	
	@XmlElement(name = "name")
	private String name;
	@XmlElement(name = "latitude")
	private Float latitude;	
	@XmlElement(name = "longitude")
	private Float longitude;
	@XmlElementWrapper(name = "floorList")
	@XmlElement(name = "floorInfo")
	private List<FloorInfo> floorInfoList;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public Float getLatitude() {
		return latitude;
	}
	public void setLatitude(Float latitude) {
		this.latitude = latitude;
	}
	
	public Float getLongitude() {
		return longitude;
	}
	public void setLongitude(Float longitude) {
		this.longitude = longitude;
	}
	
	public List<FloorInfo> getFloorInfoList() {
		return floorInfoList;
	}
	public void setFloorInfoList(List<FloorInfo> floorInfoList) {
		this.floorInfoList = floorInfoList;
	}
	
}
