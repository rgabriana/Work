package com.emscloud.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author sharad k Mahajan
 * 
 */
@Entity
@Table(name = "profile_template", schema = "public")
@XmlRootElement(name = "profiletemplate")
@XmlAccessorType(XmlAccessType.NONE)
public class ProfileTemplate implements Serializable {

    private static final long serialVersionUID = 431776385828817164L;
    private Long id;
    private String name;
   
    private Set<ProfileGroups> profiles;
 
    private List<ProfileGroups> profilesList;
    private boolean displayTemplate;
    private Long templateNo;
    @Transient
	private Long fixtureCount=0L;
    private Long profileCount=0L;
    
	public ProfileTemplate() {
    }

    public ProfileTemplate(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * @return the id
     */
    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="profile_template_seq")
    @SequenceGenerator(name="profile_template_seq", sequenceName="profile_template_seq",allocationSize=1, initialValue=1)
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
    @XmlElement(name = "name")
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
    @OneToMany(cascade={CascadeType.REMOVE},mappedBy="profileTemplate",fetch = FetchType.LAZY)
	public Set<ProfileGroups> getProfiles() {
		return profiles;
	}

	public void setProfiles(Set<ProfileGroups> profiles) {
		this.profiles = profiles;
	}
	 @Transient
    public List<ProfileGroups> getProfilesList(Set<ProfileGroups> setProfiles){       
        List<ProfileGroups> list = new ArrayList<ProfileGroups>();    
        if(setProfiles!=null)
        {
         	list.addAll(setProfiles);
        }
        return list;
    }
    @Transient
	public Long getFixtureCount() {
		return fixtureCount;
	}

	public void setFixtureCount(Long fixtureCount) {
		this.fixtureCount = fixtureCount;
	}
	@Column(name = "display_template")
	public boolean isDisplayTemplate() {
		return displayTemplate;
	}

	public void setDisplayTemplate(boolean displayTemplate) {
		this.displayTemplate = displayTemplate;
	}
	 @Transient
	public Long getProfileCount() {
		if(profiles!=null && profiles.size()>0)
		return (long) profiles.size();
		else
		return (long) 0;
	}

	public void setProfileCount(Long profileCount) {
		this.profileCount = profileCount;
	}
	 @Column(name = "template_no")
	public Long getTemplateNo() {
		return templateNo;
	}

	public void setTemplateNo(Long templateNo) {
		this.templateNo = templateNo;
	}

}
