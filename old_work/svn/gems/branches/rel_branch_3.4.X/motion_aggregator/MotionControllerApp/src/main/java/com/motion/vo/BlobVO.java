package com.motion.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="blob")
@XmlAccessorType(XmlAccessType.FIELD)
public class BlobVO {
	@XmlElement(name = "name")
	String name;
	@XmlElement(name = "color")
	String color ;
	@XmlElement(name = "xpos")
	String xPos ;
	@XmlElement(name = "ypos")
	String yPos ;
	@XmlElement(name = "radius")
	String radius ;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getxPos() {
		return xPos;
	}
	public void setxPos(String xPos) {
		this.xPos = xPos;
	}
	public String getyPos() {
		return yPos;
	}
	public void setyPos(String yPos) {
		this.yPos = yPos;
	}
	public String getRadius() {
		return radius;
	}
	public void setRadius(String radius) {
		this.radius = radius;
	}

}

