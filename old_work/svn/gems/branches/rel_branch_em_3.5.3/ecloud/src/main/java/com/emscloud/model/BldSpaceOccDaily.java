package com.emscloud.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "building_space_occupancy_daily", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BldSpaceOccDaily extends AggregatedSpaceOccData {

    private static final long serialVersionUID = -6644153185147549999L;

    @Column(name = "avg_no_of_sensors")
    private Long avgNoOfSensors;

    @Column(name = "avg_total_sensors")
    private Long avgTotalSensors;

    @Column(name = "total_no_of_1bits")
    private Long totalNoOf1Bits;

    @Column(name = "total_total_bits")
    private Long totalTotalBits;

    public Long getAvgNoOfSensors() {
        return avgNoOfSensors;
    }

    public void setAvgNoOfSensors(Long avgNoOfSensors) {
        this.avgNoOfSensors = avgNoOfSensors;
    }

    public Long getAvgTotalSensors() {
        return avgTotalSensors;
    }

    public void setAvgTotalSensors(Long avgTotalSensors) {
        this.avgTotalSensors = avgTotalSensors;
    }

    public Long getTotalNoOf1Bits() {
        return totalNoOf1Bits;
    }

    public void setTotalNoOf1Bits(Long totalNoOf1Bits) {
        this.totalNoOf1Bits = totalNoOf1Bits;
    }

    public Long getTotalTotalBits() {
        return totalTotalBits;
    }

    public void setTotalTotalBits(Long totalTotalBits) {
        this.totalTotalBits = totalTotalBits;
    }

}