package com.communicator.model.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class AreaVO  implements IData{

	@XmlElement(name = "actionType")
	private String actionType;
	@XmlElement(name = "id")
    private Long id;
	@XmlElement(name = "name")
    private String name;
	@XmlElement(name = "description")
    private String description;
	@XmlElement(name = "floorId")
    private Long floorId;
	@XmlElement(name = "areaPlanUrl")
    private String areaPlanUrl;
    
    public AreaVO() {
    	
    }
    
    public AreaVO(String actionType, Long id, String name, String description,
    				Long floorId, String areaPlanUrl) {
    	this.actionType = actionType;
    	this.id = id;
    	this.name = name;
    	this.description = description;
    	this.floorId = floorId;
    	this.areaPlanUrl = areaPlanUrl;
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
	 * @return the floorId
	 */
	public Long getFloorId() {
		return floorId;
	}

	/**
	 * @param floorId the floorId to set
	 */
	public void setFloorId(Long floorId) {
		this.floorId = floorId;
	}

	/**
	 * @return the areaPlanUrl
	 */
	public String getAreaPlanUrl() {
		return areaPlanUrl;
	}

	/**
	 * @param areaPlanUrl the areaPlanUrl to set
	 */
	public void setAreaPlanUrl(String areaPlanUrl) {
		this.areaPlanUrl = areaPlanUrl;
	}
    
}
