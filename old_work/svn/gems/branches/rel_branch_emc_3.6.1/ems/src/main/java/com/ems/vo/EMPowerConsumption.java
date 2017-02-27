package com.ems.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement(name = "emEnergyConsumption")
@XmlAccessorType(XmlAccessType.NONE)
public class EMPowerConsumption implements Serializable {
    private static final long serialVersionUID = -417271237512093198L;
   
    @XmlElement(name = "energy-lighting")
    private Double energyUsedLighting;
    
    @XmlElement(name = "energy-plugload")
    private Double energyUsedPlugload;

	public Double getEenergyUsedLighting() {
		return energyUsedLighting;
	}

	public void setEnergyUsedLighting(Double energyUsedLighting) {
		this.energyUsedLighting = energyUsedLighting;
	}

	public Double getEnergyUsedPlugload() {
		return energyUsedPlugload;
	}

	public void setEnergyUsedPlugload(Double energyUsedPlugload) {
		this.energyUsedPlugload = energyUsedPlugload;
	}
}