package com.emscloud.model;

import java.io.Serializable;
import java.util.Date;

public class SpaceOccCK implements Serializable {
    private static final long serialVersionUID = -6644153185147549999L;

    private Long customerId;

    private Long levelId;

    private Long groupId;

    private Date captureAt;

    public SpaceOccCK() {
    }

    public SpaceOccCK(Long customerId, Long levelId, Long groupId, Date captureAt) {
        this.levelId = levelId;
        this.groupId = groupId;
        this.captureAt = captureAt;
        this.customerId = customerId;
    }

    @Override
    public int hashCode() {
        return this.getCaptureAt().hashCode() + getLevelId().hashCode() + getGroupId().hashCode()
                + getCustomerId().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        boolean flag = false;
        SpaceOccCK myPK = (SpaceOccCK) o;

        if ((o instanceof SpaceOccCK) && (this.getCaptureAt().equals(myPK.getCaptureAt()))
                && (this.levelId == myPK.getLevelId()) && (this.groupId == myPK.getGroupId())
                && (this.customerId == myPK.getCustomerId())) {
            flag = true;
        }
        return flag;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getLevelId() {
        return levelId;
    }

    public void setLevelId(Long levelId) {
        this.levelId = levelId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Date getCaptureAt() {
        return captureAt;
    }

    public void setCaptureAt(Date captureAt) {
        this.captureAt = captureAt;
    }

}
