package com.ems.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ems.types.DayType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SweepTimerDetails implements Serializable {
	
	private static final long serialVersionUID = 4378793821912895415L;
	@XmlElement(name = "id")
	private Long id;
	private SweepTimer sweepTimer;
	@XmlElement(name = "day")
	private DayType day;
	@XmlElement(name = "shortOrder")
	private  Integer shortOrder;
	@XmlElement(name = "overrideTimer")
	private Integer overrideTimer;
	@XmlElement(name = "startTime1")
	private String startTime1;
	@XmlElement(name = "endTime1")
	private String endTime1;
	@XmlElement(name = "startTime2")
	private String startTime2;
	@XmlElement(name = "endTime2")
	private String endTime2;
	@XmlElement(name = "startTime3")
    private String startTime3;
    @XmlElement(name = "endTime3")
    private String endTime3;
	
	
	
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return the sweepTimer
	 */
	public SweepTimer getSweepTimer() {
		return sweepTimer;
	}
	/**
	 * @param sweepTimer the sweepTimer to set
	 */
	public void setSweepTimer(SweepTimer sweepTimer) {
		this.sweepTimer = sweepTimer;
	}
	/**
	 * @return the day
	 */
	public DayType getDay() {
		return day;
	}
	/**
	 * @param day the day to set
	 */
	public void setDay(DayType day) {
		this.day = day;
	}
	/**
	 * @return the shortOrder
	 */
	public Integer getShortOrder() {
		return shortOrder;
	}
	/**
	 * @param shortOrder the shortOrder to set
	 */
	public void setShortOrder(Integer shortOrder) {
		this.shortOrder = shortOrder;
	}
	/**
	 * @return the overrideTimer
	 */
	public Integer getOverrideTimer() {
		return overrideTimer;
	}
	/**
	 * @param overrideTimer the overrideTimer to set
	 */
	public void setOverrideTimer(Integer overrideTimer) {
		this.overrideTimer = overrideTimer;
	}
	/**
	 * @return the startTime1
	 */
	public String getStartTime1() {
		return startTime1;
	}
	/**
	 * @param startTime1 the startTime1 to set
	 */
	public void setStartTime1(String startTime1) {
		this.startTime1 = startTime1;
	}
	/**
	 * @return the endTime1
	 */
	public String getEndTime1() {
		return endTime1;
	}
	/**
	 * @param endTime1 the endTime1 to set
	 */
	public void setEndTime1(String endTime1) {
		this.endTime1 = endTime1;
	}
	/**
	 * @return the startTime2
	 */
	public String getStartTime2() {
		return startTime2;
	}
	/**
	 * @param startTime2 the startTime2 to set
	 */
	public void setStartTime2(String startTime2) {
		this.startTime2 = startTime2;
	}
	/**
	 * @return the endTime2
	 */
	public String getEndTime2() {
		return endTime2;
	}
	/**
	 * @param endTime2 the endTime2 to set
	 */
	public void setEndTime2(String endTime2) {
		this.endTime2 = endTime2;
	}
	
	
	public String getStartTime3() {
        return startTime3;
    }
    public void setStartTime3(String startTime3) {
        this.startTime3 = startTime3;
    }
    public String getEndTime3() {
        return endTime3;
    }
    public void setEndTime3(String endTime3) {
        this.endTime3 = endTime3;
    }
    public static boolean isTimeBetween(Date date, String start, String end) {
	  
	  StringTokenizer st = new StringTokenizer(start, ":");
	  int startHr = Integer.parseInt(st.nextToken());
	  int startMin = Integer.parseInt(st.nextToken());
	  
	  st = new StringTokenizer(end, ":");
	  int endHr = Integer.parseInt(st.nextToken());
	  int endMin = Integer.parseInt(st.nextToken());
	  
	  Calendar cal = Calendar.getInstance();
	  cal.set(Calendar.HOUR_OF_DAY, startHr);
	  cal.set(Calendar.MINUTE, startMin);
	  Date startDate = cal.getTime();
	  
	  cal.set(Calendar.HOUR_OF_DAY, endHr);
	  cal.set(Calendar.MINUTE, endMin);
	  Date endDate = cal.getTime();
	  
	  if(endDate.before(startDate)) {
	    //ideally this will not be the case as validation will be added in the client
	    cal.add(Calendar.DAY_OF_MONTH, 1);
	    endDate = cal.getTime();
	  }
	  cal.add(Calendar.MINUTE, 1);
	  endDate  = cal.getTime();
	  
	  if(date.after(startDate) && date.before(endDate)) {
	    return true;
	  }
	  return false;
	  
	} //end of method isTimeBetween

	public boolean isSweepTimerActive(Date date) {
	  
	  boolean active = false;
	  if(startTime1 != null && endTime1 != null &&
	      startTime1.length() > 0 && endTime1.length() > 0) {
	    active = isTimeBetween(date, startTime1, endTime1);
	  } 
	  if(!active) {
	    if(startTime2 != null && endTime2 != null &&
		startTime2.length() > 0 && endTime2.length() > 0) {
	      active = isTimeBetween(date, startTime2, endTime2);
	    }
	  }
	  
	  if(!active) {
	    if(startTime3 != null && endTime3 != null &&
		startTime3.length() > 0 && endTime3.length() > 0) {
	      active = isTimeBetween(date, startTime3, endTime3);
	    }
	  }
	  return active;
	  
	} //end of method isSweepTimerActive 
	
}
