package com.emscloud.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Sharad K Mahajan
 * 
 */
@Entity
@Table(name = "profile_groups", schema = "public")
@XmlRootElement(name = "profilegroup")
@XmlAccessorType(XmlAccessType.NONE)
public class ProfileGroups implements Serializable {

    private static final long serialVersionUID = 431776385828817164L;
    private Long id;
    private String name;
    private ProfileHandler profileHandler;
    private Short profileNo;
    private ProfileGroups derivedFromGroup;
    private ProfileTemplate profileTemplate;
    private boolean displayProfile;
    private boolean defaultProfile;
    private Customer company;
    private Long fixtureCount=0L;
    private boolean globalCreatedProfile;

	public ProfileGroups() {
    }

    public ProfileGroups(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public ProfileGroups(Long id, String name, long profileHandlerId, short profileChecksum, short globalProfileChecksum,
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
    
    public ProfileGroups(Long id, String name, long templateId, boolean displayProfile,boolean defaultProfile,boolean globalCreatedProfile) {
        this.id = id;
        this.name = name;
        profileTemplate = new ProfileTemplate();
        profileTemplate.setId(templateId);
        this.displayProfile= displayProfile;
        this.defaultProfile= defaultProfile;
        this.globalCreatedProfile= globalCreatedProfile;
    }

    // Temporary placeholder, need to fetch the dr reactivity, will have to be fixed in the
    // GroupDao layer with correct query.
    public ProfileGroups(Long id, String name, long profileHandlerId, short profileChecksum, short globalProfileChecksum,
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
    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="profile_groups_seq")
    @SequenceGenerator(name="profile_groups_seq", sequenceName="profile_groups_seq",allocationSize=1, initialValue=1)
	@Column(name = "id",unique = true, nullable = false)
    @XmlElement(name = "id")
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
    @Column(name = "name")
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
     * @return the profileHandler
     */
    @ManyToOne(targetEntity = ProfileHandler.class,fetch = FetchType.LAZY)
	@JoinColumn(name = "profile_handler_id")
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
    @Column(name = "profile_no")
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
	@ManyToOne(targetEntity = ProfileGroups.class,fetch = FetchType.LAZY)
	@JoinColumn(name = "derived_from_group")
	public ProfileGroups getDerivedFromGroup() {
		return derivedFromGroup;
	}
	 /**
     * @param derivedFromGroup
     *           the derivedFromGroup to set
     */
	public void setDerivedFromGroup(ProfileGroups derivedFromGroup) {
		this.derivedFromGroup = derivedFromGroup;
	}

	@ManyToOne(targetEntity = ProfileTemplate.class,fetch = FetchType.LAZY)
	@JoinColumn(name = "template_id")
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
	@Column(name = "display_profile")
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
	@Column(name = "default_profile")
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
	@Transient
	public Long getFixtureCount() {
		return fixtureCount;
	}

	public void setFixtureCount(Long fixtureCount) {
		this.fixtureCount = fixtureCount;
	}

    /**
     * @return the company
     */
	@ManyToOne(targetEntity = Customer.class,fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id",nullable=true)
    public Customer getCompany() {
        return company;
    }

    /**
     * @param company the company to set
     */
    public void setCompany(Customer company) {
        this.company = company;
    }
    @Column(name = "global_created_profile")
	public boolean isGlobalCreatedProfile() {
		return globalCreatedProfile;
	}

	public void setGlobalCreatedProfile(boolean globalCreatedProfile) {
		this.globalCreatedProfile = globalCreatedProfile;
	}

}
