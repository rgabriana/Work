package com.ems.vo.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="campusInfo")
@XmlAccessorType(XmlAccessType.NONE)
public class CampusInfo {
	
	@XmlElement(name = "uid")
	private String uid;	
	@XmlElement(name = "name")
	private String name;
	@XmlElement(name = "location")
	private String location;	
	@XmlElement(name = "zipCode")
	private String zipCode;
	@XmlElementWrapper(name = "buildingList")
	@XmlElement(name = "buildingInfo")
	private List<BuildingInfo> buildingInfoList;
	
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
	
	public String getLocation() {
		return location;
	}
	public void setLcation(String location) {
		this.location = location;
	}
	
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	
	public List<BuildingInfo> getBuildingInfoList() {
		return buildingInfoList;
	}
	public void setFloorInfoList(List<BuildingInfo> buildingInfoList) {
		this.buildingInfoList = buildingInfoList;
	}
	
}
