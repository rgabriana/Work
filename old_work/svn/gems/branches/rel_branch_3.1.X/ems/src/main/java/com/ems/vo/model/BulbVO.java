package com.ems.vo.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.model.Bulb;

@XmlRootElement(name="bulbVO")
@XmlAccessorType(XmlAccessType.FIELD)
public class BulbVO {
    
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "manufacturer")
    private String manufacturer;
    @XmlElement(name = "bulbName")
    private String bulbName;
    @XmlElement(name = "type")
    private String type;
    @XmlElement(name = "initialLumens")
    private Long initialLumens;
    @XmlElement(name = "designLumens")
    private Long designLumens;
    @XmlElement(name = "energy")
    private Integer energy;
    @XmlElement(name = "lifeInsStart")
    private Long lifeInsStart;
    @XmlElement(name = "lifeProgStart")
    private Long lifeProgStart;
    @XmlElement(name = "diameter")
    private Integer diameter;
    @XmlElement(name = "length")
    private Double length;
    @XmlElement(name = "cri")
    private Integer cri;
    @XmlElement(name = "colorTemp")
    private Integer colorTemp;
    
    public BulbVO(){
        
    }
    
    public BulbVO(Bulb b){
        this.id = b.getId();
        this.manufacturer = b.getManufacturer();
        this.bulbName = b.getBulbName();
        this.type = b.getType();
        this.initialLumens = b.getInitialLumens();
        this.designLumens = b.getDesignLumens();
        this.energy = b.getEnergy();
        this.lifeInsStart = b.getLifeInsStart();
        this.lifeProgStart = b.getLifeProgStart();
        this.diameter = b.getDiameter();
        this.length = b.getLength();
        this.cri = b.getCri();
        this.colorTemp = b.getColorTemp();
    }

}
