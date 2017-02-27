package com.ems.model;

import java.util.Date;

/**
 * 
 @author Abhishek sinha
 * 
 */
public class FixtureImages {

    private static final long serialVersionUID = 6311363006338951159L;
    private Long id;
    private String imageName;
    private String location;
    private Date uploadedOn;

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
     * @return the imageName
     */
    public String getImageName() {
        return imageName;
    }

    /**
     * @param imageName
     *            the imageName to set
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location
     *            the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the uploadedOn
     */
    public Date getUploadedOn() {
        return uploadedOn;
    }

    /**
     * @param uploadedOn
     *            the uploadedOn to set
     */
    public void setUploadedOn(Date uploadedOn) {
        this.uploadedOn = uploadedOn;
    }

}
