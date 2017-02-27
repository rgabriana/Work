package com.communicator.model.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CompanyVO implements IData {
	
	@XmlElement(name = "actionType")
    private String actionType;
	@XmlElement(name = "id")
    private Long id;
	@XmlElement(name = "address")
    private String address;
	@XmlElement(name = "name")
    private String name;
	@XmlElement(name = "contact")
    private String contact;
	@XmlElement(name = "email")
    private String email;
	@XmlElement(name = "completionStatus")
    private Integer completionStatus;
	@XmlElement(name = "selfLogin")
    private Boolean selfLogin;
	@XmlElement(name = "validDomain")
    private String validDomain;
	@XmlElement(name = "notificationEmail")
    private String notificationEmail;
	@XmlElement(name = "severityLevel")
    private String severityLevel;
	@XmlElement(name = "timezone")
    private Long timezone;
	@XmlElement(name = "price")
    private Float price;
	@XmlElement(name = "timeZone")
    private String timeZone;

    public CompanyVO() {
    }

    public CompanyVO(String actionType, Long id, String address, String name, String contact, 
    		String email, Integer completionStatus,
    		Boolean selfLogin, String validDomain, String notificationEmail,
            String severityLevel, Long timezone,
            Float price, String timeZone) {
        this.setActionType(actionType);
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
        this.timeZone = timeZone;
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
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
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
	 * @return the contact
	 */
	public String getContact() {
		return contact;
	}

	/**
	 * @param contact the contact to set
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the completionStatus
	 */
	public Integer getCompletionStatus() {
		return completionStatus;
	}

	/**
	 * @param completionStatus the completionStatus to set
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
	 * @param selfLogin the selfLogin to set
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
	 * @param validDomain the validDomain to set
	 */
	public void setValidDomain(String validDomain) {
		this.validDomain = validDomain;
	}

	/**
	 * @return the notificationEmail
	 */
	public String getNotificationEmail() {
		return notificationEmail;
	}

	/**
	 * @param notificationEmail the notificationEmail to set
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
	 * @param severityLevel the severityLevel to set
	 */
	public void setSeverityLevel(String severityLevel) {
		this.severityLevel = severityLevel;
	}

	/**
	 * @return the timezone
	 */
	public Long getTimezone() {
		return timezone;
	}

	/**
	 * @param timezone the timezone to set
	 */
	public void setTimezone(Long timezone) {
		this.timezone = timezone;
	}

	/**
	 * @return the price
	 */
	public Float getPrice() {
		return price;
	}

	/**
	 * @param price the price to set
	 */
	public void setPrice(Float price) {
		this.price = price;
	}

	/**
	 * @return the timeZone
	 */
	public String getTimeZone() {
		return timeZone;
	}

	/**
	 * @param timeZone the timeZone to set
	 */
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
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

}
