package com.emscloud.vo;

import java.util.List;

import com.emscloud.model.OccuSpaceStatDataDTO;

/**
 * Transfer object to define the run time data retrieval configuration for Occupany Data
 * 
 * @author admin
 * 
 */
public class OccupancyReportTO {
    private List<OccuSpaceStatDataDTO> chartData;
    private long custId;
    private long levelId;
    private String modelName;
    private String actName;
    private String abbrName;
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getActName() {
        return actName;
    }

    public void setActName(String actName) {
        this.actName = actName;
    }

    public String getAbbrName() {
        return abbrName;
    }

    public void setAbbrName(String abbrName) {
        this.abbrName = abbrName;
    }

    public List<OccuSpaceStatDataDTO> getChartData() {
        return chartData;
    }

    public void setChartData(List<OccuSpaceStatDataDTO> chartData) {
        this.chartData = chartData;
    }

    public long getCustId() {
        return custId;
    }

    public void setCustId(long custId) {
        this.custId = custId;
    }

    public long getLevelId() {
        return levelId;
    }

    public void setLevelId(long levelId) {
        this.levelId = levelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

}
