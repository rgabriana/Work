package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FixtureClass implements Serializable {
	
	private static final long serialVersionUID = 1049510381838518894L;
	
	@XmlElement(name = "id")
	private Long id;
	
	@XmlElement(name = "name")
	private String name;
	
	@XmlElement(name = "noOfBallasts")
	private Integer noOfBallasts;
	
	@XmlElement(name = "voltage")
	private Integer voltage;	
	
	@XmlElement(name = "ballast")
	private Ballast ballast;	
	
	@XmlElement(name = "bulb")
	private Bulb bulb;
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	

	public Ballast getBallast() {
		return ballast;
	}

	public void setBallast(Ballast ballast) {
		this.ballast = ballast;
	}

	public void setBulb(Bulb bulb) {
		this.bulb = bulb;
	}

	public Bulb getBulb() {
		return bulb;
	}

	public void setNoOfBallasts(Integer noOfBallasts) {
		this.noOfBallasts = noOfBallasts;
	}

	public Integer getNoOfBallasts() {
		return noOfBallasts;
	}

	public void setVoltage(Integer voltage) {
		this.voltage = voltage;
	}

	public Integer getVoltage() {
		return voltage;
	}

}
