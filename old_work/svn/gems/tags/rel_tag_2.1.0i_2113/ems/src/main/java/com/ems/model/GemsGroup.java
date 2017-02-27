package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Shilpa Chalasani
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class GemsGroup implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -4516595387316023573L;

    @XmlElement(name = "id")
    private Long id;

    @XmlElement(name = "name")
    private String groupName;

    @XmlElement(name = "type")
    private GemsGroupType type;

    @XmlElement(name = "description")
    private String description;

    private Company company;
    private Floor floor;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the group name
     */
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * @return the group type
     */
    public GemsGroupType getType() {
        return type;
    }

    public void setType(GemsGroupType type) {
        this.type = type;
    }

    /**
     * @return the group description
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the company
     */
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

	public Floor getFloor() {
		return floor;
	}

	public void setFloor(Floor floor) {
		this.floor = floor;
	}
}
