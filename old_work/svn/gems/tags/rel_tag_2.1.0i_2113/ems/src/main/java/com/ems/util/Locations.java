package com.ems.util;

import java.util.List;

public class Locations {

	private String locationName;
	private List<Locations> childLocationList;
	private Long locationId;
	private String locationType;
	
	public Long getLocationId() {
		return locationId;
	}
	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}
	public String getLocationType() {
		return locationType;
	}
	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}
	public List<Locations> getChildLocationList() {
		return childLocationList;
	}
	public void setChildLocationList(List<Locations> childLocationList) {
		this.childLocationList = childLocationList;
	}
	public String getLocationName() {
		return locationName;
	}
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
}
