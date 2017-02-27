package com.emscloud.vo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author mark.clark
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmFloorList {

	@XmlElement(name = "floor")
	private List<EmFloor> floors = new ArrayList<EmFloor>();

	public List<EmFloor> getFloors() {
		return floors;
	}

	public void setFloors(List<EmFloor> floors) {
		this.floors = floors;
	}
}
