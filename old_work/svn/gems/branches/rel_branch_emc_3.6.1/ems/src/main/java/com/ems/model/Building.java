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
public class Building implements Serializable {

    private static final long serialVersionUID = -7576321785227223109L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "name")
    private String name;
    private Campus campus;
    private Set<Floor> floors;
    private ProfileHandler profileHandler;
    @XmlElement(name = "tenant")
    private Tenant tenant;
    private Long sweepTimerId;
    
    @XmlElement(name = "latitude")
    private Float latitude;
    @XmlElement(name = "longitude")
    private Float longitude;
    
    @XmlElement(name = "useOrgLocation")
    private Boolean useOrgLocation;
    
    private String uid;
  
    @XmlElement(name = "visible")
    private Boolean visible = true;

    public Building() {
    }

    public Building(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Building(Long id, String name, Long profileHandlerId) {
        this.id = id;
        this.name = name;
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
     * @return the campus
     */
    public Campus getCampus() {
        return campus;
    }

    /**
     * @param campus
     *            the campus to set
     */
    public void setCampus(Campus campus) {
        this.campus = campus;
    }

    /**
     * @return the floors
     */
    public Set<Floor> getFloors() {
        return floors;
    }

    /**
     * @param floors
     *            the floors to set
     */
    public void setFloors(Set<Floor> floors) {
        this.floors = floors;
    }

    //added by Nitin
    /*@return List of floors*/
    
    public List<Floor> getFloorsList(Set<Floor> setfloors){       
        List<Floor> list = new ArrayList<Floor>();  
        if(setfloors!=null)
        {
         	list.addAll(setfloors);
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

    public void addFloor(Floor floor) {
        if (floors == null) {
            floors = new HashSet<Floor>();
        }
        floors.add(floor);
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

	public void setLatitude(Float latitude) {
		this.latitude = latitude;
	}

	public Float getLatitude() {
		return latitude;
	}

	public void setLongitude(Float longitude) {
		this.longitude = longitude;
	}

	public Float getLongitude() {
		return longitude;
	}

	public void setUseOrgLocation(Boolean useOrgLocation) {
		this.useOrgLocation = useOrgLocation;
	}

	public Boolean getUseOrgLocation() {
		return useOrgLocation;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public Boolean getVisible() {
		return visible;
	}

}
