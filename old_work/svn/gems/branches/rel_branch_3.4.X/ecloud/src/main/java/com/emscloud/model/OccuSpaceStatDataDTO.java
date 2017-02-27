package com.emscloud.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Actual data representation in json. e.g. value = 9 means 9% of say Avg QTD of
 * Conference Room sensors = 560 means no of sensors in Conference Room and
 * totalSqFt = 56000 means conference rooms total sq ft
 * 
 * @author ADMIN
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class OccuSpaceStatDataDTO {

	// public StatisticTypeMasterDTO statMaster = new StatisticTypeMasterDTO();
	// public OccupancyTypeMasterDTO occMaster = new OccupancyTypeMasterDTO();
	@XmlElement(name = "spaceData")
	public List<SpaceDataDTO> spaceData;
	@XmlElement(name = "occMaster")
	public OccupancyMasterDTO occMaster = new OccupancyMasterDTO();

	public List<SpaceDataDTO> getSpaceData() {
		return spaceData;
	}

	public void setSpaceData(List<SpaceDataDTO> spaceData) {
		this.spaceData = spaceData;
	}

	public OccupancyMasterDTO getOccMaster() {
		return occMaster;
	}

	public void setOccMaster(OccupancyMasterDTO occMaster) {
		this.occMaster = occMaster;
	}

}
