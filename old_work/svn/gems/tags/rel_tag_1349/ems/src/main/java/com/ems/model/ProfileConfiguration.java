package com.ems.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.ems.utils.ArgumentUtils;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
public class ProfileConfiguration implements Serializable {

    private static final long serialVersionUID = 4378793820912895414L;
    private Long id;
    private Set<WeekDay> weekDays;
    private Set<Holiday> holidays;
    private String morningTime;
    private String dayTime;
    private String eveningTime;
    private String nightTime;
    private Set<Holiday> currentYearHoliday;
    private Set<Holiday> nextYearHoliday;

    /**
     * @return the id
     */
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
}