package com.emscloud.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author mark.clark
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmFloor {
	
	@XmlElement(name = "floorId")
	private long floorId;

	@XmlElement(name = "emId")
	private long emId;

	public long getFloorId() {
		return floorId;
	}

	public void setFloorId(long floorId) {
		this.floorId = floorId;
	}

	public long getEmId() {
		return emId;
	}

	public void setEmId(long emId) {
		this.emId = emId;
	}
}
