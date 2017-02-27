/**
 * enLighted Inc @ 2011
 */
package com.ems.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author yogesh
 */
public class EmsAudit implements Serializable {

    private static final long serialVersionUID = 8153690457372451431L;
    private Long id;
    private Long txnId;
    private Long deviceId;
    private Integer deviceType;
    private Integer attempts;
    private String action;
    private Date startTime;
    private Date endTime;
    private String status;
    private String comments;
    private String deviceName;

    public EmsAudit() {

    }

    public EmsAudit(Long id, Long txnId, Long deviceId, Integer deviceType, Integer attempts, String action,
            Date startTime, Date endTime, String status, String comments, String deviceName) {
        this.id = id;
        this.txnId = txnId;
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.attempts = attempts;
        this.action = action;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.comments = comments;
        this.deviceName = deviceName;
    }

    public String toString() {
        return "[" + this.id + ", " + this.txnId + ", " + deviceName + ", (" + deviceId + "), " + this.deviceType
                + ", " + this.attempts + ", " + this.action + ", " + this.startTime + ", " + this.endTime + ", "
                + this.status + "]";
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the txnId
     * 
     */
    public Long getTxnId() {
        return txnId;
    }

    /**
     * @param txnId
     *            the txnId to set
     */
    public void setTxnId(Long txnId) {
        this.txnId = txnId;
    }

    /**
     * @return the deviceId
     */
    public Long getDeviceId() {
        return deviceId;
    }

    /**
     * @param deviceId
     *            the deviceId to set
     */
    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * @return the deviceType
     */
    public Integer getDeviceType() {
        return deviceType;
    }

    /**
     * @param deviceType
     *            the deviceType to set
     */
    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * @return the attempts
     */
    public Integer getAttempts() {
        return attempts;
    }

    /**
     * @param attempts
     *            the attempts to set
     */
    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return the startTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTime
     *            the startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @param endTime
     *            the endTime to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the comments
     */
    public String getComments() {
        return comments;
    }

    /**
     * @param comments
     *            the comments to set
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * @param deviceName
     *            the deviceName to set
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * @return the deviceName
     */
    public String getDeviceName() {
        return deviceName;
    }

}
