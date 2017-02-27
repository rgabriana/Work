/**
 * 
 */
package com.ems.vo.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.model.DRTarget;

/**
 * @author Chetan
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DRTargetList {
    public static final int DEFAULT_ROWS = 5;

    @XmlElement(name = "page")
    private int page;
    @XmlElement(name = "records")
    private long records;
    @XmlElement(name = "total")
    private long total;
    @XmlElement(name = "drtarget")
    private List<DRTarget> drtarget;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Long getRecords() {
        return records;
    }

    public void setRecords(long records) {
        this.records = records;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        //this.records = total;
        this.total = (int) (Math.ceil(records / new Double(DEFAULT_ROWS)));
        if (this.total == 0) {
            this.total = 1;
        }
    }

	public void setDrtarget(List<DRTarget> drtarget) {
		this.drtarget = drtarget;
	}

	public List<DRTarget> getDrtarget() {
		return drtarget;
	}
  }
