package com.ems.vo.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="sensorconfig")
@XmlAccessorType(XmlAccessType.NONE)
public class SensorConfig {
	public static final int typeSensor = 0; 
	public static final int typeUnmanaged = 1; 
	public static final int typePlugload = 3;

	@XmlElement(name = "type")
	String type ;
	@XmlElement(name = "modelno")
	String modelNo ;
	@XmlElement(name = "serialno")
	String serialNo ;
	@XmlElement(name = "version")
	String version ;
	@XmlElement(name = "ishopper")
	Short isHopper ;
	@XmlElement(name = "xaxis")
	Long x ;
	@XmlElement(name = "yaxis")
	Long y ;
	@XmlElement(name = "ballastname")
	String ballastName ;
	@XmlElement(name = "bulbname")
	String bulbName ;
	@XmlElement(name = "macaddress")
	String mac ;
	@XmlElement(name = "noofballasts")
	Long noOfBallasts ;
	@XmlElement(name = "voltage")
	Long voltage ;
	@XmlElement(name = "fixturetype")
	String fixtureType;
	@XmlElement(name = "fixturetypeid")
	Long fixtureTypeId;
	@XmlElement(name = "name")
	String name ;
	public String getFixtureType() {
		return fixtureType;
	}
	public void setFixtureType(String fixtureType) {
		this.fixtureType = fixtureType;
	}
	public Long getFixtureTypeId() {
		return fixtureTypeId;
	}
	public void setFixtureTypeId(Long fixtureTypeId) {
		this.fixtureTypeId = fixtureTypeId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long getX() {
		return x;
	}
	public void setX(Long x) {
		this.x = x;
	}
	public Long getY() {
		return y;
	}
	public void setY(Long y) {
		this.y = y;
	}
	public String getBallastName() {
		return ballastName;
	}
	public void setBallastName(String ballastName) {
		this.ballastName = ballastName;
	}
	public String getBulbName() {
		return bulbName;
	}
	public void setBulbName(String bulbName) {
		this.bulbName = bulbName;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public Long getNoOfBallasts() {
		return noOfBallasts;
	}
	public void setNoOfBallasts(Long noOfBallasts) {
		this.noOfBallasts = noOfBallasts;
	}
	public Long getVoltage() {
		return voltage;
	}
	public void setVoltage(Long voltage) {
		this.voltage = voltage;
	}
	public String getModelNo() {
		return modelNo;
	}
	public void setModelNo(String modelNo) {
		this.modelNo = modelNo;
	}
	public String getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Short getIsHopper() {
		return isHopper;
	}
	public void setIsHopper(Short isHopper) {
		this.isHopper = isHopper;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}