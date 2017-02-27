package com.ems.model;

import com.ems.types.FacilityType;

/**
 * 
 * @author lalit
 * 
 */
public class TenantLocations {

    private Long id;
    private FacilityType approvedLocationType;
    private Long locationId;;
    private Tenant tenant;

    public TenantLocations() {
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

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
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
