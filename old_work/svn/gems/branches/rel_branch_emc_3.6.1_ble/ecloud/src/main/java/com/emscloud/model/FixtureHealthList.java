package com.emscloud.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.emscloud.vo.FixtureHealthDataVO;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FixtureHealthList {
	
	public static final int DEFAULT_ROWS = 20;

    @XmlElement(name = "page")
    private int page;
    @XmlElement(name = "records")
    private long records;
    @XmlElement(name = "total")
    private long total;
    @XmlElement(name = "healthDataVOList")
    private List<FixtureHealthDataVO> healthDataVOList;
    
    public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public long getRecords() {
		return records;
	}
	public void setRecords(long records) {
		this.records = records;
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.records = total;
        this.total = (int) (Math.ceil(records / new Double(DEFAULT_ROWS)));
        if (this.total == 0) {
            this.total = 1;
        }
	}
	public List<FixtureHealthDataVO> getHealthDataVOList() {
		return healthDataVOList;
	}
	public void setHealthDataVOList(List<FixtureHealthDataVO> healthDataVOList) {
		this.healthDataVOList = healthDataVOList;
	}	

}