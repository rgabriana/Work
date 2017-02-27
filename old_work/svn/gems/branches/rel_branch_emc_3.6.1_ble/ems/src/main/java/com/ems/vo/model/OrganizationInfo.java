package com.ems.vo.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="organizationInfo")
@XmlAccessorType(XmlAccessType.NONE)
public class OrganizationInfo {
	
	@XmlElement(name = "uid")
	private String uid;	
	@XmlElement(name = "name")
	private String name;
	@XmlElementWrapper(name = "campusList")
	@XmlElement(name = "campusInfo")
	private List<CampusInfo> campusInfoList;
	
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
	
	public List<CampusInfo> getCampusInfoList() {
		return campusInfoList;
	}
	public void setCampusInfoList(List<CampusInfo> campusInfoList) {
		this.campusInfoList = campusInfoList;
	}
	
}
