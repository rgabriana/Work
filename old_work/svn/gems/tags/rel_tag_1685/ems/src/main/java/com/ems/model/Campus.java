package com.ems.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 @author pankaj kumar chauhan
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Campus implements Serializable {

    private static final long serialVersionUID = 5485533189628971724L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "name")
    private String name;
    private String location;
    @XmlElement(name = "zipcode")
    private String zipcode;
    private Set<Building> buildings;
    private ProfileHandler profileHandler;
    @XmlElement(name = "tenant")
    private Tenant tenant;
    private List<Building> buildingsList;
    private Long sweepTimerId;
    
    public Campus() {
    }

    public Campus(Long id, String name, String location, String zipcode, Long profileHandlerId) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.zipcode = zipcode;
        ProfileHandler profileHandler = new ProfileHandler();
        profileHandler.setId(profileHandlerId);
        this.profileHandler = profileHandler;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
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
     * @param name
     *            the name to set
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
     * @param location
     *            the location to set
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
     * @param zipcode
     *            the zipcode to set
     */
    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    /**
     * @return the buildings
     */
    public Set<Building> getBuildings() {
        return buildings;
    }

    /**
     * @param buildings
     *            the buildings to set
     */
    public void setBuildings(Set<Building> buildings) {
        this.buildings = buildings;
    }

    //added by Nitin
    /*@return List of buildings*/
    
    public List<Building> getBuildingsList(Set<Building> setbuildings){       
        List<Building> list = new ArrayList<Building>(setbuildings);        
        return list;
    }
    
    public void addBuilding(Building building) {
        if (buildings == null) {
            buildings = new HashSet<Building>();
        }
        buildings.add(building);
    }

    /**
     * @return the profileHandler
     */
    public ProfileHandler getProfileHandler() {
        return profileHandler;
    }

    /**
     * @param profileHandler
     *            the profileHandler to set
     */
    public void setProfileHandler(ProfileHandler profileHandler) {
        this.profileHandler = profileHandler;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

	/**
	 * @return the sweepTimerId
	 */
	public Long getSweepTimerId() {
		return sweepTimerId;
	}

	/**
	 * @param sweepTimerId the sweepTimerId to set
	 */
	public void setSweepTimerId(Long sweepTimerId) {
		this.sweepTimerId = sweepTimerId;
	}

}
