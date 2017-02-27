package com.emscloud.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Where;

import com.communication.utils.ArgumentUtils;

/**
 * 
 * @author Sharad K Mahajan
 * 
 */
@Entity
@Table(name = "profile_configuration", schema = "public")
@XmlRootElement(name = "profileconfiguration")
@XmlAccessorType(XmlAccessType.NONE)
public class ProfileConfiguration implements Serializable {

	
	static final Logger logger = Logger.getLogger("ProfileLogger");
	
    private static final long serialVersionUID = 4378793820912895414L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "weekDays")
    private Set<WeekDay> weekDays;
    private Set<Holiday> holidays;
    @XmlElement(name = "morningTime")
    private String morningTime;
    @XmlElement(name = "dayTime")
    private String dayTime;
    @XmlElement(name = "eveningTime")
    private String eveningTime;
    @XmlElement(name = "nightTime")
    private String nightTime;
    private Set<Holiday> currentYearHoliday;
    private Set<Holiday> nextYearHoliday;

    /**
     * @return the id
     */
    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="profile_configuration_seq")
    @SequenceGenerator(name="profile_configuration_seq", sequenceName="profile_configuration_seq",allocationSize=1, initialValue=1)
	@Column(name = "id",unique = true, nullable = false)
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the weekDays
     * @hibernate.set
     */
    @OneToMany(cascade={CascadeType.PERSIST,CascadeType.MERGE},mappedBy="profileConfiguration",fetch=FetchType.EAGER)
    @OrderBy("shortOrder")
    public Set<WeekDay> getWeekDays() {
        return weekDays;
    }

    /**
     * @param weekDays
     *            the weekDays to set
     */
    public void setWeekDays(Set<WeekDay> weekDays) {
        this.weekDays = weekDays;
    }

    /**
     * @return the holidays
     */
    @OneToMany(cascade={CascadeType.PERSIST,CascadeType.MERGE},mappedBy="profileConfiguration",fetch=FetchType.EAGER)
    public Set<Holiday> getHolidays() {
        return holidays;
    }

    /**
     * @param holidays
     *            the holidays to set
     */
    public void setHolidays(Set<Holiday> holidays) {
        this.holidays = holidays;
    }

    /**
     * @return the morningTime
     */
    @Column(name = "morning_time")
    public String getMorningTime() {
        return morningTime;
    }

    /**
     * @param morningTime
     *            the morningTime to set
     */
    public void setMorningTime(String morningTime) {
        this.morningTime = morningTime;
    }

    /**
     * @return the dayTime
     */
    @Column(name = "day_time")
    public String getDayTime() {
        return dayTime;
    }

    /**
     * @param dayTime
     *            the dayTime to set
     */
    public void setDayTime(String dayTime) {
        this.dayTime = dayTime;
    }

    /**
     * @return the eveningTime
     */
    @Column(name = "evening_time")
    public String getEveningTime() {
        return eveningTime;
    }

    /**
     * @param eveningTime
     *            the eveningTime to set
     */
    public void setEveningTime(String eveningTime) {
        this.eveningTime = eveningTime;
    }

    /**
     * @return the nightTime
     */
    @Column(name = "night_time")
    public String getNightTime() {
        return nightTime;
    }

    /**
     * @param nightTime
     *            the nightTime to set
     */
    public void setNightTime(String nightTime) {
        this.nightTime = nightTime;
    }

    /**
     * @return the currentYearHoliday
     */
    @OneToMany(cascade={CascadeType.PERSIST,CascadeType.MERGE},mappedBy="profileConfiguration",fetch=FetchType.EAGER)
    @Where(clause ="date_part('year', holiday) = date_part('year', CURRENT_DATE)+1")
    public Set<Holiday> getCurrentYearHoliday() {
        return currentYearHoliday;
    }

    /**
     * @param currentYearHoliday
     *            the currentYearHoliday to set
     */
    public void setCurrentYearHoliday(Set<Holiday> currentYearHoliday) {
        this.currentYearHoliday = currentYearHoliday;
    }

    /**
     * @return the nextYearHoliday
     */
    @OneToMany(cascade={CascadeType.PERSIST,CascadeType.MERGE},mappedBy="profileConfiguration",fetch=FetchType.EAGER)
    @Where(clause ="date_part('year', holiday) = date_part('year', CURRENT_DATE)+1")
    public Set<Holiday> getNextYearHoliday() {
        return nextYearHoliday;
    }

    /**
     * @param nextYearHoliday
     *            the nextYearHoliday to set
     */
    public void setNextYearHoliday(Set<Holiday> nextYearHoliday) {
        this.nextYearHoliday = nextYearHoliday;
    }

    public ProfileConfiguration copy() {
        ProfileConfiguration pc = new ProfileConfiguration();
        pc.setDayTime(this.getDayTime());
        pc.setEveningTime(this.getEveningTime());
        pc.setMorningTime(this.getMorningTime());
        pc.setNightTime(this.getNightTime());
        Set<WeekDay> newWeekDays = new HashSet<WeekDay>();
        Set<Holiday> newHolidays = new HashSet<Holiday>();
        Set<Holiday> newCurrentYearHoliday = new HashSet<Holiday>();
        Set<Holiday> newNextYearHoliday = new HashSet<Holiday>();
        if (!ArgumentUtils.isNullOrEmpty(weekDays)) {
            newWeekDays = setWeekDays(newWeekDays, weekDays, pc);
            pc.setWeekDays(newWeekDays);
        }
        if (!ArgumentUtils.isNullOrEmpty(holidays)) {
            newHolidays = setHolidays(newHolidays, holidays, pc);
            pc.setHolidays(newHolidays);
        }
        if (!ArgumentUtils.isNullOrEmpty(currentYearHoliday)) {
            newCurrentYearHoliday = setHolidays(newCurrentYearHoliday, currentYearHoliday, pc);
            pc.setCurrentYearHoliday(newCurrentYearHoliday);
        }
        if (!ArgumentUtils.isNullOrEmpty(nextYearHoliday)) {
            newNextYearHoliday = setHolidays(newNextYearHoliday, nextYearHoliday, pc);
            pc.setNextYearHoliday(newNextYearHoliday);
        }
        return pc;
    }

    /**
     * Copy target profile configuration to source.
     */
    public void copyFrom(ProfileConfiguration target) {
        this.setDayTime(target.getDayTime());
        this.setEveningTime(target.getEveningTime());
        this.setMorningTime(target.getMorningTime());
        this.setNightTime(target.getNightTime());
        Set<WeekDay> newWeekDays = target.getWeekDays();
        Set<Holiday> newHolidays = target.getHolidays();
        Set<Holiday> newCurrentYearHoliday = target.getCurrentYearHoliday();
        Set<Holiday> newNextYearHoliday = target.getNextYearHoliday();

        int i = 0;
        if (!ArgumentUtils.isNullOrEmpty(this.weekDays) && !ArgumentUtils.isNullOrEmpty(newWeekDays)) {
            Object[] oNewWeekDays = (Object[]) newWeekDays.toArray();
            Object[] oWeekDays = (Object[]) this.weekDays.toArray();
            for (i = 0; i < oNewWeekDays.length; i++) {
                ((WeekDay) oWeekDays[i]).copyFrom((WeekDay) oNewWeekDays[i]);
            }
        }

        if (!ArgumentUtils.isNullOrEmpty(this.holidays) && !ArgumentUtils.isNullOrEmpty(newHolidays)) {
            Object[] oNewHolidays = (Object[]) newHolidays.toArray();
            Object[] oHolidays = (Object[]) this.holidays.toArray();
            for (i = 0; i < oNewHolidays.length; i++) {
                ((Holiday) oHolidays[i]).copyFrom((Holiday) oNewHolidays[i]);
            }
        }

        if (!ArgumentUtils.isNullOrEmpty(this.currentYearHoliday)
                && !ArgumentUtils.isNullOrEmpty(newCurrentYearHoliday)) {
            Object[] oNewCurrentYearHoliday = (Object[]) newCurrentYearHoliday.toArray();
            Object[] oCurrentYearHoliday = (Object[]) this.currentYearHoliday.toArray();
            for (i = 0; i < oNewCurrentYearHoliday.length; i++) {
                ((Holiday) oCurrentYearHoliday[i]).copyFrom((Holiday) oNewCurrentYearHoliday[i]);
            }
        }

        if (!ArgumentUtils.isNullOrEmpty(this.nextYearHoliday) && !ArgumentUtils.isNullOrEmpty(newNextYearHoliday)) {
            Object[] oNewNextYearHoliday = (Object[]) newNextYearHoliday.toArray();
            Object[] oNextYearHoliday = (Object[]) this.nextYearHoliday.toArray();
            for (i = 0; i < oNewNextYearHoliday.length; i++) {
                ((Holiday) oNextYearHoliday[i]).copyFrom((Holiday) oNewNextYearHoliday[i]);
            }
        }
    }

    public void copyPCTimingsFrom(ProfileConfiguration target) {
        this.setDayTime(target.getDayTime());
        this.setEveningTime(target.getEveningTime());
        this.setMorningTime(target.getMorningTime());
        this.setNightTime(target.getNightTime());
    }
    private Set<WeekDay> setWeekDays(Set<WeekDay> newWeekDays, Set<WeekDay> weekDays, ProfileConfiguration pc) {
        for (WeekDay weekDay : weekDays) {
            WeekDay wd = weekDay.copy();
            wd.setProfileConfiguration(pc);
            newWeekDays.add(wd);
        }
        return newWeekDays;
    }

    private Set<Holiday> setHolidays(Set<Holiday> newHolidays, Set<Holiday> holidays, ProfileConfiguration pc) {
        for (Holiday holiday : holidays) {
            Holiday h = holiday.copy();
            h.setProfileConfiguration(pc);
            newHolidays.add(h);
        }
        return newHolidays;
    }
    
    public int compare(ProfileConfiguration p2) {
        if(!this.getMorningTime().equalsIgnoreCase(p2.getMorningTime()))
            return -1;
        if(!this.getDayTime().equalsIgnoreCase(p2.getDayTime()))
            return -1;              
        if(!this.getEveningTime().equalsIgnoreCase(p2.getEveningTime()))
            return -1;
        if(!this.getNightTime().equalsIgnoreCase(p2.getNightTime()))
            return -1;  
        
        Set<WeekDay> newWeekDays = p2.getWeekDays();

        int i = 0, j = 0;
        int result = 0;
        if (!ArgumentUtils.isNullOrEmpty(this.weekDays) && !ArgumentUtils.isNullOrEmpty(newWeekDays)) {
            Object[] oNewWeekDays = (Object[]) newWeekDays.toArray();
            Object[] oWeekDays = (Object[]) this.weekDays.toArray();
            for (i = 0; i < oNewWeekDays.length; i++) {
            	for(j = 0; j < oWeekDays.length; j++) {
            		WeekDay oSrc = (WeekDay)oWeekDays[j];
            		WeekDay oDest = (WeekDay)oNewWeekDays[i];
            		if (oSrc.getDay().equals(oDest.getDay())) {
            			if (!oSrc.getType().equals(oDest.getType())) {
							logger.error("(" + this.getId() + " : " + p2.getId()
									+ "), (" + oSrc.getDay() + " : "
									+ oDest.getDay() + "), (" + oSrc.getType()
									+ " : " + oDest.getType() + ")");
            				return -1;
            			}
            		}
            	}
            }
        }
        return result;
    }
}
