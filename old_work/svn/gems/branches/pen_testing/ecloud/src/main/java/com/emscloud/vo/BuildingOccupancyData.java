package com.emscloud.vo;

import java.util.List;

public class BuildingOccupancyData {

	private Long buildingId;
	private Long campusId;
	private String name;
	private Float locX;
	private Float locY;
	private Long avgNoOfSensors;
	private Long totalNoOfSensors;
	private Long occupPercent;
    private Long total1bits;
    private Long totalBits;
    private List<ChildLevelOccupancyData> childLevels;
    
    public BuildingOccupancyData() {
	
	}
	public BuildingOccupancyData(Long buildingId,Long avgNoOfSensors,
			Long totalNoOfSensors, Long total1bits, Long totalBits) {
		super();
		this.buildingId = buildingId;
		this.avgNoOfSensors = avgNoOfSensors;
		this.totalNoOfSensors = totalNoOfSensors;
		this.total1bits = total1bits;
		this.totalBits = totalBits;
		if(this.total1bits!=null&&this.totalBits!=null)
		{
			this.occupPercent = this.total1bits*100/this.totalBits; 
		}
	}
	public Long getBuildingId() {
		return buildingId;
	}
	public void setBuildingId(Long buildingId) {
		this.buildingId = buildingId;
	}
	public Long getCampusId() {
		return campusId;
	}
	public void setCampusId(Long campusId) {
		this.campusId = campusId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Float getLocX() {
		return locX;
	}
	public void setLocX(Float locX) {
		this.locX = locX;
	}
	public Float getLocY() {
		return locY;
	}
	public void setLocY(Float locY) {
		this.locY = locY;
	}
	public Long getAvgNoOfSensors() {
		return avgNoOfSensors;
	}
	public void setAvgNoOfSensors(Long avgNoOfSensors) {
		this.avgNoOfSensors = avgNoOfSensors;
	}
	public Long getTotalNoOfSensors() {
		return totalNoOfSensors;
	}
	public void setTotalNoOfSensors(Long totalNoOfSensors) {
		this.totalNoOfSensors = totalNoOfSensors;
	}
	public Long getOccupPercent() {
		return occupPercent;
	}
	public void setOccupPercent(Long occupPercent) {
		this.occupPercent = occupPercent;
	}
	public Long getTotal1bits() {
		return total1bits;
	}
	public void setTotal1bits(Long total1bits) {
		this.total1bits = total1bits;
	}
	public Long getTotalBits() {
		return totalBits;
	}
	public void setTotalBits(Long totalBits) {
		this.totalBits = totalBits;
	}
	public List<ChildLevelOccupancyData> getChildLevels() {
		return childLevels;
	}
	public void setChildLevels(List<ChildLevelOccupancyData> childLevels) {
		this.childLevels = childLevels;
	}
    
    
	
}
