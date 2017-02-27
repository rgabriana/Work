package com.ems.model;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author Shiv Mohan
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DRTarget implements Serializable {

    private static final long serialVersionUID = -7223349608424876732L;

    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "pricelevel")
    private String priceLevel;
    @XmlElement(name = "pricing")
    private Double pricing;
    @XmlElement(name = "duration")
    private Integer duration;
    @XmlElement(name = "targetreduction")
    private Integer targetReduction;
    @XmlElement(name = "enabled")
    private String enabled;
    @XmlElement(name = "starttime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @XmlElement(name = "dridentifier")
    private String drIdentifier;
    @XmlElement(name = "drstatus")
    private String drStatus;
    @XmlElement(name = "drtype")
    private String drType;
    @XmlElement(name = "optin")
    private Boolean optIn;
    @XmlElement(name = "priority")
    private Integer priority;
    @XmlElement(name = "uid")
    private Integer uid;
    @XmlElement(name = "startafter")
    private Long startAfter;
    @XmlElement(name = "jitter")
    private Long jitter;
    @XmlElement(name = "canceltime")
    private Date cancelTime;
    @XmlElement(name = "description")
    private String description;

    public DRTarget() {
    }

    /**
     * @return unique identifier
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return price level
     */
    public String getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(String priceLevel) {
        this.priceLevel = priceLevel;
    }

    /**
     * @return pricing
     */
    public Double getPricing() {
        return pricing;
    }

    public void setPricing(Double pricing) {
        this.pricing = pricing;
    }

    /**
     * @return duration
     */
    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    /**
     * @return target reduction
     */
    public Integer getTargetReduction() {
        return targetReduction;
    }

    public void setTargetReduction(Integer targetReduction) {
        this.targetReduction = targetReduction;
    }

    /**
     * @return enabled
     */
    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    /**
     * @return start time
     */
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

	/**
	 * @return the drIdentifier
	 */
	public String getDrIdentifier() {
		return drIdentifier;
	}

	/**
	 * @param drIdentifier the drIdentifier to set
	 */
	public void setDrIdentifier(String drIdentifier) {
		this.drIdentifier = drIdentifier;
	}

	/**
	 * @return the drStatus
	 */
	public String getDrStatus() {
		return drStatus;
	}

	/**
	 * @param drStatus the drStatus to set
	 */
	public void setDrStatus(String drStatus) {
		this.drStatus = drStatus;
	}

	/**
	 * @return the drType
	 */
	public String getDrType() {
		return drType;
	}

	/**
	 * @param drType the drType to set
	 */
	public void setDrType(String drType) {
		this.drType = drType;
	}

	/**
	 * @return the optIn
	 */
	public Boolean getOptIn() {
		return optIn;
	}

	/**
	 * @param optIn the optIn to set
	 */
	public void setOptIn(Boolean optIn) {
		this.optIn = optIn;
	}

	/**
	 * @return the priority
	 */
	public Integer getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	/**
	 * @return the uid
	 */
	public Integer getUid() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(Integer uid) {
		this.uid = uid;
	}

	/**
	 * @return the startAfter
	 */
	public Long getStartAfter() {
		return startAfter;
	}

	/**
	 * @param startAfter the startAfter to set
	 */
	public void setStartAfter(Long startAfter) {
		this.startAfter = startAfter;
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

	/**
	 * @return the cancelTime
	 */
	public Date getCancelTime() {
		return cancelTime;
	}

	/**
	 * @param cancelTime the cancelTime to set
	 */
	public void setCancelTime(Date cancelTime) {
		this.cancelTime = cancelTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
