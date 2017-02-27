package com.ems.vo;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Sharad K Mahajan
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EMEvents implements Serializable {

    private static final long serialVersionUID = 431776385828817164L;

    @XmlElement(name="id")
    private Long id;
    @XmlElement(name="eventTime")
    private String eventTime;
    @XmlElement(name="severity")
    private String severity;
    @XmlElement(name="eventType")
    private String eventType;
    @XmlElement(name="description")
    private String description;
    @XmlElement(name="active")
    private Boolean active;
    @XmlElement(name="resolutionComments")
    private String resolutionComments;
    @XmlElement(name="resolvedBy")
    private String resolvedBy;
    @XmlElement(name="resolvedOn")
    private String resolvedOn;
    @XmlElement(name="device")
    private Long deviceId;
    @XmlElement(name="location")
    private String location;
    @XmlElement(name="deviceName")
    private String deviceName;
    @XmlElement(name="deviceType")
    private String deviceType;
   
	public EMEvents() {
    }


    /**
     * @return the eventTime
     */
    public String getEventTime() {
        return eventTime;
    }

    /**
     * @param eventTime the eventTime to set
     */
    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    /**
     * @return the severity
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * @param severity the severity to set
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /**
     * @return the eventType
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * @param eventType the eventType to set
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the active
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * @return the resolutionComments
     */
    public String getResolutionComments() {
        return resolutionComments;
    }

    /**
     * @param resolutionComments the resolutionComments to set
     */
    public void setResolutionComments(String resolutionComments) {
        this.resolutionComments = resolutionComments;
    }

    /**
     * @return the resolvedBy
     */
    public String getResolvedBy() {
        return resolvedBy;
    }

    /**
     * @param resolvedBy the resolvedBy to set
     */
    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    /**
     * @return the resolvedOn
     */
    public String getResolvedOn() {
        return resolvedOn;
    }

    /**
     * @param resolvedOn the resolvedOn to set
     */
    public void setResolvedOn(String resolvedOn) {
        this.resolvedOn = resolvedOn;
    }

    /**
     * @return the deviceId
     */
    public Long getDeviceId() {
        return deviceId;
    }

    /**
     * @param deviceId the deviceId to set
     */
    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }


    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }


    /**
     * @return the deviceName
     */
    public String getDeviceName() {
        return deviceName;
    }


    /**
     * @param deviceName the deviceName to set
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }


    /**
     * @return the deviceType
     */
    public String getDeviceType() {
        return deviceType;
    }


    /**
     * @param deviceType the deviceType to set
     */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

}
