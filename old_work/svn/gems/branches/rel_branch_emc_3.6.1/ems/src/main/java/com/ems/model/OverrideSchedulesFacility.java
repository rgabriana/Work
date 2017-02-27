package com.ems.model;

import java.io.Serializable;

import com.ems.types.FacilityType;

/**
 * 
 * @author sampath
 */
public class OverrideSchedulesFacility implements Serializable{
    private Long id;
    private FacilityType facilityType;
    private Long facilityId;;
    private DRTarget drTarget;

    public OverrideSchedulesFacility() {
        super();
    }

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setFacilityType(FacilityType facilityType) {
		this.facilityType = facilityType;
	}

	public FacilityType getFacilityType() {
		return facilityType;
	}

	public void setFacilityId(Long facilityId) {
		this.facilityId = facilityId;
	}

	public Long getFacilityId() {
		return facilityId;
	}

	public void setDrTarget(DRTarget drTarget) {
		this.drTarget = drTarget;
	}

	public DRTarget getDrTarget() {
		return drTarget;
	}
   
}
