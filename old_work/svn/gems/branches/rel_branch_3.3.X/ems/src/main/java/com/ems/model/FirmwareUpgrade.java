package com.ems.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */

public class FirmwareUpgrade implements Serializable {

    private static final long serialVersionUID = -8065155150542933435L;
    private Long id;
    private User user;
    private String fileName;
    private String version;
    private Date upgradeOn;
    private Integer deviceType;

    public FirmwareUpgrade() {
    }

    public FirmwareUpgrade(Long id, long userId, String sFirstname, String fileName, String version, Date upgradeOn,
            Integer deviceType) {
        this.id = id;
        User user = new User();
        user.setId(userId);
        user.setFirstName(sFirstname);
        this.setUser(user);
        this.fileName = fileName;
        this.version = version;
        this.upgradeOn = upgradeOn;
        this.deviceType = deviceType;
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
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the upgradeOn
     */
    public Date getUpgradeOn() {
        return upgradeOn;
    }

    /**
     * @param upgradeOn
     *            the upgradeOn to set
     */
    public void setUpgradeOn(Date upgradeOn) {
        this.upgradeOn = upgradeOn;
    }

    /**
     * @return the deviceType
     */
    public Integer getDeviceType() {
        return deviceType;
    }

    /**
     * @param deviceType
     *            the deviceType (0: Fixture, 1: Gateway) to set
     */
    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

}
