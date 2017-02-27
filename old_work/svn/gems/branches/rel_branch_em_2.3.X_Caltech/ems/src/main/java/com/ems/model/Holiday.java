package com.ems.model;

import java.io.Serializable;
import java.util.Date;

import com.ems.utils.DateUtil;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
public class Holiday implements Serializable {

    private static final long serialVersionUID = 7892717005507144148L;
    private Long id;
    private Date holiday;
    private ProfileConfiguration profileConfiguration;

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
     * @return the holiday
     */
    public Date getHoliday() {
        return holiday;
    }

    /**
     * @param holiday
     *            the holiday to set
     */
    public void setHoliday(Date holiday) {
        this.holiday = holiday;
    }

    /**
     * @return the profileConfiguration
     */
    public ProfileConfiguration getProfileConfiguration() {
        return profileConfiguration;
    }

    /**
     * @param profileConfiguration
     *            the profileConfiguration to set
     */
    public void setProfileConfiguration(ProfileConfiguration profileConfiguration) {
        this.profileConfiguration = profileConfiguration;
    }

    public String getHolidayString() {
        return DateUtil.formatDate(holiday, "MMM dd");
    }

    public Holiday copy() {
        Holiday h = new Holiday();
        h.setHoliday(this.getHoliday());
        return h;
    }

    public void copyFrom(Holiday target) {
        this.setHoliday(target.getHoliday());
    }

}
