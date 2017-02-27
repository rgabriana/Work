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
public class Scene implements Serializable {
    private static final long serialVersionUID = 6244463205137456257L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "switchid")
    private Long switchId;
    @XmlElement(name = "name")
    private String name;

    public Scene() {

    }

    public Scene(Long id, Long switchId, String name) {
        this.id = id;
        this.switchId = switchId;
        this.name = name;
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

}
