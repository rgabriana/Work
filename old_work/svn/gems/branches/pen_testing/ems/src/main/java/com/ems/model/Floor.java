package com.ems.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
public class Floor implements Serializable {

    private static final long serialVersionUID = 6311363006338951159L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "description")
    // Required on create floor page.
    private String description;
    private Building building;
    private Set<Area> areas;
    private Set<Device> fixtures;
    private ProfileHandler profileHandler;
    @XmlElement(name = "floorplanurl")
    private String floorPlanUrl;
    private PlanMap planMap;
    private Set<Device> gateways;
    private byte[] byteImage;
    @XmlElement(name = "installedsensors")
    private Integer noInstalledSensors;
    @XmlElement(name = "installedfixtures")
    private Integer noInstalledFixtures;
    @XmlElement(name = "tenant")
    private Tenant tenant;
    private Long sweepTimerId;
    private Date uploadedOn;

    public Floor() {
    }

    public Floor(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Floor(Long id, String name, String description, String floorPlanUrl, Long profileHandlerId, Long planMapId,
            Integer noInstalledSensors, Integer noInstalledFixtures) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.floorPlanUrl = floorPlanUrl;
        ProfileHandler profileHandler = new ProfileHandler();
        profileHandler.setId(profileHandlerId);
        this.profileHandler = profileHandler;
        PlanMap planMap = new PlanMap();
        planMap.setId(planMapId);
        this.planMap = planMap;
        this.noInstalledSensors = noInstalledSensors;
        this.noInstalledFixtures = noInstalledFixtures;
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
     * @return the building
     */
    public Building getBuilding() {
        return building;
    }

    /**
     * @param building
     *            the building to set
     */
    public void setBuilding(Building building) {
        this.building = building;
    }

    /**
     * @return the fixtures
     */
    public Set<Device> getFixtures() {
        return fixtures;
    }

    /**
     * @param fixtures
     *            the fixtures to set
     */
    public void setFixtures(Set<Device> fixtures) {
        this.fixtures = fixtures;
    }

    /**
     * @return the areas
     */
    public Set<Area> getAreas() {
        return areas;
    }

    /**
     * @param areas
     *            the areas to set
     */
    public void setAreas(Set<Area> areas) {
        this.areas = areas;
    }

    //added by Nitin
    /*@return List of areas*/
    
    public List<Area> getAreasList(Set<Area> setareas){       
        List<Area> list = new ArrayList<Area>(); 
        if(setareas!=null)
        {
         	list.addAll(setareas);
        }
        return list;
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

    public void addArea(Area area) {
        if (areas == null) {
            areas = new HashSet<Area>();
        }
        areas.add(area);
    }

    /**
	 */
    public String getFloorPlanUrl() {
        return floorPlanUrl;
    }

    public void setFloorPlanUrl(String floorPlanUrl) {
        this.floorPlanUrl = floorPlanUrl;
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

    /**
     * @return the gateways
     */
    public Set<Device> getGateways() {
        return gateways;
    }

    /**
     * @param gateways
     *            the gateways to set
     */
    public void setGateways(Set<Device> gateways) {
        this.gateways = gateways;
    }

    /**
     * @return number of installed sensors
     */
    public Integer getNoInstalledSensors() {
        return noInstalledSensors;
    }

    public void setNoInstalledSensors(Integer noInstalledSensors) {
        this.noInstalledSensors = noInstalledSensors;
    }

    /**
     * @return number of installed fixtures
     */
    public Integer getNoInstalledFixtures() {
        return noInstalledFixtures;
    }

    public void setNoInstalledFixtures(Integer noInstalledFixtures) {
        this.noInstalledFixtures = noInstalledFixtures;
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

	public Date getUploadedOn() {
		return uploadedOn;
	}

	public void setUploadedOn(Date uploadedOn) {
		this.uploadedOn = uploadedOn;
	}
}
