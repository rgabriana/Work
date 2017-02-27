package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author
 * 
 */
public class FixtureCustomGroupsProfile implements Serializable{

	private static final long serialVersionUID = 6482742821916778099L;
	
	private Long id;
    private Long fixtureId;
    private Long profileHandler;
    private Long groupId;

    public FixtureCustomGroupsProfile() {
    }

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public Long getFixtureId() {
		return fixtureId;
	}

	public void setFixtureId(Long fixtureId) {
		this.fixtureId = fixtureId;
	}

	public Long getProfileHandler() {
		return profileHandler;
	}

	public void setProfileHandler(Long profileHandler) {
		this.profileHandler = profileHandler;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	
}