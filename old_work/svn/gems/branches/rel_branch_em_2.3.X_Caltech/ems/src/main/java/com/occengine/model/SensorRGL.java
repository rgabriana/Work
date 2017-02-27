/**
 * 
 */
package com.occengine.model;

import java.io.Serializable;

import com.occengine.model.Zone;

/**
 * @author sreedhar.kamishetti
 *
 */
public class SensorRGL implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8572349371276991906L;

	private Long id;
	private String mac;
	private Zone zone;
	
	/**
	 * 
	 */
	public SensorRGL() {
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

}
