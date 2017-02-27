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
	private Long motionEvents;
	
	/**
	 * 
	 */
	public AggregatedRawData() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @return the motionEvents
	 */
	public Long getMotionEvents() {
		return motionEvents;
	}
	/**
	 * @param motionEvents the motionEvents to set
	 */
	public void setMotionEvents(Long motionEvents) {
		this.motionEvents = motionEvents;
	}

} //end of class AggregatedRawData
