/**
 * 
 */
package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.types.DeviceType;
import com.ems.types.LocatorDeviceType;

/**
 * @author Sampath Akula
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class LocatorDevice extends Device implements Serializable{

    
    private static final long serialVersionUID = -9067068675731027368L;

    @XmlElement(name = "locatorDeviceType")
    private LocatorDeviceType locatorDeviceType;
    
    public LocatorDevice() {
          this.type = DeviceType.LocatorDevice.getName();
    }
    
    /**
       * @return the id
       */
      public Long getId() {
      
        return id;
      }
      
      /**
       * @param id the id to set
       */
      public void setId(Long id) {
      
        this.id = id;
      }
      
      /**
       * @return the name
       */
      public String getName() {
      
        return name;
      }
      
      /**
       * @param name the name to set
       */
      public void setName(String name) {
      
        this.name = name;
      }
      
      /**
       * @return the xaxis
       */
      public Integer getXaxis() {
          return xaxis;
      }

      /**
       * @param xaxis
       *            the xaxis to set
       */
      public void setXaxis(Integer xaxis) {
          this.xaxis = xaxis;
      }

      /**
       * @return the yaxis
       */
      public Integer getYaxis() {
          return yaxis;
      }

      /**
       * @param yaxis
       *            the yaxis to set
       */
      public void setYaxis(Integer yaxis) {
          this.yaxis = yaxis;
      }
      
      /**
       * @return the location
       */
      public String getLocation() {
          return location;
      }
      
      /**
       * @param location
       */
      public void setLocation(String location) {
          this.location = location;
      }

      /**
       * @param type
       */
      public void setType(String type) {
          this.type = type;
      }

      /**
       * @return the type
       */
      public String getType() {
          return type;
      }  
      
      /**
       * @return the floor
       */
      public Floor getFloor() {
          return floor;
      }

      /**
       * @param floor
       *            the floor to set
       */
      public void setFloor(Floor floor) {
          this.floor = floor;
      }

     
    

    /**
       * @return the area
       */
      public Area getArea() {
          return area;
      }

      /**
       * @param area
       *            the area to set
       */
      public void setArea(Area area) {
          this.area = area;
      }

     
    

    /**
       * @return the campusId
       */
      public Long getCampusId() {
          return campusId;
      }

      /**
       * @param campusId
       *            the campusId to set
       */
      public void setCampusId(Long campusId) {
          this.campusId = campusId;
      }

      /**
       * @return the buildingId
       */
      public Long getBuildingId() {
          return buildingId;
      }

      /**
       * @param buildingId
       *            the buildingId to set
       */
      public void setBuildingId(Long buildingId) {
          this.buildingId = buildingId;
      }
      
      /**
       * @return the macAddress
       */
      public String getMacAddress() {
      
        return macAddress;
      }
      
      /**
       * @param macAddress the macAddress to set
       */
      public void setMacAddress(String macAddress) {
      
        this.macAddress = macAddress;
      }
      
      /**
       * @return the version
       */
      public String getVersion() {
      
        return version;
      }
      
      /**
       * @param version the version to set
       */
      public void setVersion(String version) {
      
        this.version = version;
      }
      
      /**
       * @return the modelNo
       */
      public String getModelNo() {
      
        return modelNo;
      }
      
      /**
       * @param modelNo the modelNo to set
       */
      public void setModelNo(String modelNo) {
      
        this.modelNo = modelNo;
      }
    
    public LocatorDeviceType getLocatorDeviceType() {
        return locatorDeviceType;
    }

    public void setLocatorDeviceType(LocatorDeviceType locatorDeviceType) {
        this.locatorDeviceType = locatorDeviceType;
    }
   
}
