package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.types.DeviceType;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Wds extends Device implements Serializable {

	private static final long serialVersionUID = -8346640146081015941L;
    
    @XmlElement(name = "state")
	private String state;
    @XmlElement(name = "gatewayid")
	private Long gatewayId;
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
	@XmlElement(name = "switchId")
	private Long switchId;
	
	public Wds() {
		// TODO Auto-generated constructor stub
	  type = DeviceType.WDS.getName();
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

	/**
	 * @return the switchId
	 */
	public Long getSwitchId() {
		return switchId;
	}

	/**
	 * @param switchId the switchId to set
	 */
	public void setSwitchId(Long switchId) {
		this.switchId = switchId;
	}
    
}
