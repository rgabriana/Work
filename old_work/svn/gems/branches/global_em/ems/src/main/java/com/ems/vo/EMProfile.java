package com.ems.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.model.ProfileHandler;

/**
 * 
 * @author Sharad K Mahajan
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EMProfile implements Serializable {

    private static final long serialVersionUID = 431776385828817164L;

    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "profileNo")
    private Short profileNo;
    @XmlElement(name = "derivedFromGroup")
    private Long derivedFromGroup;
    @XmlElement(name = "profileTemplate")
    private Long profileTemplate;
    @XmlElement(name = "profileTemplateName")
    private String profileTemplateName;
    @XmlElement(name = "profilehandler")
    private ProfileHandler profileHandler;
    
	public EMProfile() {
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
     * @return the profileHandler
     */
    public ProfileHandler getProfileHandler() {
        return profileHandler;
    }

    /**
     * @param profileHandler the profileHandler to set
     */
    public void setProfileHandler(ProfileHandler profileHandler) {
        this.profileHandler = profileHandler;
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

    /**
     * @return the profileTemplate
     */
    public Long getProfileTemplate() {
        return profileTemplate;
    }

    /**
     * @param profileTemplate the profileTemplate to set
     */
    public void setProfileTemplate(Long profileTemplate) {
        this.profileTemplate = profileTemplate;
    }

    /**
     * @return the profileTemplateName
     */
    public String getProfileTemplateName() {
        return profileTemplateName;
    }

    /**
     * @param profileTemplateName the profileTemplateName to set
     */
    public void setProfileTemplateName(String profileTemplateName) {
        this.profileTemplateName = profileTemplateName;
    }
}
