package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SceneLevel implements Serializable {
    private static final long serialVersionUID = 7454363205137456257L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "switchid")
    private Long switchId;
    @XmlElement(name = "sceneid")
    private Long sceneId;
    @XmlElement(name = "fixtureid")
    private Long fixtureId;
    @XmlElement(name = "lightlevel")
    private Integer lightLevel;

    public SceneLevel() {

    }

    public SceneLevel(Long id, Long switchId, Long sceneId, Long fixtureId, Integer lightLevel) {
        this.id = id;
        this.switchId = switchId;
        this.sceneId = sceneId;
        this.fixtureId = fixtureId;
        this.lightLevel = lightLevel;

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
     * @return the name
     */
    public Long getSwitchId() {
        return switchId;
    }

    /**
     * @param switchId
     *            the switchId to set
     */
    public void setSwitchId(Long switchId) {
        this.switchId = switchId;
    }

    /**
     * @return the name
     */
    public Long getSceneId() {
        return sceneId;
    }

    /**
     * @param sceneId
     *            the sceneId to set
     */
    public void setSceneId(Long sceneId) {
        this.sceneId = sceneId;
    }

    /**
     * @return the name
     */
    public Long getFixtureId() {
        return fixtureId;
    }

    /**
     * @param fixtureId
     *            the fixtureId to set
     */
    public void setFixtureId(Long fixtureId) {
        this.fixtureId = fixtureId;
    }

    /**
     * @return the name
     */
    public Integer getLightLevel() {
        return lightLevel;
    }

    /**
     * @param switchId
     *            the switchId to set
     */
    public void setLightLevel(Integer lightLevel) {
        this.lightLevel = lightLevel;
    }
}
