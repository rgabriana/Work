/**
 * 
 */
package com.ems.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author sreedhar.kamishetti
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DeviceFirmware {

	@XmlElement(name = "type")
	private String type;
	@XmlElement(name = "description")
	private String description;
	@XmlElement(name = "cpu")
	private String cpu;
	@XmlElement(name = "imageFile")
	private String imageFile;
	@XmlElement(name = "version")
	private String version;
	@XmlElement(name = "md5sum")
	private String md5sum;
	@XmlElement(name = "models")
	private String models;
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the cpu
	 */
	public String getCpu() {
		return cpu;
	}
	/**
	 * @param cpu the cpu to set
	 */
	public void setCpu(String cpu) {
		this.cpu = cpu;
	}
	/**
	 * @return the imageFile
	 */
	public String getImageFile() {
		return imageFile;
	}
	/**
	 * @param imageFile the imageFile to set
	 */
	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * @return the md5sum
	 */
	public String getMd5sum() {
		return md5sum;
	}
	/**
	 * @param md5sum the md5sum to set
	 */
	public void setMd5sum(String md5sum) {
		this.md5sum = md5sum;
	}
	/**
	 * @return the models
	 */
	public String getModels() {
		return models;
	}
	/**
	 * @param models the models to set
	 */
	public void setModels(String models) {
		this.models = models;
	}
	
}
