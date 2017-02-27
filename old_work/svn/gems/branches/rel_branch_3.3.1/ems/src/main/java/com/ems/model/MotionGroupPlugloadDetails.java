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
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MotionGroupPlugloadDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    @XmlElement(name = "id")
	private Long id;
	private GemsGroupPlugload gemsGroupPlugload;
    @XmlElement(name = "type")
	private Short type;
    @XmlElement(name = "ambienttype")
	private Short ambientType;
    @XmlElement(name = "value")
	private Short useEmValue;
    @XmlElement(name = "loambval")
	private Short loAmbValue;
    @XmlElement(name = "hiambval")
	private Short hiAmbValue;
    @XmlElement(name = "tod")
	private Integer tod;
    @XmlElement(name = "lightlevel")
	private Short lightLevel;
	
	public MotionGroupPlugloadDetails() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public GemsGroupPlugload getGemsGroupPlugload() {
		return gemsGroupPlugload;
	}

	public void setGemsGroupPlugload(GemsGroupPlugload gemsGroupPlugload) {
		this.gemsGroupPlugload = gemsGroupPlugload;
	}

	public Short getType() {
		return type;
	}

	public void setType(Short type) {
		this.type = type;
	}

	public Short getAmbientType() {
		return ambientType;
	}

	public void setAmbientType(Short ambientType) {
		this.ambientType = ambientType;
	}

	public Short getUseEmValue() {
		return useEmValue;
	}

	public void setUseEmValue(Short useEmValue) {
		this.useEmValue = useEmValue;
	}

	public Short getLoAmbValue() {
		return loAmbValue;
	}

	public void setLoAmbValue(Short loAmbValue) {
		this.loAmbValue = loAmbValue;
	}

	public Short getHiAmbValue() {
		return hiAmbValue;
	}

	public void setHiAmbValue(Short hiAmbValue) {
		this.hiAmbValue = hiAmbValue;
	}

	public Integer getTod() {
		return tod;
	}

	public void setTod(Integer tod) {
		this.tod = tod;
	}

	public Short getLightLevel() {
		return lightLevel;
	}

	public void setLightLevel(Short lightLevel) {
		this.lightLevel = lightLevel;
	}
	
	public void copy(MotionGroupPlugloadDetails mgfd) {
		this.type = mgfd.type;
		this.ambientType = mgfd.ambientType;
		this.useEmValue = mgfd.useEmValue;
		this.loAmbValue = mgfd.loAmbValue;
		this.hiAmbValue = mgfd.hiAmbValue;
		this.tod = mgfd.tod;
		this.lightLevel = mgfd.lightLevel;
	}
}
