package com.emscloud.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * THe list of this DTO will be the actual json objects that will be rendered in the UI for pictorial representation
 * using d3.js
 * 
 * @author ADMIN
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class OccSpaceStatDTO {
    public Long id = 0l;
    public String facilityName = "defaultFacility";
    public String country = "US";
    public OccupancyMasterDTO statMaster = new OccupancyMasterDTO();
    public List<OccuSpaceStatDataDTO> data = new ArrayList<OccuSpaceStatDataDTO>() {
        {
        }
    };

    public List<OccuSpaceStatDataDTO> rollOverdata = new ArrayList<OccuSpaceStatDataDTO>() {
        {
        }
    };

    // List of all available StatTypes in the app
    public List<OccupancyMasterDTO> allStatTypes = new ArrayList<OccupancyMasterDTO>() {
        {
        }
    };

    // List of all available OccupancyTypes in the app
    public List<OccupancyMasterDTO> allOccTypes = new ArrayList<OccupancyMasterDTO>() {
        {
        }
    };

    // List of all available SpaceTypes in the app
    public List<OccupancyMasterDTO> allSpaceTypes = new ArrayList<OccupancyMasterDTO>() {
        {
        }
    };

    @XmlElement(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @XmlElement(name = "facilityName")
    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    @XmlElement(name = "country")
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @XmlElement(name = "data")
    public List<OccuSpaceStatDataDTO> getData() {
        return data;
    }

    public void setData(List<OccuSpaceStatDataDTO> data) {
        this.data = data;
    }

    @XmlElement(name = "allStatTypes")
    public List<OccupancyMasterDTO> getAllStatTypes() {
        return allStatTypes;
    }

    public void setAllStatTypes(List<OccupancyMasterDTO> allStatTypes) {
        this.allStatTypes = allStatTypes;
    }

    @XmlElement(name = "allOccTypes")
    public List<OccupancyMasterDTO> getAllOccTypes() {
        return allOccTypes;
    }

    public void setAllOccTypes(List<OccupancyMasterDTO> allOccTypes) {
        this.allOccTypes = allOccTypes;
    }

    @XmlElement(name = "allSpaceTypes")
    public List<OccupancyMasterDTO> getAllSpaceTypes() {
        return allSpaceTypes;
    }

    public void setAllSpaceTypes(List<OccupancyMasterDTO> allSpaceTypes) {
        this.allSpaceTypes = allSpaceTypes;
    }

    @XmlElement(name = "rollOverdata")
    public List<OccuSpaceStatDataDTO> getRollOverdata() {
        return rollOverdata;
    }

    public void setRollOverdata(List<OccuSpaceStatDataDTO> rollOverdata) {
        this.rollOverdata = rollOverdata;
    }

    @XmlElement(name = "statMaster")
    public OccupancyMasterDTO getStatMaster() {
        return statMaster;
    }

    public void setStatMaster(OccupancyMasterDTO statMaster) {
        this.statMaster = statMaster;
    }

}
