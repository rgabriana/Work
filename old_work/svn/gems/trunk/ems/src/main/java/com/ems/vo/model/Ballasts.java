package com.ems.vo.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.model.Ballast;

/**
 * @author Sampath Akula
 */
@XmlRootElement(name = "ballasts")
@XmlAccessorType(XmlAccessType.NONE)
public class Ballasts{

    
	@XmlElement(name = "ballast")
    private List<Ballast> ballasts;

	public void setBallasts(List<Ballast> ballasts) {
		this.ballasts = ballasts;
	}

	public List<Ballast> getBallasts() {
		return ballasts;
	}

 
   
}
