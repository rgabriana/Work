/**
 * 
 */
package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author yogesh
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SystemConfiguration implements Serializable {

    private static final long serialVersionUID = 3417517521123672780L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "value")
    private String value;

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
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     *            the Name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the Value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * @param name
     *            the Value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

}