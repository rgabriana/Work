package com.ems.model;

/**
 * 
 @author Abhishek sinha
 * 
 */
public class FixtureUpgradeTime {

    private static final long serialVersionUID = 6311363006338951159L;
    private Long id;
    private String upgradeTime;

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
     * @return the upgradeTime
     */
    public String getUpgradeTime() {
        return upgradeTime;
    }

    /**
     * @param upgradeTime
     *            the upgradeTime to set
     */
    public void setUpgradeTime(String upgradeTime) {
        this.upgradeTime = upgradeTime;
    }

}
