package com.ems.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class AssignPlugloadList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 431776385828817164L;
	
	public static final int DEFAULT_ROWS = 10;
	
	@XmlElement(name = "page")
	private int page;
	@XmlElement(name = "total")
	private long total;
	@XmlElement(name = "records")
	private long records;	
	
	@XmlElement(name = "assignFixtures")
	private List<AssignPlugload> profilePlugloadList = null;

	public AssignPlugloadList() {
		// TODO Auto-generated constructor stub
		setProfilePlugloadList(new ArrayList<AssignPlugload>());
	}

	
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


	public void setProfilePlugloadList(List<AssignPlugload> profilePlugloadList) {
		this.profilePlugloadList = profilePlugloadList;
	}


	public List<AssignPlugload> getProfilePlugloadList() {
		return profilePlugloadList;
	}
    
}
