package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Wds implements Serializable {

	private static final long serialVersionUID = -8346640146081015941L;
    @XmlElement(name = "id")
	private Long id;
    @XmlElement(name = "name")
	private String name;
    @XmlElement(name = "macaddress")
	private String macAddress;
    @XmlElement(name = "state")
	private String state;
    @XmlElement(name = "gatewayid")
	private Long gatewayId;
    @XmlElement(name = "floorid")
	private Long floorId;
    @XmlElement(name = "buildingid")
	private Long buildingId;
    @XmlElement(name = "campusid")
	private Long campusId;
    @XmlElement(name = "areaid")
	private Long areaId;
    @XmlElement(name = "xaxis")
	private Integer xaxis;
    @XmlElement(name = "yaxis")
	private Integer yaxis;
	private Switch wdsSwitch;
	private WdsModelType wdsModelType;
	private ButtonMap buttonMap;
	private SwitchGroup switchGroup;
	@XmlElement(name = "wdsNo")
	private Integer wdsNo;
    @XmlElement(name = "associationstate")
	private Integer associationState;
  @XmlElement(name = "upgradestatus")
  private String upgradeStatus;  
  @XmlElement(name = "version")
  private String version;
	
	public Wds() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
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
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the macAddress
	 */
	public String getMacAddress() {
		return macAddress;
	}

	/**
	 * @param macAddress the macAddress to set
	 */
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the gatewayId
	 */
	public Long getGatewayId() {
		return gatewayId;
	}

	/**
	 * @param gatewayId the gatewayId to set
	 */
	public void setGatewayId(Long gatewayId) {
		this.gatewayId = gatewayId;
	}

	/**
	 * @return the floorId
	 */
	public Long getFloorId() {
		return floorId;
	}

	/**
	 * @param floorId the floorId to set
	 */
	public void setFloorId(Long floorId) {
		this.floorId = floorId;
	}

	/**
	 * @return the buildingId
	 */
	public Long getBuildingId() {
		return buildingId;
	}

	/**
	 * @param buildingId the buildingId to set
	 */
	public void setBuildingId(Long buildingId) {
		this.buildingId = buildingId;
	}

	/**
	 * @return the campusId
	 */
	public Long getCampusId() {
		return campusId;
	}

	/**
	 * @param campusId the campusId to set
	 */
	public void setCampusId(Long campusId) {
		this.campusId = campusId;
	}

	/**
	 * @return the areaId
	 */
	public Long getAreaId() {
		return areaId;
	}

	/**
	 * @param areaId the areaId to set
	 */
	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}

	/**
	 * @return the xaxis
	 */
	public Integer getXaxis() {
		return xaxis;
	}

	/**
	 * @param xaxis the xaxis to set
	 */
	public void setXaxis(Integer xaxis) {
		this.xaxis = xaxis;
	}

	/**
	 * @return the yaxis
	 */
	public Integer getYaxis() {
		return yaxis;
	}

	/**
	 * @param yaxis the yaxis to set
	 */
	public void setYaxis(Integer yaxis) {
		this.yaxis = yaxis;
	}

	/**
	 * @return the wdsModelType
	 */
	public WdsModelType getWdsModelType() {
		return wdsModelType;
	}

	/**
	 * @param wdsModelType the wdsModelType to set
	 */
	public void setWdsModelType(WdsModelType wdsModelType) {
		this.wdsModelType = wdsModelType;
	}

	/**
	 * @return the buttonMap
	 */
	public ButtonMap getButtonMap() {
		return buttonMap;
	}

	/**
	 * @param buttonMap the buttonMap to set
	 */
	public void setButtonMap(ButtonMap buttonMap) {
		this.buttonMap = buttonMap;
	}

	/**
	 * @return the switchGroup
	 */
	public SwitchGroup getSwitchGroup() {
		return switchGroup;
	}

	/**
	 * @param switchGroup the switchGroup to set
	 */
	public void setSwitchGroup(SwitchGroup switchGroup) {
		this.switchGroup = switchGroup;
	}

	/**
	 * @return the wdsNo
	 */
	public Integer getWdsNo() {
		return wdsNo;
	}

	/**
	 * @param wdsNo the wdsNo to set
	 */
	public void setWdsNo(Integer wdsNo) {
		this.wdsNo = wdsNo;
	}

	/**
	 * @return the wdsSwitch
	 */
	public Switch getWdsSwitch() {
		return wdsSwitch;
	}

	/**
	 * @param wdsSwitch the wdsSwitch to set
	 */
	public void setWdsSwitch(Switch wdsSwitch) {
		this.wdsSwitch = wdsSwitch;
	}

    /**
     * @return the associationState
     */
    public Integer getAssociationState() {
        return associationState;
    }

    /**
     * @param associationState the associationState to set
     */
    public void setAssociationState(Integer associationState) {
        this.associationState = associationState;
    }
    
    public void setUpgradeStatus(String upgradeStatus) {
      this.upgradeStatus = upgradeStatus;
    }

    /**
     * @return upgradeStatus
     */
    public String getUpgradeStatus() {
      return upgradeStatus;
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
    
}
