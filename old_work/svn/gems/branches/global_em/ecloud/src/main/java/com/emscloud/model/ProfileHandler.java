package com.emscloud.model;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CascadeType;
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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import com.emscloud.server.ServerConstants;
import com.emscloud.server.util.ServerUtil;

/**
 * 
 * @author Sharad K Mahajan
 * 
 */
@Entity
@Table(name = "profile_handler", schema = "public")
@XmlRootElement(name = "profilehandler")
@XmlAccessorType(XmlAccessType.NONE)
public class ProfileHandler implements Serializable, Cloneable  {

    private static final long serialVersionUID = 5619650083032600792L;
    private static Logger profileLogger = Logger.getLogger("ProfileLogger");

    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "morningProfile")
    private Profile morningProfile;
    @XmlElement(name = "dayProfile")
    private Profile dayProfile;
    @XmlElement(name = "eveningProfile")
    private Profile eveningProfile;
    @XmlElement(name = "nightProfile")
    private Profile nightProfile;
    @XmlElement(name = "morningProfileWeekEnd")
    private Profile morningProfileWeekEnd;
    @XmlElement(name = "dayProfileWeekEnd")
    private Profile dayProfileWeekEnd;
    @XmlElement(name = "eveningProfileWeekEnd")
    private Profile eveningProfileWeekEnd;
    @XmlElement(name = "nightProfileWeekEnd")
    private Profile nightProfileWeekEnd;
    @XmlElement(name = "morningProfileHoliday")
    private Profile morningProfileHoliday;
    @XmlElement(name = "dayProfileHoliday")
    private Profile dayProfileHoliday;
    @XmlElement(name = "eveningProfileHoliday")
    private Profile eveningProfileHoliday;
    @XmlElement(name = "nightProfileHoliday")
    private Profile nightProfileHoliday;
    @XmlElement(name = "profileConfiguration")
    private ProfileConfiguration profileConfiguration;
    @XmlElement(name = "darkLux")
    private Integer darkLux;
    @XmlElement(name = "neighborLux")
    private Integer neighborLux;
    @XmlElement(name = "envelopeOnLevel")
    private Integer envelopeOnLevel;
    @XmlElement(name = "dropPercent")
    private Integer dropPercent;
    @XmlElement(name = "risePercent")
    private Integer risePercent;
    @XmlElement(name = "dimBackoffTime")
    private Short dimBackoffTime;
    @XmlElement(name = "intensityNormTime")
    private Short intensityNormTime;
    @XmlElement(name = "onAmbLightLevel")
    private Integer onAmbLightLevel;
    @XmlElement(name = "minLevelBeforeOff")
    private Short minLevelBeforeOff;
    @XmlElement(name = "relaysConnected")
    private Integer relaysConnected;
    @XmlElement(name = "profileChecksum")
    private Short profileChecksum;
    @XmlElement(name = "globalProfileChecksum")
    private Short globalProfileChecksum;
    @XmlElement(name = "standaloneMotionOverride")
    private Short standaloneMotionOverride;
    @XmlElement(name = "drReactivity")
    private Byte drReactivity;
    @XmlElement(name = "toOffLinger")
    private Integer toOffLinger;
    @XmlElement(name = "initialOnLevel")
    private Byte initialOnLevel;
    @XmlElement(name = "profileGroupId")
    private Short profileGroupId;
    @XmlElement(name = "profileFlag")
    private Byte profileFlag;
    @XmlElement(name = "initialOnTime")
    private Integer initialOnTime;
    @XmlElement(name = "isHighBay")
    private Byte isHighBay;
    @XmlElement(name = "motionThresholdGain")
    private Integer motionThresholdGain;
    @XmlElement(name = "drHighLevel")
    private Byte drHighLevel = 0;
    @XmlElement(name = "drModerateLevel")
    private Byte drModerateLevel = 0;
    @XmlElement(name = "drLowLevel")
    private Byte drLowLevel = 0;
    @XmlElement(name = "drSpecialLevel")
    private Byte drSpecialLevel = 0;
    @XmlElement(name = "daylightHarvesting")
    private Byte daylightHarvesting = 0;
    
    public Object clone() {
        return new ProfileHandler();
    }

    /**
     * @return the id
     */
    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="profile_handler_seq")
    @SequenceGenerator(name="profile_handler_seq", sequenceName="profile_handler_seq",allocationSize=1, initialValue=1)
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
     * @return the morningProfile
     */
    @ManyToOne(targetEntity=Profile.class,cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
	@JoinColumn(name = "morning_profile_id",insertable=true,updatable=true)
    public Profile getMorningProfile() {
        return morningProfile;
    }

    /**
     * @param morningProfile
     *            the morningProfile to set
     */
    public void setMorningProfile(Profile morningProfile) {
        this.morningProfile = morningProfile;
    }

    /**
     * @return the dayProfile
     */
    @ManyToOne(targetEntity=Profile.class,cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
   	@JoinColumn(name = "day_profile_id",insertable=true,updatable=true)
    public Profile getDayProfile() {
        return dayProfile;
    }

    /**
     * @param dayProfile
     *            the dayProfile to set
     */
    public void setDayProfile(Profile dayProfile) {
        this.dayProfile = dayProfile;
    }

    /**
     * @return the eveningProfile
     */
    @ManyToOne(targetEntity=Profile.class,cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
   	@JoinColumn(name = "evening_profile_id",insertable=true,updatable=true)
    public Profile getEveningProfile() {
        return eveningProfile;
    }

    /**
     * @param eveningProfile
     *            the eveningProfile to set
     */
    public void setEveningProfile(Profile eveningProfile) {
        this.eveningProfile = eveningProfile;
    }

    /**
     * @return the nightProfile
     */
    @ManyToOne(targetEntity=Profile.class,cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
   	@JoinColumn(name = "night_profile_id",insertable=true,updatable=true)
    public Profile getNightProfile() {
        return nightProfile;
    }

    /**
     * @param nightProfile
     *            the nightProfile to set
     */
    public void setNightProfile(Profile nightProfile) {
        this.nightProfile = nightProfile;
    }

    /**
     * @return the morningProfileWeekEnd
     */
    @ManyToOne(targetEntity=Profile.class,cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
   	@JoinColumn(name = "morning_profile_weekend",insertable=true,updatable=true)
    public Profile getMorningProfileWeekEnd() {
        return morningProfileWeekEnd;
    }

    /**
     * @param morningProfileWeekEnd
     *            the morningProfileWeekEnd to set
     */
    public void setMorningProfileWeekEnd(Profile morningProfileWeekEnd) {
        this.morningProfileWeekEnd = morningProfileWeekEnd;
    }

    /**
     * @return the dayProfileWeekEnd
     */
    @ManyToOne(targetEntity=Profile.class,cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
   	@JoinColumn(name = "day_profile_weekend",insertable=true,updatable=true)
    public Profile getDayProfileWeekEnd() {
        return dayProfileWeekEnd;
    }

    /**
     * @param dayProfileWeekEnd
     *            the dayProfileWeekEnd to set
     */
    public void setDayProfileWeekEnd(Profile dayProfileWeekEnd) {
        this.dayProfileWeekEnd = dayProfileWeekEnd;
    }

    /**
     * @return the eveningProfileWeekEnd
     */
    @ManyToOne(targetEntity=Profile.class,cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
   	@JoinColumn(name = "evening_profile_weekend",insertable=true,updatable=true)
    public Profile getEveningProfileWeekEnd() {
        return eveningProfileWeekEnd;
    }

    /**
     * @param eveningProfileWeekEnd
     *            the eveningProfileWeekEnd to set
     */
    public void setEveningProfileWeekEnd(Profile eveningProfileWeekEnd) {
        this.eveningProfileWeekEnd = eveningProfileWeekEnd;
    }

    /**
     * @return the nightProfileWeekEnd
     */
    @ManyToOne(targetEntity=Profile.class,cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
   	@JoinColumn(name = "night_profile_weekend",insertable=true,updatable=true)
    public Profile getNightProfileWeekEnd() {
        return nightProfileWeekEnd;
    }

    /**
     * @param nightProfileWeekEnd
     *            the nightProfileWeekEnd to set
     */
    public void setNightProfileWeekEnd(Profile nightProfileWeekEnd) {
        this.nightProfileWeekEnd = nightProfileWeekEnd;
    }

    /**
     * @return the morningProfileHoliday
     */
    @ManyToOne(targetEntity=Profile.class,cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
   	@JoinColumn(name = "morning_profile_holiday",insertable=true,updatable=true)
    public Profile getMorningProfileHoliday() {
        return morningProfileHoliday;
    }

    /**
     * @param morningProfileHoliday
     *            the morningProfileHoliday to set
     */
    public void setMorningProfileHoliday(Profile morningProfileHoliday) {
        this.morningProfileHoliday = morningProfileHoliday;
    }

    /**
     * @return the dayProfileHoliday
     */
    @ManyToOne(targetEntity=Profile.class,cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
   	@JoinColumn(name = "day_profile_holiday",insertable=true,updatable=true)
    public Profile getDayProfileHoliday() {
        return dayProfileHoliday;
    }

    /**
     * @param dayProfileHoliday
     *            the dayProfileHoliday to set
     */
    public void setDayProfileHoliday(Profile dayProfileHoliday) {
        this.dayProfileHoliday = dayProfileHoliday;
    }

    /**
     * @return the eveningProfileHoliday
     */
    @ManyToOne(targetEntity=Profile.class,cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
   	@JoinColumn(name = "evening_profile_holiday",insertable=true,updatable=true)
    public Profile getEveningProfileHoliday() {
        return eveningProfileHoliday;
    }

    /**
     * @param eveningProfileHoliday
     *            the eveningProfileHoliday to set
     */
    public void setEveningProfileHoliday(Profile eveningProfileHoliday) {
        this.eveningProfileHoliday = eveningProfileHoliday;
    }

    /**
     * @return the nightProfileHoliday
     */
    @ManyToOne(targetEntity=Profile.class,cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
   	@JoinColumn(name = "night_profile_holiday",insertable=true,updatable=true)
    public Profile getNightProfileHoliday() {
        return nightProfileHoliday;
    }

    /**
     * @param nightProfileHoliday
     *            the nightProfileHoliday to set
     */
    public void setNightProfileHoliday(Profile nightProfileHoliday) {
        this.nightProfileHoliday = nightProfileHoliday;
    }

    /**
     * @return the profileConfiguration
     */
    @ManyToOne(targetEntity=ProfileConfiguration.class,cascade={CascadeType.PERSIST,CascadeType.MERGE},fetch = FetchType.EAGER)
   	@JoinColumn(name = "profile_configuration_id",insertable=true,updatable=true)
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

    /**
     * @return dark lux
     */
    @Column(name = "dark_lux")
    public Integer getDarkLux() {
        return darkLux;
    }

    public void setDarkLux(Integer darkLux) {
        this.darkLux = darkLux;
    }

    /**
     * @return neighbor lux
     */
    @Column(name = "neighbor_lux")
    public Integer getNeighborLux() {
        return neighborLux;
    }

    public void setNeighborLux(Integer neighborLux) {
        this.neighborLux = neighborLux;
    }

    /**
     * @return envelope on level
     */
    @Column(name = "envelope_on_level")
    public Integer getEnvelopeOnLevel() {
        return envelopeOnLevel;
    }

    public void setEnvelopeOnLevel(Integer envelopeOnLevel) {
        this.envelopeOnLevel = envelopeOnLevel;
    }

    /**
     * @return drop percentage
     */
    @Column(name = "drop")
    public Integer getDropPercent() {
        return dropPercent;
    }

    public void setDropPercent(Integer dropPercent) {
        this.dropPercent = dropPercent;
    }

    /**
     * @return rise percentage
     */
    @Column(name = "rise")
    public Integer getRisePercent() {
        return risePercent;
    }

    public void setRisePercent(Integer risePercent) {
        this.risePercent = risePercent;
    }

    /**
     * @return dim back off time
     */
    @Column(name = "dim_backoff_time")
    public Short getDimBackoffTime() {
        return dimBackoffTime;
    }

    public void setDimBackoffTime(Short dimBackoffTime) {
        this.dimBackoffTime = dimBackoffTime;
    }

    /**
     * @return intensity norm time
     */
    @Column(name = "intensity_norm_time")
    public Short getIntensityNormTime() {
        return intensityNormTime;
    }

    public void setIntensityNormTime(Short intensityNormTime) {
        this.intensityNormTime = intensityNormTime;
    }

    /**
     * @return on ambience light level
     */
    @Column(name = "on_amb_light_level")
    public Integer getOnAmbLightLevel() {
        return onAmbLightLevel;
    }

    public void setOnAmbLightLevel(Integer onAmbLightLevel) {
        this.onAmbLightLevel = onAmbLightLevel;
    }

    /**
     * @return minimum level before off
     */
    @Column(name = "min_level_before_off")
    public Short getMinLevelBeforeOff() {
        return minLevelBeforeOff;
    }

    public void setMinLevelBeforeOff(Short minLevelBeforeOff) {
        this.minLevelBeforeOff = minLevelBeforeOff;
    }

    /**
     * @return relays connected
     */
    @Column(name = "relays_connected")
    public Integer getRelaysConnected() {
        return relaysConnected;
    }

    public void setRelaysConnected(Integer relaysConnected) {
        this.relaysConnected = relaysConnected;
    }

    /**
     * @return profile checksum
     */
    @Column(name = "profile_checksum")
    public Short getProfileChecksum() {
        return profileChecksum;
    }

    public void setProfileChecksum(Short profileChecksum) {
        this.profileChecksum = profileChecksum;
    }

    /**
     * @return global profile checksum
     */
    @Column(name = "global_profile_checksum")
    public Short getGlobalProfileChecksum() {
        return globalProfileChecksum;
    }

    public void setGlobalProfileChecksum(Short globalProfileChecksum) {
        this.globalProfileChecksum = globalProfileChecksum;
    }

    /**
     * @return standalone motion override
     */
    @Column(name = "standalone_motion_override")
    public Short getStandaloneMotionOverride() {
        return standaloneMotionOverride;
    }

    public void setStandaloneMotionOverride(Short standaloneMotionOverride) {
        this.standaloneMotionOverride = standaloneMotionOverride;
    }

    /**
     * @return dr reactivity
     */
    @Column(name = "dr_reactivity")
    public Byte getDrReactivity() {
        return drReactivity;
    }

    public void setDrReactivity(Byte drReactivity) {
        this.drReactivity = drReactivity;
    }

    /**
     * @return to off linger
     */
    @Column(name = "to_off_linger")
    public Integer getToOffLinger() {
        if (toOffLinger == null)
            return 10;
        return toOffLinger;
    }

    public void setToOffLinger(Integer toOffLinger) {
        this.toOffLinger = toOffLinger;
    }

    /**
     * @return to initial on time
     */
    @Column(name = "initial_on_time")
    public Integer getInitialOnTime() {
        if (initialOnTime == null)
            return 5;
        return initialOnTime;
    }

    public void setInitialOnTime(Integer initialOnTime) {
        this.initialOnTime = initialOnTime;
    }

    /**
     * @return initial on level
     */
    @Column(name = "initial_on_level")
    public Byte getInitialOnLevel() {
        return initialOnLevel;
    }

    public void setInitialOnLevel(Byte initialOnLevel) {
        this.initialOnLevel = initialOnLevel;
    }

    /**
     * @return profile group id
     */
    @Column(name = "profile_group_id")
    public Short getProfileGroupId() {
        return profileGroupId;
    }

    public void setProfileGroupId(Short profileGroupId) {
        this.profileGroupId = profileGroupId;
    }

    /**
     * @return profile flag
     */
    @Column(name = "profile_flag")
    public Byte getProfileFlag() {
        if (profileFlag == null) {
            return 0;
        }
        return profileFlag;
    }

    public void setProfileFlag(Byte profileFlag) {
        this.profileFlag = profileFlag;
    }
    
    /**
     * @return the isHighBay
     */
    @Column(name = "is_high_bay")
    public Byte getIsHighBay() {
        if (isHighBay == null)
            return 0;
      return isHighBay;
    }
    
    /**
     * @param isHighBay the isHighBay to set
     */
    public void setIsHighBay(Byte isHighBay) {
    
      this.isHighBay = isHighBay;
    }
    
    /**
     * @return the motionThresholdGain
     */
    @Column(name = "motion_threshold_gain")
    public Integer getMotionThresholdGain() {
    if (motionThresholdGain == null)
        return 0;
      return motionThresholdGain;
    }
    
    /**
     * @param motionThresholdGain the motionThresholdGain to set
     */
    public void setMotionThresholdGain(Integer motionThresholdGain) {
    
      this.motionThresholdGain = motionThresholdGain;
    }
       
    /**
     * @return the drHighLevel
     */
    @Column(name = "dr_high_level")
    public Byte getDrHighLevel() {
    if (drHighLevel == null)
        return 0;
      return drHighLevel;
    }
    
    /**
     * @param drHighLevel the drHighLevel to set
     */
    public void setDrHighLevel(Byte drHighLevel) {
    
      this.drHighLevel = drHighLevel;
    }
    
    /**
     * @return the drModerateLevel
     */
    @Column(name = "dr_moderate_level")
    public Byte getDrModerateLevel() {
    if (drModerateLevel == null)
        return 0;
      return drModerateLevel;
    }
    
    /**
     * @param drModerateLevel the drModerateLevel to set
     */
    public void setDrModerateLevel(Byte drModerateLevel) {
    
      this.drModerateLevel = drModerateLevel;
    }
    
    /**
     * @return the drLowLevel
     */
    @Column(name = "dr_low_level")
    public Byte getDrLowLevel() {
    if (drLowLevel == null)
        return 0;
      return drLowLevel;
    }
    
    /**
     * @param drLowLevel the drLowLevel to set
     */
    public void setDrLowLevel(Byte drLowLevel) {
    
      this.drLowLevel = drLowLevel;
    }
    
    /**
     * @return the specialOverrideProfile
     */
    @Column(name = "dr_special_level")
    public Byte getDrSpecialLevel() {
    if (drSpecialLevel == null)
        return 0;
      return drSpecialLevel;
    }
    
    /**
     * @param specialOverrideProfile the specialOverrideProfile to set
     */
    public void setDrSpecialLevel(Byte drSpecialLevel) {
    
      this.drSpecialLevel = drSpecialLevel;
    }
    
    /**
     * dayLightHarvesting is the common value for title24 and other properties. The rightmost first bit represents the value for title24 and so on..
     * 
     * @return
     */
    @Column(name = "daylightharvesting")
    public Byte getDaylightHarvesting() {
    	if(daylightHarvesting == null){
    		daylightHarvesting = 0;
    	}
		return daylightHarvesting;
	}

	public void setDaylightHarvesting(Byte daylightHarvesting) {
		if(daylightHarvesting == null){
			this.daylightHarvesting = 0;
    	}else{
    		this.daylightHarvesting = daylightHarvesting;
    	}
	}
	
    public ProfileHandler copy() {
        ProfileHandler ph = new ProfileHandler();
        ph.setDayProfile(dayProfile.copy());
        ph.setDayProfileHoliday(dayProfileHoliday.copy());
        ph.setDayProfileWeekEnd(dayProfileWeekEnd.copy());
        ph.setEveningProfile(eveningProfile.copy());
        ph.setEveningProfileHoliday(eveningProfileHoliday.copy());
        ph.setEveningProfileWeekEnd(eveningProfileWeekEnd.copy());
        ph.setMorningProfile(morningProfile.copy());
        ph.setMorningProfileHoliday(morningProfileHoliday.copy());
        ph.setMorningProfileWeekEnd(morningProfileWeekEnd.copy());
        ph.setNightProfile(nightProfile.copy());
        ph.setNightProfileHoliday(nightProfileHoliday.copy());
        ph.setNightProfileWeekEnd(nightProfileWeekEnd.copy());
        ph.setProfileConfiguration(profileConfiguration.copy());
        ph.setDarkLux(darkLux);
        ph.setNeighborLux(neighborLux);
        ph.setEnvelopeOnLevel(envelopeOnLevel);
        ph.setDropPercent(dropPercent);
        ph.setRisePercent(risePercent);
        ph.setDimBackoffTime(dimBackoffTime);
        ph.setIntensityNormTime(intensityNormTime);
        ph.setOnAmbLightLevel(onAmbLightLevel);
        ph.setMinLevelBeforeOff(minLevelBeforeOff);
        ph.setRelaysConnected(relaysConnected);
        ph.setProfileChecksum(profileChecksum);
        ph.setGlobalProfileChecksum(globalProfileChecksum);
        ph.setStandaloneMotionOverride(standaloneMotionOverride);
        ph.setDrReactivity(drReactivity);
        ph.setToOffLinger(toOffLinger);
        ph.setInitialOnLevel(initialOnLevel);
        ph.setProfileGroupId(profileGroupId);
        ph.setProfileFlag(profileFlag);
        ph.setInitialOnTime(initialOnTime);
        ph.setIsHighBay(isHighBay);
        ph.setMotionThresholdGain(motionThresholdGain);
        ph.setDrHighLevel(drHighLevel);
        ph.setDrModerateLevel(drModerateLevel);
        ph.setDrLowLevel(drLowLevel);
        ph.setDrSpecialLevel(drSpecialLevel);
        ph.setDaylightHarvesting(daylightHarvesting);
        return ph;
    }

    public void copyFrom(ProfileHandler target) {
        this.dayProfile.copyFrom(target.getDayProfile());
        this.dayProfileHoliday.copyFrom(target.getDayProfileHoliday());
        this.dayProfileWeekEnd.copyFrom(target.getDayProfileWeekEnd());
        this.eveningProfile.copyFrom(target.getEveningProfile());
        this.eveningProfileHoliday.copyFrom(target.getEveningProfileHoliday());
        this.eveningProfileWeekEnd.copyFrom(target.getEveningProfileWeekEnd());
        this.morningProfile.copyFrom(target.getMorningProfile());
        this.morningProfileHoliday.copyFrom(target.getMorningProfileHoliday());
        this.morningProfileWeekEnd.copyFrom(target.getMorningProfileWeekEnd());
        this.nightProfile.copyFrom(target.getNightProfile());
        this.nightProfileHoliday.copyFrom(target.getNightProfileHoliday());
        this.nightProfileWeekEnd.copyFrom(target.getNightProfileWeekEnd());
        this.profileConfiguration.copyFrom(target.getProfileConfiguration());
        this.setDarkLux(target.getDarkLux());
        this.setNeighborLux(target.getNeighborLux());
        this.setEnvelopeOnLevel(target.getEnvelopeOnLevel());
        this.setDropPercent(target.getDropPercent());
        this.setRisePercent(target.getRisePercent());
        this.setDimBackoffTime(target.getDimBackoffTime());
        this.setIntensityNormTime(target.getIntensityNormTime());
        this.setOnAmbLightLevel(target.getOnAmbLightLevel());
        this.setMinLevelBeforeOff(target.getMinLevelBeforeOff());
        this.setRelaysConnected(target.getRelaysConnected());
        this.setProfileChecksum(target.getProfileChecksum());
        this.setGlobalProfileChecksum(target.getGlobalProfileChecksum());
        this.setStandaloneMotionOverride(target.getStandaloneMotionOverride());
        this.setDrReactivity(target.getDrReactivity());
        this.setToOffLinger(target.getToOffLinger());
        this.setInitialOnLevel(target.getInitialOnLevel());
        this.setProfileGroupId(target.getProfileGroupId());
        this.setProfileFlag(target.getProfileFlag());
        this.setInitialOnTime(target.getInitialOnTime());
        this.setIsHighBay(target.getIsHighBay());
        this.setMotionThresholdGain(target.getMotionThresholdGain());
        this.setDrHighLevel(target.getDrHighLevel());
        this.setDrModerateLevel(target.getDrModerateLevel());
        this.setDrLowLevel(target.getDrLowLevel());
        this.setDrSpecialLevel(target.getDrSpecialLevel());
        this.setDaylightHarvesting(target.getDaylightHarvesting());
    }
    
    public void copyProfilesFrom(ProfileHandler target) {
        this.dayProfile.copyFrom(target.getDayProfile());
        this.dayProfileWeekEnd.copyFrom(target.getDayProfileWeekEnd());
        this.eveningProfile.copyFrom(target.getEveningProfile());
        this.eveningProfileWeekEnd.copyFrom(target.getEveningProfileWeekEnd());
        this.morningProfile.copyFrom(target.getMorningProfile());
        this.morningProfileWeekEnd.copyFrom(target.getMorningProfileWeekEnd());
        this.nightProfile.copyFrom(target.getNightProfile());
        this.nightProfileWeekEnd.copyFrom(target.getNightProfileWeekEnd());
        
        //Add holidays too
        this.dayProfileHoliday.copyFrom(target.getDayProfileHoliday());
        this.morningProfileHoliday.copyFrom(target.getMorningProfileHoliday());
        this.eveningProfileHoliday.copyFrom(target.getEveningProfileHoliday());        
        this.nightProfileHoliday.copyFrom(target.getNightProfileHoliday());
    }

    public void copyPCTimesFrom(ProfileHandler target) {
        this.profileConfiguration.copyPCTimingsFrom(target.getProfileConfiguration());
    }
    
    public void copyOverrideProfilesFrom(ProfileHandler target) {    	
    	this.setDrLowLevel(target.getDrLowLevel());
    	this.setDrModerateLevel(target.getDrModerateLevel());
    	this.setDrHighLevel(target.getDrHighLevel());
    	this.setDrSpecialLevel(target.getDrSpecialLevel());
    	this.setDaylightHarvesting(target.getDaylightHarvesting());
    }
    
    public void copyAdvanceProfileFrom(ProfileHandler target) {
        this.setDarkLux(target.getDarkLux());
        this.setNeighborLux(target.getNeighborLux());
        this.setEnvelopeOnLevel(target.getEnvelopeOnLevel());
        this.setDropPercent(target.getDropPercent());
        this.setRisePercent(target.getRisePercent());
        this.setDimBackoffTime(target.getDimBackoffTime());
        this.setIntensityNormTime(target.getIntensityNormTime());
        this.setOnAmbLightLevel(target.getOnAmbLightLevel());
        this.setMinLevelBeforeOff(target.getMinLevelBeforeOff());
        if(target.getRelaysConnected()!=null)
           	this.setRelaysConnected(target.getRelaysConnected());
        this.setProfileChecksum(target.getProfileChecksum());
        this.setGlobalProfileChecksum(target.getGlobalProfileChecksum());
        if(target.getStandaloneMotionOverride()!=null)
        	this.setStandaloneMotionOverride(target.getStandaloneMotionOverride());
        this.setDrReactivity(target.getDrReactivity());
        this.setToOffLinger(target.getToOffLinger());
        this.setInitialOnLevel(target.getInitialOnLevel());
        if(target.getProfileGroupId()!=null)
        	this.setProfileGroupId(target.getProfileGroupId());
        if(target.getProfileFlag()!=null)
        	this.setProfileFlag(target.getProfileFlag());
        if(target.getInitialOnTime()!=null)
        	this.setInitialOnTime(target.getInitialOnTime());
        if(target.getIsHighBay()!=null)
        	this.setIsHighBay(target.getIsHighBay());
        if(target.getMotionThresholdGain()!=null)
        	this.setMotionThresholdGain(target.getMotionThresholdGain());
        if(target.getDrHighLevel() != null) {
        	this.setDrHighLevel(target.getDrHighLevel());
        }
        if(target.getDrModerateLevel() != null) {
        	this.setDrModerateLevel(target.getDrModerateLevel());
        }
        if(target.getDrLowLevel() != null) {
        	this.setDrLowLevel(target.getDrLowLevel());
        }
        if(target.getDrSpecialLevel() != null) {
        	this.setDrSpecialLevel(target.getDrSpecialLevel());
        }
        if(target.getDaylightHarvesting() != null)
        	this.setDaylightHarvesting(target.getDaylightHarvesting());
    }

    private short convertProfileTimeToShort(String time) {

        boolean night = true;
        String meridian = time.substring(time.length() - 2, time.length());
        int colIndex = time.indexOf(":");
        short hr = Short.parseShort(time.substring(0, colIndex));
        
        if (meridian.equals("AM") && hr == 12) {
        	hr =0;
        	night = false;
        }
        else if (meridian.equals("AM") && hr < 12) {
            night = false;
        } else if(hr == 12 && meridian.equals("PM")){
            night = false;
        }
      
        if (night) {
            hr += 12;
        }
        int spaceIndex = time.indexOf(" ");
        short min = Short.parseShort(time.substring(colIndex + 1, spaceIndex));
        min = (short) ((hr * 60) + min);
        return min;

    } // end of method convertProfileTimeToShort

    @Transient
    public Profile getCurrentProfile() {

        Calendar currentDate = Calendar.getInstance();
        int minOfDay = currentDate.get(Calendar.HOUR_OF_DAY) * 60 + currentDate.get(Calendar.MINUTE);
        ProfileConfiguration prConfig = profileConfiguration;

        // if it is holiday return holiday profiles
        // 1.3 UI does not support setting holidays dates in the system.
//        boolean holiday = false;
////        if (prConfig.getHolidays() != null) {
////            Iterator<Holiday> holidaysIter = prConfig.getHolidays().iterator();
////            Holiday oHoliday = null;
////            while (holidaysIter.hasNext()) {
////                oHoliday = holidaysIter.next();
////                if (oHoliday.getHoliday().compareTo(currentDate.getTime()) == 0) {
////                    holiday = true;
////                    break;
////                }
////            }
////        }
//        if (holiday) {
//            if (minOfDay >= convertProfileTimeToShort(prConfig.getNightTime())) {
//                return nightProfileHoliday;
//            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getEveningTime())) {
//                return eveningProfileHoliday;
//            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getDayTime())) {
//                return dayProfileHoliday;
//            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getMorningTime())) {
//                return morningProfileHoliday;
//            } else {
//                return nightProfileHoliday;
//            }
//        }

        // In GEMS monday is mapped as 1, tuesday is mapped as 2..... sunday as 7
        // In Java, sunday is mapped as 1, monday is mapped as 2, tuesday is mapped as 3
        int dayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }
        Iterator<WeekDay> weekDaysIter = prConfig.getWeekDays().iterator();
        WeekDay weekDay = null;
        while (weekDaysIter.hasNext()) {
            weekDay = weekDaysIter.next();
            if (weekDay.getShortOrder() == dayOfWeek) {
                break;
            }
        }
        if (weekDay.getType().equals("weekday")) {
            if (minOfDay >= convertProfileTimeToShort(prConfig.getNightTime())) {
                return nightProfile;
            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getEveningTime())) {
                return eveningProfile;
            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getDayTime())) {
                return dayProfile;
            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getMorningTime())) {
                return morningProfile;
            } else {
                return nightProfile;
            }
        } else {
            if (minOfDay >= convertProfileTimeToShort(prConfig.getNightTime())) {
                return nightProfileWeekEnd;
            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getEveningTime())) {
                return eveningProfileWeekEnd;
            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getDayTime())) {
                return dayProfileWeekEnd;
            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getMorningTime())) {
                return morningProfileWeekEnd;
            } else {
                return nightProfileWeekEnd;
            }
        }

    } // end of method getCurrentProfile

    /**
     * Converts into a string format
     * 
     * @param time
     * @return
     */
    private String convertProfileTimeToString(int time) {
        try {
            DecimalFormat df = new DecimalFormat("#00.###");
            int mm = time % 60;
            int hr = time / 60;
            String paher = "AM";
            if (hr > 12) {
                hr = hr - 12;
                paher = "PM";
            }
            return hr + ":" + df.format(mm) + " " + paher;
        } catch (ArithmeticException ae) {
            profileLogger.warn(ae.getMessage());
        }
        return "12:00 AM";
    } // end of method convertProfileTimeToString

    private void fillPacket(byte[] packet, short value, int index) {

        byte[] byteArr = ServerUtil.shortToByteArray(value);
        System.arraycopy(byteArr, 0, packet, index, byteArr.length);

    } // end of method fillPacket

    public byte[] getProfileByteArray(Profile mornProfile, Profile dayProfile, Profile evenProfile, Profile nightProfile) {

        byte[] profileArray = new byte[72]; // morn, day, even, night * 6 profile attributes
        int index = 0;
        // morning
        profileArray[index++] = mornProfile.getMinLevel().byteValue();
        profileArray[index++] = mornProfile.getOnLevel().byteValue();
        fillPacket(profileArray, (short) (mornProfile.getMotionDetectDuration().shortValue() * 60), index++);
        index++;
        fillPacket(profileArray, (short) (mornProfile.getManualOverrideDuration().shortValue() * 60), index++);
        index++;
        profileArray[index++] = mornProfile.getMotionSensitivity().byteValue(); // (byte)1;
        profileArray[index++] = mornProfile.getRampUpTime().byteValue(); // ramp time (how quickly dim/bright in hms
                                                                         // (100 ms)
        profileArray[index++] = mornProfile.getAmbientSensitivity().byteValue(); // (byte)5 ambient sensitivity
        // pad with 9 bytes
        for (int i = 0; i < 9; i++) {
            profileArray[index++] = 0;
        }

        // day
        profileArray[index++] = dayProfile.getMinLevel().byteValue();
        profileArray[index++] = dayProfile.getOnLevel().byteValue();
        fillPacket(profileArray, (short) (dayProfile.getMotionDetectDuration().shortValue() * 60), index++);
        index++;
        fillPacket(profileArray, (short) (dayProfile.getManualOverrideDuration().shortValue() * 60), index++);
        index++;
        profileArray[index++] = dayProfile.getMotionSensitivity().byteValue(); // (byte)1;
        profileArray[index++] = dayProfile.getRampUpTime().byteValue(); // ramp time (how quickly dim/bright in hms (100
                                                                        // ms)
        profileArray[index++] = dayProfile.getAmbientSensitivity().byteValue(); // (byte)5 ambient sensitivity
        // pad with 9 bytes
        for (int i = 0; i < 9; i++) {
            profileArray[index++] = 0;
        }

        // evening
        profileArray[index++] = evenProfile.getMinLevel().byteValue();
        profileArray[index++] = evenProfile.getOnLevel().byteValue();
        fillPacket(profileArray, (short) (evenProfile.getMotionDetectDuration().shortValue() * 60), index++);
        index++;
        fillPacket(profileArray, (short) (evenProfile.getManualOverrideDuration().shortValue() * 60), index++);
        index++;
        profileArray[index++] = evenProfile.getMotionSensitivity().byteValue(); // (byte)1;
        profileArray[index++] = evenProfile.getRampUpTime().byteValue(); // ramp time (how quickly dim/bright in hms
                                                                         // (100 ms)
        profileArray[index++] = evenProfile.getAmbientSensitivity().byteValue(); // (byte)5 ambient sensitivity
        // pad with 9 bytes
        for (int i = 0; i < 9; i++) {
            profileArray[index++] = 0;
        }

        // night
        profileArray[index++] = nightProfile.getMinLevel().byteValue();
        profileArray[index++] = nightProfile.getOnLevel().byteValue();
        fillPacket(profileArray, (short) (nightProfile.getMotionDetectDuration().shortValue() * 60), index++);
        index++;
        fillPacket(profileArray, (short) (nightProfile.getManualOverrideDuration().shortValue() * 60), index++);
        index++;
        profileArray[index++] = nightProfile.getMotionSensitivity().byteValue(); // (byte)1;
        profileArray[index++] = nightProfile.getRampUpTime().byteValue(); // (byte)50; //ramp time (how quickly
                                                                          // dim/bright in hms (100 ms)
        profileArray[index++] = nightProfile.getAmbientSensitivity().byteValue(); // (byte)5 ambient sensitivity
        // pad with 9 bytes
        for (int i = 0; i < 9; i++) {
            profileArray[index++] = 0;
        }
        return profileArray;

    } // end of method getProfileByteArray
    @Transient
    public byte[] getScheduledProfileByteArray() {

        byte[] weekDayByteArr = getProfileByteArray(morningProfile, dayProfile, eveningProfile, nightProfile);

        byte[] weekEndByteArr = getProfileByteArray(morningProfileWeekEnd, dayProfileWeekEnd, eveningProfileWeekEnd,
                nightProfileWeekEnd);

        byte[] holidayByteArr = getProfileByteArray(morningProfileHoliday, dayProfileHoliday, eveningProfileHoliday,
                nightProfileHoliday);

        byte[] profileByteArr = new byte[weekDayByteArr.length + weekEndByteArr.length + holidayByteArr.length];
        System.arraycopy(weekDayByteArr, 0, profileByteArr, 0, weekDayByteArr.length);
        System.arraycopy(weekEndByteArr, 0, profileByteArr, weekDayByteArr.length, weekEndByteArr.length);
        System.arraycopy(holidayByteArr, 0, profileByteArr, weekDayByteArr.length + weekEndByteArr.length,
                holidayByteArr.length);

        return profileByteArr;

    } // end of method getProfileByteArray

    @Transient
    public byte[] getGlobalProfileByteArray() {

        ProfileConfiguration prConfg = getProfileConfiguration();

        byte[] packet = new byte[53];
        int i = 0;
        short mornTime = convertProfileTimeToShort(prConfg.getMorningTime());
        fillPacket(packet, mornTime, i++);
        i++;
        short dayTime = convertProfileTimeToShort(prConfg.getDayTime());
        fillPacket(packet, dayTime, i++);
        i++;
        short evenTime = convertProfileTimeToShort(prConfg.getEveningTime());
        fillPacket(packet, evenTime, i++);
        i++;
        short nightTime = convertProfileTimeToShort(prConfg.getNightTime());
        fillPacket(packet, nightTime, i++);
        i++;

        fillPacket(packet, darkLux.shortValue(), i++);
        i++;
        fillPacket(packet, neighborLux.shortValue(), i++);
        i++;
        fillPacket(packet, envelopeOnLevel.shortValue(), i++);
        i++;
        packet[i++] = dropPercent.byteValue();
        packet[i++] = risePercent.byteValue();
        packet[i++] = relaysConnected.byteValue();
        packet[i++] = dimBackoffTime.byteValue(); // byte in min dim back off time
        fillPacket(packet, intensityNormTime.shortValue(), i++);
        i++;
        packet[i++] = minLevelBeforeOff.byteValue();// min level before off
        // range of standalone motion override is 0 - 200
        short standMotionOverride = standaloneMotionOverride.shortValue();
        if (standMotionOverride > 127) {
            packet[i++] = (byte) (standMotionOverride - 256);
        } else {
            packet[i++] = (byte) (standMotionOverride);
        }
        packet[i++] = drReactivity.byteValue();
        fillPacket(packet, getToOffLinger().shortValue(), i++);
        i++;
        packet[i++] = getWeekDaysAndWeekEnds(prConfg);
        packet[i++] = initialOnLevel;
        packet[i++] = profileGroupId.byteValue();
        packet[i++] = getProfileFlag();
        fillPacket(packet, getInitialOnTime().shortValue(), i++);
        i++;
        packet[i++] = getIsHighBay();
        fillPacket(packet, getMotionThresholdGain().shortValue(), i++);
        i++;
        int drLevels = 0;
        drLevels |= getDrHighLevel().intValue() << 13;
        drLevels |= getDrModerateLevel().intValue() << 10;
        drLevels |= getDrLowLevel().intValue() << 7;
        drLevels |= getDrSpecialLevel().intValue() << 4;
        ServerUtil.fillShortInByteArray(drLevels, packet, i++);        
        i++;
        
        byte daylightHarvesting = 0;
        daylightHarvesting |= (getDaylightHarvesting() << 0);
        packet[i++] = daylightHarvesting;
        
        // pad with 16 bytes
        for (int j = 0; j < 16; j++) {
            packet[i++] = 0;
        }
        return packet;

    } // end of method getGlobalProfileByteArray
    
    public static void main(String args[] ) {
    	
    	int drLevels = 0;
    	Byte high = 4;
    	Byte moderate = 2;
    	Byte low = 3;
    	
    	drLevels |= high.intValue() << 13;    	
    	drLevels |= moderate.intValue() << 10;
    	drLevels |= low.intValue() << 7;
    	byte[] packet = new byte[2];
    	ServerUtil.fillShortInByteArray(drLevels, packet, 0);
    	
    	System.out.println("drlevels -- " + ServerUtil.getLogPacket(packet));
    	System.out.println(" dr levels = " + drLevels);
    	
    	drLevels = ServerUtil.extractShortFromByteArray(packet, 0);
    	System.out.println(" dr levels = " + drLevels);
    	
    	byte high1 = (byte)((drLevels & 0xe000) >> 13);	
    	byte moderate1 = (byte)((drLevels & 0x1c00) >> 10);
    	byte low1 = (byte)((drLevels & 0x0380) >> 7);
    	
    	System.out.println("high -- " + high1);
    	
    }
    @Transient
    private byte getWeekDaysAndWeekEnds(ProfileConfiguration prConfg) {
        char days[] = { '0', '0', '0', '1', '1', '1', '1', '1' };
        Set<WeekDay> oWeekDays = prConfg.getWeekDays();
        // Count starts from 1.
        if (oWeekDays != null) {
            for (WeekDay weekDay : oWeekDays) {
                if (weekDay.getDay().equals("Sunday")) {
                    if (weekDay.getType().equals("weekday")) {
                        days[1] = '1';
                    } else {
                        days[1] = '0';
                    }
                } else if (weekDay.getDay().equals("Saturday")) {
                    if (weekDay.getType().equals("weekday")) {
                        days[2] = '1';
                    } else {
                        days[2] = '0';
                    }
                } else if (weekDay.getDay().equals("Friday")) {
                    if (weekDay.getType().equals("weekend")) {
                        days[3] = '0';
                    } else {
                        days[3] = '1';
                    }
                } else if (weekDay.getDay().equals("Thursday")) {
                    if (weekDay.getType().equals("weekend")) {
                        days[4] = '0';
                    } else {
                        days[4] = '1';
                    }
                } else if (weekDay.getDay().equals("Wednesday")) {
                    if (weekDay.getType().equals("weekend")) {
                        days[5] = '0';
                    } else {
                        days[5] = '1';
                    }
                } else if (weekDay.getDay().equals("Tuesday")) {
                    if (weekDay.getType().equals("weekend")) {
                        days[6] = '0';
                    } else {
                        days[6] = '1';
                    }
                } else if (weekDay.getDay().equals("Monday")) {
                    if (weekDay.getType().equals("weekend")) {
                        days[7] = '0';
                    } else {
                        days[7] = '1';
                    }
                }
            }
        }
        String strDays = String.valueOf(days);
        return Byte.parseByte(strDays, 2);
    }
    @Transient
    private void setWeekDaysAndWeekEnds(ProfileConfiguration prConfg, char[] days) {
        Set<WeekDay> oWeekDays = new HashSet<WeekDay>();
        for (int count = 1; count < days.length; count++) {
            WeekDay oDay = new WeekDay();
            if (days[count] == '0') {
                oDay.setType("weekend");
            } else {
                oDay.setType("weekday");
            }
            if (count == 1) {
                oDay.setDay("Sunday");
                oDay.setShortOrder(7);
            } else if (count == 2) {
                oDay.setDay("Saturday");
                oDay.setShortOrder(6);
            } else if (count == 3) {
                oDay.setDay("Friday");
                oDay.setShortOrder(5);
            } else if (count == 4) {
                oDay.setDay("Thursday");
                oDay.setShortOrder(4);
            } else if (count == 5) {
                oDay.setDay("Wednesday");
                oDay.setShortOrder(3);
            } else if (count == 6) {
                oDay.setDay("Tuesday");
                oDay.setShortOrder(2);
            } else if (count == 7) {
                oDay.setDay("Monday");
                oDay.setShortOrder(1);
            }
            oDay.setProfileConfiguration(prConfg);
            oWeekDays.add(oDay);
        }
        prConfg.setWeekDays(oWeekDays);
    }

    public void create() {
        morningProfile = new Profile();
        dayProfile = new Profile();
        eveningProfile = new Profile();
        nightProfile = new Profile();
        morningProfileWeekEnd = new Profile();
        dayProfileWeekEnd = new Profile();
        eveningProfileWeekEnd = new Profile();
        nightProfileWeekEnd = new Profile();
        morningProfileHoliday = new Profile();
        dayProfileHoliday = new Profile();
        eveningProfileHoliday = new Profile();
        nightProfileHoliday = new Profile();
        profileConfiguration = new ProfileConfiguration();
    }

    /**
     * Constructs a Weekday profile object from packet received from SU
     * 
     * @param pkt
     */
    public void setWeekdayProfileFromByteArray(byte[] pkt) {

        if (morningProfile == null) {
            morningProfile = new Profile();
        }
        if (dayProfile == null) {
            dayProfile = new Profile();
        }
        if (eveningProfile == null) {
            eveningProfile = new Profile();
        }
        if (nightProfile == null) {
            nightProfile = new Profile();
        }
        setProfileFromByteArray(pkt, morningProfile, dayProfile, eveningProfile, nightProfile);
        printWeekdayProfile();

    } // end of method setWeekdayProfileFromByteArray

    /**
     * Constructs a Weekend profile from packet received from SU
     * 
     * @param pkt
     */
    public void setWeekendProfileFromByteArray(byte[] pkt) {

        if (morningProfileWeekEnd == null) {
            morningProfileWeekEnd = new Profile();
        }
        if (dayProfileWeekEnd == null) {
            dayProfileWeekEnd = new Profile();
        }
        if (eveningProfileWeekEnd == null) {
            eveningProfileWeekEnd = new Profile();
        }
        if (nightProfileWeekEnd == null) {
            nightProfileWeekEnd = new Profile();
        }
        setProfileFromByteArray(pkt, morningProfileWeekEnd, dayProfileWeekEnd, eveningProfileWeekEnd,
                nightProfileWeekEnd);
        printWeekendProfile();

    } // end of method setWeekendProfileFromByteArray

    /**
     * Constructs a Holiday profile from packet received from SU.
     * 
     * @param pkt
     */
    public void setHolidayProfileFromByteArray(byte pkt[]) {

        if (morningProfileHoliday == null) {
            morningProfileHoliday = new Profile();
        }
        if (dayProfileHoliday == null) {
            dayProfileHoliday = new Profile();
        }
        if (eveningProfileHoliday == null) {
            eveningProfileHoliday = new Profile();
        }
        if (nightProfileHoliday == null) {
            nightProfileHoliday = new Profile();
        }
        setProfileFromByteArray(pkt, morningProfileHoliday, dayProfileHoliday, eveningProfileHoliday,
                nightProfileHoliday);
        printHolidayProfile();

    } // end of method setHolidayProfileFromByteArray

    /**
     * Constructs a profile from packet received from SU.
     * 
     * @param pkt
     */
    public void setProfileFromByteArray(byte[] pkt, Profile morningProfile, Profile dayProfile, Profile eveningProfile,
            Profile nightProfile) {

        int index = ServerConstants.RES_CMD_PKT_MSG_START_POS;
        if (pkt[0] == ServerConstants.FRAME_START_MARKER) { // old packet
            return;
        }
        index++; // profile type
        // morning

        morningProfile.setMinLevel((long) pkt[index++]);
        morningProfile.setOnLevel((long) pkt[index++]);
        byte[] tempShortByteArr = { pkt[index++], pkt[index++] };
        morningProfile.setMotionDetectDuration((long) ServerUtil.byteArrayToShort(tempShortByteArr) / 60);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        morningProfile.setManualOverrideDuration((long) ServerUtil.byteArrayToShort(tempShortByteArr) / 60);
        morningProfile.setMotionSensitivity((long) pkt[index++]);
        morningProfile.setRampUpTime((long) pkt[index++]);
        morningProfile.setAmbientSensitivity((int) pkt[index++]);
        index += 9; // holes in the profile

        // day
        dayProfile.setMinLevel((long) pkt[index++]);
        dayProfile.setOnLevel((long) pkt[index++]);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        dayProfile.setMotionDetectDuration((long) ServerUtil.byteArrayToShort(tempShortByteArr) / 60);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        dayProfile.setManualOverrideDuration((long) ServerUtil.byteArrayToShort(tempShortByteArr) / 60);
        dayProfile.setMotionSensitivity((long) pkt[index++]);
        dayProfile.setRampUpTime((long) pkt[index++]);
        dayProfile.setAmbientSensitivity((int) pkt[index++]);
        index += 9;

        // evening
        eveningProfile.setMinLevel((long) pkt[index++]);
        eveningProfile.setOnLevel((long) pkt[index++]);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        eveningProfile.setMotionDetectDuration((long) ServerUtil.byteArrayToShort(tempShortByteArr) / 60);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        eveningProfile.setManualOverrideDuration((long) ServerUtil.byteArrayToShort(tempShortByteArr) / 60);
        eveningProfile.setMotionSensitivity((long) pkt[index++]);
        eveningProfile.setRampUpTime((long) pkt[index++]);
        eveningProfile.setAmbientSensitivity((int) pkt[index++]);
        index += 9;

        // night
        nightProfile.setMinLevel((long) pkt[index++]);
        nightProfile.setOnLevel((long) pkt[index++]);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        nightProfile.setMotionDetectDuration((long) ServerUtil.byteArrayToShort(tempShortByteArr) / 60);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        nightProfile.setManualOverrideDuration((long) ServerUtil.byteArrayToShort(tempShortByteArr) / 60);
        nightProfile.setMotionSensitivity((long) pkt[index++]);
        nightProfile.setRampUpTime((long) pkt[index++]);
        nightProfile.setAmbientSensitivity((int) pkt[index++]);

    } // end of method setHolidayProfileFromByteArray

    /**
     * Constructs a Advance profile from the packet received from SU
     * 
     * @param pkt
     */
    public void setAdvanceProfile(byte[] pkt) {
        int index = ServerConstants.RES_CMD_PKT_MSG_START_POS;
        if (pkt[0] == ServerConstants.FRAME_START_MARKER) { // old packet
            return;
        }
        index++; // profile type
        if (profileConfiguration == null) {
            profileConfiguration = new ProfileConfiguration();
        }

        byte[] tempShortByteArr = { pkt[index++], pkt[index++] };
        profileConfiguration.setMorningTime(convertProfileTimeToString(ServerUtil.byteArrayToShort(tempShortByteArr)));

        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        profileConfiguration.setDayTime(convertProfileTimeToString(ServerUtil.byteArrayToShort(tempShortByteArr)));

        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        profileConfiguration.setEveningTime(convertProfileTimeToString(ServerUtil.byteArrayToShort(tempShortByteArr)));

        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        profileConfiguration.setNightTime(convertProfileTimeToString(ServerUtil.byteArrayToShort(tempShortByteArr)));

        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        setDarkLux((int) ServerUtil.byteArrayToShort(tempShortByteArr));

        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        setNeighborLux((int) ServerUtil.byteArrayToShort(tempShortByteArr));

        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        setEnvelopeOnLevel((int) ServerUtil.byteArrayToShort(tempShortByteArr));

        setDropPercent((int) pkt[index++]);
        setRisePercent((int) pkt[index++]);
        setRelaysConnected((int) pkt[index++]);
        setDimBackoffTime((short) pkt[index++]);

        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        setIntensityNormTime((short) ServerUtil.byteArrayToShort(tempShortByteArr));

        setMinLevelBeforeOff((short) pkt[index++]);
        setStandaloneMotionOverride((short) pkt[index++]);
        setDrReactivity(pkt[index++]);
        tempShortByteArr = null;
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        setToOffLinger((int) ServerUtil.byteArrayToShort(tempShortByteArr));
        int iBits = (int) pkt[index++];
        setWeekDaysAndWeekEnds(profileConfiguration, Integer.toBinaryString(0x100 | iBits).substring(1).toCharArray());
        setInitialOnLevel(pkt[index++]);
        setProfileGroupId((short) pkt[index++]);
        setProfileFlag(pkt[index++]);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        setInitialOnTime((int) ServerUtil.byteArrayToShort(tempShortByteArr));
        setIsHighBay(pkt[index++]);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        setMotionThresholdGain((int) ServerUtil.byteArrayToShort(tempShortByteArr));
        
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        int drLevels = ServerUtil.byteArrayToShort(tempShortByteArr);
      	setDrHighLevel((byte)((drLevels & 0xe000) >> 13));	
      	setDrModerateLevel((byte)((drLevels & 0x1c00) >> 10));
      	setDrLowLevel((byte)((drLevels & 0x0380) >> 7));
      	setDrSpecialLevel((byte)((drLevels & 0x0070) >> 4));
      	byte dayLightHarvesting = pkt[index++];
      	setDaylightHarvesting((byte)((dayLightHarvesting >> 0) & 1));
        printAdvanceProfile();
    }

    /**
     * Prints profile details
     * 
     * @param pf
     */
    private void printWeekdayProfile() {
        StringBuffer oBuff = new StringBuffer();
        oBuff.append("Schedule Profile:").append("\r\n");
        oBuff.append("Weekday MIN_L   MAX_L   OCC_T   MOD_T   OCC_S   RMP     AMB_S").append("\r\n");
        oBuff.append("Morning\t");
        // morning
        oBuff.append(morningProfile.getMinLevel()).append("\t");
        oBuff.append(morningProfile.getOnLevel()).append("\t");
        oBuff.append(morningProfile.getMotionDetectDuration()).append("\t");
        oBuff.append(morningProfile.getManualOverrideDuration()).append("\t");
        oBuff.append(morningProfile.getMotionSensitivity()).append("\t");
        oBuff.append(morningProfile.getRampUpTime()).append("\t");
        oBuff.append(morningProfile.getAmbientSensitivity()).append("\r\n");

        // day
        oBuff.append("Day\t");
        oBuff.append(dayProfile.getMinLevel()).append("\t");
        oBuff.append(dayProfile.getOnLevel()).append("\t");
        oBuff.append(dayProfile.getMotionDetectDuration()).append("\t");
        oBuff.append(dayProfile.getManualOverrideDuration()).append("\t");
        oBuff.append(dayProfile.getMotionSensitivity()).append("\t");
        oBuff.append(dayProfile.getRampUpTime()).append("\t");
        oBuff.append(dayProfile.getAmbientSensitivity()).append("\r\n");

        // evening
        oBuff.append("Evening\t");
        oBuff.append(eveningProfile.getMinLevel()).append("\t");
        oBuff.append(eveningProfile.getOnLevel()).append("\t");
        oBuff.append(eveningProfile.getMotionDetectDuration()).append("\t");
        oBuff.append(eveningProfile.getManualOverrideDuration()).append("\t");
        oBuff.append(eveningProfile.getMotionSensitivity()).append("\t");
        oBuff.append(eveningProfile.getRampUpTime()).append("\t");
        oBuff.append(eveningProfile.getAmbientSensitivity()).append("\r\n");

        // night
        oBuff.append("Night\t");
        oBuff.append(nightProfile.getMinLevel()).append("\t");
        oBuff.append(nightProfile.getOnLevel()).append("\t");
        oBuff.append(nightProfile.getMotionDetectDuration()).append("\t");
        oBuff.append(nightProfile.getManualOverrideDuration()).append("\t");
        oBuff.append(nightProfile.getMotionSensitivity()).append("\t");
        oBuff.append(nightProfile.getRampUpTime()).append("\t");
        oBuff.append(nightProfile.getAmbientSensitivity()).append("\r\n");
        profileLogger.debug(oBuff.toString());
    }

    /**
     * Prints weekend profile details
     * 
     * @param pf
     */
    private void printWeekendProfile() {
        StringBuffer oBuff = new StringBuffer();
        oBuff.append("Schedule Profile:").append("\r\n");
        oBuff.append("Weekend MIN_L   MAX_L   OCC_T   MOD_T   OCC_S   RMP     AMB_S").append("\r\n");
        oBuff.append("Morning\t");
        // morning
        oBuff.append(morningProfileWeekEnd.getMinLevel()).append("\t");
        oBuff.append(morningProfileWeekEnd.getOnLevel()).append("\t");
        oBuff.append(morningProfileWeekEnd.getMotionDetectDuration()).append("\t");
        oBuff.append(morningProfileWeekEnd.getManualOverrideDuration()).append("\t");
        oBuff.append(morningProfileWeekEnd.getMotionSensitivity()).append("\t");
        oBuff.append(morningProfileWeekEnd.getRampUpTime()).append("\t");
        oBuff.append(morningProfileWeekEnd.getAmbientSensitivity()).append("\r\n");

        // day
        oBuff.append("Day\t");
        oBuff.append(dayProfileWeekEnd.getMinLevel()).append("\t");
        oBuff.append(dayProfileWeekEnd.getOnLevel()).append("\t");
        oBuff.append(dayProfileWeekEnd.getMotionDetectDuration()).append("\t");
        oBuff.append(dayProfileWeekEnd.getManualOverrideDuration()).append("\t");
        oBuff.append(dayProfileWeekEnd.getMotionSensitivity()).append("\t");
        oBuff.append(dayProfileWeekEnd.getRampUpTime()).append("\t");
        oBuff.append(dayProfileWeekEnd.getAmbientSensitivity()).append("\r\n");

        // evening
        oBuff.append("Evening\t");
        oBuff.append(eveningProfileWeekEnd.getMinLevel()).append("\t");
        oBuff.append(eveningProfileWeekEnd.getOnLevel()).append("\t");
        oBuff.append(eveningProfileWeekEnd.getMotionDetectDuration()).append("\t");
        oBuff.append(eveningProfileWeekEnd.getManualOverrideDuration()).append("\t");
        oBuff.append(eveningProfileWeekEnd.getMotionSensitivity()).append("\t");
        oBuff.append(eveningProfileWeekEnd.getRampUpTime()).append("\t");
        oBuff.append(eveningProfileWeekEnd.getAmbientSensitivity()).append("\r\n");

        // night
        oBuff.append("Night\t");
        oBuff.append(nightProfileWeekEnd.getMinLevel()).append("\t");
        oBuff.append(nightProfileWeekEnd.getOnLevel()).append("\t");
        oBuff.append(nightProfileWeekEnd.getMotionDetectDuration()).append("\t");
        oBuff.append(nightProfileWeekEnd.getManualOverrideDuration()).append("\t");
        oBuff.append(nightProfileWeekEnd.getMotionSensitivity()).append("\t");
        oBuff.append(nightProfileWeekEnd.getRampUpTime()).append("\t");
        oBuff.append(nightProfileWeekEnd.getAmbientSensitivity()).append("\r\n");
        profileLogger.debug(oBuff.toString());
    }

    /**
     * Prints holiday profile details
     * 
     * @param pf
     */
    private void printHolidayProfile() {
        StringBuffer oBuff = new StringBuffer();
        oBuff.append("Schedule Profile:").append("\r\n");
        oBuff.append("Holiday MIN_L   MAX_L   OCC_T   MOD_T   OCC_S   RMP     AMB_S").append("\r\n");
        oBuff.append("Morning\t");
        // morning
        oBuff.append(morningProfileHoliday.getMinLevel()).append("\t");
        oBuff.append(morningProfileHoliday.getOnLevel()).append("\t");
        oBuff.append(morningProfileHoliday.getMotionDetectDuration()).append("\t");
        oBuff.append(morningProfileHoliday.getManualOverrideDuration()).append("\t");
        oBuff.append(morningProfileHoliday.getMotionSensitivity()).append("\t");
        oBuff.append(morningProfileHoliday.getRampUpTime()).append("\t");
        oBuff.append(morningProfileHoliday.getAmbientSensitivity()).append("\r\n");

        // day
        oBuff.append("Day\t");
        oBuff.append(dayProfileHoliday.getMinLevel()).append("\t");
        oBuff.append(dayProfileHoliday.getOnLevel()).append("\t");
        oBuff.append(dayProfileHoliday.getMotionDetectDuration()).append("\t");
        oBuff.append(dayProfileHoliday.getManualOverrideDuration()).append("\t");
        oBuff.append(dayProfileHoliday.getMotionSensitivity()).append("\t");
        oBuff.append(dayProfileHoliday.getRampUpTime()).append("\t");
        oBuff.append(dayProfileHoliday.getAmbientSensitivity()).append("\r\n");

        // evening
        oBuff.append("Evening\t");
        oBuff.append(eveningProfileHoliday.getMinLevel()).append("\t");
        oBuff.append(eveningProfileHoliday.getOnLevel()).append("\t");
        oBuff.append(eveningProfileHoliday.getMotionDetectDuration()).append("\t");
        oBuff.append(eveningProfileHoliday.getManualOverrideDuration()).append("\t");
        oBuff.append(eveningProfileHoliday.getMotionSensitivity()).append("\t");
        oBuff.append(eveningProfileHoliday.getRampUpTime()).append("\t");
        oBuff.append(eveningProfileHoliday.getAmbientSensitivity()).append("\r\n");

        // night
        oBuff.append("Night\t");
        oBuff.append(nightProfileHoliday.getMinLevel()).append("\t");
        oBuff.append(nightProfileHoliday.getOnLevel()).append("\t");
        oBuff.append(nightProfileHoliday.getMotionDetectDuration()).append("\t");
        oBuff.append(nightProfileHoliday.getManualOverrideDuration()).append("\t");
        oBuff.append(nightProfileHoliday.getMotionSensitivity()).append("\t");
        oBuff.append(nightProfileHoliday.getRampUpTime()).append("\t");
        oBuff.append(nightProfileHoliday.getAmbientSensitivity()).append("\r\n");
        profileLogger.debug(oBuff.toString());
    }

    private void printAdvanceProfile() {
        StringBuffer oBuff = new StringBuffer();
        oBuff.append("MT\tDT\tET\tNT\tDL\tNL\tEL\tDP\tRP\tRC\tBK\tNT\tLO\tSAMO\tDRr\tLOF\tWDB\tIOL\tPGID").append(
                "\r\n");
        oBuff.append(profileConfiguration.getMorningTime()).append("\t");
        oBuff.append(profileConfiguration.getDayTime()).append("\t");
        oBuff.append(profileConfiguration.getEveningTime()).append("\t");
        oBuff.append(profileConfiguration.getNightTime()).append("\t");
        oBuff.append(getDarkLux()).append("\t");
        oBuff.append(getNeighborLux()).append("\t");
        oBuff.append(getEnvelopeOnLevel()).append("\t");
        oBuff.append(getDropPercent()).append("\t");
        oBuff.append(getRisePercent()).append("\t");
        oBuff.append(getRelaysConnected()).append("\t");
        oBuff.append(getDimBackoffTime()).append("\t");
        oBuff.append(getIntensityNormTime()).append("\t");
        oBuff.append(getMinLevelBeforeOff()).append("\t");
        oBuff.append(getStandaloneMotionOverride()).append("\t");
        oBuff.append(getDrReactivity()).append("\t");
        oBuff.append(getToOffLinger()).append("\t");
        oBuff.append(getWeekDaysAndWeekEnds(profileConfiguration));
        oBuff.append(getInitialOnLevel()).append("\t");
        oBuff.append(getProfileGroupId()).append("\t");
        oBuff.append(getProfileFlag()).append("\t");
        oBuff.append(getInitialOnTime()).append("\t");
        oBuff.append(getIsHighBay()).append("\t");
        oBuff.append(getMotionThresholdGain()).append("\t");
        oBuff.append(getDrHighLevel()).append("\t");
        oBuff.append(getDrModerateLevel()).append("\t");
        oBuff.append(getDrLowLevel()).append("\t");
        oBuff.append(getDrSpecialLevel()).append("\t");
        oBuff.append(getDaylightHarvesting()).append("\t");
        profileLogger.debug(oBuff.toString());
    }

    public int compare(ProfileHandler p2) {
        // Compare Advance attributes
        if (this.getDarkLux().intValue() != p2.getDarkLux().intValue())
        {
        	Integer darkLux = this.getDarkLux().intValue();
        	Integer darkLux2 = p2.getDarkLux().intValue();
        	System.out.println("Place Holder"+darkLux + ":"+darkLux2);
            return -1;
        }
        if (this.getNeighborLux().intValue() != p2.getNeighborLux().intValue())
            return -1;
        if (this.getEnvelopeOnLevel().intValue() != p2.getEnvelopeOnLevel().intValue())
            return -1;
        if (this.getDropPercent().intValue() != p2.getDropPercent().intValue())
            return -1;
        if (this.getRisePercent().intValue() != p2.getRisePercent().intValue())
            return -1;
        if (this.getDimBackoffTime().intValue() != p2.getDimBackoffTime().intValue())
            return -1;
        if (this.getIntensityNormTime().intValue() != p2.getIntensityNormTime().intValue())
            return -1;
        if (this.getOnAmbLightLevel().intValue() != p2.getOnAmbLightLevel().intValue())
            return -1;
        if (this.getMinLevelBeforeOff().intValue() != p2.getMinLevelBeforeOff().intValue())
            return -1;
        if (this.getRelaysConnected().intValue() != p2.getRelaysConnected().intValue())
            return -1;
        if (this.getStandaloneMotionOverride().intValue() != p2.getStandaloneMotionOverride().intValue())
            return -1;
        if (this.getDrReactivity().intValue() != p2.getDrReactivity().intValue())
            return -1;
        if (this.getToOffLinger().intValue() != p2.getToOffLinger().intValue())
            return -1;
        if (this.getInitialOnLevel().intValue() != p2.getInitialOnLevel().intValue())
            return -1;
        if (this.getProfileGroupId().intValue() != p2.getProfileGroupId().intValue())
            return -1;
        if (this.getProfileFlag().intValue() != p2.getProfileFlag().intValue())
            return -1;
        if (this.getInitialOnTime().intValue() != p2.getInitialOnTime().intValue())
            return -1;
        if (this.getIsHighBay().intValue() != p2.getIsHighBay().intValue())
            return -1;
        if (this.getMotionThresholdGain().intValue() != p2.getMotionThresholdGain().intValue())
            return -1;
        if(this.getDrHighLevel().byteValue() != p2.getDrHighLevel().byteValue())
        	return -1;
        if(this.getDrModerateLevel().byteValue() != p2.getDrModerateLevel().byteValue())
        	return -1;
        if(this.getDrLowLevel().byteValue() != p2.getDrLowLevel().byteValue())
        	return -1;
        if(this.getDrSpecialLevel().byteValue() != p2.getDrSpecialLevel().byteValue())
        	return -1;

        // Compare profile now
        if (this.getMorningProfile().compare(p2.getMorningProfile()) <= -1)
            return -1;

        if (this.getDayProfile().compare(p2.getDayProfile()) <= -1)
            return -1;

        if (this.getEveningProfile().compare(p2.getEveningProfile()) <= -1)
            return -1;

        if (this.getNightProfile().compare(p2.getNightProfile()) <= -1)
            return -1;

        if (this.getMorningProfileWeekEnd().compare(p2.getMorningProfileWeekEnd()) <= -1)
            return -1;

        if (this.getDayProfileWeekEnd().compare(p2.getDayProfileWeekEnd()) <= -1)
            return -1;

        if (this.getEveningProfileWeekEnd().compare(p2.getEveningProfileWeekEnd()) <= -1)
            return -1;

        if (this.getNightProfileWeekEnd().compare(p2.getNightProfileWeekEnd()) <= -1)
            return -1;

        if (this.getMorningProfileHoliday().compare(p2.getMorningProfileHoliday()) <= -1)
            return -1;

        if (this.getDayProfileHoliday().compare(p2.getDayProfileHoliday()) <= -1)
            return -1;

        if (this.getEveningProfileHoliday().compare(p2.getEveningProfileHoliday()) <= -1)
            return -1;

        if (this.getNightProfileHoliday().compare(p2.getNightProfileHoliday()) <= -1)
            return -1;

        // Compare profile configuration
        if (this.getProfileConfiguration().compare(p2.getProfileConfiguration()) <= -1)
            return -1;
        if (this.getDaylightHarvesting().intValue() != p2.getDaylightHarvesting().intValue())
            return -1;
        return 0;
    }
    
    public int copyProfileOverrides(ProfileHandler p2) {
        int bSynced = 0;
        if (this.morningProfileHoliday.compare(p2.getMorningProfileHoliday()) <= -1) {
            this.morningProfileHoliday.copyFrom(p2.getMorningProfileHoliday());
            bSynced++;
        }
        if (this.dayProfileHoliday.compare(p2.getDayProfileHoliday()) <= -1) {
            this.dayProfileHoliday.copyFrom(p2.getDayProfileHoliday());
            bSynced++;
        }
        if (this.eveningProfileHoliday.compare(p2.getEveningProfileHoliday()) <= -1) {
            this.eveningProfileHoliday.copyFrom(p2.getEveningProfileHoliday());
            bSynced++;
        }
        if (this.nightProfileHoliday.compare(p2.getNightProfileHoliday()) <= -1) {
            this.nightProfileHoliday.copyFrom(p2.getNightProfileHoliday());
            bSynced++;
        }
        // Check for default override mapping to DR events
        if(this.getDrLowLevel().byteValue() != p2.getDrLowLevel().byteValue()) {
            this.setDrLowLevel(p2.getDrLowLevel().byteValue());
            bSynced++;
        }
        if(this.getDrModerateLevel().byteValue() != p2.getDrModerateLevel().byteValue()) {
            this.setDrModerateLevel(p2.getDrModerateLevel().byteValue());
            bSynced++;
        }
        if(this.getDrHighLevel().byteValue() != p2.getDrHighLevel().byteValue()) {
            this.setDrHighLevel(p2.getDrHighLevel().byteValue());
            bSynced++;
        }
        if(this.getDrSpecialLevel().byteValue() != p2.getDrSpecialLevel().byteValue()) {
            this.setDrSpecialLevel(p2.getDrSpecialLevel().byteValue());
            bSynced++;
        }
        return bSynced;
    }
}
