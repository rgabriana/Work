package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "plugloadGroups")
@XmlAccessorType(XmlAccessType.NONE)
public class PlugloadGroups implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@XmlElement(name = "id")
	private Long id;
	 @XmlElement(name = "name")
	private String name;
	 
	private Long plugloadCount=0L;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public PlugloadGroups() {
    }

	public Company getCompany() {
		return company;
	}
	public void setCompany(Company company) {
		this.company = company;
	}
	
	public Tenant getTenant() {
		return tenant;
	}
	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}



	public Short getProfileNo() {
		return profileNo;
	}
	public void setProfileNo(Short profileNo) {
		this.profileNo = profileNo;
	}
	public PlugloadGroups getDerivedFromGroup() {
		return derivedFromGroup;
	}
	public void setDerivedFromGroup(PlugloadGroups derivedFromGroup) {
		this.derivedFromGroup = derivedFromGroup;
	}
	public PlugloadProfileHandler getPlugloadProfileHandler() {
		return plugloadProfileHandler;
	}
	public void setPlugloadProfileHandler(
			PlugloadProfileHandler plugloadProfileHandler) {
		this.plugloadProfileHandler = plugloadProfileHandler;
	}
	public PlugloadProfileTemplate getPlugloadProfileTemplate() {
		return plugloadProfileTemplate;
	}
	public void setPlugloadProfileTemplate(
			PlugloadProfileTemplate plugloadProfileTemplate) {
		this.plugloadProfileTemplate = plugloadProfileTemplate;
	}

	public boolean isDisplayProfile() {
		return displayProfile;
	}
	public void setDisplayProfile(boolean displayProfile) {
		this.displayProfile = displayProfile;
	}
	public boolean isDefaultProfile() {
		return defaultProfile;
	}
	public void setDefaultProfile(boolean defaultProfile) {
		this.defaultProfile = defaultProfile;
	}

	public void setPlugloadCount(Long plugloadCount) {
		this.plugloadCount = plugloadCount;
	}
	public Long getPlugloadCount() {
		return plugloadCount;
	}

	@XmlElement(name = "company")
	private Company company;
	 
	@XmlElement(name = "plugloadProfileHandler")
	private PlugloadProfileHandler plugloadProfileHandler;
	
	@XmlElement(name = "profileNo")
	private Short profileNo;
	 
	private PlugloadGroups derivedFromGroup;
	 
	Tenant tenant;	 
	private PlugloadProfileTemplate plugloadProfileTemplate;
	
	@XmlElement(name = "displayProfile")
	private boolean displayProfile;
	
	@XmlElement(name = "defaultProfile")
	private boolean defaultProfile;
	
	 
	  


}
