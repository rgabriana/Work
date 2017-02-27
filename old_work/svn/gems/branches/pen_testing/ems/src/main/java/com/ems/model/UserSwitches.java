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
public class UserSwitches implements Serializable {

    private static final long serialVersionUID = 5934001953359079576L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "switchid")
    private Long switchId;
    @XmlElement(name = "userid")
    private Long userId;

    public UserSwitches() {

    }

    public UserSwitches(Long id, Long userId, Long switchId) {
        this.id = id;
        this.switchId = switchId;
        this.userId = userId;
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
    public Long getUserId() {
        return userId;
    }

    /**
     * 
     * @param userId
     *            the userId to set
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

}
