package com.ems.model;

import java.io.Serializable;

/**
 * @author Shiv Mohan
 */
public class EventType implements Serializable {

    private static final long serialVersionUID = -4730881609086124447L;

    private Long id;
    private String type;
    private String description;

    /**
     * @return id
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return event type
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return description
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
