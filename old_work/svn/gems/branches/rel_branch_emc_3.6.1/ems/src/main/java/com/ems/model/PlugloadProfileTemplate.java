package com.ems.model;

import java.io.Serializable;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class PlugloadProfileTemplate implements Serializable{
	
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
/*	  id bigint NOT NULL,
	  name character varying(255),
	  display_template boolean DEFAULT true,
	  template_no bigint,
*/
	@XmlElement(name = "id")
	private Long id;
	
	@XmlElement(name = "name")
	String name;
	
	private Long plugloadCount=0L;
    private Long plugloadProfileCount=0L;
    
    private List<PlugloadGroups> plugloadProfilesList;
    
    public PlugloadProfileTemplate(){
    	
    }
    
    public PlugloadProfileTemplate(Long id, String name) {
        this.id = id;
        this.name = name;
    }
	
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
	
	public Long getTemplateNo() {
		return templateNo;
	}
	public void setTemplateNo(Long templateNo) {
		this.templateNo = templateNo;
	}
	public void setDisplayTemplate(boolean displayTemplate) {
		this.displayTemplate = displayTemplate;
	}
	public boolean isDisplayTemplate() {
		return displayTemplate;
	}
	
	public void setPlugloadCount(Long plugloadCount) {
		this.plugloadCount = plugloadCount;
	}
	public Long getPlugloadCount() {
		return plugloadCount;
	}

	public void setPlugloadProfileCount(Long plugloadProfileCount) {
		this.plugloadProfileCount = plugloadProfileCount;
	}
	public Long getPlugloadProfileCount() {
		return (long) plugloadProfiles.size();
	}
	
		
	public void setPlugloadProfilesList(List<PlugloadGroups> plugloadProfilesList) {
		this.plugloadProfilesList = plugloadProfilesList;
	}
	
	public List<PlugloadGroups> getPlugloadProfilesList(Set<PlugloadGroups> setPlugloadProfiles) {
		List<PlugloadGroups> list = new ArrayList<PlugloadGroups>();    
        if(setPlugloadProfiles!=null)
        {
         	list.addAll(setPlugloadProfiles);
        }
        return list;
	}
	@XmlElement(name = "displayTemplate")
	private boolean displayTemplate;
	@XmlElement(name = "templateNo")
	private Long templateNo;
	
	 private Set<PlugloadGroups> plugloadProfiles;
	public Set<PlugloadGroups> getPlugloadProfiles() {
		return plugloadProfiles;
	}
	public void setPlugloadProfiles(Set<PlugloadGroups> plugloadProfiles) {
		this.plugloadProfiles = plugloadProfiles;
	}

}
