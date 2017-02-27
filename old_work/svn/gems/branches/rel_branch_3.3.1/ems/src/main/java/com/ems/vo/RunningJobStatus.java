package com.ems.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class RunningJobStatus implements Serializable {
    private static final long serialVersionUID = -417271237512093198L;
    @XmlElement(name = "drStatus")
    DRStatus drStatus;

	/**
	 * @return the drStatus
	 */
	public DRStatus getDrStatus() {
		return drStatus;
	}

	/**
	 * @param drStatus the drStatus to set
	 */
	public void setDrStatus(DRStatus drStatus) {
		this.drStatus = drStatus;
	}
}