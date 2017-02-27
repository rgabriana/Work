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
    @XmlElement(name = "profileNo")
    private Short profileNo;
    private Groups derivedFromGroup;
    private Tenant tenant; 
    private ProfileTemplate profileTemplate;
    private boolean displayProfile;
    @XmlElement(name = "defaultProfile")
    private boolean defaultProfile;

    private Long fixtureCount=0L;

	public Groups() {
    }

    public Groups(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Groups(Long id, String name, long profileHandlerId, short profileChecksum, short globalProfileChecksum,
    		Short profileGroupId, Short profileNo,long templateId,boolean defaultProfile) {
        this.id = id;
        this.name = name;
        this.profileNo = profileNo;
        profileHandler = new ProfileHandler();
        profileHandler.setId(profileHandlerId);
        profileHandler.setProfileChecksum(profileChecksum);
        profileHandler.setGlobalProfileChecksum(globalProfileChecksum);
        profileHandler.setProfileGroupId(profileGroupId);
        profileTemplate = new ProfileTemplate();
        profileTemplate.setId(templateId);
        this.defaultProfile= defaultProfile;
    }
    
    public Groups(Long id, String name, long templateId, boolean displayProfile,boolean defaultProfile) {
        this.id = id;
        this.name = name;
        profileTemplate = new ProfileTemplate();
        profileTemplate.setId(templateId);
        this.displayProfile= displayProfile;
        this.defaultProfile= defaultProfile;
    }

    // Temporary placeholder, need to fetch the dr reactivity, will have to be fixed in the
    // GroupDao layer with correct query.
    public Groups(Long id, String name, long profileHandlerId, short profileChecksum, short globalProfileChecksum,
    		Short profileGroupId, byte drReactivity, Short profileNo,long templateId,boolean defaultProfile) {
        this.id = id;
        this.name = name;
        this.profileNo = profileNo;
        profileHandler = new ProfileHandler();
        profileHandler.setId(profileHandlerId);
        profileHandler.setProfileChecksum(profileChecksum);
        profileHandler.setGlobalProfileChecksum(globalProfileChecksum);
        profileHandler.setProfileGroupId(profileGroupId);
        profileHandler.setDrReactivity(drReactivity);
        profileTemplate = new ProfileTemplate();
        profileTemplate.setId(templateId);
        this.defaultProfile= defaultProfile;
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

	public Short getProfileNo() {
		return profileNo;
	}
	 /**
     * @param ProfileNo
     *            the ProfileNo to set
     */
	public void setProfileNo(Short profileNo) {
		this.profileNo = profileNo;
	}

	public Groups getDerivedFromGroup() {
		return derivedFromGroup;
	}
	 /**
     * @param derivedFromGroup
     *           the derivedFromGroup to set
     */
	public void setDerivedFromGroup(Groups derivedFromGroup) {
		this.derivedFromGroup = derivedFromGroup;
	}

	public Tenant getTenant() {
		return tenant;
	}
	 /**
     * @param Tenant
     *          the Tenant to set
     */
	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}
	
	public ProfileTemplate getProfileTemplate() {
		return profileTemplate;
	}
	/**
     * @param ProfileTemplate
     *          the ProfileTemplate to set
     */
	public void setProfileTemplate(ProfileTemplate profileTemplate) {
		this.profileTemplate = profileTemplate;
	}
	
	public boolean isDisplayProfile() {
		return displayProfile;
	}
	/**
     * @param displayProfile
     *          the displayProfile to set
     */
	public void setDisplayProfile(boolean displayProfile) {
		this.displayProfile = displayProfile;
	}
	public boolean isDefaultProfile() {
		return defaultProfile;
	}
	/**
     * @param defaultProfile
     *          the defaultProfile to set
     */
	public void setDefaultProfile(boolean defaultProfile) {
		this.defaultProfile = defaultProfile;
	}

	public Long getFixtureCount() {
		return fixtureCount;
	}

	public void setFixtureCount(Long fixtureCount) {
		this.fixtureCount = fixtureCount;
	}

}
