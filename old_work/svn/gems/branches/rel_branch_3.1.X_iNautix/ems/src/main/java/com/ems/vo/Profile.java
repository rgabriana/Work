package com.ems.vo;

import javax.xml.bind.annotation.XmlElement;

public class Profile {

	@XmlElement(name = "name")
	private String name;
	@XmlElement(name = "groupId")
	private Long groupId;
	
	public Profile() {
		// TODO Auto-generated constructor stub
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

} //end of class Profile
