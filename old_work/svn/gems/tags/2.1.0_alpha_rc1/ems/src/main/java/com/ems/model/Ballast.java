/**
 * 
 */
package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author EMS
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Ballast implements Serializable {

    /**
   * 
   */
    private static final long serialVersionUID = -8756352579939616113L;
    @XmlElement(name = "id")
    private Long id;
    private Long itemNum;
    @XmlElement(name = "name")
    private String ballastName;
    @XmlElement(name = "inputvoltage")
    private String inputVoltage;
    private String lampType;
    @XmlElement(name = "lampnum")
    private Integer lampNum;
    @XmlElement(name = "ballastfactor")
    private Double ballastFactor;
    @XmlElement(name = "voltpowermapid")
    private Long voltPowerMapId;
    @XmlElement(name = "wattage")
    private Integer wattage;
    private String ballastManufacturer;
    @XmlElement(name = "fixturewattage")
    private Integer fixtureWattage;

    public Ballast() {
    }

    /**
     * @return
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return
     */
    public Long getItemNum() {
        return itemNum;
    }

    public void setItemNum(Long itemNum) {
        this.itemNum = itemNum;
    }

    /**
     * @return
     */
    public String getBallastName() {
        return ballastName;
    }

    public void setBallastName(String ballastName) {
        this.ballastName = ballastName;
    }

    /**
     * @return
     */
    public String getInputVoltage() {
        return inputVoltage;
    }

    public void setInputVoltage(String inputVoltage) {
        this.inputVoltage = inputVoltage;
    }

    /**
     * @return
     */
    public String getLampType() {
        return lampType;
    }

    public void setLampType(String lampType) {
        this.lampType = lampType;
    }

    /**
     * @return
     */
    public Integer getLampNum() {
        return lampNum;
    }

    public void setLampNum(Integer lampNum) {
        this.lampNum = lampNum;
    }

    /**
     * @return
     */
    public Double getBallastFactor() {
        return ballastFactor;
    }

    public void setBallastFactor(Double ballastFactor) {
        this.ballastFactor = ballastFactor;
    }

    /**
     * @return
     */
    public Long getVoltPowerMapId() {
        return voltPowerMapId;
    }

    public void setVoltPowerMapId(Long voltPowerMapId) {
        this.voltPowerMapId = voltPowerMapId;
    }

    /**
     * @return
     */
    public Integer getWattage() {
        return wattage;
    }

    public void setWattage(Integer wattage) {
        this.wattage = wattage;
    }

    /**
     * @return
     */
    public String getBallastManufacturer() {
        return ballastManufacturer;
    }

    public void setBallastManufacturer(String ballastManufacturer) {
        this.ballastManufacturer = ballastManufacturer;
    }

    public Integer getFixtureWattage() {
        return fixtureWattage;
    }

    public void setFixtureWattage(Integer fixtureWattage) {
        this.fixtureWattage = fixtureWattage;
    }

} // end of class Ballast
