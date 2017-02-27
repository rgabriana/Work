package com.communicator.model.vo;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FloorVO implements IData {
	
	@XmlElement(name = "actionType")
	private String actionType;
	@XmlElement(name = "id")
    private Long id;
	@XmlElement(name = "name")
    private String name;
	@XmlElement(name = "description")
    private String description;
	@XmlElement(name = "buildingId")
    private Long buildingId;
	@XmlElement(name = "floorPlanUrl")
    private String floorPlanUrl;
	@XmlElement(name = "noInstalledSensors")
    private Integer noInstalledSensors;
	@XmlElement(name = "noInstalledFixtures")
    private Integer noInstalledFixtures;
	@XmlElement(name = "floorPlanUploadedTime")
    private Date floorPlanUploadedTime;
    
    public FloorVO() {
    	
    }
    
    public FloorVO(String actionType, Long id, String name, String description, Long buildingId, String floorPlanUrl, 
    				Integer noInstalledSensors, Integer noInstalledFixtures, Date floorPlanUploadedTime) {
    	this.actionType = actionType;
    	this.id = id;
    	this.name = name;
    	this.description = description;
    	this.buildingId = buildingId;
    	this.floorPlanUrl = floorPlanUrl;
    	this.noInstalledSensors = noInstalledSensors;
    	this.noInstalledFixtures = noInstalledFixtures;
    	this.floorPlanUploadedTime = floorPlanUploadedTime;
    	
    }

	/**
	 * @return the actionType
	 */
	public String getActionType() {
		return actionType;
	}

	/**
	 * @param actionType the actionType to set
	 */
	public void setActionType(String actionType) {
		this.actionType = actionType;
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the buildingId
	 */
	public Long getBuildingId() {
		return buildingId;
	}

	/**
	 * @param buildingId the buildingId to set
	 */
	public void setBuildingId(Long buildingId) {
		this.buildingId = buildingId;
	}

	/**
	 * @return the floorPlanUrl
	 */
	public String getFloorPlanUrl() {
		return floorPlanUrl;
	}

	/**
	 * @param floorPlanUrl the floorPlanUrl to set
	 */
	public void setFloorPlanUrl(String floorPlanUrl) {
		this.floorPlanUrl = floorPlanUrl;
	}

	/**
	 * @return the noInstalledSensors
	 */
	public Integer getNoInstalledSensors() {
		return noInstalledSensors;
	}

	/**
	 * @param noInstalledSensors the noInstalledSensors to set
	 */
	public void setNoInstalledSensors(Integer noInstalledSensors) {
		this.noInstalledSensors = noInstalledSensors;
	}

	/**
	 * @return the noInstalledFixtures
	 */
	public Integer getNoInstalledFixtures() {
		return noInstalledFixtures;
	}

	/**
	 * @param noInstalledFixtures the noInstalledFixtures to set
	 */
	public void setNoInstalledFixtures(Integer noInstalledFixtures) {
		this.noInstalledFixtures = noInstalledFixtures;
	}

	/**
	 * @return the floorPlanUploadedTime
	 */
	public Date getFloorPlanUploadedTime() {
		return floorPlanUploadedTime;
	}

	/**
	 * @param floorPlanUploadedTime the floorPlanUploadedTime to set
	 */
	public void setFloorPlanUploadedTime(Date floorPlanUploadedTime) {
		this.floorPlanUploadedTime = floorPlanUploadedTime;
	}

}
