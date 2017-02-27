package com.ems.vo;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.types.DrLevel;
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DRStatus implements Serializable {
    private static final long serialVersionUID = -417271237512093198L;
    @XmlElement(name = "status")
	Boolean status = false;
    @XmlElement(name = "timeremaning")
	Integer timeremaning =0;
    @XmlElement(name = "level")
	DrLevel level = DrLevel.SPECIAL;
    @XmlElement(name = "type")
    String type="";
    @XmlElement(name = "price")
    Double price;
    
    Date startTime;
    Integer duration;
    Long jitter;
    
	/**
	 * @return the status
	 */
	public Boolean getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(Boolean status) {
		this.status = status;
	}
	/**
	 * @return the duration
	 */
	public Integer getDuration() {
		return duration;
	}
	/**
	 * @param duration the duration to set
	 */
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
	/**
	 * @return the level
	 */
	public DrLevel getLevel() {
		return level;
	}
	/**
	 * @param level the level to set
	 */
	public void setLevel(DrLevel level) {
		this.level = level;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	/**
	 * @return the timeremaning
	 */
	public Integer getTimeremaning() {
		return timeremaning;
	}
	/**
	 * @param timeremaning the timeremaning to set
	 */
	public void setTimeremaning(Integer timeremaning) {
		this.timeremaning = timeremaning;
	}
	/**
	 * @return the price
	 */
	public Double getPrice() {
		return price;
	}
	/**
	 * @param price the price to set
	 */
	public void setPrice(Double price) {
		this.price = price;
	}
	/**
	 * @return the jitter
	 */
	public Long getJitter() {
		return jitter;
	}
	/**
	 * @param jitter the jitter to set
	 */
	public void setJitter(Long jitter) {
		this.jitter = jitter;
	}
}