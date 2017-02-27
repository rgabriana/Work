package com.enlightedinc.hvac.model;

import java.io.Serializable;

public class ZonesSensor  implements Serializable{
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 8410977588249582789L;
    private Long id;
    private Zone zone;
    private Sensor sensor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public Zone getZone() {
    	return zone;
    }

	public void setZone(Zone zones) {
    	this.zone = zones;
    }

	public Sensor getSensor() {
    	return sensor;
    }

	public void setSensor(Sensor sensor) {
    	this.sensor = sensor;
    }

   

}
