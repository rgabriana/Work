package com.ems.model;

import com.ems.types.FacilityType;

/**
 * 
 * @author lalit
 */
public class UserLocations {
    private Long id;
    private FacilityType approvedLocationType;
    private Long locationId;;

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
