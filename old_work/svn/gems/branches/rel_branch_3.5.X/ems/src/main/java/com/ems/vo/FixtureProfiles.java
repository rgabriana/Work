/**
 * 
 */
package com.ems.vo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author sreedhar.kamishetti
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FixtureProfiles {

	@XmlElement(name = "id")
	private Long id;
	@XmlElement(name = "name")
	private String name;
	@XmlElement(name = "currentProfile")
	private String currentProfile;
	@XmlElement(name = "groupId")
	private Long groupId;
	@XmlElement(name = "applicableProfiles")
	private List<Profile> applicableProfiles;
	
	public FixtureProfiles() {
		
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
	 * @return the currentProfile
	 */
	public String getCurrentProfile() {
		return currentProfile;
	}

	/**
	 * @param currentProfile the currentProfile to set
	 */
	public void setCurrentProfile(String currentProfile) {
		this.currentProfile = currentProfile;
	}

	/**
	 * @return the applicableProfiles
	 */
	public List<Profile> getApplicableProfiles() {
		return applicableProfiles;
	}

	/**
	 * @param applicableProfiles the applicableProfiles to set
	 */
	public void setApplicableProfiles(List<Profile> applicableProfiles) {
		this.applicableProfiles = applicableProfiles;
	}

	/**
	 * @return the groupId
	 */
	public Long getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	
} //end of class FixtureProfiles
