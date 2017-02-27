package com.ems.profile;

public class Profile {
    
    private Long minLevel = ProfileConstants.MIN_LEVEL;
    private Long maxLevel = ProfileConstants.MIN_LEVEL;
    
    
    public Long getMinLevel() {
        return minLevel;
    }
    public void setMinLevel(Long minLevel) {
        this.minLevel = minLevel;
    }
    public Long getMaxLevel() {
        return maxLevel;
    }
    public void setMaxLevel(Long maxLevel) {
        this.maxLevel = maxLevel;
    }
    
    

}
