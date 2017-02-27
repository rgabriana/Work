package com.communicator.model.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BuildingVO  implements IData{
	
	@XmlElement(name = "actionType")
	private String actionType;
	@XmlElement(name = "id")
    private Long id;
	@XmlElement(name = "name")
    private String name;
	@XmlElement(name = "campusId")
    private Long campusId;
    
    public BuildingVO() {
    	
    }
    
    public BuildingVO(String actionType, Long id, String name, Long campusId) {
    	this.actionType = actionType;
    	this.id = id;
    	this.name = name;
    	this.campusId = campusId;
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
	 * @return the campusId
	 */
	public Long getCampusId() {
		return campusId;
	}

	/**
	 * @param campusId the campusId to set
	 */
	public void setCampusId(Long campusId) {
		this.campusId = campusId;
	}

}
