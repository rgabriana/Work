package com.ems.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.model.Groups;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class AssignFixture implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 431776385828817164L;

	@XmlElement(name = "sno")
	Integer number;
	
	@XmlElement(name = "fixtureName")
	String fixtureName;
	
	@XmlElement(name="fixtureId")
	Long fixtureId;
	
	@XmlElement(name = "templateName")
	String template;
	
	@XmlElement(name="currentGroupId")
	Long currentGroupId;
	

	Long templateId;
	
	public Long getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}

	public Long getCurrentGroupId() {
		return currentGroupId;
	}

	public void setCurrentGroupId(Long currentGroupId) {
		this.currentGroupId = currentGroupId;
	}

	@XmlElement(name = "profileList")
	List<Groups> profileList = new ArrayList<Groups>();
	
	public Long getFixtureId() {
		return fixtureId;
	}

	public void setFixtureId(Long fixtureId) {
		this.fixtureId = fixtureId;
	}

	public List<Groups> getProfileList() {
		return profileList;
	}

	public void setProfileList(List<Groups> profileList) {
		this.profileList = profileList;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}	

	public String getFixtureName() {
		return fixtureName;
	}

	public void setFixtureName(String fixtureName) {
		this.fixtureName = fixtureName;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
    
}
