package com.ems.model;

import java.io.Serializable;

/**
 * 
 * @author Abhishek sinha
 * 
 */

public class Timezone implements Serializable {

    private static final long serialVersionUID = -4580938118425322244L;
    private Long id;
    private String name;
    private String description;

    public Timezone() {
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
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
