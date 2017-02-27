/**
 * 
 */
package com.ems.model;

/**
 * 
 */
import java.io.Serializable;
import java.util.Date;

/**
 * @author EMS
 */
public class InventoryDevice implements Serializable {

    private static final long serialVersionUID = 732745239737281298L;
    private Long id;
    private String macAddr;
    private String snapAddr;
    private String version;
    private String deviceName;
    private String networkId;
    private Date discoveredTime;
    private Long floorId;
    private int channel;
    private String ipAddress;
    private String subnetMask;
    private Integer commType;
    private Integer deviceType;
    private Long gwId;
    private String status;;
    private Short currApp;

    /**
   * 
   */
    public InventoryDevice() {
        // TODO Auto-generated constructor stub
    }

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
     * @return MAC Address
     */
    public String getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(String macAddr) {
        this.macAddr = macAddr;
    }

    /**
     * @return Snap Address
     */
    public String getSnapAddr() {
        return snapAddr;
    }

    public void setSnapAddr(String snapAddr) {
        this.snapAddr = snapAddr;
    }

    /**
     * @return version
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return device name
     */
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * @return network id
     */
    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    /**
     * @return discovered time
     */
    public Date getDiscoveredTime() {
        return discoveredTime;
    }

    public void setDiscoveredTime(Date discoveredTime) {
        this.discoveredTime = discoveredTime;
    }

    /**
     * @return status
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the floor
     */
    public Long getFloorId() {
        return floorId;
    }

    public void setFloorId(Long floorId) {
        this.floorId = floorId;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    /**
     * @return IP address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * @return subnetMask
     */
    public String getSubnetMask() {
        return subnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }

    /**
     * @return comm type
     */
    public Integer getCommType() {
        return commType;
    }

    public void setCommType(Integer commType) {
        this.commType = commType;
    }

    /**
     * @return device type
     */
    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * @return gw id
     */
    public Long getGwId() {
        return gwId;
    }

    public void setGwId(Long gwId) {
        this.gwId = gwId;
    }

    /**
     * @return currApp
     */
    public Short getCurrApp() {
        return currApp;
    }

    public void setCurrApp(Short currApp) {
        this.currApp = currApp;
    }

} // end of class InventoryDevice
