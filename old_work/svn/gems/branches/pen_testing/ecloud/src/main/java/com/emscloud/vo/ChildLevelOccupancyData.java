package com.emscloud.vo;

public class ChildLevelOccupancyData {

    private Long levelId;
    private String levelName;
    private Long nos;
    private Long sqft;
    private Long occupPercent;

    public ChildLevelOccupancyData(Long levelId, String levelName, Long nos, Long sqft, Long total1bits,
            Long totalBits, Long occupPercent) {
        super();
        this.levelId = levelId;
        this.levelName = levelName;
        this.nos = nos;
        this.sqft = sqft;
        this.occupPercent = occupPercent;
    }

    public Long getLevelId() {
        return levelId;
    }

    public void setLevelId(Long levelId) {
        this.levelId = levelId;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public Long getNos() {
        return nos;
    }

    public void setNos(Long nos) {
        this.nos = nos;
    }

    public Long getSqft() {
        return sqft;
    }

    public void setSqft(Long sqft) {
        this.sqft = sqft;
    }

    public Long getOccupPercent() {
        return occupPercent;
    }

    public void setOccupPercent(Long occupPercent) {
        this.occupPercent = occupPercent;
    }
}
