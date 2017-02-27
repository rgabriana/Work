/**
 * 
 */
package com.occengine.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author sreedhar.kamishetti
 *
 */
public class Sensor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8572349371276991906L;

	private Long id;
	private String mac;
	//private Long zoneId;	
	private Date lastStatusTime;
	
	private Integer occStatus;
	private Float avgTemperature;
	private Integer avgAmbientLight;
	
	private Zone zone;
	
	/**
	 * 
	 */
	public Sensor() {
		// TODO Auto-generated constructor stub
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
	 * @return the occStatus
	 */
	public Integer getOccStatus() {
		return occStatus;
	}

	/**
	 * @param occStatus the occStatus to set
	 */
	public void setOccStatus(Integer occStatus) {
		this.occStatus = occStatus;
	}

	/**
	 * @return the lastStatusTime
	 */
	public Date getLastStatusTime() {
		return lastStatusTime;
	}

	/**
	 * @param lastStatusTime the lastStatusTime to set
	 */
	public void setLastStatusTime(Date lastStatusTime) {
		this.lastStatusTime = lastStatusTime;
	}

	/**
	 * @return the avgTemperature
	 */
	public Float getAvgTemperature() {
		return avgTemperature;
	}

	/**
	 * @param avgTemperature the avgTemperature to set
	 */
	public void setAvgTemperature(Float avgTemperature) {
		this.avgTemperature = avgTemperature;
	}

	/**
	 * @return the mac
	 */
	public String getMac() {
		return mac;
	}

	/**
	 * @param mac the mac to set
	 */
	public void setMac(String mac) {
		this.mac = mac;
	}

	/**
	 * @return the avgAmbientLight
	 */
	public Integer getAvgAmbientLight() {
		return avgAmbientLight;
	}

	/**
	 * @param avgAmbientLight the avgAmbientLight to set
	 */
	public void setAvgAmbientLight(Integer avgAmbientLight) {
		this.avgAmbientLight = avgAmbientLight;
	}
	
	
	/**
	 * @return the zone
	 */
	public Zone getZone() {
		return zone;
	}

	/**
	 * @param zone the zone to set
	 */
	public void setZone(Zone zone) {
		this.zone = zone;
	}

	public static void main(String args[]) {
		
		String s = "SU-2-00";    
		byte[] bytes = s.getBytes();
		
		StringBuffer sb = new StringBuffer();
    for(int i = 0; i < bytes.length; i++) {
      sb.append(String.format("%x", bytes[i]));
      sb.append(' ');
    }
    System.out.println(sb.toString());		
		
	}
	
}
