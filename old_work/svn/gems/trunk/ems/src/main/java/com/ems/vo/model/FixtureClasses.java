package com.ems.vo.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.model.FixtureClass;

/**
 * @author Sampath Akula
 */
@XmlRootElement(name = "fixtureClasses")
@XmlAccessorType(XmlAccessType.NONE)
public class FixtureClasses{

    
	@XmlElement(name = "fixtureClass")
    private List<FixtureClass> fixtureClasses;

	public void setFixtureClasses(List<FixtureClass> fixtureClasses) {
		this.fixtureClasses = fixtureClasses;
	}

	public List<FixtureClass> getFixtureClasses() {
		return fixtureClasses;
	}
	
   
}
