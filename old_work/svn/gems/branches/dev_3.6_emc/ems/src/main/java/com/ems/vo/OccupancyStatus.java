package com.ems.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class OccupancyStatus implements Serializable {
    private static final long serialVersionUID = -417271237512093198L;
   
    @XmlElement(name = "occupancyState")
    private String occupancyState;

	public String getOccupancyState() {
		return occupancyState;
	}

	public void setOccupancyState(String occupancyState) {
		this.occupancyState = occupancyState;
	}
    
   
}