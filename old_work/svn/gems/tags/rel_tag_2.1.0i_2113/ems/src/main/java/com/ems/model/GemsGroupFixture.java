package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class GemsGroupFixture implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -7847348169567111880L;

    @XmlElement(name = "id")
    private Long id;

    @XmlElement(name = "group")
    private GemsGroup group;

    @XmlElement(name = "fixture")
    private Fixture fixture;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the gems group
     */
    public GemsGroup getGroup() {
        return group;
    }

    public void setGroup(GemsGroup group) {
        this.group = group;
    }

    /**
     * @return the fixture
     */
    public Fixture getFixture() {
        return fixture;
    }

    public void setFixture(Fixture fixture) {
        this.fixture = fixture;
    }
}
