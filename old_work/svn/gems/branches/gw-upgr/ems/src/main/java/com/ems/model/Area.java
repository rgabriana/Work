package com.ems.model;

import java.io.Serializable;
import java.util.HashSet;
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
public class Area implements Serializable {

    private static final long serialVersionUID = 6311363006338951159L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "description")
    private String description;
    private Floor floor;
    private String areaPlanUrl;
    private Set<SubArea> subAreas;
    //private Set<Device> fixtures;
    private ProfileHandler profileHandler;
    private PlanMap planMap;
    private byte[] byteImage;
    @XmlElement(name = "tenant")
    private Tenant tenant;
    private Long sweepTimerId;
    
    @XmlElement(name = "zonesensorenable")
    private Boolean zoneSensorEnable = false;
    
    @XmlElement(name = "occupancyState")
    private Integer occupancyState;

    public Area() {
    }

    public Area(Long id, String name, String description, Long floorId, Long profileHandlerId, Long planMapId) {
        this.id = id;
        this.name = name;
        this.description = description;
        Floor floor = new Floor();
        floor.setId(floorId);
        this.floor = floor;
        ProfileHandler profileHandler = new ProfileHandler();
        profileHandler.setId(profileHandlerId);
        this.profileHandler = profileHandler;
        PlanMap planMap = new PlanMap();
        planMap.setId(planMapId);
        this.planMap = planMap;
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
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the floor
     */
    public Floor getFloor() {
        return floor;
    }

    /**
     * @param floor
     *            the floor to set
     */
    public void setFloor(Floor floor) {
        this.floor = floor;
    }

    /**
     * @return the subAreas
     */
    public Set<SubArea> getSubAreas() {
        return subAreas;
    }

    /**
     * @param subAreas
     *            the subAreas to set
     */
    public void setSubAreas(Set<SubArea> subAreas) {
        this.subAreas = subAreas;
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

    public void addSubArea(SubArea subArea) {
        if (subAreas == null) {
            subAreas = new HashSet<SubArea>();
        }
        subAreas.add(subArea);
    }

    /**
     * @return the fixtures
     */
    /*public Set<Device> getFixtures() {
        return fixtures;
    }*/

    /**
     * @param fixtures
     *            the fixtures to set
     */
    /*public void setFixtures(Set<Device> fixtures) {
        this.fixtures = fixtures;
    }*/

    /**
     * @return the areaPlanUrl
     */
    public String getAreaPlanUrl() {
        return areaPlanUrl;
    }

    /**
     * @param areaPlanUrl
     *            the areaPlanUrl to set
     */
    public void setAreaPlanUrl(String areaPlanUrl) {
        this.areaPlanUrl = areaPlanUrl;
    }

    /**
     * @return the PlanMap
     */
    public PlanMap getPlanMap() {
        return planMap;
    }

    public void setPlanMap(PlanMap planMap) {
        this.planMap = planMap;
    }

    public byte[] getByteImage() {
        return byteImage;
    }

    public void setByteImage(byte[] byteImage) {
        this.byteImage = byteImage;
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

	/**
	 * @return the occupancyState
	 */
	public Integer getOccupancyState() {
		return occupancyState;
	}

	/**
	 * @param occupancyState the occupancyState to set
	 */
	public void setOccupancyState(Integer occupancyState) {
		this.occupancyState = occupancyState;
	}

	public void setZoneSensorEnable(Boolean zoneSensorEnable) {
		this.zoneSensorEnable = zoneSensorEnable;
	}

	public Boolean getZoneSensorEnable() {
		return zoneSensorEnable;
	}

}
