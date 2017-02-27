package com.motion.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="line")
@XmlAccessorType(XmlAccessType.FIELD)
public class LineVO {
	@XmlElement(name = "name")
	String name ;
	@XmlElement(name = "color")
	String color ;
	@XmlElement(name = "xPosStart")
	String xPosStart ;
	@XmlElement(name = "yPosStart")
	String yPosStart ;
	@XmlElement(name = "xPosEnd")
	String xPosEnd ;
	@XmlElement(name = "yPosEnd")
	String yPosEnd ;
	@XmlElement(name = "width")
	String Width ;
	@XmlElement(name = "pattern")
	String pattern ;
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
	public String getxPosStart() {
		return xPosStart;
	}
	public void setxPosStart(String xPosStart) {
		this.xPosStart = xPosStart;
	}
	public String getyPosStart() {
		return yPosStart;
	}
	public void setyPosStart(String yPosStart) {
		this.yPosStart = yPosStart;
	}
	public String getxPosEnd() {
		return xPosEnd;
	}
	public void setxPosEnd(String xPosEnd) {
		this.xPosEnd = xPosEnd;
	}
	public String getyPosEnd() {
		return yPosEnd;
	}
	public void setyPosEnd(String yPosEnd) {
		this.yPosEnd = yPosEnd;
	}
	public String getWidth() {
		return Width;
	}
	public void setWidth(String width) {
		Width = width;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

}

