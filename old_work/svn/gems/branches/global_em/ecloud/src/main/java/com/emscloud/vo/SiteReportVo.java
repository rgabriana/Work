package com.emscloud.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SiteReportVo implements Serializable{
	
	private static final long serialVersionUID = -5404694233653717365L;
	@XmlElement(name ="id") 
	private Long id;
	@XmlElement(name ="name") 
	private String name;
	@XmlElement(name ="fixtureCount") 
	private Long fixtureCount;
	@XmlElement(name ="sensorCount") 
	private Long sensorCount;
	@XmlElement(name ="cuCount") 
	private Long cuCount;
	@XmlElement(name ="gatewayCount") 
	private Long gatewayCount;
	@XmlElement(name ="ballastCount")
	private Long ballastCount;
	@XmlElement(name ="lampsCount")
	private Long lampsCount;
	@XmlElement(name ="customer")
	private String customer;
	@XmlElement(name ="fxtypecount")
	private Long fxTypeCount;
	@XmlElement(name ="geoLocation")
	private String geoLocation;
	
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return the fixtureCount
     */
    public Long getFixtureCount() {
        return fixtureCount;
    }
    /**
     * @param fixtureCount the fixtureCount to set
     */
    public void setFixtureCount(Long fixtureCount) {
        this.fixtureCount = fixtureCount;
    }
    /**
     * @return the sensorCount
     */
    public Long getSensorCount() {
        return sensorCount;
    }
    /**
     * @param sensorCount the sensorCount to set
     */
    public void setSensorCount(Long sensorCount) {
        this.sensorCount = sensorCount;
    }
    public Long getCuCount() {
		return cuCount;
	}
	public void setCuCount(Long cuCount) {
		this.cuCount = cuCount;
	}
	/**
     * @return the gatewayCount
     */
    public Long getGatewayCount() {
        return gatewayCount;
    }
    /**
     * @param gatewayCount the gatewayCount to set
     */
    public void setGatewayCount(Long gatewayCount) {
        this.gatewayCount = gatewayCount;
    }
    /**
     * @return the ballastCount
     */
    public Long getBallastCount() {
        return ballastCount;
    }
    /**
     * @param ballastCount the ballastCount to set
     */
    public void setBallastCount(Long ballastCount) {
        this.ballastCount = ballastCount;
    }
    /**
     * @return the lampsCount
     */
    public Long getLampsCount() {
        return lampsCount;
    }
    /**
     * @param lampsCount the lampsCount to set
     */
    public void setLampsCount(Long lampsCount) {
        this.lampsCount = lampsCount;
    }
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }
    /**
     * @return the customer
     */
    public String getCustomer() {
        return customer;
    }
    /**
     * @param customer the customer to set
     */
    public void setCustomer(String customer) {
        this.customer = customer;
    }
	public Long getFxTypeCount() {
		return fxTypeCount;
	}
	public void setFxTypeCount(Long fxTypeCount) {
		this.fxTypeCount = fxTypeCount;
	}
	public void setGeoLocation(String geoLocation) {
		this.geoLocation = geoLocation;
	}
	public String getGeoLocation() {
		return geoLocation;
	}
		
}
