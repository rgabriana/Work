package com.emscloud.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.emscloud.communication.vos.EMEvents;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EMEventList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 431776385828817164L;
	
	public static final int DEFAULT_ROWS = 100;
	
	@XmlElement(name = "page")
	private int page;
	@XmlElement(name = "total")
	private long total;
	@XmlElement(name = "records")
	private long records;	
	
	@XmlElementWrapper(name = "emEvents")
	@XmlElement(name = "emEvent")
	List<EMEvents> emEventsList = null;

	public EMEventList() {
	    emEventsList = new ArrayList<EMEvents>();
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

    /**
     * @return the emEventsList
     */
    public List<EMEvents> getEmEventsList() {
        return emEventsList;
    }

    /**
     * @param emEventsList the emEventsList to set
     */
    public void setEmEventsList(List<EMEvents> emEventsList) {
        this.emEventsList = emEventsList;
    }
}