package com.emscloud.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@IdClass(SpaceOccCK.class)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@XmlAccessorType(XmlAccessType.NONE)
public class AggregatedSpaceOccData implements Serializable {
    private static final long serialVersionUID = -6644153185147549999L;

    @Id
    @Column(name = "cust_id")
    private Long customerId;

    @Id
    @Column(name = "level_id")
    private Long levelId;
    @Id
    @Column(name = "group_id")
    private Long groupId;
    @Id
    @Column(name = "capture_at")
    private Date captureAt;

    @XmlElement(name = "customerId")
    public Long getCustomerId() {
        return customerId;
    }

    @XmlElement(name = "groupId")
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    @XmlElement(name = "floorId")
    public Long getLevelId() {
        return levelId;
    }

    public void setLevelId(Long levelId) {
        this.levelId = levelId;
    }

    @XmlElement(name = "captureAt")
    public Date getCaptureAt() {
        return captureAt;
    }

    public void setCaptureAt(Date captureAt) {
        this.captureAt = captureAt;
    }

    @Override
    public String toString() {
        return "captureAt:" + captureAt + "customerId:" + customerId + "levelId:" + levelId + "groupId:" + groupId;
    }

}
