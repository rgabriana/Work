package com.emscloud.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SpaceOccChartData implements Serializable {

    private static final long serialVersionUID = -6644153185147549999L;

    public SpaceOccChartData(final Long groupId, final Long avgNoOfSensors, final Long totalNoOfSensors,
            final String profileName, final Long total1bits, final Long totalBits) {
        this.avgNoOfSensors = avgNoOfSensors;
        this.totalNoOfSensors = totalNoOfSensors;
        this.profileName = profileName;
        this.total1bits = total1bits;
        this.totalBits = totalBits;
        this.groupId = groupId;
    }

    private Long groupId;

    private Long avgNoOfSensors;
    private Long totalNoOfSensors;
    private String profileName;
    private Long total1bits;
    private Long totalBits;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getAvgNoOfSensors() {
        return avgNoOfSensors;
    }

    public void setAvgNoOfSensors(Long avgNoOfSensors) {
        this.avgNoOfSensors = avgNoOfSensors;
    }

    public Long getTotalNoOfSensors() {
        return totalNoOfSensors;
    }

    public void setTotalNoOfSensors(Long totalNoOfSensors) {
        this.totalNoOfSensors = totalNoOfSensors;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public Long getTotal1bits() {
        return total1bits;
    }

    public void setTotal1bits(Long total1bits) {
        this.total1bits = total1bits;
    }

    public Long getTotalBits() {
        return totalBits;
    }

    public void setTotalBits(Long totalBits) {
        this.totalBits = totalBits;
    }

}
