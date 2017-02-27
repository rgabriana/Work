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
public class AggregatedEnergyData extends EnergyData {

	@XmlElement(name = "motionEventCount")
	private Long motionEventCount;
	
	/**
	 * 
	 */
	public AggregatedEnergyData() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the motionEventCount
	 */
	public Long getMotionEventCount() {
		return motionEventCount;
	}

	/**
	 * @param motionEventCount the motionEventCount to set
	 */
	public void setMotionEventCount(Long motionEventCount) {
		this.motionEventCount = motionEventCount;
	}

} //end of class AggregatedEnergyData
