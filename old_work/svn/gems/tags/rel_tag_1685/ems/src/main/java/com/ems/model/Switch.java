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
    // private Floor floor;
    private Long buildingId;
    @XmlElement(name = "campusid")
    private Long campusId;
    @XmlElement(name = "areaid")
    private Long areaId;
    @XmlElement(name = "xaxis")
    private Integer xaxis;
    @XmlElement(name = "yaxis")
    private Integer yaxis;
    @XmlElement(name = "dimmercontrol")
    private Integer dimmerControl = 0;
    @XmlElement(name = "sceneid")
    private Long sceneId;
    @XmlElement(name = "activecontrol")
    private Integer activeControl = 1;

    public Switch() {

    }
    
    public Switch(Long id, String name, Long floorId, Long buildingId, Long campusId, Integer xaxis, Integer yaxis,
            Integer dimmerControl, Long sceneId, Integer activeControl, Long areaId) {
        this.id = id;
        this.name = name;
        this.floorId = floorId;
        // this.floor = floor;
        this.buildingId = buildingId;
        this.campusId = campusId;
        this.xaxis = xaxis;
        this.yaxis = yaxis;
        this.dimmerControl = dimmerControl;
        this.sceneId = sceneId;
        this.activeControl = activeControl;
        this.areaId = areaId;
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

    /**
     * @return the dimmerControl
     */
    public Integer getDimmerControl() {
        return dimmerControl;
    }

    public void setDimmerControl(Integer dimmerControl) {
        this.dimmerControl = dimmerControl;
    }

    /**
     * @return the sceneId
     */
    public Long getSceneId() {
        return sceneId;
    }

    public void setSceneId(Long sceneId) {
        this.sceneId = sceneId;
    }

    /**
     * @return the activeControl
     */
    public Integer getActiveControl() {
        return activeControl;
    }

    public void setActiveControl(Integer activeControl) {
        this.activeControl = activeControl;
    }

	public Long getAreaId() {
		return areaId;
	}

	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}

}
