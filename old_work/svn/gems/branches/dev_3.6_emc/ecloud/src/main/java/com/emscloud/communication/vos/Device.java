/**
 * 
 */
package com.emscloud.communication.vos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Sreedhar
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Device {

	@XmlElement(name = "id")
	Long id;
	@XmlElement(name = "name")
	String name;
	@XmlElement(name = "xaxis")
	Integer xaxis;
	@XmlElement(name = "yaxis")
	Integer yaxis;
	@XmlElement(name = "location")
	String location;
	String type;

	@XmlElement(name = "areaId")
	private Long areaId;
	@XmlElement(name = "floor")
	Floor floor;
	@XmlElement(name = "floorId")
	private Long floorId;
	@XmlElement(name = "campusid")
	Long campusId;
	@XmlElement(name = "buildingid")
	Long buildingId;
	@XmlElement(name = "macaddress")
	String macAddress;
	@XmlElement(name = "version")
	String version;
	@XmlElement(name = "modelNo")
	String modelNo;
	@XmlElement(name = "pcbaPartNo")
	String pcbaPartNo;
	@XmlElement(name = "pcbaSerialNo")
	String pcbaSerialNo;
	@XmlElement(name = "hlaPartNo")
	String hlaPartNo;
	@XmlElement(name = "hlaSerialNo")
	String hlaSerialNo;
	String reachabilityStatus;
	String sev;
	String regExprValidation = "[a-zA-Z0-9-]+";

	/**
   * 
   */
	public Device() {

		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the id
	 */
	public Long getId() {

		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {

		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {

		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {

		this.name = name;
	}

	/**
	 * @return the xaxis
	 */
	public Integer getXaxis() {
		return xaxis;
	}

	/**
	 * @param xaxis
	 *            the xaxis to set
	 */
	public void setXaxis(Integer xaxis) {
		this.xaxis = xaxis;
	}

	/**
	 * @return the yaxis
	 */
	public Integer getYaxis() {
		return yaxis;
	}

	/**
	 * @param yaxis
	 *            the yaxis to set
	 */
	public void setYaxis(Integer yaxis) {
		this.yaxis = yaxis;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the floor
	 */
	public Floor getFloor() {
		return floor;
	}

	/**
	 * @param floor
	 *            the floor to set
	 */
	public void setFloor(Floor floor) {
		this.floor = floor;
	}

	/**
	 * @return the floorId
	 */
	public Long getFloorId() {
		return floorId;
	}

	/**
	 * @param floorId
	 *            the floorId to set
	 */
	public void setFloorId(Long floorId) {
		this.floorId = floorId;
	}

	/**
	 * @return the areaId
	 */
	public Long getAreaId() {
		return areaId;
	}

	/**
	 * @param areaId
	 *            the areaId to set
	 */
	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}

	/**
	 * @return the campusId
	 */
	public Long getCampusId() {
		return campusId;
	}

	/**
	 * @param campusId
	 *            the campusId to set
	 */
	public void setCampusId(Long campusId) {
		this.campusId = campusId;
	}

	/**
	 * @return the buildingId
	 */
	public Long getBuildingId() {
		return buildingId;
	}

	/**
	 * @param buildingId
	 *            the buildingId to set
	 */
	public void setBuildingId(Long buildingId) {
		this.buildingId = buildingId;
	}

	/**
	 * @return the macAddress
	 */
	public String getMacAddress() {

		return macAddress;
	}

	/**
	 * @param macAddress
	 *            the macAddress to set
	 */
	public void setMacAddress(String macAddress) {

		this.macAddress = macAddress;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {

		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {

		this.version = version;
	}

	/**
	 * @return the modelNo
	 */
	public String getModelNo() {

		return modelNo;
	}

	/**
	 * @param modelNo
	 *            the modelNo to set
	 */
	public void setModelNo(String modelNo) {

		this.modelNo = modelNo;
	}

	/**
	 * @return the reachabilityStatus
	 */
	public String getReachabilityStatus() {

		return reachabilityStatus;
	}

	/**
	 * @param reachabilityStatus
	 *            the reachabilityStatus to set
	 */
	public void setReachabilityStatus(String reachabilityStatus) {

		this.reachabilityStatus = reachabilityStatus;
	}

	/**
	 * @return the severity
	 */
	public String getSev() {

		return sev;
	}

	/**
	 * @param severity
	 *            the severity to set
	 */
	public void setSev(String severity) {

		this.sev = severity;
	}

	/**
	 * @return the pcbaPartNo
	 */
	public String getPcbaPartNo() {
		return pcbaPartNo;
	}

	/**
	 * @param pcbaPartNo
	 *            the pcbaPartNo to set
	 */
	public void setPcbaPartNo(String pcbaPartNo) {
		if (pcbaPartNo != null && pcbaPartNo.matches(regExprValidation) == true)
			this.pcbaPartNo = pcbaPartNo;
	}

	/**
	 * @return the pcbaSerialNo
	 */
	public String getPcbaSerialNo() {
		return pcbaSerialNo;
	}

	/**
	 * @param pcbaSerialNo
	 *            the pcbaSerialNo to set
	 */
	public void setPcbaSerialNo(String pcbaSerialNo) {
		if (pcbaSerialNo != null
				&& pcbaSerialNo.matches(regExprValidation) == true)
			this.pcbaSerialNo = pcbaSerialNo;
	}

	/**
	 * @return the hlaPartNo
	 */
	public String getHlaPartNo() {
		return hlaPartNo;
	}

	/**
	 * @param hlaPartNo
	 *            the hlaPartNo to set
	 */
	public void setHlaPartNo(String hlaPartNo) {
		if (hlaPartNo != null && hlaPartNo.matches(regExprValidation) == true)
			this.hlaPartNo = hlaPartNo;
	}

	/**
	 * @return the hlaSerialNo
	 */
	public String getHlaSerialNo() {
		return hlaSerialNo;
	}

	/**
	 * @param hlaSerialNo
	 *            the hlaSerialNo to set
	 */
	public void setHlaSerialNo(String hlaSerialNo) {
		if (hlaSerialNo != null
				&& hlaSerialNo.matches(regExprValidation) == true)
			this.hlaSerialNo = hlaSerialNo;
	}

} // end of class Device
