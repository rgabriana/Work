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
public class SwitchFixtures implements Serializable {

    private static final long serialVersionUID = -791151452649436223L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "switchid")
    private Long switchId;
    @XmlElement(name = "fixtureid")
    private Long fixtureId;

    public SwitchFixtures() {

    }

    public SwitchFixtures(Long id, Long switchId, Long fixtureId) {
        this.id = id;
        this.switchId = switchId;
        this.fixtureId = fixtureId;
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
     * @return the switchId
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
     * 
     * @return the fixtureId
     */
    public Long getFixtureId() {
        return fixtureId;
    }

    /**
     * 
     * @param fixtureId
     *            the fixtureId to set
     */
    public void setFixtureId(Long fixtureId) {
        this.fixtureId = fixtureId;
    }

}
