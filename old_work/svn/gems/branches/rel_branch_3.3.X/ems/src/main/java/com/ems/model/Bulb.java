package com.ems.model;

import java.io.Serializable;
import java.lang.reflect.Field;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Shiv Mohan
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Bulb implements Serializable {

    private static final long serialVersionUID = 1049510381838518894L;

    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "manufacturer")
    private String manufacturer;
    @XmlElement(name = "name")
    private String bulbName;
    @XmlElement(name = "type")
    private String type;
    @XmlElement(name = "initiallumens")
    private Long initialLumens;
    @XmlElement(name = "designlumens")
    private Long designLumens;
    @XmlElement(name = "energy")
    private Integer energy;
    @XmlElement(name = "lifeinsstart")
    private Long lifeInsStart;
    @XmlElement(name = "lifeprogstart")
    private Long lifeProgStart;
    @XmlElement(name = "diameter")
    private Integer diameter;
    @XmlElement(name = "length")
    private Double length;
    @XmlElement(name = "cri")
    private Integer cri;
    @XmlElement(name = "colortemp")
    private Integer colorTemp;

    public Bulb() {
    }

    /**
	 */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return bulb manufacturer
     */
    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * @return bulb name
     */
    public String getBulbName() {
        return bulbName;
    }

    public void setBulbName(String bulbName) {
        this.bulbName = bulbName;
    }

    /**
     * @return bulb type
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return initial lumens
     */
    public Long getInitialLumens() {
        return initialLumens;
    }

    public void setInitialLumens(Long initialLumens) {
        this.initialLumens = initialLumens;
    }

    /**
     * @return design lumens
     */
    public Long getDesignLumens() {
        return designLumens;
    }

    public void setDesignLumens(Long designLumens) {
        this.designLumens = designLumens;
    }

    /**
     * @return energy
     */
    public Integer getEnergy() {
        return energy;
    }

    public void setEnergy(Integer energy) {
        this.energy = energy;
    }

    /**
     * @return lifeInsStart
     */
    public Long getLifeInsStart() {
        return lifeInsStart;
    }

    public void setLifeInsStart(Long lifeInsStart) {
        this.lifeInsStart = lifeInsStart;
    }

    /**
     * @return lifeProgStart
     */
    public Long getLifeProgStart() {
        return lifeProgStart;
    }

    public void setLifeProgStart(Long lifeProgStart) {
        this.lifeProgStart = lifeProgStart;
    }

    /**
     * @return diameter
     */
    public Integer getDiameter() {
        return diameter;
    }

    public void setDiameter(Integer diameter) {
        this.diameter = diameter;
    }

    /**
     * @return length
     */
    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    /**
     * @return CRI
     */
    public Integer getCri() {
        return cri;
    }

    public void setCri(Integer cri) {
        this.cri = cri;
    }

    /**
     * @return colorTemp
     */
    public Integer getColorTemp() {
        return colorTemp;
    }

    public void setColorTemp(Integer colorTemp) {
        this.colorTemp = colorTemp;
    }	
    
    public String compare(Bulb bulb) {
		// TODO Auto-generated method stub
		String changeLog = "{";		
		changeLog = changeLog + "id:"+this.getId();		
		if(this.getBulbName()!=null && bulb.getBulbName()!=null && !this.getBulbName().trim().equals(bulb.getBulbName().trim()))
		{
			changeLog = changeLog + ",name:" + this.getBulbName().trim() + "," + bulb.getBulbName().trim();
		}	
		else
		{
			changeLog = changeLog + ",name:"+this.getBulbName().trim();
		}
		if(this.getColorTemp() != null && bulb.getColorTemp()!= null && this.getColorTemp().compareTo(bulb.getColorTemp())!=0)
		{
			changeLog = changeLog + ",colortemp:" + this.getColorTemp() + "," + bulb.getColorTemp();
		}
		if(this.getCri()!=null && bulb.getCri()!= null && this.getCri().compareTo(bulb.getCri())!=0)
		{
			changeLog = changeLog + ",cri:" + this.getCri() + "," + bulb.getCri();			
		}		
		if(this.getDesignLumens()!=null && bulb.getDesignLumens()!=null && this.getDesignLumens().compareTo(bulb.getDesignLumens())!=0)
		{
			changeLog = changeLog + ",designlumens:"+ this.getDesignLumens() + "," + bulb.getDesignLumens();
		}
		if(this.getDiameter()!=null && bulb.getDiameter()!=null && this.getDiameter().compareTo(bulb.getDiameter())!=0)
		{
			changeLog = changeLog + ",diameter:"+ this.getDiameter() + "," + bulb.getDiameter();
		}
		if(this.getEnergy()!=null && bulb.getEnergy()!=null && this.getEnergy().compareTo(bulb.getEnergy())!=0)
		{
			changeLog = changeLog + ",energy:"+this.getEnergy() + "," + bulb.getEnergy();
		}
		if(this.getInitialLumens()!=null && bulb.getInitialLumens()!=null && this.getInitialLumens().compareTo(bulb.getInitialLumens())!=0)
		{
			changeLog = changeLog + ",initiallumens:"+this.getInitialLumens() + ","+bulb.getInitialLumens();
		}
		if(this.getLength()!=null && bulb.getLength()!= null && this.getLength().compareTo(bulb.getLength())!=0)
		{
			changeLog = changeLog + ",length:" + this.getLength() + ","+bulb.getLength();
		}
		if(this.getLifeInsStart()!=null && bulb.getLifeInsStart()!=null && this.getLifeInsStart().compareTo(bulb.getLifeInsStart())!=0)
		{
			changeLog = changeLog + ",lifeinsstart:" + this.getLifeInsStart() + ","+bulb.getLifeInsStart();
		}
		if(this.getLifeProgStart()!=null && bulb.getLifeProgStart()!=null && this.getLifeProgStart().compareTo(bulb.getLifeProgStart())!=0)
		{
			changeLog = changeLog + ",lifeprogstart:" + this.getLifeProgStart() + ","+bulb.getLifeProgStart();
		}
		if(this.getManufacturer()!=null && bulb.getManufacturer()!=null && !this.getManufacturer().trim().equals(bulb.getManufacturer().trim()))
		{
			changeLog = changeLog + ",manufacturer:" + this.getManufacturer().trim() + ","+bulb.getManufacturer().trim();
		}
		if(this.getType()!=null && bulb.getType()!=null && !this.getType().trim().equals(bulb.getType().trim()))
		{
			changeLog = changeLog + ",type:" + this.getType().trim() + ","+bulb.getType().trim();
		}
		////////Only for null checks, avoid the objects , both of which have null values				
		if((this.getColorTemp() == null || bulb.getColorTemp()== null)&&!(this.getColorTemp()==null&&bulb.getColorTemp()==null))
		{
			changeLog = changeLog + ",colortemp:" + this.getColorTemp() + "," + bulb.getColorTemp();
		}
		if((this.getCri()==null || bulb.getCri()== null)&&!(this.getCri()==null&&bulb.getCri()==null))
		{
			changeLog = changeLog + ",cri:" + this.getCri() + "," + bulb.getCri();			
		}		
		if((this.getDesignLumens()==null || bulb.getDesignLumens()==null)&&!(this.getDesignLumens()==null&&bulb.getDesignLumens()==null))
		{
			changeLog = changeLog + ",designlumens:"+ this.getDesignLumens() + "," + bulb.getDesignLumens();
		}
		if((this.getDiameter()==null || bulb.getDiameter()==null) && !(this.getDiameter()==null&&bulb.getDiameter()==null))
		{
			changeLog = changeLog + ",diameter:"+ this.getDiameter() + "," + bulb.getDiameter();
		}
		if((this.getEnergy()==null || bulb.getEnergy()==null) && !(this.getEnergy()==null&&bulb.getEnergy()==null))
		{
			changeLog = changeLog + ",energy:"+this.getEnergy() + "," + bulb.getEnergy();
		}
		if((this.getInitialLumens()==null || bulb.getInitialLumens()==null) && !(this.getInitialLumens()==null&&bulb.getInitialLumens()==null))
		{
			changeLog = changeLog + ",initiallumens:"+this.getInitialLumens() + ","+bulb.getInitialLumens();
		}
		if((this.getLength()==null || bulb.getLength()== null) && !(this.getLength()==null&&bulb.getLength()==null))
		{
			changeLog = changeLog + ",length:" + this.getLength() + ","+bulb.getLength();
		}
		if((this.getLifeInsStart()==null || bulb.getLifeInsStart()==null) && !(this.getLifeInsStart()==null&&bulb.getLifeInsStart()==null))
		{
			changeLog = changeLog + ",lifeinsstart:" + this.getLifeInsStart() + ","+bulb.getLifeInsStart();
		}
		if((this.getLifeProgStart()==null || bulb.getLifeProgStart()==null) && !(this.getLifeProgStart()==null&&bulb.getLifeProgStart()==null))
		{
			changeLog = changeLog + ",lifeprogstart:" + this.getLifeProgStart() + ","+bulb.getLifeProgStart();
		}
		changeLog = changeLog + "}";
		return changeLog;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuilder result = new StringBuilder();

		  result.append( "{" );

		  //determine fields declared in this class only (no fields of superclass)
		  Field[] fields = this.getClass().getDeclaredFields();
		  int length = fields.length;
		  //print field names paired with their values
		  for ( Field field : fields  ) {
			if(field.getName().equalsIgnoreCase("serialVersionUID")) continue;
		    result.append("");
		    try {
		      result.append(field.getName() );
		      result.append(":");
		      //requires access to private field:
		      if(field.get(this)==null) result.append("");
		      else
		      {
		    	  String trimmedValue = field.get(this).toString();
		    	  result.append(trimmedValue.trim());
		      }
		      result.append(",");
		    } catch ( IllegalAccessException ex ) {
		      
		    }
		  }
		  result.append("}");
		  if(result.charAt(result.length()-2)==',')
			{
				result.deleteCharAt(result.length()-2);
			}
		  return result.toString();
	}    
   
}
