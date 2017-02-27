/**
 * 
 */
package com.ems.vo.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.model.Wds;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class WdsList {
    public static final int DEFAULT_ROWS = 20;

    @XmlElement(name = "page")
    private int page;
    @XmlElement(name = "records")
    private long records;
    @XmlElement(name = "total")
    private long total;
    @XmlElement(name = "wdses")
    private List<Wds> Wds;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Long getRecords() {
        return records;
    }

    public void setRecords(Long records) {
        this.records = records;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.records = total;
        this.total = (int) (Math.ceil(records / new Double(DEFAULT_ROWS)));
        if (this.total == 0) {
            this.total = 1;
        }
    }

	public void setWds(List<Wds> wds) {
		Wds = wds;
	}

	public List<Wds> getWds() {
		return Wds;
	}

}
