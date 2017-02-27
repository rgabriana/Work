package com.ems.model;

import java.util.Date;
import java.util.List;

public class EventsAndFaultFilter {
	private Long groupId;
	private Date startTime;
	private Date endTime;
	private List<String> eventTypes;
	private List<String> severities;
	private String location;
	private Long campusId;
	private Long buildingId;
	private Long floorId;
	private Long areaId;
	private String resolvedString;
	private Boolean resolved;
	private String searchQuery;
	
	public Long getGroupId() {
		return groupId;
	}
	
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	public Date getEndTime() {
		return endTime;
	}
	
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	public List<String> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(List<String> eventTypes) {
		this.eventTypes = eventTypes;
	}

	public List<String> getSeverities() {
		return severities;
	}

	public void setSeverities(List<String> severities) {
		this.severities = severities;
	}

	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getResolvedString() {
		return resolvedString;
	}

	public void setResolvedString(String resolvedString) {
		this.resolvedString = resolvedString;
		if(this.resolvedString.equals("1")){
			this.resolved = new Boolean(true);
		}else if (this.resolvedString.equals("0")) {
			this.resolved = new Boolean(false);
		}else{
			this.resolved = null;
		}
	}

	public Boolean getResolved() {
		return resolved;
	}

	public void setResolved(Boolean resolved) {
		this.resolved = resolved;
	}

	public String getSearchQuery() {
		return searchQuery;
	}
	
	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}

	public Long getCampusId() {
		return campusId;
	}

	public void setCampusId(Long campusId) {
		this.campusId = campusId;
	}

	public Long getBuildingId() {
		return buildingId;
	}

	public void setBuildingId(Long buildingId) {
		this.buildingId = buildingId;
	}

	public Long getFloorId() {
		return floorId;
	}

	public void setFloorId(Long floorId) {
		this.floorId = floorId;
	}

	public Long getAreaId() {
		return areaId;
	}

	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}
	
}
