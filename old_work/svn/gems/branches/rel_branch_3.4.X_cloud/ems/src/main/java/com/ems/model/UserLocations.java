package com.ems.model;

import java.io.Serializable;

import com.ems.types.FacilityType;

/**
 * 
 * @author lalit
 */
public class UserLocations implements Serializable{
    private Long id;
    private FacilityType approvedLocationType;
    private Long locationId;;
    private User user;

    public UserLocations() {
        super();
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public FacilityType getApprovedLocationType() {
        return approvedLocationType;
    }

    public void setApprovedLocationType(FacilityType approvedLocationType) {
        this.approvedLocationType = approvedLocationType;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
}
