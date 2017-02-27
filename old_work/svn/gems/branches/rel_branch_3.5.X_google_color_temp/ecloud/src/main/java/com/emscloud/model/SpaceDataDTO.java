package com.emscloud.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SpaceDataDTO {
    @XmlElement(name = "spaceMaster")
    public OccupancyMasterDTO spaceMaster;
    @XmlElement(name = "sensors")
    public Long sensors = 0l;
    @XmlElement(name = "totalSqFt")
    public Long totalSqFt = 0l;
    @XmlElement(name = "value")
    public Long value = 0l;
    @XmlElement(name = "totalSensors")
    public Long totalSensors = 0l;

    public Long getTotalSensors() {
        return totalSensors;
    }

    public void setTotalSensors(Long totalSensors) {
        this.totalSensors = totalSensors;
    }

    public OccupancyMasterDTO getSpaceMaster() {
        return spaceMaster;
    }

    public void setSpaceMaster(OccupancyMasterDTO spaceMaster) {
        this.spaceMaster = spaceMaster;
    }

    public Long getSensors() {
        return sensors;
    }

    public void setSensors(Long sensors) {
        this.sensors = sensors;
    }

    public Long getTotalSqFt() {
        return totalSqFt;
    }

    public void setTotalSqFt(Long totalSqFt) {
        this.totalSqFt = totalSqFt;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

}
