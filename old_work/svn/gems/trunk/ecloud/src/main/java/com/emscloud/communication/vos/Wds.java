package com.emscloud.communication.vos;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.emscloud.types.DeviceType;
import com.emscloud.communication.vos.Switch;
import com.emscloud.communication.vos.WdsModelType;
import com.emscloud.communication.vos.ButtonMap;
import com.emscloud.communication.vos.SwitchGroup;


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
	@XmlElement(name = "switchId")
	private Long switchId;
	@XmlElement(name = "switchName")
	private String switchName;
	
	@XmlElement(name = "batteryVoltage")
	private Integer batteryVoltage;
		
	@XmlElement(name = "voltageCaptureAt")
	private Date voltageCaptureAt;
	
	@XmlElement(name = "batteryLevel")
	private String batteryLevel;
	
	@XmlElement(name = "captureAtStr")
	private String captureAtStr;
	
	@XmlElement(name = "gatewayName")
	private String gatewayName;
	
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
	/**
	 * @param get Gateway name
	 */
	public String getGatewayName() {
		return gatewayName;
	}
	
	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}

	public Integer getBatteryVoltage() {
		return batteryVoltage;
	}

	public void setBatteryVoltage(Integer batteryVoltage) {
		this.batteryVoltage = batteryVoltage;
	}

	public Date getVoltageCaptureAt() {
		return voltageCaptureAt;
	}

	public void setVoltageCaptureAt(Date voltageCaptureAt) {
		this.voltageCaptureAt = voltageCaptureAt;
	}

	public String getBatteryLevel() {
		return batteryLevel;
	}

	public void setBatteryLevel(String batteryLevel) {
		this.batteryLevel = batteryLevel;
	}

	public String getCaptureAtStr() {
		return captureAtStr;
	}

	public void setCaptureAtStr(String captureAtStr) {
		this.captureAtStr = captureAtStr;
	}
	
	public String getSwitchName() {
		return switchName;
	}

	public void setSwitchName(String switchName) {
		this.switchName = switchName;
	}
}
