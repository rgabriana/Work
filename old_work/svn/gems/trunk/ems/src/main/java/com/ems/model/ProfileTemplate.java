package com.ems.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author sharad k Mahajan
 * 
 */
@XmlRootElement(name = "profiletemplate")
@XmlAccessorType(XmlAccessType.NONE)
public class ProfileTemplate implements Serializable {

    private static final long serialVersionUID = 431776385828817164L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "name")
    private String name;
    private Set<Groups> profiles;
    private List<Groups> profilesList;
    
    private boolean displayTemplate;
    private Long templateNo;

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

	public Set<Groups> getProfiles() {
		return profiles;
	}

	public void setProfiles(Set<Groups> profiles) {
		this.profiles = profiles;
	}
	
    public List<Groups> getProfilesList(Set<Groups> setProfiles){       
        List<Groups> list = new ArrayList<Groups>();    
        if(setProfiles!=null)
        {
         	list.addAll(setProfiles);
        }
        return list;
    }

	public Long getFixtureCount() {
		return fixtureCount;
	}

	public void setFixtureCount(Long fixtureCount) {
		this.fixtureCount = fixtureCount;
	}

	public boolean isDisplayTemplate() {
		return displayTemplate;
	}

	public void setDisplayTemplate(boolean displayTemplate) {
		this.displayTemplate = displayTemplate;
	}

	public Long getProfileCount() {
		return (long) profiles.size();
	}

	public void setProfileCount(Long profileCount) {
		this.profileCount = profileCount;
	}
	public Long getTemplateNo() {
		return templateNo;
	}

	public void setTemplateNo(Long templateNo) {
		this.templateNo = templateNo;
	}

}
