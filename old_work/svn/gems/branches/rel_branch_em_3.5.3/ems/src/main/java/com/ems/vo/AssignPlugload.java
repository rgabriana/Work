package com.ems.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.model.PlugloadGroups;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class AssignPlugload implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 431776385828817164L;

	@XmlElement(name = "sno")
	private Integer number;
	
	@XmlElement(name = "plugloadName")
	private String plugloadName;
	
	@XmlElement(name="plugloadId")
	private Long plugloadId;
	
	@XmlElement(name = "templateName")
	private String template;
	
	@XmlElement(name="currentPlugloadGroupId")
	private Long currentPlugloadGroupId;
	

	private Long templateId;
	
	public Long getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}

	
	@XmlElement(name = "profileList")
	private List<PlugloadGroups> profileList = new ArrayList<PlugloadGroups>();
	
	
	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
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

	public void setPlugloadName(String plugloadName) {
		this.plugloadName = plugloadName;
	}

	public String getPlugloadName() {
		return plugloadName;
	}

	public void setPlugloadId(Long plugloadId) {
		this.plugloadId = plugloadId;
	}

	public Long getPlugloadId() {
		return plugloadId;
	}

	public void setCurrentPlugloadGroupId(Long currentPlugloadGroupId) {
		this.currentPlugloadGroupId = currentPlugloadGroupId;
	}

	public Long getCurrentPlugloadGroupId() {
		return currentPlugloadGroupId;
	}

	public void setProfileList(List<PlugloadGroups> profileList) {
		this.profileList = profileList;
	}

	public List<PlugloadGroups> getProfileList() {
		return profileList;
	}
    
}
