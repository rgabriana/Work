package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Switch implements Serializable {

    private static final long serialVersionUID = 6311462105337952259L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "floorid")
    private Long floorId;
    private Long buildingId;
    @XmlElement(name = "campusid")
    private Long campusId;
    @XmlElement(name = "areaid")
    private Long areaId;
    @XmlElement(name = "xaxis")
    private Integer xaxis;
    @XmlElement(name = "yaxis")
    private Integer yaxis;
    
    private GemsGroup gemsGroup;
    @XmlElement(name = "modetype")
    private Short modeType;
    @XmlElement(name = "initialsceneactivetime")
    private Integer initialSceneActiveTime;
    @XmlElement(name = "extendsceneactivetime")
    private Integer extendSceneActiveTime;
    @XmlElement(name = "operationmode")
    private Short operationMode;
    @XmlElement(name = "forceAutoMode")
    private Short forceAutoMode = 0;
    
    public Switch() {

    }
    
	public Switch(Long id, String name, Long floorId, Long buildingId,
			Long campusId, Integer xaxis, Integer yaxis, Long areaId,
			Short modeType, Integer initialSceneActiveTime,
			Integer extendSceneActiveTime, Short operationMode) {
        this.id = id;
        this.name = name;
        this.floorId = floorId;
        this.buildingId = buildingId;
        this.campusId = campusId;
        this.xaxis = xaxis;
        this.yaxis = yaxis;
        this.areaId = areaId;
        this.modeType = modeType;
        this.initialSceneActiveTime = initialSceneActiveTime;
        this.extendSceneActiveTime = extendSceneActiveTime;
        this.operationMode = operationMode;
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
     * @return the floorId
     */
    public Long getFloorId() {
        return floorId;
    }

    /**
     * @param floorId
     *            the floorId to set
     */
    public void setFloorId(Long floorId) {
        this.floorId = floorId;
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
     * @return the campusId
     */
    public Long getCampusId() {
        return campusId;
    }

    /**
     * @param campusId
     *            the campusId to set
     */
    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }

    /**
     * @return the buildingId
     */
    public Long getBuildingId() {
        return buildingId;
    }

    /**
     * @param buildingId
     *            the buildingId to set
     */
    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    /**
     * @return the xaxis
     */
    public Integer getXaxis() {
        return xaxis;
    }

    /**
     * @param xaxis
     *            the xaxis to set
     */
    public void setXaxis(Integer xaxis) {
        this.xaxis = xaxis;
    }

    /**
     * @return the yaxis
     */
    public Integer getYaxis() {
        return yaxis;
    }

    /**
     * @param yaxis
     *            the yaxis to set
     */
    public void setYaxis(Integer yaxis) {
        this.yaxis = yaxis;
    }

	public Long getAreaId() {
		return areaId;
	}

	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}

	/**
	 * @return the gemsGroup
	 */
	public GemsGroup getGemsGroup() {
		return gemsGroup;
	}

	/**
	 * @param gemsGroup the gemsGroup to set
	 */
	public void setGemsGroup(GemsGroup gemsGroup) {
		this.gemsGroup = gemsGroup;
	}

	/**
	 * @return the modeType
	 */
	public Short getModeType() {
		return modeType;
	}

	/**
	 * @param modeType the modeType to set
	 */
	public void setModeType(Short modeType) {
		this.modeType = modeType;
	}

	/**
	 * @return the initialSceneActiveTime
	 */
	public Integer getInitialSceneActiveTime() {
		return initialSceneActiveTime;
	}

	/**
	 * @param initialSceneActiveTime the initialSceneActiveTime to set
	 */
	public void setInitialSceneActiveTime(Integer initialSceneActiveTime) {
		this.initialSceneActiveTime = initialSceneActiveTime;
	}

	public Integer getExtendSceneActiveTime() {
		return extendSceneActiveTime;
	}

	public void setExtendSceneActiveTime(Integer extendSceneActiveTime) {
		this.extendSceneActiveTime = extendSceneActiveTime;
	}

	/**
	 * @return the operationMode
	 */
	public Short getOperationMode() {
		return operationMode;
	}

	/**
	 * @param operationMode the operationMode to set
	 */
	public void setOperationMode(Short operationMode) {
		this.operationMode = operationMode;
	}

	public Short getForceAutoMode() {
		return forceAutoMode;
	}

	public void setForceAutoMode(Short forceAutoMode) {
		this.forceAutoMode = forceAutoMode;
	}

}
