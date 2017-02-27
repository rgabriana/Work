package com.ems.vo.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.model.Bulb;

/**
 * @author Sampath Akula
 */
@XmlRootElement(name = "bulbs")
@XmlAccessorType(XmlAccessType.NONE)
public class Bulbs{

    
	@XmlElement(name = "bulb")
    private List<Bulb> bulbs;

	public void setBulbs(List<Bulb> bulbs) {
		this.bulbs = bulbs;
	}

	public List<Bulb> getBulbs() {
		return bulbs;
	}
 
   
}
