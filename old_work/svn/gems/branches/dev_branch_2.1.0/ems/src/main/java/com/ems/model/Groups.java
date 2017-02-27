package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@XmlRootElement(name = "group")
@XmlAccessorType(XmlAccessType.NONE)
public class Groups implements Serializable {

    private static final long serialVersionUID = 431776385828817164L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "name")
    private String name;
    private Company company;
    private ProfileHandler profileHandler;

    public Groups() {
    }

    public Groups(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Groups(Long id, String name, long profileHandlerId, short profileChecksum, short globalProfileChecksum,
            byte profileGroupId) {
        this.id = id;
        this.name = name;
        profileHandler = new ProfileHandler();
        profileHandler.setId(profileHandlerId);
        profileHandler.setProfileChecksum(profileChecksum);
        profileHandler.setGlobalProfileChecksum(globalProfileChecksum);
        profileHandler.setProfileGroupId(profileGroupId);
    }

    // Temporary placeholder, need to fetch the dr reactivity, will have to be fixed in the
    // GroupDao layer with correct query.
    public Groups(Long id, String name, long profileHandlerId, short profileChecksum, short globalProfileChecksum,
            byte profileGroupId, byte drReactivity) {
        this.id = id;
        this.name = name;
        profileHandler = new ProfileHandler();
        profileHandler.setId(profileHandlerId);
        profileHandler.setProfileChecksum(profileChecksum);
        profileHandler.setGlobalProfileChecksum(globalProfileChecksum);
        profileHandler.setProfileGroupId(profileGroupId);
        profileHandler.setDrReactivity(drReactivity);
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
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
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the company
     */
    public Company getCompany() {
        return company;
    }

    /**
     * @param company
     *            the company to set
     */
    public void setCompany(Company company) {
        this.company = company;
    }

    /**
     * @return the profileHandler
     */
    public ProfileHandler getProfileHandler() {
        return profileHandler;
    }

    /**
     * @param profileHandler
     *            the profileHandler to set
     */
    public void setProfileHandler(ProfileHandler profileHandler) {
        this.profileHandler = profileHandler;
    }
}
