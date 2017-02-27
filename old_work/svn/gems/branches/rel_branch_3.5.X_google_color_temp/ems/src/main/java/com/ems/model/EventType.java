package com.ems.model;

import java.io.Serializable;

import com.ems.util.Constants;

/**
 * @author Shiv Mohan
 */
public class EventType implements Serializable {

    private static final long serialVersionUID = -4730881609086124447L;

    private Long id;
    private String type;
    private String description;
    private Short active;
    private Short severity;
   
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
    
    /**
     * @return active
     */
    public Short getActive() {
        return active;
    }

    public void setActive(Short active) {
        this.active = active;
    }
    
    /**
     * @return severity
     */
    public Short getSeverity() {
        return severity;
    }

    public void setSeverity(Short severity) {
        this.severity = severity;
    }
    
    public String getSeverityString() {
    	
    	return EventsAndFault.getSeverityString(severity);
    	
    } //end of method getSeverityString
        
}
