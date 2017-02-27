package com.emscloud.communication.vos;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Floor implements Serializable {

	private static final long serialVersionUID = 6311363006338951159L;
	@XmlElement(name = "id")
	private Long id;
	@XmlElement(name = "name")
	private String name;
	@XmlElement(name = "description")
	// Required on create floor page.
	private String description;
	@XmlElement(name = "floorplanurl")
	private String floorPlanUrl;

	private byte[] byteImage;
	@XmlElement(name = "installedsensors")
	private Integer noInstalledSensors;
	@XmlElement(name = "installedfixtures")
	private Integer noInstalledFixtures;
	@XmlElement(name = "tenant")
	private Long sweepTimerId;
	private Date uploadedOn;

	public Floor() {
	}

	public Floor(Long id, String name) {
		this.id = id;
		this.name = name;
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 */
	public String getFloorPlanUrl() {
		return floorPlanUrl;
	}

	public void setFloorPlanUrl(String floorPlanUrl) {
		this.floorPlanUrl = floorPlanUrl;
	}

	public byte[] getByteImage() {
		return byteImage;
	}

	public void setByteImage(byte[] byteImage) {
		this.byteImage = byteImage;
	}

	/**
	 * @return number of installed sensors
	 */
	public Integer getNoInstalledSensors() {
		return noInstalledSensors;
	}

	public void setNoInstalledSensors(Integer noInstalledSensors) {
		this.noInstalledSensors = noInstalledSensors;
	}

	/**
	 * @return number of installed fixtures
	 */
	public Integer getNoInstalledFixtures() {
		return noInstalledFixtures;
	}

	public void setNoInstalledFixtures(Integer noInstalledFixtures) {
		this.noInstalledFixtures = noInstalledFixtures;
	}

	/**
	 * @return the sweepTimerId
	 */
	public Long getSweepTimerId() {
		return sweepTimerId;
	}

	/**
	 * @param sweepTimerId
	 *            the sweepTimerId to set
	 */
	public void setSweepTimerId(Long sweepTimerId) {
		this.sweepTimerId = sweepTimerId;
	}

	public Date getUploadedOn() {
		return uploadedOn;
	}

	public void setUploadedOn(Date uploadedOn) {
		this.uploadedOn = uploadedOn;
	}

	@Override
	public String toString() {
		return "Floor [id=" + id + ", name=" + name + ", description="
				+ description + ", floorPlanUrl=" + floorPlanUrl
				+ ", byteImage=" + Arrays.toString(byteImage)
				+ ", noInstalledSensors=" + noInstalledSensors
				+ ", noInstalledFixtures=" + noInstalledFixtures
				+ ", sweepTimerId=" + sweepTimerId + ", uploadedOn="
				+ uploadedOn + "]";
	}
}
