package com.ems.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement(name = "plugloadEnergy")
@XmlAccessorType(XmlAccessType.NONE)
public class PlugloadPower implements Serializable {
    private static final long serialVersionUID = -417271237512093198L;
   
    @XmlElement(name = "managed-energy")
    private Double managedEnergy;

    @XmlElement(name = "unmanaged-energy")
    private Double unmanagedEnergy;

	public Double getManagedEnergy() {
		return managedEnergy;
	}

	public void setManagedEnergy(Double managedEnergy) {
		this.managedEnergy = managedEnergy;
	}

	public Double getUnmanagedEnergy() {
		return unmanagedEnergy;
	}

	public void setUnmanagedEnergy(Double unmanagedEnergy) {
		this.unmanagedEnergy = unmanagedEnergy;
	}

    
}