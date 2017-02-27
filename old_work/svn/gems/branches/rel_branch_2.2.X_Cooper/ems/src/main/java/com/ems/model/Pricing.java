package com.ems.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Shiv Mohan
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Pricing implements Serializable {

    private static final long serialVersionUID = -4873459303001427271L;

    public static final int DEFAULT_YEAR = 1970;
    public static final int DEFAULT_MONTH = Calendar.JANUARY;
    public static final int DEFAULT_DAY = 1;

    private Long id;
    private String priceLevel;
    private String interval;
    private Double price;
    private String dayType;
    private Date fromTime;
    private Date toTime;

    public Pricing() {
    }

    public Pricing(Long id, String priceLevel, String interval, Double price) {
        super();
        this.id = id;
        this.priceLevel = priceLevel;
        this.interval = interval;
        this.price = price;
    }

    /**
     * @return the unique identifier
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
     * @return interval
     */
    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    /**
     * @return price
     */
    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    /**
     * @return day type
     */
    public String getDayType() {
        return dayType;
    }

    public void setDayType(String dayType) {
        this.dayType = dayType;
    }

    /**
     * @return from time
     */
    public Date getFromTime() {
        return fromTime;
    }

    public void setFromTime(Date fromTime) {
        this.fromTime = fromTime;
    }

    /**
     * @return to time
     */
    public Date getToTime() {
        return toTime;
    }

    public void setToTime(Date toTime) {
        this.toTime = toTime;
    }
}
