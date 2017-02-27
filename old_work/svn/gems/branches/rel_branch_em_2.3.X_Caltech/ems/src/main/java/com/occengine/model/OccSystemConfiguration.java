/**
 * 
 */
package com.occengine.model;

import java.io.Serializable;

/**
 * @author yogesh
 */
public class OccSystemConfiguration implements Serializable {

    private static final long serialVersionUID = 3417517521123672780L;
    private Long id;
    private String name;
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
