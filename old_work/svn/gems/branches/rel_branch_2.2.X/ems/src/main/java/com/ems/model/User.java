package com.ems.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.types.UserStatus;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class User implements Serializable {

    private static final long serialVersionUID = -4580938118425322244L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "role")
    private Role role;
    @XmlElement(name = "tenant")
    private Tenant tenant;
    @XmlElement(name = "email")
    private String email;
    private String password;
    @XmlElement(name = "firstname")
    private String firstName;
    @XmlElement(name = "lastname")
    private String lastName;
    @XmlElement(name = "contact")
    private String contact;
    @XmlElement(name = "createdon")
    private Date createdOn;

    private UserStatus status;

    private Set<UserLocations> userLocations;

    @XmlElement(name = "locationid")
    private Long locationId;
    @XmlElement(name = "locationtype")
    private String locationType;

    private String oldPasswords;
    @XmlElement(name = "location")
    private String location;
    @XmlElement(name = "approvedlocation")
    private String approvedLocation;
    @XmlElement(name = "approvedlocationid")
    private Long approvedLocationId;
    @XmlElement(name = "approvedlocationtype")
    private String approvedLocationType;
    
    @XmlElement(name = "termConditionAccepted")
    private boolean termConditionAccepted;
    
    @XmlElement(name = "selected")
    private boolean selected;
    

    public User() {
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
     * @return the email
     */
    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email
     *            the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName
     *            the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName
     *            the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the createdOn
     */
    public Date getCreatedOn() {
        return createdOn;
    }

    /**
     * @param createdOn
     *            the createdOn to set
     */
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * @return the role
     */
    public Role getRole() {
        return role;
    }

    /**
     * @param role
     *            the role to set
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * @return the location id
     */
    public Long getLocationId() {
        return locationId;
    }

    /**
     * @param location_id
     *            the location id to set
     */
    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    /**
     * @return the location type
     */
    public String getLocationType() {
        return locationType;
    }

    /**
     * @param location_type
     *            the location type to set
     */
    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    /**
     * @return the contact
     */
    public String getContact() {
        return contact;
    }

    /**
     * @param contact
     *            the contact to set
     */
    public void setContact(String contact) {
        this.contact = contact;
    }

    /**
     * @return the oldPasswords
     */
    public String getOldPasswords() {
        return oldPasswords;
    }

    /**
     * @param oldPasswords
     *            the oldPasswords to set
     */
    public void setOldPasswords(String oldPasswords) {
        this.oldPasswords = oldPasswords;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the approvedLocationId
     */
    public Long getApprovedLocationId() {
        return approvedLocationId;
    }

    /**
     * @param approvedLocationId
     *            the approvedLocationId to set
     */
    public void setApprovedLocationId(Long approvedLocationId) {
        this.approvedLocationId = approvedLocationId;
    }

    /**
     * @return the approvedLocationType
     */
    public String getApprovedLocationType() {
        return approvedLocationType;
    }

    /**
     * @param approvedLocationType
     *            the approvedLocationType to set
     */
    public void setApprovedLocationType(String approvedLocationType) {
        this.approvedLocationType = approvedLocationType;
    }

    public String getApprovedLocation() {
        return approvedLocation;
    }

    public void setApprovedLocation(String approvedLocation) {
        this.approvedLocation = approvedLocation;
    }



    public Set<UserLocations> getUserLocations() {
        return userLocations;
    }

    public void setUserLocations(Set<UserLocations> userLocations) {
        this.userLocations = userLocations;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

	public boolean getTermConditionAccepted() {
		return termConditionAccepted;
	}

	public void setTermConditionAccepted(boolean b) {
		this.termConditionAccepted = b;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}
