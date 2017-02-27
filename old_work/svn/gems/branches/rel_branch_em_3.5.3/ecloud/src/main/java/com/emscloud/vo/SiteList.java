package com.emscloud.vo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.emscloud.model.Site;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SiteList {
	public static final int DEFAULT_ROWS = 20;

    @XmlElement(name = "page")
    private int page;
    @XmlElement(name = "records")
    private long records;
    @XmlElement(name = "total")
    private long total;
    @XmlElement(name = "sites")
    private List<Site> sitesList;
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
    /**
     * @return the sitesList
     */
    public List<Site> getSitesList() {
        return sitesList;
    }
    /**
     * @param sitesList the sitesList to set
     */
    public void setSitesList(List<Site> sitesList) {
        this.sitesList = sitesList;
    }
}
