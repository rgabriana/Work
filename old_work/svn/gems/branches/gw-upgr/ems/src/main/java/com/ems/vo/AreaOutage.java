package com.ems.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class AreaOutage implements Serializable {
    private static final long serialVersionUID = -417271237512093198L;
   
    @XmlElement(name = "totalSensors")
    private long totalSensors = 0;
    
    @XmlElement(name = "OutSensors")
    private long OutSensors = 0;

	public long getTotalSensors() {
		return totalSensors;
	}

	public void setTotalSensors(long totalSensors) {
		this.totalSensors = totalSensors;
	}

	public long getOutSensors() {
		return OutSensors;
	}

	public void setOutSensors(long outSensors) {
		OutSensors = outSensors;
	}

}