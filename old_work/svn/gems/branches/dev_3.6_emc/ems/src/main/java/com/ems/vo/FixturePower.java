package com.ems.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement(name = "fixtureEnergy")
@XmlAccessorType(XmlAccessType.NONE)
public class FixturePower implements Serializable {
    private static final long serialVersionUID = -417271237512093198L;
   
    @XmlElement(name = "energy")
    private Double energy;

	public Double getEnergy() {
		return energy;
	}

	public void setEnergy(Double energy) {
		this.energy = energy;
	}
}