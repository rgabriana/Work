package com.ems.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.context.i18n.LocaleContextHolder;

import com.ems.types.RoleType;
import com.ems.types.UserStatus;
import com.ems.util.Constants;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@XmlRootElement(name = "user")
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
    @XmlElement(name = "passwordChangedAt")
    private Date passwordChangedAt;
    public Date getPasswordChangedAt() {
		return passwordChangedAt;
	}

	public void setPasswordChangedAt(Date passwordChangedAt) {
		this.passwordChangedAt = passwordChangedAt;
	}

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
    @XmlElement(name = "noOfLoginAttempts")
    private Long noOfLoginAttempts = 0l;

    @XmlElement(name = "forgotPasswordIdentifier")
    private String forgotPasswordIdentifier;
    
    @XmlElement(name = "userLocked")
    private boolean userLocked = false;
    
    @XmlElement(name = "unlockTime")
    private Date unlockTime = null;
    
    private boolean isSuperAdmin = false;
    private boolean isAdmin = false;
    private long noOfAttemptsRemaining = 0;
    
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

	public Long getNoOfLoginAttempts() {
		return noOfLoginAttempts;
	}

	public void setNoOfLoginAttempts(Long noOfLoginAttempts) {
		this.noOfLoginAttempts = noOfLoginAttempts;
	}

	public boolean isUserLocked() {
		if(( isAdmin())){
			final Date now = new Date();
			final boolean isLocked = getUnlockTime() != null && now.getTime() <= getUnlockTime().getTime(); 
			userLocked = this.getNoOfLoginAttempts() >= (Constants.MAX_LOGIN_ATTEMPTS_ADMINS - 1) && isLocked;
		}else{
			userLocked = !( isAdmin()) &&  this.getNoOfLoginAttempts() >= (Constants.MAX_LOGIN_ATTEMPTS - 1);
		}
		return userLocked;
	}

	public void setUserLocked(boolean userLocked) {
		this.userLocked = userLocked;
	}

	public String getForgotPasswordIdentifier() {
		return forgotPasswordIdentifier;
	}

	public void setForgotPasswordIdentifier(String forgotPasswordIdentifier) {
		this.forgotPasswordIdentifier = forgotPasswordIdentifier;
	}

	public boolean isSuperAdmin() {
		isSuperAdmin = Constants.ADMIN.equals(this.getEmail());
		return isSuperAdmin;
	}

	public boolean isAdmin() {
		isAdmin = isSuperAdmin() || this.getRole() != null && ( this.getRole().getRoleType() == RoleType.Admin || this.getRole().getRoleType() == RoleType.FacilitiesAdmin);
		return isAdmin;
	}

	public long getNoOfAttemptsRemaining() {
		noOfAttemptsRemaining = Constants.MAX_LOGIN_ATTEMPTS - this.getNoOfLoginAttempts();
		if(isAdmin()){
			noOfAttemptsRemaining = Constants.MAX_LOGIN_ATTEMPTS_ADMINS - this.getNoOfLoginAttempts();
		}
		return noOfAttemptsRemaining;
	}

	public Date getUnlockTime() {
		return unlockTime;
	}

	public void setUnlockTime(Date unlockTime) {
		this.unlockTime = unlockTime;
	}

}
