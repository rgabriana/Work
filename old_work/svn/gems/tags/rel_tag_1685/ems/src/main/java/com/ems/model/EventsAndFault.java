package com.ems.model;

import java.io.Serializable;
import java.util.Date;

import com.ems.util.Constants;
import com.ems.utils.ArgumentUtils;

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

    private Long id;
    private Date eventTime;
    private String severity;
    private String eventType;
    private String description;
    private Boolean active;
    private Fixture fixture;
    private String resolutionComments;
    private User resolvedBy;
    private Date resolvedOn;
    private Gateway gateway;

    public EventsAndFault() {
    }

    public EventsAndFault(Long id, Date eventTime, String location, String severity, String eventType,
            String description, Long fixtureId, Long floorId, Long buildingId, Long campusId, Boolean active,
            Date resolvedOn, String fixtureName) {
        this.id = id;
        this.eventTime = eventTime;
        this.severity = severity;
        this.eventType = eventType;
        this.description = description;
        Fixture fixture = new Fixture();
        fixture.setId(fixtureId);
        Floor floor = new Floor();
        floor.setId(floorId);
        fixture.setFloor(floor);
        fixture.setCampusId(campusId);
        fixture.setBuildingId(buildingId);
        if (location != null && fixtureName != null) {
            fixture.setLocation(location + "->" + fixtureName);
        }
        this.fixture = fixture;
        this.active = active;
        this.resolvedOn = resolvedOn;
    }

    public EventsAndFault(Long id, Date eventTime, String location, String severity, String eventType,
            String description, Long fixtureId, Long floorId, Long buildingId, Long campusId, Boolean active,
            String fixtureName, String gatewayName, String gatewayLocation, String resolutionComments, Long userId,
            String userEmail, Date resolvedOn) {
        this.id = id;
        this.eventTime = eventTime;
        this.severity = severity;
        this.eventType = eventType;
        this.description = description;
        Fixture fixture = new Fixture();
        fixture.setId(fixtureId);
        Floor floor = new Floor();
        floor.setId(floorId);
        fixture.setFloor(floor);
        fixture.setCampusId(campusId);
        fixture.setBuildingId(buildingId);
        fixture.setSensorId(fixtureName);
        fixture.setLocation(location);
        Gateway gateway = new Gateway();
        gateway.setGatewayName(gatewayName);
        gateway.setLocation(gatewayLocation);
        this.gateway = gateway;
        this.fixture = fixture;
        this.active = active;
        User user = new User();
        if (userId != null) {
            user.setId(userId);
        }
        if (!ArgumentUtils.isNullOrEmpty(userEmail)) {
            user.setEmail(userEmail);
        }
        this.resolvedBy = user;
        this.resolvedOn = resolvedOn;
        this.resolutionComments = resolutionComments;
    }

    /**
     * Used for fetching gateway
     */
    public EventsAndFault(Long id, Date eventTime, String severity, String eventType, String description, Long floorId,
            Long buildingId, Long campusId, Boolean active, String gatewayName, Long gatewayId, String location) {
        this.id = id;
        this.eventTime = eventTime;
        this.severity = severity;
        this.eventType = eventType;
        this.description = description;
        Gateway gateway = new Gateway();
        gateway.setId(gatewayId);
        Floor floor = new Floor();
        floor.setId(floorId);
        gateway.setFloor(floor);
        gateway.setCampusId(campusId);
        gateway.setBuildingId(buildingId);
        gateway.setGatewayName(gatewayName);
        gateway.setLocation(location);
        this.gateway = gateway;
        this.active = active;
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
     * @param gatewayId
     * @param gw_floorId
     * @param gw_buildingId
     * @param gw_campusId
     * @param gatewayName
     * @param gw_location
     */
    public EventsAndFault(Long id, Date eventTime, String severity, String eventType, String description,
            Boolean active, Date resolvedOn, Long fixtureId, Long fx_floorId, Long fx_buildingId, Long fx_campusId,
            String fixtureName, String fx_location, Long gatewayId, Long gw_floorId, Long gw_buildingId,
            Long gw_campusId, String gatewayName, String gw_location) {
        this.id = id;
        this.eventTime = eventTime;
        this.severity = severity;
        this.eventType = eventType;
        this.description = description;
        this.active = active;
        this.resolvedOn = resolvedOn;
        Fixture fixture = null;
        if (fixtureId != null) {
            fixture = new Fixture();
            fixture.setId(fixtureId);
            Floor floor = new Floor();
            floor.setId(fx_floorId);
            fixture.setFloor(floor);
            fixture.setCampusId(fx_campusId);
            fixture.setBuildingId(fx_buildingId);
            fixture.setSensorId(fixtureName);
            fixture.setLocation(fx_location);
        }
        this.fixture = fixture;
        Gateway gateway = null;
        if (gatewayId != null) {
            gateway = new Gateway();
            gateway.setId(gatewayId);
            Floor floor = new Floor();
            floor.setId(gw_floorId);
            gateway.setFloor(floor);
            gateway.setCampusId(gw_campusId);
            gateway.setBuildingId(gw_buildingId);
            gateway.setGatewayName(gatewayName);
            gateway.setLocation(gw_location);
        }
        this.gateway = gateway;
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
     * @return the fixture where the event occurs
     */
    public Fixture getFixture() {
        return fixture;
    }

    public void setFixture(Fixture fixture) {
        this.fixture = fixture;
    }

    /**
     * @return the gateway where the event occurs
     */
    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
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
