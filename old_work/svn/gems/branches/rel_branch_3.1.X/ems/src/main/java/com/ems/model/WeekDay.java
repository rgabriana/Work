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
@XmlRootElement(name = "weekday")
@XmlAccessorType(XmlAccessType.NONE)
public class WeekDay implements Serializable {

    private static final long serialVersionUID = 2426088921974146841L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "day")
    private String day;
    @XmlElement(name = "shortOrder")
    private Integer shortOrder;
    @XmlElement(name = "type")
    private String type;
    private ProfileConfiguration profileConfiguration;

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
     * @return the day
     */
    public String getDay() {
        return day;
    }

    /**
     * @param day
     *            the day to set
     */
    public void setDay(String day) {
        this.day = day;
    }

    /**
     * @return the profileConfiguration
     */
    public ProfileConfiguration getProfileConfiguration() {
        return profileConfiguration;
    }

    /**
     * @param profileConfiguration
     *            the profileConfiguration to set
     */
    public void setProfileConfiguration(ProfileConfiguration profileConfiguration) {
        this.profileConfiguration = profileConfiguration;
    }

    /**
     * @return the shortOrder
     */
    public Integer getShortOrder() {
        return shortOrder;
    }

    /**
     * @param shortOrder
     *            the shortOrder to set
     */
    public void setShortOrder(Integer shortOrder) {
        this.shortOrder = shortOrder;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    public WeekDay copy() {
        WeekDay wDay = new WeekDay();
        wDay.setDay(this.getDay());
        wDay.setShortOrder(this.getShortOrder());
        wDay.setType(this.getType());
        return wDay;
    }

    public void copyFrom(WeekDay target) {
        this.setDay(target.getDay());
        this.setShortOrder(target.getShortOrder());
        this.setType(target.getType());
    }
    
    public int compare(WeekDay p2) {
        if (!this.day.equals(p2.day) && !this.type.equals(p2.type))
            return -1;
        return 0;
    }
}
