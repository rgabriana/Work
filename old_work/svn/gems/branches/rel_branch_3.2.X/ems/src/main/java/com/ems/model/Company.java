package com.ems.model;

import java.io.Serializable;
import java.util.List;

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
public class Company implements Serializable {

    private static final long serialVersionUID = -8346640146041015941L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "address")
    private String address;
    @XmlElement(name = "contact")
    private String contact;
    @XmlElement(name = "email")
    private String email;
    @XmlElement(name = "timezone")
    private Long timezone;
    private ProfileHandler profileHandler;
    @XmlElement(name = "completionstatus")
    private Integer completionStatus;
    private Boolean selfLogin;
    @XmlElement(name = "validdomain")
    private String validDomain;
    @XmlElement(name = "notificationemail")
    private String notificationEmail;
    @XmlElement(name = "severitylevel")
    private String severityLevel;
    @XmlElement(name = "price")
    private Float price;
    @XmlElement(name = "timezone_name")
    private String timeZone;
    @SuppressWarnings("unused")
    private boolean commissioned;
    @XmlElement(name = "campus")
    private List<Campus> campuses;
    @XmlElement(name = "tenant")
    private Tenant tenant;
    @XmlElement(name = "pricingType")
    private Integer pricingType;    
    private Long sweepTimerId;
    
    @XmlElement(name = "ntpEnable")
    private String ntpEnable = "Y";  
    @XmlElement(name = "ntpServers")
    private String ntpServers = "time.nist.gov";  
    
    public Company() {
    }

    public Company(Long id, String name, String address, String contact, String email, Long timezone,
            Integer completionStatus, Boolean selfLogin, String validDomain, String notificationEmail,
            String severityLevel, Float price, Long profileHandlerId, String timeZone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.email = email;
        this.timezone = timezone;
        this.completionStatus = completionStatus;
        this.selfLogin = selfLogin;
        this.validDomain = validDomain;
        this.notificationEmail = notificationEmail;
        this.severityLevel = severityLevel;
        this.price = price;
        ProfileHandler profileHandler = new ProfileHandler();
        profileHandler.setId(profileHandlerId);
        this.profileHandler = profileHandler;
        this.timeZone = timeZone;
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
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address
     *            the address to set
     */
    public void setAddress(String address) {
        this.address = address;
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
     * @return the completionStatus
     */
    public Integer getCompletionStatus() {
        return completionStatus;
    }

    /**
     * @param completionStatus
     *            the completionStatus to set
     */
    public void setCompletionStatus(Integer completionStatus) {
        this.completionStatus = completionStatus;
    }

    /**
     * @return the selfLogin
     */
    public Boolean getSelfLogin() {
        return selfLogin;
    }

    /**
     * @param selfLogin
     *            the selfLogin to set
     */
    public void setSelfLogin(Boolean selfLogin) {
        this.selfLogin = selfLogin;
    }

    /**
     * @return the validDomain
     */
    public String getValidDomain() {
        return validDomain;
    }

    /**
     * @param validDomain
     *            the validDomain to set
     */
    public void setValidDomain(String validDomain) {
        this.validDomain = validDomain;
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
     * @return the timezone
     */
    public Long getTimezone() {
        return timezone;
    }

    /**
     * @param timezone
     *            the timezone to set
     */
    public void setTimezone(Long timezone) {
        this.timezone = timezone;
    }

    /**
     * @return the timeZone
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * @param timeZone
     *            the timeZone to set
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
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

    /**
     * @return the notificationEmail
     */
    public String getNotificationEmail() {
        return notificationEmail;
    }

    /**
     * @param notificationEmail
     *            the notificationEmail to set
     */
    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }

    /**
     * @return the severityLevel
     */
    public String getSeverityLevel() {
        return severityLevel;
    }

    /**
     * @param severityLevel
     *            the severityLevel to set
     */
    public void setSeverityLevel(String severityLevel) {
        this.severityLevel = severityLevel;
    }

    /**
     * @return the price
     */
    public Float getPrice() {
        return price;
    }

    /**
     * @param price
     *            the price to set
     */
    public void setPrice(Float price) {
        this.price = price;
    }

    public List<Campus> getCampuses() {
        return campuses;
    }

    public void setCampuses(List<Campus> campuses) {
        this.campuses = campuses;
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

	public void setPricingType(Integer pricingType) {
		this.pricingType = pricingType;
	}

	public Integer getPricingType() {
		return pricingType;
	}

	public String getNtpEnable() {
		return ntpEnable;
}

	public void setNtpEnable(String ntpEnable) {
		this.ntpEnable = ntpEnable;
	}

	public String getNtpServers() {
		return ntpServers;
	}

	public void setNtpServers(String ntpServers) {
		this.ntpServers = ntpServers;
	}
}
