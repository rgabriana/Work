/**
 * 
 */
package com.ems.model;

import java.io.Serializable;
import java.math.BigDecimal;

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
    @XmlElement(name = "itemNum")
    private Long itemNum;
    @XmlElement(name = "name")
    private String ballastName;
    @XmlElement(name = "inputVoltage")
    private String inputVoltage;
    @XmlElement(name = "bulbType")
    private String lampType;
    @XmlElement(name = "noOfBulbs")
    private Integer lampNum;
    @XmlElement(name = "ballastFactor")
    private Double ballastFactor;
    @XmlElement(name = "voltpowermapid")
    private Long voltPowerMapId;
    @XmlElement(name = "bulbWattage")
    private Integer wattage;
    @XmlElement(name = "ballastManufacturer")
    private String ballastManufacturer;
    @XmlElement(name = "fixturewattage")
    private Integer fixtureWattage;
    
    @XmlElement(name="displayLabel")
    private String displayLabel;
    @XmlElement(name="baselineLoad")
    private BigDecimal baselineLoad;

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
    
    public void setDisplayLabel(String label) {
    	this.displayLabel = label;
    }

    public String getDisplayLabel() {
    	return displayLabel;
    }
    
    public void setBaselineLoad(BigDecimal load) {
    	this.baselineLoad = load;
    }

    public BigDecimal getBaselineLoad() {
    	return baselineLoad;
    }
} // end of class Ballast
