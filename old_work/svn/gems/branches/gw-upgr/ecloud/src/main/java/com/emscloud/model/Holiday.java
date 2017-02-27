package com.emscloud.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.emscloud.util.DateUtil;

/**
 * 
 * @author Sharad K Mahajan
 * 
 */
@Entity
@Table(name = "holiday", schema = "public")
public class Holiday implements Serializable {

    private static final long serialVersionUID = 7892717005507144148L;
    private Long id;
    private Date holiday;
    private ProfileConfiguration profileConfiguration;

    /**
     * @return the id
     */
    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="holiday_seq")
    @SequenceGenerator(name="holiday_seq", sequenceName="holiday_seq",allocationSize=1, initialValue=1)
	@Column(name = "id")
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
    @Column(name="holiday")
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
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "profile_configuration_id")
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
    @Transient
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
