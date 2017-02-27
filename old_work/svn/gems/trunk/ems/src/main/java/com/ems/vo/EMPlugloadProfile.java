package com.ems.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.model.PlugloadProfileHandler;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EMPlugloadProfile implements Serializable {

    private static final long serialVersionUID = 431776385828817164L;

    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "profileNo")
    private Short profileNo;
    @XmlElement(name = "derivedFromGroup")
    private Long derivedFromGroup;
    @XmlElement(name = "plugloadProfileTemplate")
    private Long plugloadProfileTemplate;
    @XmlElement(name = "plugloadProfileTemplateName")
    private String plugloadProfileTemplateName;
    @XmlElement(name = "plugloadProfileHandler")
    private PlugloadProfileHandler plugloadProfileHandler;
    
	public EMPlugloadProfile() {
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
     * @return the profileNo
     */
    public Short getProfileNo() {
        return profileNo;
    }

    /**
     * @param profileNo the profileNo to set
     */
    public void setProfileNo(Short profileNo) {
        this.profileNo = profileNo;
    }

    /**
     * @return the derivedFromGroup
     */
    public Long getDerivedFromGroup() {
        return derivedFromGroup;
    }

    /**
     * @param derivedFromGroup the derivedFromGroup to set
     */
    public void setDerivedFromGroup(Long derivedFromGroup) {
        this.derivedFromGroup = derivedFromGroup;
    }

	public void setPlugloadProfileHandler(PlugloadProfileHandler plugloadProfileHandler) {
		this.plugloadProfileHandler = plugloadProfileHandler;
	}

	public PlugloadProfileHandler getPlugloadProfileHandler() {
		return plugloadProfileHandler;
	}

	public void setPlugloadProfileTemplate(Long plugloadProfileTemplate) {
		this.plugloadProfileTemplate = plugloadProfileTemplate;
	}

	public Long getPlugloadProfileTemplate() {
		return plugloadProfileTemplate;
	}

	public void setPlugloadProfileTemplateName(
			String plugloadProfileTemplateName) {
		this.plugloadProfileTemplateName = plugloadProfileTemplateName;
	}

	public String getPlugloadProfileTemplateName() {
		return plugloadProfileTemplateName;
	}
}
