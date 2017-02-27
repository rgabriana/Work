/**
 * 
 */
package com.ems.model;

import java.io.Serializable;
import java.lang.reflect.Field;
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
    @XmlElement(name="isPowerMap")
    private Boolean isPowerMap;
    @XmlElement(name = "inputvolt")
    private Double inputVolt;
    
    @XmlElement(name = "isDefault")
    private Integer isDefault;
    
    @XmlElement(name = "ballasttype")
    private Integer ballastType;

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

	public void setIsPowerMap(Boolean isPowerMap) {
		this.isPowerMap = isPowerMap;
	}

	public Boolean getIsPowerMap() {
		return isPowerMap;
	}

	public void setInputVolt(Double inputVolt) {
		this.inputVolt = inputVolt;
	}

	public Double getInputVolt() {
		return inputVolt;
	}

	public void setIsDefault(Integer isDefault) {
		this.isDefault = isDefault;
	}

	public Integer getIsDefault() {
		return isDefault;
	}

	public Integer getBallastType() {
		return ballastType;
	}

	public void setBallastType(Integer ballastType) {
		this.ballastType = ballastType;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		StringBuilder result = new StringBuilder();
		result.append("{");
		// determine fields declared in this class only (no fields of
		// superclass)
		Field[] fields = this.getClass().getDeclaredFields();
		
		// print field names paired with their values
		for (Field field : fields) {
			if (field.getName().equalsIgnoreCase("serialVersionUID")
					|| field.getName().equalsIgnoreCase("fixtureWattage")
					|| field.getName().equalsIgnoreCase("isPowerMap")
					|| field.getName().equalsIgnoreCase("isDefault")
					|| field.getName().equalsIgnoreCase("ballastType")
					|| field.getName().equalsIgnoreCase("voltPowerMapId")
					|| field.getName().equalsIgnoreCase("inputVolt"))
				continue;
			result.append("");
			try {
				result.append(field.getName());
				result.append(":");
				// requires access to private field:
				if (field.get(this) == null)
					result.append("");
				else
				{	String trimmedValue = field.get(this).toString();
					result.append(trimmedValue.trim());
				}
				result.append(",");

			} catch (IllegalAccessException ex) {

			}

		}		
		result.append("}");
		if(result.charAt(result.length()-2)==',')
		{
			result.deleteCharAt(result.length()-2);
		}
		return result.toString();
	}
	
	public String compare(Ballast ballast)
	{
	String changeLog="{";
	changeLog = changeLog + "id:"+this.getId();
	if(!this.getBallastName().trim().equals(ballast.getBallastName().trim()))
	{
		changeLog = changeLog + ",name:"+this.getBallastName().trim() + ","+ballast.getBallastName().trim();
	}
	else
	{
		changeLog = changeLog + ",name :"+this.getBallastName().trim(); 
	}
	if(!this.getInputVoltage().trim().equals(ballast.getInputVoltage().trim()))
	{
		changeLog = changeLog + ",inputVoltage:"+this.getInputVoltage().trim() + ","+ballast.getInputVoltage().trim();
	}
	if(!this.getLampType().trim().equals(ballast.getLampType().trim()))
	{
		changeLog = changeLog + ",bulbType:"+this.getLampType().trim() + ","+ballast.getLampType().trim();		
	}
	if(this.getLampNum().compareTo(ballast.getLampNum())!=0)
	{
		changeLog = changeLog + ",noOfBulbs:" + this.getLampNum() + ","+ballast.getLampNum();
	}
	if(this.getBallastFactor().compareTo(ballast.getBallastFactor())!=0)
	{
		changeLog = changeLog + ",ballastFactor:"+ this.getBallastFactor() + ","+ballast.getBallastFactor();
	}
	if(this.getWattage().compareTo(ballast.getWattage())!=0)
	{
		changeLog = changeLog + ",bulbWattage:" + this.getWattage() + "," + ballast.getWattage();
	}
	if(!this.getBallastManufacturer().trim().equals(ballast.getBallastManufacturer().trim()))
	{
		changeLog = changeLog + ",ballastManufacturer:"+this.getBallastManufacturer().trim() + ","+ballast.getBallastManufacturer().trim();
	}
	if(this.getBaselineLoad()!=null && ballast.getBaselineLoad()!=null && this.getBaselineLoad().compareTo(ballast.getBaselineLoad())!=0)
	{
		changeLog = changeLog + ",baselineLoad:"+this.getBaselineLoad() + ","+ballast.getBaselineLoad();
	}
	if(this.getDisplayLabel()!=null && ballast.getDisplayLabel()!=null && !this.getDisplayLabel().equals(ballast.getDisplayLabel()))
	{
		changeLog = changeLog + ",displayLabel:"+ this.getDisplayLabel() + ","+ballast.getDisplayLabel();
	}
	if(this.getItemNum()!=null && ballast.getItemNum()!=null && this.getItemNum().compareTo(ballast.getItemNum())!=0)
	{
		changeLog = changeLog + ",itemNum:"+this.getItemNum() + ","+ballast.getItemNum();
	}
	
	///For null values
	if((this.getBaselineLoad()==null || ballast.getBaselineLoad()==null) && !(this.getBaselineLoad()==null&&ballast.getBaselineLoad()==null))
	{
		changeLog = changeLog + ",baselineLoad:"+this.getBaselineLoad() + ","+ballast.getBaselineLoad();
	}
	if((this.getItemNum()==null || ballast.getItemNum()==null) && !(this.getItemNum()==null&&ballast.getItemNum()==null))
	{
		changeLog = changeLog + ",itemNum:"+this.getItemNum() + ","+ballast.getItemNum();
	}
	changeLog = changeLog + "}";
	return changeLog;
	}
} // end of class Ballast
