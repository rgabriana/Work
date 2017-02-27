package com.ems.model;

import java.io.Serializable;

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
    private String manufacturer;
    @XmlElement(name = "name")
    private String bulbName;
    private String type;
    private Long initialLumens;
    private Long designLumens;
    private Integer energy;
    private Long lifeInsStart;
    private Long lifeProgStart;
    private Integer diameter;
    private Double length;
    private Integer cri;
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
}
