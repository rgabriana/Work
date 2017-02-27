package com.ems.model;

import java.util.Date;

/**
 * 
 @author Abhishek sinha
 * 
 */
public class FixtureImageVersion {

    private static final long serialVersionUID = 6311363006338951159L;
    private Long id;
    private FixtureImages fixtureImages;
    private FixtureUpgradeTime fixtureUpgradeTime;
    private Boolean upgradeStatus;
    private Date upgradeDate;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param sensorId
     *            the sensorId to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the fixtureImages
     */
    public FixtureImages getFixtureImages() {
        return fixtureImages;
    }

    /**
     * @param fixtureImages
     *            the fixtureImages to set
     */
    public void setFixtureImages(FixtureImages fixtureImages) {
        this.fixtureImages = fixtureImages;
    }

    /**
     * @return the fixtureUpgradeTime
     */
    public FixtureUpgradeTime getFixtureUpgradeTime() {
        return fixtureUpgradeTime;
    }

    /**
     * @param fixtureUpgradeTime
     *            the fixtureUpgradeTime to set
     */
    public void setFixtureUpgradeTime(FixtureUpgradeTime fixtureUpgradeTime) {
        this.fixtureUpgradeTime = fixtureUpgradeTime;
    }

    /**
     * @return the upgradeStatus
     */
    public Boolean getUpgradeStatus() {
        return upgradeStatus;
    }

    /**
     * @param upgradeStatus
     *            the upgradeStatus to set
     */
    public void setUpgradeStatus(Boolean upgradeStatus) {
        this.upgradeStatus = upgradeStatus;
    }

    /**
     * @return the upgradeDate
     */
    public Date getUpgradeDate() {
        return upgradeDate;
    }

    /**
     * @param upgradeDate
     *            the upgradeDate to set
     */
    public void setUpgradeDate(Date upgradeDate) {
        this.upgradeDate = upgradeDate;
    }

}
