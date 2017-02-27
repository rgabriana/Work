package com.ems.vo.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="placementInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlacementInfoVO {
	 @XmlElement(name = "type")
	String type ;
	 @XmlElement(name = "buildingid")
	Long buildingId ;
	 @XmlElement(name = "floorid")
	Long floorId ;
	 @XmlElement(name = "campusid")
	Long campusId ;
	 @XmlElement(name = "xaxis")
	Long x ;
	 @XmlElement(name = "yaxis")
	Long y ;
	 @XmlElement(name = "ballastid")
	Long ballastId ;
	 @XmlElement(name = "bulbid")
	Long buldId ;
	 @XmlElement(name = "macaddress")
	String mac ;
	 @XmlElement(name = "noofballasts")
	Long noOfBallasts ;
	 @XmlElement(name = "voltage")
	Long voltage ;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long getBuildingId() {
		return buildingId;
	}
	public void setBuildingId(Long buildingId) {
		this.buildingId = buildingId;
	}
	public Long getFloorId() {
		return floorId;
	}
	public void setFloorId(Long floorId) {
		this.floorId = floorId;
	}
	public Long getCampusId() {
		return campusId;
	}
	public void setCampusId(Long campusId) {
		this.campusId = campusId;
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
	public Long getBallastId() {
		return ballastId;
	}
	public void setBallastId(Long ballastId) {
		this.ballastId = ballastId;
	}
	public Long getBuldId() {
		return buldId;
	}
	public void setBuldId(Long buldId) {
		this.buldId = buldId;
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
	
	


}
