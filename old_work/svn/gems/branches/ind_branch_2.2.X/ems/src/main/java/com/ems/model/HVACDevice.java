/**
 * 
 */
package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author yogesh
 * Model to support HVAC devices such as Vent, AC etc;
 */
@XmlRootElement(name = "hvacdevice")
@XmlAccessorType(XmlAccessType.FIELD)
public class HVACDevice implements Serializable {
	private static final long serialVersionUID = 3079616841757585621L;
	@XmlElement(name = "id")
	private Long id;
	@XmlElement(name = "name")
	private String name;
	@XmlElement(name = "deviceid")
	private String deviceId;
	@XmlElement(name = "floorid")
	private Long floorId;
	@XmlElement(name = "buildingid")
	private Long buildingId;
	@XmlElement(name = "campusid")
	private Long campusId;
	@XmlElement(name = "areaid")
	private Long areaId;
	@XmlElement(name = "xaxis")
	private Integer xaxis;
	@XmlElement(name = "yaxis")
	private Integer yaxis;
	@XmlElement(name = "devicetype")
	private Integer deviceType = 1;
	@XmlElement(name = "controllerid")
	private String controllerId;

	public HVACDevice() {

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
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * @param deviceId the deviceId to set
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
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
	 * @return the deviceType
	 */
	public Integer getDeviceType() {
		return deviceType;
	}

	/**
	 * @param deviceType
	 *            the deviceType to set
	 */
	public void setDeviceType(Integer deviceType) {
		this.deviceType = deviceType;
	}

	public String getControllerId() {
		return controllerId;
	}

	public void setControllerId(String controllerId) {
		this.controllerId = controllerId;
	}

}
