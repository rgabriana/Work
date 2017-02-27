package com.ems.model;

import java.io.Serializable;
import java.util.Date;

import com.ems.util.Constants;

/**
 * @author Shiv Mohan
 */
public class EventsAndFault implements Serializable {

    private static final long serialVersionUID = 7218674899669489740L;

    // severities
    public static final int CRITICAL_SEV = 1;
    public static final int MAJOR_SEV = 2;
    public static final int MINOR_SEV = 3;
    public static final int INFO_SEV = 4;

    public static final String CRITICAL_SEV_STR = Constants.SEVERITY_CRITICAL;
    public static final String MAJOR_SEV_STR = Constants.SEVERITY_MAJOR;
    public static final String MINOR_SEV_STR = Constants.SEVERITY_MINOR;
    public static final String INFO_SEV_STR = Constants.SEVERITY_INFORMATIONAL;
    public static final String WARNING_SEV_STR = Constants.SEVERITY_WARNING;

    // event types
    public static final int DR_EVENT = 1;
    public static final int FIXTURE_OUTAGE_EVENT = 2;

    public static final String DR_EVENT_STR = "DR Condition";
    public static final String FIXTURE_OUTAGE_EVENT_STR = "Fixture Out";
    public static final String FIXTURE_PROFILE_PUSH_USERACTION = "Push Profile";
    public static final String FIXTURE_PROFILE_GROUP_ASSOCIATION_CHANGED = "Fixture associated Group Changed";
    public static final String FIXTURE_PROFILE_MISMATCH_USERACTION = "Profile Mismatch User Action";
    public static final String FIXTURE_PROFILE_MISMATCH = "Profile Mismatch";
    public static final String FIXTURE_BAD_PROFILE = "Bad Profile";
    public static final String FIXTURE_IMG_UP_STR = "Fixture Upgrade";
    public static final String GW_IMG_UP_STR = "Gateway Upgrade";
    public static final String FIXTURE_CU_FAILURE = "Fixture CU Failure";
    public static final String FIXTURE_IMG_CHECKSUM_FAILURE = "Fixture Image Checksum Failure";
    public static final String GW_SSL_CONN_FAILURE = "Gateway Connection Failure";
    public static final String FIXTURE_HARDWARE_FAILUE = "Fixture Hardware Failure";
    public static final String FIXTURE_TOO_HOT = "Fixture Too Hot";
    public static final String FIXTURE_CPU_USAGE_HIGH = "Fixture CPU Usage is High";
    public static final String FIXTURE_ERRONEOUS_ENERGY_STR = "Erroneous Energy Reading";
    
    public static final String GW_CONFIG_FAILURE = "Gateway configuration error";

    public static final String DISCOVER_EVENT_STR = "Discovery";
    public static final String COMMISSION_EVENT_STR = "Commissioning";

    public static final String BACNET_EVENT_STR = "Bacnet";
    public static final String EM_UPGRADE = "EM upgrade";
    public static final String SCHEDULER_EVENT = "Scheduler";
    public static final String FIXTURE_GROUP_CHANGE_EVENT = "Fixture group change";

    private Long id;
    private Date eventTime;
    private String severity;
    private String eventType;
    private String description;
    private Boolean active;
    private String resolutionComments;
    private User resolvedBy;
    private Date resolvedOn;
    private Device device;

    public EventsAndFault() {
    }

    /**
     * 
     * @param id
     * @param eventTime
     * @param severity
     * @param eventType
     * @param description
     * @param active
     * @param resolvedOn
     * @param fixtureId
     * @param fx_floorId
     * @param fx_buildingId
     * @param fx_campusId
     * @param fixtureName
     * @param fx_location
     */
    public EventsAndFault(Long id, Date eventTime, String severity, String eventType, String description,
            Boolean active, Date resolvedOn, Long fixtureId, Long fx_floorId, Long fx_buildingId, Long fx_campusId,
            String fixtureName, String fx_location) {
        this.id = id;
        this.eventTime = eventTime;
        this.severity = severity;
        this.eventType = eventType;
        this.description = description;
        this.active = active;
        this.resolvedOn = resolvedOn;
        Device fixture = null;
        if (fixtureId != null) {
            fixture = new Device();
            fixture.setId(fixtureId);
            Floor floor = new Floor();
            floor.setId(fx_floorId);
            fixture.setFloor(floor);
            fixture.setCampusId(fx_campusId);
            fixture.setBuildingId(fx_buildingId);
            fixture.setName(fixtureName);
            if (fx_location != null && fixtureName != null) {
              fixture.setLocation(fx_location + "->" + fixtureName);
            }
        }
        this.device = fixture;
        
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return time at which the event occurred
     */
    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    /**
     * @return the severity of the event
     */
    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /**
     * @return the type of event
     */
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * @return the description of the event
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return true or false based on whether event is active or not
     */
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * @return the device where the event occurs
     */
    public Device getDevice() {
        return device;
    }

    public void setDevice(Device fixture) {
        this.device = fixture;
    }

    /**
     * @return comments on resolving an event
     */
    public String getResolutionComments() {
        return resolutionComments;
    }

    public void setResolutionComments(String resolutionComments) {
        this.resolutionComments = resolutionComments;
    }

    /**
     * @return the user who marked the event as resolved
     */
    public User getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(User resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    /**
     * @return the date on which event was marked as resolved
     */
    public Date getResolvedOn() {
        return resolvedOn;
    }

    public void setResolvedOn(Date resolvedOn) {
        this.resolvedOn = resolvedOn;
    }
}
