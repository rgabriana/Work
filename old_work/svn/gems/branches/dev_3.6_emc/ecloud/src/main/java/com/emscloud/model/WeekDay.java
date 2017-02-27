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
@Table(name = "weekday", schema = "public")
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
    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="weekday_seq")
    @SequenceGenerator(name="weekday_seq", sequenceName="weekday_seq",allocationSize=1, initialValue=1)
	@Column(name = "id")
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
    @Column(name="day")
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
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "profile_configuration_id")
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
    @Column(name="short_order")
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
    @Column(name="type")
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
