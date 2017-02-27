package com.ems.vo.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SensorEnergyStats implements Serializable{

	@XmlElement(name="intervalStart")
	private Date captureAt;
	
	public Date getCaptureAt() {
		return captureAt;
	}

	public void setCaptureAt(Date captureAt) {
		this.captureAt = captureAt;
	}
	
	@XmlElement(name="sensor")
	private List<com.ems.model.Sensor> list;

	public List<com.ems.model.Sensor> getList() {
		if(list == null){
			System.out.println("============ list is null");
			list = new ArrayList<com.ems.model.Sensor>();
		}
		return list;
	}

	public void setList(List<com.ems.model.Sensor> list) {
		this.list = list;
	}

	

	



}
