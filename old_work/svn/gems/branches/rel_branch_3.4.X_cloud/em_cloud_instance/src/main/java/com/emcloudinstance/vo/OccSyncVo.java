package com.emcloudinstance.vo;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class OccSyncVo {

    private Integer levelId = 0;
    private Integer groupId = 0;
    private Date captureAt;
    private Integer noOfSensors = 0;
    private Integer totalSensors = 0;
    private Integer noOf1bits = 0;
    private Integer totalBits = 0;
    private Integer tempCount = 0;

    public Integer getTempCount() {
        return tempCount;
    }

    public void setTempCount(Integer tempCount) {
        this.tempCount = tempCount;
    }

    @XmlElement(name = "noOfSensors")
    public Integer getNoOfSensors() {
        return noOfSensors;
    }

    public void setNoOfSensors(Integer noOfSensors) {
        this.noOfSensors = noOfSensors;
    }

    @XmlElement(name = "totalSensors")
    public Integer getTotalSensors() {
        return totalSensors;
    }

    public void setTotalSensors(Integer totalSensors) {
        this.totalSensors = totalSensors;
    }

    @XmlElement(name = "noOf1bits")
    public Integer getNoOf1bits() {
        return noOf1bits;
    }

    public void setNoOf1bits(Integer noOf1bits) {
        this.noOf1bits = noOf1bits;
    }

    @XmlElement(name = "totalBits")
    public Integer getTotalBits() {
        return totalBits;
    }

    public void setTotalBits(Integer totalBits) {
        this.totalBits = totalBits;
    }

    @XmlElement(name = "captureAt")
    public Date getCaptureAt() {
        return captureAt;
    }

    @XmlElement(name = "groupId")
    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public void setCaptureAt(Date captureAt) {
        this.captureAt = captureAt;
    }

    @XmlElement(name = "levelId")
    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

}
