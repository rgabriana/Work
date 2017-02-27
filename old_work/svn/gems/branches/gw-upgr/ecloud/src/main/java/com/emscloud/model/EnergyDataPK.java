package com.emscloud.model;

import java.io.Serializable;
import java.util.Date;

public class EnergyDataPK implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -6644153185147543517L;

	public EnergyDataPK(){}
    public EnergyDataPK(Long lId, Date cAt){
    	this.levelId = lId;
    	this.captureAt = cAt;
    }

	private Long levelId;
	private Date captureAt;

    @Override
    public int hashCode(){        
        return this.getCaptureAt().hashCode() + getLevelId().hashCode();
    }

    @Override
    public boolean equals(Object o){
        boolean flag = false;
        EnergyDataPK myPK = (EnergyDataPK) o;

        if((o instanceof EnergyDataPK) 
                && (this.getCaptureAt().equals(myPK.getCaptureAt()))
                && (this.levelId == myPK.getLevelId())){
            flag = true;
        }
        return flag;
    }
    
// rest of the code with getters only
	public Long getLevelId() {
		return levelId;
	}
	public void setLevelId(Long levelId) {
		this.levelId = levelId;
	}
	public Date getCaptureAt() {
		return captureAt;
	}
	public void setCaptureAt(Date captureAt) {
		this.captureAt = captureAt;
	}
}