/**
 * 
 */
package com.emscloud.vo;

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
public class RawEnergyData extends EnergyData {

	@XmlElement(name = "motionBits")
	private Long motionBits;
	
	/**
	 * 
	 */
	public RawEnergyData() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the motionEvents
	 */
	public Long getMotionBits() {
		return motionBits;
	}

	/**
	 * @param motionEvents the motionEvents to set
	 */
	public void setMotionBits(Long motionBits) {
		this.motionBits = motionBits;
	}

} //end of class RawEnergyData