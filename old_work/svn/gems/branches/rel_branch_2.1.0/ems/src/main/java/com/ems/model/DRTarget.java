package com.ems.model;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Shiv Mohan
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DRTarget implements Serializable {

    private static final long serialVersionUID = -7223349608424876732L;
    public static final String ENABLED = "Yes";
    public static final String DISABLED = "No";

    private Long id;
    private String priceLevel;
    private Double pricing;
    private Integer duration;
    private Integer targetReduction;
    private String enabled;
    private Date startTime;

    public DRTarget() {
    }

    public DRTarget(Long id, String priceLevel, Double pricing, Integer duration, Integer targetReduction,
            String enabled) {
        super();
        this.id = id;
        this.priceLevel = priceLevel;
        this.pricing = pricing;
        this.duration = duration;
        this.targetReduction = targetReduction;
        this.enabled = enabled;
    }

    public void initiate() {
        this.setEnabled(ENABLED);
        this.setStartTime(new Date());
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
}
