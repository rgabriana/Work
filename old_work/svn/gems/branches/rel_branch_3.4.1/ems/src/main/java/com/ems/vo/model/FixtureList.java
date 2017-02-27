/**
 * 
 */
package com.ems.vo.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.model.Fixture;

/**
 * @author yogesh
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FixtureList {
    public static final int DEFAULT_ROWS = 20;

    @XmlElement(name = "page")
    private int page;
    @XmlElement(name = "records")
    private long records;
    @XmlElement(name = "total")
    private long total;
    @XmlElement(name = "fixture")
    private List<Fixture> fixture;

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

    public List<Fixture> getFixture() {
        return fixture;
    }

    public void setFixture(List<Fixture> fixture) {
        this.fixture = fixture;
    }
}
