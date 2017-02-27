package com.communicator.model.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CampusVO  implements IData{
	
	@XmlElement(name = "actionType")
	private String actionType;
	@XmlElement(name = "id")
    private Long id;
	@XmlElement(name = "name")
    private String name;
	@XmlElement(name = "location")
    private String location;
	@XmlElement(name = "zipcode")
    private String zipcode;
	@XmlElement(name = "companyId")
    private Long companyId;
    
    public CampusVO() {
    }

    public CampusVO(String actionType, Long id, String name, String location, 
    		String zipcode, Long companyId) {
    	
        this.actionType = actionType;
        this.id = id;
        this.name = name;
        this.location = location;
        this.zipcode = zipcode;
        this.setCompanyId(companyId);

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
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the zipcode
	 */
	public String getZipcode() {
		return zipcode;
	}

	/**
	 * @param zipcode the zipcode to set
	 */
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	/**
	 * @return the companyId
	 */
	public Long getCompanyId() {
		return companyId;
	}

	/**
	 * @param companyId the companyId to set
	 */
	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

}
