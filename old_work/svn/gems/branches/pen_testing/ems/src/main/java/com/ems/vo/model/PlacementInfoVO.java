package com.ems.vo.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="placementInfo")
@XmlAccessorType(XmlAccessType.NONE)
public class PlacementInfoVO {
	@XmlElement(name = "companyname")
	private String companyName;	
	@XmlElement(name = "campusname")
	private String campusName;
	@XmlElement(name = "bldgname")
	private String bldgName;
	@XmlElement(name = "floorname")
	private String floorName;
	@XmlElementWrapper(name = "sensorconfiglist")
	@XmlElement(name = "sensorconfig")
	private List<SensorConfig> sensorConfigList;
	
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	
	public String getCampusName() {
		return campusName;
	}
	public void setCampusName(String campusName) {
		this.campusName = campusName;
	}
	public String getBldgName() {
		return bldgName;
	}
	public void setBldgName(String bldgName) {
		this.bldgName = bldgName;
	}
	public String getFloorName() {
		return floorName;
	}
	public void setFloorName(String floorName) {
		this.floorName = floorName;
	}
	public List<SensorConfig> getSensorConfigList() {
		return sensorConfigList;
	}
	public void setSensorConfigList(List<SensorConfig> sensorConfigList) {
		this.sensorConfigList = sensorConfigList;
	}
}
