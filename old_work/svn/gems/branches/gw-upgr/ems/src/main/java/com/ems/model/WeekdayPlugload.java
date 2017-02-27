package com.ems.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "weekdayPlugload")
@XmlAccessorType(XmlAccessType.NONE)
public class WeekdayPlugload implements Serializable {
	
	public static final List<String> days = new ArrayList<String>();
	
	static{
		days.add("Monday");
		days.add("Tuesday");
		days.add("Wednesday");
		days.add("Thursday");
		days.add("Friday");
		days.add("Saturday");
		days.add("Sunday");
	}
	
	
	

    private static final long serialVersionUID = 1L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "day")
    private String day;
    @XmlElement(name = "shortOrder")
    private Integer shortOrder;
    @XmlElement(name = "type")
    private String type;
    /*@XmlElement(name = "plugloadProfileConfiguration")*/
    private PlugloadProfileConfiguration plugloadProfileConfiguration;

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

 

    public PlugloadProfileConfiguration getPlugloadProfileConfiguration() {
		return plugloadProfileConfiguration;
	}

	public void setPlugloadProfileConfiguration(
			PlugloadProfileConfiguration plugloadProfileConfiguration) {
		this.plugloadProfileConfiguration = plugloadProfileConfiguration;
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

    public WeekdayPlugload copy() {
    	WeekdayPlugload wDay = new WeekdayPlugload();
        wDay.setDay(this.getDay());
        wDay.setShortOrder(this.getShortOrder());
        wDay.setType(this.getType());
        return wDay;
    }

    public void copyFrom(WeekdayPlugload target) {
        this.setDay(target.getDay());
        this.setShortOrder(target.getShortOrder());
        this.setType(target.getType());
    }
    
    public int compare(WeekdayPlugload p2) {
        if (!this.day.equals(p2.day) && !this.type.equals(p2.type))
            return -1;
        return 0;
    }
}
