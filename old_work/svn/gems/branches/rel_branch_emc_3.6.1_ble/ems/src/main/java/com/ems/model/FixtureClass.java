package com.ems.model;

import java.io.Serializable;
import java.lang.reflect.Field;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FixtureClass implements Serializable {
	
	private static final long serialVersionUID = 1049510381838518894L;
	
	@XmlElement(name = "id")
	private Long id;
	
	@XmlElement(name = "name")
	private String name;
	
	@XmlElement(name = "noOfBallasts")
	private Integer noOfBallasts;
	
	@XmlElement(name = "voltage")
	private Integer voltage;	
	
	@XmlElement(name = "ballast")
	private Ballast ballast;	
	
	@XmlElement(name = "bulb")
	private Bulb bulb;
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	

	public Ballast getBallast() {
		return ballast;
	}

	public void setBallast(Ballast ballast) {
		this.ballast = ballast;
	}

	public void setBulb(Bulb bulb) {
		this.bulb = bulb;
	}

	public Bulb getBulb() {
		return bulb;
	}

	public void setNoOfBallasts(Integer noOfBallasts) {
		this.noOfBallasts = noOfBallasts;
	}

	public Integer getNoOfBallasts() {
		return noOfBallasts;
	}

	public void setVoltage(Integer voltage) {
		this.voltage = voltage;
	}

	public Integer getVoltage() {
		return voltage;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
    	StringBuilder result = new StringBuilder();
    	  result.append("{");    	  

    	  //determine fields declared in this class only (no fields of superclass)
    	  Field[] fields = this.getClass().getDeclaredFields();

    	  //print field names paired with their values
    	  for ( Field field : fields  ) {
    		if(field.getName().equalsIgnoreCase("serialVersionUID")) continue;    		
    	    result.append("");
    	    try {
    	      result.append(field.getName());
    	      result.append(":");
    	      //requires access to private field:
    	      if(field.get(this) instanceof Ballast)
    	      {
    	    	Ballast ballast= (Ballast) field.get(this);
    	    	result.append("ballastId: "+ballast.getId());
    	    	result.append(",");
    	    	continue;
    	      } 
    	      if(field.get(this) instanceof Bulb)
    	      {
    	    	Bulb bulb = (Bulb)field.get(this);
    	    	result.append("bulbId:"+bulb.getId()+",bulbName:"+bulb.getBulbName().trim());
    	    	result.append(",");
    	    	continue;
    	      }    	      
				if (field.get(this) == null) {
					result.append("");
				} else {
					String trimmedValue = field.get(this).toString();
					result.append(trimmedValue.trim());
				}
    	      result.append(",");
    	    } catch ( IllegalAccessException ex ) {
    	      
    	    }
    	    
    	  }
    	  result.append("}");
		if (result.charAt(result.length() - 2) == ',') {
			result.deleteCharAt(result.length() - 2);
		}

    	  return result.toString();
	}
	
	public String compare(FixtureClass fixtureClass)
	{
		String changeLog = "{";
		changeLog = changeLog + "id:"+this.getId();
		if(!this.getName().trim().equals(fixtureClass.getName().trim()))
		{
			changeLog = changeLog + ",name:"+this.getName().trim()+ ","+fixtureClass.getName().trim();
		}
		else
		{
			changeLog = changeLog + ",name:"+this.getName().trim();
		}
		if(this.getBallast().getId().compareTo(fixtureClass.getBallast().getId())!=0)
		{
			changeLog = changeLog + ",ballastId:"+this.getBallast().getId() + ","+fixtureClass.getBallast().getId();
		}
		if(this.getBulb().getId().compareTo(fixtureClass.getBulb().getId())!=0)
		{
			changeLog = changeLog +",bulbId:"+this.getBulb().getId() + ","+fixtureClass.getBulb().getId();
			changeLog = changeLog +",bulbName:"+this.getBulb().getBulbName() + ","+fixtureClass.getBulb().getBulbName();
		}
		if(this.getVoltage().compareTo(fixtureClass.getVoltage())!=0)
		{
			changeLog = changeLog+ ",voltage:"+this.getVoltage() + ","+fixtureClass.getVoltage();
		}
		if(this.getNoOfBallasts().compareTo(fixtureClass.getNoOfBallasts())!=0)
		{
			changeLog = changeLog + ",noOfBallasts:" + this.getNoOfBallasts() + "," + fixtureClass.getNoOfBallasts();
		}
		changeLog = changeLog + "}";
		return changeLog;
	}

}
