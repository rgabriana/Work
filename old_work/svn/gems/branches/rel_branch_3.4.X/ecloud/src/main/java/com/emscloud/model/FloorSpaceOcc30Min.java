package com.emscloud.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "floor_space_occupancy_30min", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FloorSpaceOcc30Min extends AggregatedSpaceOccData {

    private static final long serialVersionUID = -6644153185147549999L;

    @Column(name = "no_of_sensors")
    private Long noOfSensors = 0l;

    @Column(name = "total_sensors")
    private Long totalSensors = 0l;

    @Column(name = "no_of_1bits")
    private Long noOf1Bits = 0l;

    @Column(name = "total_bits")
    private Long totalBits = 0l;

    public Long getNoOfSensors() {
        return noOfSensors;
    }

    public void setNoOfSensors(Long noOfSensors) {
        this.noOfSensors = noOfSensors;
    }

    public Long getTotalSensors() {
        return totalSensors;
    }

    public void setTotalSensors(Long totalSensors) {
        this.totalSensors = totalSensors;
    }

    public Long getNoOf1Bits() {
        return noOf1Bits;
    }

    public void setNoOf1Bits(Long noOf1Bits) {
        this.noOf1Bits = noOf1Bits;
    }

    public Long getTotalBits() {
        return totalBits;
    }

    public void setTotalBits(Long totalBits) {
        this.totalBits = totalBits;
    }

    @Override
    public String toString() {
        return super.toString() + "noOfSensors:" + noOfSensors + "totalSensors:" + totalSensors + "noOf1Bits:"
                + noOf1Bits + "totalBits:" + totalBits;
    }

}
