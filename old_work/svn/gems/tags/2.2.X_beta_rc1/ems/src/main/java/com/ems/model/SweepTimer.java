package com.ems.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SweepTimer implements Serializable {
	
	private static final long serialVersionUID = 4378793821912895414L;
	@XmlElement(name = "id")
	private Long id;
	@XmlElementWrapper(name = "listOfSweepTimerDetails")
	@XmlElement(name = "sweepTimerDetails")
	private Collection<SweepTimerDetails> sweepTimerDetails;
	@XmlElement(name = "name")
	private String name;
	
	private HashMap<Integer, SweepTimerDetails> sweepTimerMap = 
	    new HashMap<Integer, SweepTimerDetails>();
	
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
	 * @return the sweepTimerDetails
	 */
	public Collection<SweepTimerDetails> getSweepTimerDetails() {
		return sweepTimerDetails;
	}
	/**
	 * @param sweepTimerDetails the sweepTimerDetails to set
	 */
	public void setSweepTimerDetails(Collection<SweepTimerDetails> sweepTimerDetails) {
		this.sweepTimerDetails = sweepTimerDetails;
		Iterator<SweepTimerDetails> it = sweepTimerDetails.iterator();
		while(it.hasNext()) {
		  SweepTimerDetails st = it.next();
		  sweepTimerMap.put(st.getShortOrder(), st);
		}
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public SweepTimerDetails getSweepTimerDetails(Date date) {
	  
	  Calendar cal = Calendar.getInstance();
	  cal.setTime(date);
	  int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
	  if(dayOfWeek == Calendar.SUNDAY) {
	    dayOfWeek = 6;
	  } else {
	    dayOfWeek -= 2;
	  }
	  //System.out.println("day of week of capture time -- " + dayOfWeek);
	  SweepTimerDetails std = sweepTimerMap.get(dayOfWeek);
	  return std;
	  
	} //end of method getOverrideTime
		
}
