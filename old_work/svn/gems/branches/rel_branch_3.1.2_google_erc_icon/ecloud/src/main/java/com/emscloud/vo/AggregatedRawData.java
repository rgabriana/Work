/**
 * 
 */
package com.emscloud.vo;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author sreedhar.kamishetti
 *
 */
public class AggregatedRawData extends AggregatedSensorData {

	@XmlElement(name = "motionEvents")
	private Integer motionEvents;
	
	/**
	 * 
	 */
	public AggregatedRawData() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @return the motionEvents
	 */
	public Integer getMotionEvents() {
		return motionEvents;
	}
	/**
	 * @param motionEvents the motionEvents to set
	 */
	public void setMotionEvents(Integer motionEvents) {
		this.motionEvents = motionEvents;
	}

} //end of class AggregatedRawData
