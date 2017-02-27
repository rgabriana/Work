package com.ems.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BulbList
{
	
	@XmlElement(name = "page")
    private int page;
    @XmlElement(name = "records")
    private long records;
    @XmlElement(name = "total")
    private long total;
    @XmlElement(name = "bulbs")
    private List<Bulb> bulbs;
    
	public static final int DEFAULT_ROWS = 20;

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
		this.total = total;
	}
	public List<Bulb> getBulbs() {
		return bulbs;
	}
	public void setBulbs(List<Bulb> bulbs) {
		this.bulbs = bulbs;
	}	
}