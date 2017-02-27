package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.action.SpringContext;
import com.ems.types.DeviceType;

/**
 * 
 @author Shilpa
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class PlacedFixture implements Serializable {

	private static final long serialVersionUID = 4668327467322946053L;
	
	@XmlElement(name = "id")
	private Long id;
	@XmlElement(name = "name")
	private String name;
	@XmlElement(name = "ballast")
	private Ballast ballast;
	@XmlElement(name = "bulb")
	private Bulb	bulb;
	@XmlElement(name = "location")
	private String location;
	@XmlElement(name = "macAddress")
	private String macAddress;
	@XmlElement(name = "floorId")
	private Long floorId;
	@XmlElement(name = "type")
	private String type;
	@XmlElement(name = "campusId")
	private Long campusId;
	@XmlElement(name = "buildingId")
	private Long buildingId;
	@XmlElement(name = "xaxis")
	private Integer xaxis;
	@XmlElement(name = "yaxis")
	private Integer yaxis;
	@XmlElement(name = "voltage")
	private short voltage;
	@XmlElement(name = "noOfFixtures")
	private Integer noOfFixtures;
	
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
	public Bulb getBulb() {
		return bulb;
	}
	public void setBulb(Bulb bulb) {
		this.bulb = bulb;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	public Long getFloorId() {
		return floorId;
	}
	public void setFloorId(Long floorId) {
		this.floorId = floorId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long getCampusId() {
		return campusId;
	}
	public void setCampusId(Long campusId) {
		this.campusId = campusId;
	}
	public Long getBuildingId() {
		return buildingId;
	}
	public void setBuildingId(Long buildingId) {
		this.buildingId = buildingId;
	}
	public Integer getXaxis() {
		return xaxis;
	}
	public void setXaxis(Integer xaxis) {
		this.xaxis = xaxis;
	}
	public Integer getYaxis() {
		return yaxis;
	}
	public void setYaxis(Integer yaxis) {
		this.yaxis = yaxis;
	}
	public short getVoltage() {
		return voltage;
	}
	public void setVoltage(short voltage) {
		this.voltage = voltage;
	}
	public Integer getNoOfFixtures() {
		return noOfFixtures;
	}
	public void setNoOfFixtures(Integer noOfFixtures) {
		this.noOfFixtures = noOfFixtures;
	}
}