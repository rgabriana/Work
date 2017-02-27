package com.ems.vo.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.model.BallastVoltPower;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BallastVoltPowerList {

	public static final int DEFAULT_ROWS = 20;

	@XmlElement(name = "page")
	private int page;
	@XmlElement(name = "records")
	private long records;
	@XmlElement(name = "total")
	private long total;
	@XmlElement(name = "rows")
	private long rows;
	
	@XmlElement(name = "ballastvoltpower")
	private List<BallastVoltPower> ballastVoltPower;
	
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
	public List<BallastVoltPower> getBallastVoltPower() {
		return ballastVoltPower;
	}
	public void setBallastVoltPower(List<BallastVoltPower> ballastVoltPower) {
		this.ballastVoltPower = ballastVoltPower;
	}
	public void setRows(long rows) {
		this.rows = rows;
	}
	public long getRows() {
		return rows;
	}	

}
